import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/****************************
 * 
 * Starts up waiting for client connections (also initializes with a specified time out period. )
 * Receives numbers & sorting choice
 * Splits numbers into chunks of 10 million
 * Sends each chunk to an available Utility server
 * Waits for servers to process and return results
 * Aggregates results and sends back to client
 * 
 *****************************/

public class Master implements Runnable {
	private String sortChoice = ""; // Choice of sort given by the client
	private List<Integer> data; // Data given by the client
	private List<int[]> chunks;
	private List<Integer> sums = new ArrayList<Integer>();

	private static final int PORT = 32005;
	private static final int CHUNK_SIZE = 10_000_000; // 10 milli 🤑
	private static int heartbeatTimeoutPeriod;

	private static List<Integer> utilityPorts = Arrays.asList(32006, 32007, 32008);
	private Socket clientSocket;
	private List<Integer> activeUtilityPorts = new ArrayList<>();
 
	
	public Master(Socket clientSocket, int heartbeatTimeoutPeriod) {
		this.clientSocket = clientSocket;
		this.sums = new ArrayList<Integer>();
		this.chunks = new ArrayList<int[]>();
		this.heartbeatTimeoutPeriod = heartbeatTimeoutPeriod;
	}

	public static void main(String[] args) throws IOException {
		heartbeatTimeoutPeriod = 5000;
		try {
			heartbeatTimeoutPeriod = Integer.parseInt(args[0]);
			System.out.println("[MASTER] Heartbeat timeout period set to: " + heartbeatTimeoutPeriod);
		} catch (Exception e) {
			System.out.println("[MASTER] Invalid heartbeat timeout specified. Using default: 5000");
		} 

		System.out.println("[MASTER] Master server is starting...");
		ServerSocket serverSocket = new ServerSocket(PORT);
		System.out.println("[MASTER] Master server listening on: " + PORT);

		while (true) {
			Socket clientSocket = serverSocket.accept();
			System.out.println("[MASTER] Client connected: " + clientSocket.getInetAddress());

			Thread t = new Thread(new Master(clientSocket, heartbeatTimeoutPeriod));
			t.start();
		}
	}

	@Override
	// There's like 15 print statements here that might be a little uncessary from debugging
	public void run() {
		try {
			this.chunks = new ArrayList<int[]>();
			this.sums = new ArrayList<Integer>();

			System.out.println("[MASTER] Finding available utility servers...");
			this.activeUtilityPorts = findAvailableUtilities();

			if (this.activeUtilityPorts.isEmpty()) {
				System.out.println("[MASTER] No utility servers available.");
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				out.writeObject("[MASTER] Error: No utility servers available");
				out.flush();
				clientSocket.close();
				return;
			}

			System.out.println("[MASTER] Sending heartbeat...");
			List<Integer> availableServers = heartbeat(heartbeatTimeoutPeriod);

			if (availableServers.isEmpty()) {
				System.out.println("[MASTER] No utility servers available after heartbeat.");
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				out.writeObject("[MASTER] Error: No utility servers available");
				out.flush();
				clientSocket.close();
				return;
			}

			System.out.println("[MASTER] Found " + availableServers.size() + " available servers after heartbeat");

			System.out.println("[MASTER] Reading job from client...");
			readJob();

			System.out.println("[MASTER] Partitioning chunks...");
			chunks = chunkData(data);

			System.out.println("[MASTER] Sending chunks...");
			sendAndReceiveData(chunks, availableServers);
			System.out.println("[MASTER] Results collected!");

			System.out.println("[MASTER] Sending results to client...");
			sendResultToClient();

			clientSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<Integer> findAvailableUtilities() {
		List<Integer> available = new ArrayList<>();

		for (int port : utilityPorts) {
			try {
				System.out.println("[MASTER] Checking utility at port " + port);
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress("localhost", port), 2000);
				System.out.println("[MASTER] Connected to utility at port " + port);

				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				System.out.println("[MASTER] Created output stream for port " + port);
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				System.out.println("[MASTER] Created input stream for port " + port);

				out.writeObject("HEARTBEAT");
				System.out.println("[MASTER] Sent HEARTBEAT to port " + port);
				out.flush();

				String response = (String) in.readObject();
				socket.close();

				if (response.equals("AVAILABLE")) {
					available.add(port);
					System.out.println("[MASTER] Utility at port " + port + " is available");
				} else {
					System.out.println("[MASTER] Utility at port " + port + " is busy");
				}
			} catch (Exception e) {
				System.out.println("[MASTER] Utility at port " + port + " is not responding.");
			}
		}
		return available;
	}

	private List<Integer> heartbeat(int timeout) {
		List<Integer> availableUtilities = new ArrayList<>();
		List<Integer> nonRespondingPorts = new ArrayList<>();

		for (int port : activeUtilityPorts) {
			try {
				System.out.println("[MASTER] Sending heartbeat to utility at port " + port);
				Socket socket = new Socket();

				socket.connect(new InetSocketAddress("localhost", port), 2000);

				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

				out.writeObject("HEARTBEAT");
				out.flush();

				socket.setSoTimeout(timeout);

				String response = (String) in.readObject();
				socket.close();

				if (response.equals("AVAILABLE")) {
					availableUtilities.add(port);
					System.out.println("[MASTER] Utility at port " + port + " is AVAILABLE");;
				} else {
					System.out.println("[MASTER] Utility at port " + port + " is not responding");
				}
			} catch (Exception e) {
				System.out.println("[MASTER] Utility at port " + port + " did not responding within time");
				nonRespondingPorts.add(port);
			}
		}

		activeUtilityPorts.removeAll(nonRespondingPorts);
		if (!nonRespondingPorts.isEmpty()) {
			System.out.println("Dropped " + nonRespondingPorts.size() + " unresponsive utilities");
		}

		return availableUtilities;
	}

	private void readJob() throws IOException, ClassNotFoundException { 
		// reads the packet
		ObjectInputStream dataIn = new ObjectInputStream(clientSocket.getInputStream()); // from the client
		HashMap<String, Object> packet = (HashMap<String, Object>) dataIn.readObject(); // data to be sorted

		sortChoice = (String) packet.get("sort");

		int[] dataArr = (int[]) packet.get("data");
		System.out.println("[MASTER] Received " + dataArr.length + " numbers from client");
		data = new ArrayList<>();
		for (int i = 0; i < dataArr.length; i++) {
			data.add(dataArr[i]);
		}

		// Just for status checks so it doesn't seem like nothings happening
		int count = 0;
		for (Integer num : data) {
			count++;
			if (count % 100000 == 0) {
				System.out.println("[MASTER] Read " + count + " integers so far from " + clientSocket.getInetAddress());
			}
		}
	}

	private List<int[]> chunkData(List<Integer> data) {
		List<int[]> chunks = new ArrayList<>();
		int total = data.size();
		int start = 0;

		while (start < total) {
			int end = Math.min(start + CHUNK_SIZE, total);
			List<Integer> sublist = data.subList(start, end);

			int[] chunk = convertList(sublist);
			chunks.add(chunk);

			start = end;
		}
		return chunks;
	}

	private int[] convertList(List<Integer> subList) {
		int[] chunk = new int[subList.size()];
		for (int i = 0; i < subList.size(); i++) {
			chunk[i] = subList.get(i);
		}
		return chunk;
	}

	// This maybe should be two methods
	private void sendAndReceiveData(List<int[]> chunks, List<Integer> availableServers) {
		try {
			int utilityIndex = 0;
			System.out.println("[MASTER] Sending " + chunks.size() + " chunks.");

			for (int[] chunk : chunks) {
				if (utilityIndex >= availableServers.size()) {
					utilityIndex = 0;
				}
				int port = availableServers.get(utilityIndex++);

				try {
					Socket s = new Socket("localhost", port);
					ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
					ObjectInputStream in = new ObjectInputStream(s.getInputStream());

					HashMap<String, Object> task = new HashMap<>();
					task.put("sort", sortChoice);
					task.put("data", chunk);

					out.writeObject(task);
					out.flush();

					int partialSum = (int) in.readObject();
					sums.add(partialSum);
					s.close();
				} catch (Exception e) {
					System.err.println("[MASTER] Data unsuccessfully sent");
					e.printStackTrace();
				}
			}
		} catch (Exception ex) {
			System.err.println("[MASTER] Error: Data could not be send successfuly.");
			ex.printStackTrace();
		}
	}

	private void sendResultToClient() {
		try {
			ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
			int totalSum = 0;

			for (int partial : sums) {
				totalSum += partial;
			}
			out.writeObject(totalSum);
			out.flush();
		} catch (IOException e) {
			System.err.println("[MASTER] Error sending final results to client.");
			e.printStackTrace();
		}
	}
}
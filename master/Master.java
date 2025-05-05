import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/****************************
 * 
 * Starts up waiting for client connections (also initializes with a specified
 * time out period. )
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
	private static File log = new File("log.txt");
	private static PrintWriter logWriter;

	private static final int PORT = 32005;
	private static final int MAX_CHUNK_SIZE = 10_000_000; // Maximum chunk size
	private static int heartbeatTimeoutPeriod;

	// Thread pooling? Just sort of picks from this list, if there's a running server on that port we're good.
	private static List<Integer> utilityPorts = Arrays.asList(32006, 32007, 32008, 32009, 32010, 32011, 32012, 32013, 32014, 32015);
	private Socket clientSocket;
	private List<Integer> activeUtilityPorts = new ArrayList<>();

	public Master(Socket clientSocket, int heartbeatTimeoutPeriod) {
		this.clientSocket = clientSocket;
		this.sums = new ArrayList<Integer>();
		this.chunks = new ArrayList<int[]>();
		this.heartbeatTimeoutPeriod = heartbeatTimeoutPeriod;
	}

	public static void main(String[] args) throws IOException {
		logWriter = new PrintWriter(log);
		logWriter.println("hello");
		heartbeatTimeoutPeriod = 5000;
		try {
			heartbeatTimeoutPeriod = Integer.parseInt(args[0]);
			logWriter.println("[MASTER] Heartbeat timeout period set to: " + heartbeatTimeoutPeriod);
		} catch (Exception e) {
			logWriter.println("[MASTER] Invalid heartbeat timeout specified. Using default: 5000");
		}
		logWriter.flush();

		System.out.println("[MASTER] Master server is starting...");
		ServerSocket serverSocket = new ServerSocket(PORT);
		logWriter.println("[MASTER] Master server listening on: " + PORT);
		logWriter.flush();

		while (true) {
			Socket clientSocket = serverSocket.accept();
			logWriter.println("[MASTER] Client connected: " + clientSocket.getInetAddress());

			Thread t = new Thread(new Master(clientSocket, heartbeatTimeoutPeriod));
			t.start();
		}
	}

	@Override
	// There's like 15 print statements here that might be a little uncessary from
	// debugging
	public void run() {
		try {
			this.chunks = new ArrayList<int[]>();
			this.sums = new ArrayList<Integer>();

			logWriter.println("[MASTER] Finding available utility servers...");
			this.activeUtilityPorts = findAvailableUtilities();

			if (this.activeUtilityPorts.isEmpty()) {
				logWriter.println("[MASTER] No utility servers available.");
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				out.writeObject("[MASTER] Error: No utility servers available");
				out.flush();
				clientSocket.close();
				return;
			}

			logWriter.println("[MASTER] Sending heartbeat...");
			List<Integer> availableServers = heartbeat(heartbeatTimeoutPeriod);

			if (availableServers.isEmpty()) {
				logWriter.println("[MASTER] No utility servers available after heartbeat.");
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				out.writeObject("[MASTER] Error: No utility servers available");
				out.flush();
				clientSocket.close();
				return;
			}

			System.out.println("[MASTER] Found " + availableServers.size() + " available servers after heartbeat");

			System.out.println("[MASTER] Reading job from client...");
			readJob();

			logWriter.println("[MASTER] Partitioning chunks...");
			chunks = chunkData(data);

			logWriter.println("[MASTER] Sending chunks...");
			sendAndReceiveData(chunks, availableServers);
			logWriter.println("[MASTER] Results collected!");

			System.out.println("[MASTER] Sending results to client...");
			sendResultToClient();

			clientSocket.close();
			logWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<Integer> findAvailableUtilities() {
		List<Integer> available = new ArrayList<>();

		HashMap<Integer, String> utilityHostNames = new HashMap<>();

		int tempPort = 32005;
		for (int port : utilityPorts) {
			utilityHostNames.put(tempPort, "utility" + (tempPort - 32005));
			tempPort++;
		}

		for (int port : utilityPorts) {
			try {
				String hostname = utilityHostNames.get(port);
				logWriter.println("[MASTER] Checking utility at port " + port);

				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(hostname, port), 5000);
				logWriter.println("[MASTER] Connected to utility at port " + port);

				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				logWriter.println("[MASTER] Created output stream for port " + port);
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				logWriter.println("[MASTER] Created input stream for port " + port);

				out.writeObject("HEARTBEAT");
				logWriter.println("[MASTER] Sent HEARTBEAT to port " + port);
				out.flush();

				String response = (String) in.readObject();
				socket.close();

				if (response.equals("AVAILABLE")) {
					available.add(port);
					logWriter.println("[MASTER] Utility at port " + port + " is available");
				} else {
					logWriter.println("[MASTER] Utility at port " + port + " is busy");
				}
				logWriter.flush();
			} catch (Exception e) {
				logWriter.println("[MASTER] Utility at port " + port + " is not responding.");
			}
		}
		return available;
	}

	private List<Integer> heartbeat(int timeout) {
		List<Integer> availableUtilities = new ArrayList<>();
		List<Integer> nonRespondingPorts = new ArrayList<>();

		HashMap<Integer, String> utilityHostNames = new HashMap<>();

		int tempPort = 32006;
		for (int port : utilityPorts) {
			utilityHostNames.put(tempPort, ("utility") + (tempPort - 32005));
			tempPort++;
		}

		for (int port : activeUtilityPorts) {
			try {
				String hostname = utilityHostNames.get(port);
				logWriter.println("[MASTER] Sending heartbeat to utility at port " + port);

				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(hostname, port), 2000);

				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

				out.writeObject("HEARTBEAT");
				out.flush();

				socket.setSoTimeout(timeout);

				String response = (String) in.readObject();
				socket.close();

				if (response.equals("AVAILABLE")) {
					availableUtilities.add(port);
					logWriter.println("[MASTER] Utility at port " + port + " is AVAILABLE");
					;
				} else {
					logWriter.println("[MASTER] Utility at port " + port + " is not responding");
				}
			} catch (Exception e) {
				logWriter.println("[MASTER] Utility at port " + port + " did not responding within time");
				nonRespondingPorts.add(port);
			}
		}

		activeUtilityPorts.removeAll(nonRespondingPorts);
		if (!nonRespondingPorts.isEmpty()) {
			logWriter.println("Dropped " + nonRespondingPorts.size() + " unresponsive utilities");
		}

		return availableUtilities;
	}

	private void readJob() throws IOException, ClassNotFoundException {
		// reads the packet
		ObjectInputStream dataIn = new ObjectInputStream(clientSocket.getInputStream()); // from the client
		HashMap<String, Object> packet = (HashMap<String, Object>) dataIn.readObject(); // data to be sorted

		sortChoice = (String) packet.get("sort");

		int[] dataArr = (int[]) packet.get("data");
		logWriter.println("[MASTER] Received " + dataArr.length + " numbers from client");
		data = new ArrayList<>();
		for (int i = 0; i < dataArr.length; i++) {
			data.add(dataArr[i]);
		}

		// Just for status checks so it doesn't seem like nothings happening
		int count = 0;
		for (Integer num : data) {
			count++;
			if (count % 100000 == 0) {
				logWriter.println("[MASTER] Read " + count + " integers so far from " + clientSocket.getInetAddress());
			}
		}
	}

	private List<int[]> chunkData(List<Integer> data) {
		List<int[]> chunks = new ArrayList<>();
		int total = data.size();
		int chunkSize;

		if (activeUtilityPorts.size() == 0) {
			System.err.println("[MASTER] Warning: No active utility ports.");
			chunkSize = 1;
		} else {
			chunkSize = data.size() / activeUtilityPorts.size();
			if (chunkSize == 0) {
				chunkSize = 1;
			}
		}

		for (int i = 0; i < total; i += chunkSize) {
			int end = Math.min(i + chunkSize, total);
			int[] chunk = new int[end - i];
			for (int j = i; j < end; j++) {
				chunk[j - i] = data.get(j);
			}
			chunks.add(chunk);
		}

		return chunks;
	}

	// This maybe should be two methods
	private void sendAndReceiveData(List<int[]> chunks, List<Integer> availableServers) {
		int utilityIndex = 0;
		try {
			logWriter.println("[MASTER] Sending " + chunks.size() + " chunks.");

			HashMap<Integer, String> utilityHostNames = new HashMap<>();

			int tempPort = 32006;
			for (int port : utilityPorts) {
				utilityHostNames.put(tempPort, ("utility") + (tempPort - 32005));
				tempPort++;
			}

			for (int[] chunk : chunks) {
				if (utilityIndex >= availableServers.size()) {
					utilityIndex = 0;
				}
				int port = availableServers.get(utilityIndex++);

				String hostname = utilityHostNames.get(port);

				try {
					Socket s = new Socket(hostname, port);
					ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
					ObjectInputStream in = new ObjectInputStream(s.getInputStream());

					HashMap<String, Object> task = new HashMap<>();
					task.put("sort", sortChoice);
					task.put("data", chunk);
					task.put("log", log);

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
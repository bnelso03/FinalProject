import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Master {
	private static String sortChoice = ""; // Choice of sort given by the client
	private static List<Integer> data; // Data given by the client
	private static List<int[]> chunks;
	private static List<Socket> knownUtilities = new ArrayList<>();
	private static List<Integer> sums = new ArrayList<Integer>();

	public static void main(String[] args) throws IOException {

		int port = 32005;
		Client client;

		// The server program starts by creating a new ServerSocket object to list on a
		// specific port.
		// When we run this server, pick a port that is not already in use or dedicated
		// to some other service.

		ServerSocket masterServer = new ServerSocket(port);

		// The constructor for ServerSocket throws an exception which you can implement
		// a try catch or simply throws IOException
		// The reason being that perhaps that port is already in use.

		System.out.println("Master server is starting...");

		while (true) {

			try {
				// Client Conection
				Socket clientSocket = masterServer.accept();
				client = new Client(clientSocket);
				Thread t = new Thread(client);
				String message = "Thread " + t.getName() + " has been assigned to this client";
				System.out.println(message);

				// Start client thread
				t.start();

				// Read data from client
				readJob(clientSocket);

				// Send heartbeat


				// Split and convert data
				chunkData(data);

				// Send chunks to utility servers
				// assignTasks(knownUtilities, chunks);

				// Collect and aggregate results from utilities
				ObjectInputStream in = new ObjectInputStream(knownUtilities.get(0).getInputStream());
				sums.add((int) in.readObject());

				// Return result to Client
				ObjectOutputStream result = new ObjectOutputStream(clientSocket.getOutputStream());
				result.writeObject(sums.get(0));
				result.flush();

			} catch (Exception ex) {
				masterServer.close();
				ex.printStackTrace();
			}

		}

	}

	private static void readJob(Socket clientSocket) throws IOException, ClassNotFoundException { // reads the packet
																									// from the client
		ObjectInputStream dataIn = new ObjectInputStream(clientSocket.getInputStream());
		HashMap<String, Object> packet = (HashMap<String, Object>) dataIn.readObject(); // data to be sorted
		sortChoice = (String) packet.get("sort");
		data = (List<Integer>) packet.get("data");
	}

	private static void chunkData(List<Integer> data) {
		try {
			// testing util connection
			Socket utilitySocket = new Socket("localhost", 32006); // manual entry for testing
			knownUtilities.add(utilitySocket);
			HashMap<String, Object> task = new HashMap<String, Object>();
			task.put("sort", sortChoice);
			task.put("data", data);
			ObjectOutputStream dataOut = new ObjectOutputStream(utilitySocket.getOutputStream());
			dataOut.writeObject(task);
			dataOut.flush();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class Master {

	static ArrayList<Utility> availableServers = new ArrayList<Utility>();
    public static void main(String[] args)throws IOException {
		
		int port = 32005; 
		Utility utility;
		String sortChoice = args[0];
		File file = new File(args[1]);
        List<Integer> input = readFile(file);
		
		//The server program starts by creating a new ServerSocket object to list on a specific port. 
		// When we run this server, pick a port that is not already in use or dedicated to some other service. 
		
		ServerSocket server = new ServerSocket(port);
		
		//The constructor for ServerSocket throws an exception which you can implement a try catch or simply throws IOException
		//The reason being that perhaps that port is already in use. 
		
		System.out.println("Master server is starting.....");
		
		while(true) {
			
			try {
				//If the server successfully binds to its port,then the ServerSocket object is successfully created and the server
				// continues to the next step -> accepting a connection from a client 
				
				// Startup
				utility = new Utility(server.accept(), sortChoice, input); //The accept method waits until a client starts up and requests a connection on the host. 
				Thread t = new Thread(utility);
				String message = "Thread " + t.getName() + " has been assigned to this client";
				availableServers.add(utility); // Add utility to available list
				System.out.println(message);
				
				// Divide data
				int numsPerServer = input.size() / availableServers.size();
				System.out.println(numsPerServer);
				// more

				// Start threads - more later
				t.start();

				
			} catch (Exception ex) {
				//server.close();
				ex.printStackTrace();
			} finally {
				server.close();
			}
			/*
			 * We get the socket's input or output stream and open some reader or writer
			 * Initiate communication with the client by writing to the socket
			 * we do this by using the while loop
			 * 
			 * 
			 */
			
			
		}

		
		
		
	}

	private static void sendHeartbeat() { // incomplete
		for (int i = 0; i < availableServers.size(); i++) {
			if (availableServers.get(i).getServerState().equals("AVAILABLE")) {

			}
		}
	}

	public static List<Integer> readFile(File inFile) throws IOException{ // temporary
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		List<Integer> output = new ArrayList<Integer>();
		String line;
		while ((line = br.readLine()) != null) {
			int num = Integer.parseInt(line);
			output.add(num);
		}
		return output;
	}
}

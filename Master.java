import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Master {

	static ArrayList<Utility> availableServers = new ArrayList<Utility>();
    public static void main(String[] args)throws IOException {
		
		int port = 32005; 
		Utility utility;

		// Inputs
		File file = new File(args[0]);
		int input = readFile();
		
		//The server program starts by creating a new ServerSocket object to list on a specific port. 
		// When we run this server, pick a port that is not already in use or dedicated to some other service. 
		
		ServerSocket server = new ServerSocket(port);
		
		//The constructor for ServerSocket throws an exception which you can implement a try catch or simply throws IOException
		//The reason being that perhaps that port is already in use. 
		
		System.out.println("Server is starting.....");
		
		while(true) {
			
			try {
				//If the server successfully binds to its port,then the ServerSocket object is successfully created and the server
				// continues to the next step-accepting a connection from a client 
				//The accept method waits until a client starts up and requests a connection on the host. 
				utility = new Utility(server.accept());
				
				Thread t = new Thread(utility);
				
				String message = "Thread " + t.getName() + " has been assigned to this client";

				availableServers.add(utility); // Add utility to available list
				
				System.out.println(message);
				
				
				t.start(); // Utility Runs

				
			} catch (Exception ex) {
				
				server.close();
				ex.printStackTrace();
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

	public static int[] readFile(File inFile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		int output[] = new int[(int) inFile.length()];
		String line = br.readLine();
		while (line != null) {

		}



			
		return null;
	}


}

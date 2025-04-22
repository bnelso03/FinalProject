import java.io.IOException;
import java.net.ServerSocket;

public class Master {
    public static void main(String[] args)throws IOException {
		
		
		int port = 32005; 
		Utility utility;
		
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
				
				System.out.println(message);
				
				
				t.start();

				
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
}

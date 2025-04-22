import java.io.PrintWriter;
import java.net.Socket;

public class Utility extends Thread {
    private Socket utility;

	public Utility(Socket utility) {
		this.utility = utility;
	}

	public static void main(String[] args) {

		String host = "localhost";

		int port = 32005;

		try {
			// creates a new socket object and we are naming it socket
			//The socket constructor requires computer name/ip address and the port number to which you want to connect. 
			//The name of the computer/host is the fully qualified IP address of the computer to which you want to connect. 
			Socket socket = new Socket(host, port);
			
			
			//Send data through the socket to the server, so the ClientTester need to write using PrintWriter
			PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
			System.out.println("Sending message.....");
			
//			pw.println("Client finished");
			
			

		} catch (Exception ex) {

			ex.printStackTrace();
		}
		
		/*	The overall logic for coding out a client socket class.
		 *  1. open a socket
		 *  2. open some stream to the socket. 
		 *  3. read data or writing data to a stream. TCP
		 *  4. close the streams/socket. 
		 * 
		 * 
		 */

	}
}

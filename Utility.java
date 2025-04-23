import java.io.PrintWriter;
import java.net.Socket;

public class Utility extends Thread {
    private Socket utility;
	private static String state;
	private String IPAddress;
	//private BufferedReader in;
	private PrintWriter output;
	private String chosenAlgorithm;
	private int[] data;

	public Utility(Socket utility) {
		this.utility = utility;
		this.state = "AVAILABLE"; // Server starts available
		this.IPAddress = "0"; // change to pooling later?
		
	}

	public Utility(Socket utility, String chosenAlgorithm) {
		this.utility = utility;
		this.state = "AVAILABLE"; // Server starts available
		this.IPAddress = "0"; // change to pooling later?
		this.chosenAlgorithm = chosenAlgorithm;
		
	}


	public static void main(String[] args) {

		String host = "localhost";

		int port = 32005;

		try {
			
			//The socket constructor requires computer name/ip address and the port number to which you want to connect. 
			//The name of the computer/host is the fully qualified IP address of the computer to which you want to connect. 

			Socket utility = new Socket(host, port);// creates a new socket object and we are naming it utility
			
			state = "LOCKED"; // change state when running
			
			//Send data through the socket to the server, so the ClientTester need to write using PrintWriter
			PrintWriter pw = new PrintWriter(utility.getOutputStream(), true);
			System.out.println("Sending message.....");


			// Sort data according to assigned algorithm
			data = sortData(data);
			
			// sum first and last 5 nums of sorted data
			int result = sumSortedData(data);

			
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

	public String getServerState() { // Returns the state of the utility server 
		return this.state;
	}

	private static int[] sortData(int[] data) { // Function that redirects data according to sort
		int[] result = Arrays.copyOf(data, data.length);
		switch(chosenAlgorithm) {
			case "bubble":
				bubbleSort(result);
				break;
			case "insertion":
				//insertionSort(result);
				break;
			case "merge":
				//mergeSort(result);
				break;
			default:
				System.out.println("Invalid algorithm");
				break;
		}
		return result;
	}

	private static int sumSortedData(int[] data) { // Adds the first and last 5 entries of the sorted data
		int sum = 0;
		int length = data.length;
		for (int i = 0; i < 5; i++) { // Sum the first 5 entries
			if (i < n) {
				sum += data[i];
			}
		}
		for (int i = (length - 5); i < 5; i++) { // Sum the last 5 entries
			sum += data[i];
		}
		return sum;
	}

	private void bubbleSort(int[] data) { // Bubble Sort
		int length = data.length;
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length - i; j++) {
				int temp = data[j];
				data[j] = data[j+1];
				data[j+1] = temp;
			}
		}
	}

}

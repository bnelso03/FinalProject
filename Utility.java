import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.UIDefaults.ActiveValue;

public class Utility extends Thread {
    private static String host = "localhost";
	private static int port = 32006; // implement port pooling later
	private static String state = "";
	private static String sort = "";
	private static int[] data;
	private static int sum = 0;

	public static void main(String[] args) throws IOException {
		ServerSocket utilityServer = new ServerSocket(port);
		System.out.println("Utility Server Starting...");
		state = "AVAILABLE";

		while (true) {
			try {

				// Master Connection
				Socket masterSocket = utilityServer.accept();

				// Accept Data from master
				readTask(masterSocket);
				state = "LOCKED";
				
				// Process given task
				runTask();
				
				// Return sum to master
				turnInTask(masterSocket);

				// Free availability
				state = "AVAILABLE";

				
			} catch (Exception ex) {
				utilityServer.close();
				ex.printStackTrace();
			}
		}
		
	}

	private static void turnInTask(Socket socket) throws IOException, ClassNotFoundException{
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(sum);
        out.flush();
	}

	private static void readTask(Socket socket) throws IOException, ClassNotFoundException{
		ObjectInputStream chunk = new ObjectInputStream(socket.getInputStream());
		HashMap<String, Object> packet = (HashMap<String, Object>) chunk.readObject();
		sort = (String) packet.get("sort"); // choice of sort
		List<Integer> dataList = (List<Integer>) packet.get("data"); // data to be sorted
		data = new int[dataList.size()];
		for (int i = 0; i < data.length; i++) {
			data[i] = dataList.get(i);
		}
	}

	private static void runTask() {
		// Sort the given data
		int[] sortedData = sortData();
		// Sum the first and last 5 numbers of the sorted data
		sumSortedData(sortedData);
	}

	public String getServerState() { // Returns the state of the utility server 
		return this.state;
	}

	private static int[] sortData() { // Function that redirects data according to sort
		int[] result = Arrays.copyOf(data, data.length);
		switch(sort) {
			case "bubble":
				bubbleSort(result);
				break;
			case "insertion":
				insertionSort(result);
				break;
			case "merge":
				//mergeSort(result);
				break;
		}
		return result;
	}

	private static void sumSortedData(int[] data) { // Adds the first and last 5 entries of the sorted data
		sum = 0;
		int length = data.length;
		for (int i = 0; i < 5; i++) { // Sum the first 5 entries
			if (i < length) {
				sum += data[i];
			}
		}
		for (int i = length - 5; i < length; i++) { // Sum the last 5 entries
			sum += data[i];
		}
	}

	private static void bubbleSort(int[] data) { // Bubble Sort
		int length = data.length;
		for (int i = 0; i < length - 1; i++) {
			for (int j = 0; j < length - i-1; j++) {
				int temp = data[j];
				data[j] = data[j+1];
				data[j+1] = temp;
			}
		}
	}

	public static void insertionSort(int[] data) {
		int length = data.length;
		int key, j = 0;
		for (int i = 2; i < length; i++) {
			key = data[i];
			// insert data[i] into sorted
			j = i - 1;
			while (j > 0 && data[i] > key) {
				data[j+1] = data[j];
				j--;
			}
			data[j+1] = key;
		}
	}

	

}

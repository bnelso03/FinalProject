import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;

/*************************
 * 
 * Just waits for tasks from the Master server and responds to heart beats.
 * When a chunk is received, it locks itself and uses (right now) an assigned algorithm.
 * It sums the first & last five numbers after sorting and then sends it back to the Master.
 * 
 ************************/
public class Utility extends Thread {
	private static String host = "localhost";
	private static int PORT; // implement port pooling later
	private static String state = "";
	private static String sort = "";
	private static int[] data;
	private static int sum = 0;

	public static void main(String[] args) throws IOException {

		if (args.length < 2) {
			System.err.println("Error, PORT and SORT must be specified");
			return;
		}

		PORT = Integer.parseInt(args[0]);
		sort = args[1].toLowerCase();

		ServerSocket utilityServer = new ServerSocket(PORT);
		System.out.println("Utility Server Starting...");
		state = "AVAILABLE";

		while (true) {
			try {
				Socket masterSocket = utilityServer.accept();
				startWorkerThread(masterSocket);
			} catch (Exception ex) {
				ex.printStackTrace();
				utilityServer.close();
			}
		}
	}

	private static void startWorkerThread(Socket socket) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				handleConnection(socket);
			}
		});
		t.start();
	}

	private static void handleConnection(Socket socket) {
		try {
			System.out.println("[Utility] Connection accepted from " + socket.getInetAddress());

			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

			Object obj = in.readObject();

			if (obj instanceof String) {
				String command = (String) obj;
				if (command.equals("HEARTBEAT")) {
					out.writeObject(state);
					out.flush();
					socket.close();
					return;
				}
			}

			System.out.println("[Utility] Received task");

			HashMap<String, Object> packet = (HashMap<String, Object>) obj;
			Object dataObj = packet.get("data");

			if (dataObj instanceof int[]) {
				int[] incomingData = (int[]) dataObj;
				data = incomingData;
			} else {
				System.err.println("Idk wtf ur trying to send me dawg 😭");
				socket.close();
				return;
			}

			state = "LOCKED";
			System.out.println("[Utility] Running task...");

			runTask();

			System.out.println("[Utility] Task complete. Sending sum back.");

			out.writeObject(sum);
			out.flush();
			socket.close();
			state = "AVAILABLE";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void runTask() {
		try {
			// I'd recommend you DO NOT uncomment this unless you're specifically testing for it. It makes things take FOREVER, but the problem set asks for it.
			// Thread.sleep(15000); // 15 sec delay
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Sort the given data
		int[] sortedData = sortData();
		// Sum the first and last 5 numbers of the sorted data
		sumSortedData(sortedData);
	}

	public String getServerState() { // Returns the state of the utility server
		return state;
	}

	private static int[] sortData() { // Function that redirects data according to sort
		int[] result = Arrays.copyOf(data, data.length);
		switch (sort) {
			case "bubble":
				bubbleSort(result);
				break;
			case "insertion":
				insertionSort(result);
				break;
			case "merge":
				int left = result[0];
				int right = result[result.length - 1];
				mergeSort(result, left, right);
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
			for (int j = 0; j < length - i - 1; j++) {
				int temp = data[j];
				data[j] = data[j + 1];
				data[j + 1] = temp;
			}
		}
	}

	public static void insertionSort(int[] data) {
		int length = data.length;
		int key, j;
		for (int i = 1; i < length; i++) {
			key = data[i];
			// insert data[i] into sorted
			j = i - 1;
			while (j >= 0 && data[j] > key) {
				data[j + 1] = data[j];
				j--;
			}
			data[j + 1] = key;
		}
	}

	// Straight from geeksforgeeks lol
	public static void mergeSort(int[] data, int left, int right) {
		if (left < right) {
			int mid = (left + right) / 2;
			mergeSort(data, left, mid);
			mergeSort(data, mid + 1, right);
			merge(data, left, mid, right);
		}
	}

	public static void merge(int[] arr, int left, int mid, int right) {
		int n1 = mid - left + 1;
		int n2 = right - mid;

		int[] L = new int[n1];
		int[] R = new int[n2];

		for (int i = 0; i < n1; i++) {
			L[i] = arr[left + i];
		}

		for (int j = 0; j < n2; j++) {
			R[j] = arr[mid + 1 + j];
		}

		int i = 0, j = 0, k = left;

		while (i < n1 && j < n2) {
			if (L[i] <= R[j]) {
				arr[k++] = L[i++];
			} else {
				arr[k++] = R[j++];
			}
		}

		while (i < n1) {
			arr[k++] = L[i++];
		}
		while (j < n2) {
			arr[k++] = R[j++];
		}
	}
}

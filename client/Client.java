import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

    public Client(Socket client) throws IOException {

    }

    /********************************
     * 
     * Basically connects to the Master server over a socket, 
     * reads the numbers from a file into an array,
     * sends the array & sort choice,
     * and then receives the final summed result.
     * 
     *******************************/
    public static void main(String args[]) throws ClassNotFoundException {
        File file = new File(args[0]); // Input file
        String sort = args[1];
        String host = "localhost"; 
        int port = 32005;

        try{
            // Create a new socket
            Socket socket = new Socket(host, port);
            System.out.println("[CLIENT] Connected to master!");

            // Send data through socket
            System.out.println("[CLIENT] Sending message through socket...");
            int[] data = readFile(file);
            System.out.println("[CLIENT] File parsed. Sending!");
            sendTask(socket, data, sort);
            System.out.println("[CLIENT] Sent!");

            // Retreive result from master
            ObjectInputStream result = new ObjectInputStream(socket.getInputStream());
            System.out.println("[CLIENT] Total sum: " + (int) result.readObject());

        } catch(IOException ex){
            ex.printStackTrace();
        }
    }

    public static void sendTask(Socket socket, int[] data, String sort) throws IOException { // sends the List object over to port
        ObjectOutputStream dataOut = new ObjectOutputStream(socket.getOutputStream()); // Create data packet
        // Create data packet
        HashMap<String, Object> packet = new HashMap<String,Object>();
        packet.put("sort", sort); // alg sort choice 
        packet.put("data", data); // data to sort

        System.out.println("[CLIENT] Sending packet containing " + data.length + " numbers...");
        dataOut.writeObject(packet); // data packet to be sorted
        dataOut.flush();
        System.out.println("[CLIENT] Packet sent!");
    }

    // Originally returned a list, but sending objects instead of primitive types ended up being like 100x slower
    public static int[] readFile(File inFile) throws IOException { 
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		List<Integer> dataList = new ArrayList<Integer>();
		String line;
        int count = 0;

		while ((line = br.readLine()) != null) {
			int num = Integer.parseInt(line);
			dataList.add(num);
            count++;

            if (count % 100000 == 0) {
                System.out.println("Parsed " + count + " numbers...");
            }
		}
        br.close();

        int[] data = new int[dataList.size()];
        for (int i = 0; i < dataList.size(); i++) {
            data[i] = dataList.get(i);
        }
        return data;
	}
}

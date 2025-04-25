import java.io.*;
import java.net.*;
import java.util.*;

public class Client extends Thread {
    private Socket client;

    public Client(Socket client) throws IOException {
        this.client = client;
    }

    public static void main(String args[]) throws ClassNotFoundException {
        File file = new File(args[0]); // Input file
        String sort = args[1];
        String host = "localhost";
        int port = 32005;
        try{
            // Create a new socket
            Socket socket = new Socket(host, port);

            // Send data through socket
            System.out.println("Sending message through socket...");
            List<Integer> data = readFile(file);
            sendTask(socket, data, sort);

            // Retreive result from master
            ObjectInputStream result = new ObjectInputStream(socket.getInputStream());
            System.out.println((int) result.readObject());

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    public static void sendTask(Socket socket, List<Integer> data, String sort) throws IOException { // sends the List object over to port
        ObjectOutputStream dataOut = new ObjectOutputStream(socket.getOutputStream());
        // Create data packet
        HashMap<String, Object> packet = new HashMap<String,Object>();
        packet.put("sort", sort); // alg sort choice 
        packet.put("data", data); // data to sort
        dataOut.writeObject(packet); // data packet to be sorted
        dataOut.flush();
    }

    public static List<Integer> readFile(File inFile) throws IOException { // Parses data from file into a list of integers
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		List<Integer> data = new ArrayList<Integer>();
		String line;
		while ((line = br.readLine()) != null) {
			int num = Integer.parseInt(line);
			data.add(num);
		}
        return data;
	}
}

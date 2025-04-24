import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    private Socket client;
    private PrintWriter out;

    public Client(String host, int port) {

        try {
            client = new Socket(host, port);
            System.out.println("Hello this is Client");
            out = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException ex) {
            System.out.println("Cant Connect to Host");
            ex.printStackTrace();
        }
    }

    public static void main(String args[]) {
        File file = new File(args[0]);
        
        String host = "localhost";
        int port = 32005;
        try{
            List<Integer> input = readFile(file);
            System.out.println("Read numbers from file: " + input);
            Client client = new Client(host,port);
            client.sendFile(input);
            System.out.println("Bye Master Server");
        }catch(IOException ex){
            System.out.println("File not found");
            ex.printStackTrace();
        }
    }

    public void sendFile(List<Integer> nums) {
        for (Integer num : nums) {
            out.println(num);
            System.out.println("Sending file finished");
        }
    }

    public static List<Integer> readFile(File inFile) throws IOException { // temporary
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

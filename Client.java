public class Client {

    


    public static void main(String args[]) {
        File file = new File(args[0]);
        int input = readFile();
    }




    public static int[] readFile(File inFile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		List<Integer> output = new ArrayList<Integer>();
		String line;
		while (line = br.readLine() != null) {
			int num = Integer.parseInt(line);
			output.add(num);
		}
		return output;
	}
    
}
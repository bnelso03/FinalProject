import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class TestFileGenerator {
    public static void main(String[] args) {
        // Define parameters
        int numberOfNumbers = 30000000; // 10 million numbers
        String fileName = "testData.txt";
        Random random = new Random();
        
        System.out.println("Starting to generate " + numberOfNumbers + " numbers...");
        long startTime = System.currentTimeMillis();
        
        try (FileWriter writer = new FileWriter(fileName)) {
            // Generate the numbers and write them to file
            for (int i = 0; i < numberOfNumbers; i++) {
                int number = random.nextInt(1000000); // Random number between 0 and 999,999
                writer.write(Integer.toString(number) + "\n");
                
                // Print progress every million numbers
                if (i > 0 && i % 1000000 == 0) {
                    System.out.println("Generated " + i + " numbers...");
                }
            }
            
            System.out.println("Successfully generated " + numberOfNumbers + " numbers.");
            System.out.println("File saved as: " + fileName);
            
            long endTime = System.currentTimeMillis();
            double durationSeconds = (endTime - startTime) / 1000.0;
            System.out.println("Generation took " + durationSeconds + " seconds.");
            
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
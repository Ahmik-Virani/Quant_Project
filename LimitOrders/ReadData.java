import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReadData {

    public ReadData(String fileName, List<Price> priceFeed) {
        // Define the path of the csv file containing the data
        String path = "../Data/" + fileName + ".csv";
        
        // Have the correct date time formatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss.SSS");

        int counter = 1;
        
        // Try to read the csv file line by line
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String line;
            while ((line = br.readLine()) != null) {

                // Store the line seperated by commas in a temporary array
                String[] records = line.split(",");

                // Then let us ensure that the timestamp is in double format
                LocalDateTime dateTime = LocalDateTime.parse(records[1], formatter);

                // Ensure all the necessary values are stored properly
                long timeStamp = dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
                float bidPrice = Float.parseFloat(records[2]);
                float askPrice = Float.parseFloat(records[3]);

                // Insert to a new price
                priceFeed.add(new Price(bidPrice, askPrice, timeStamp));

            }
        } catch (IOException e) {
            System.err.println("File I/O error: " + e.getMessage());
        }
    }
}
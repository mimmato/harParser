package FacebookEventsScrape;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class harParser {
        public static void main(String[] args) throws IOException {
            File harFile = new File("www.facebook.com_Archive [25-02-18 17-53-11].har"); // Update this path
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(harFile);

            // Print the full JSON to understand its structure
            System.out.println(rootNode.toPrettyString());
            JsonNode events = rootNode.findPath("events"); // Change this if needed
            for (JsonNode event : events) {
                String dateTime = event.findPath("day_time_sentence").asText();
                String socialContext = event.findPath("social_context").get("text").asText();
                String title = event.findPath("meta").get("title").asText();

                System.out.println("Date & Time: " + dateTime);
                System.out.println("Attendees: " + socialContext);
                System.out.println("Title: " + title);
            }
        }
    }
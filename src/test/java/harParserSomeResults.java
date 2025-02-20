import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class harParserSomeResults {
    public static void main(String[] args) {

        try {
            // Read the HAR file content
            String content = new String(Files.readAllBytes(Paths.get("www.facebook.com_Archive [25-02-18 17-53-11].har")));

            // Parse the outer JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(content);

            // Extract the raw "text" field that contains the actual JSON data
            String rawJsonText = rootNode.findPath("content").path("text").asText();

            // Parse this extracted JSON string as a new JSON object
            JsonNode actualData = objectMapper.readTree(rawJsonText);

            // Navigate to the event data
            JsonNode events = actualData.path("data").path("viewer").path("suggested_events").path("events").path("edges");

            for (JsonNode eventNode : events) {
                if (!eventNode.has("node")) {
                    System.out.println("Skipping event: Missing 'node' field");
                    continue;
                }

                JsonNode node = eventNode.path("node");

                String dateTime = node.path("day_time_sentence").asText(null);
                String title = node.path("name").asText(null);
                String socialContext = node.path("social_context").path("text").asText(null);
                String eventUrl = node.path("eventUrl").asText(null);

                if (dateTime == null || title == null || eventUrl == null) {
                    System.out.println("Skipping event: Incomplete data - " + node.toPrettyString());
                    continue;
                }

                System.out.println("Date & Time: " + dateTime);
                System.out.println("Title: " + title);
                System.out.println("Attendees: " + socialContext);
                System.out.println("Event URL: " + eventUrl);
                System.out.println("----------------------------");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
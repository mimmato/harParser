import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class harParser {
    public static void main(String[] args) {
        try {
            // Read the HAR file content
            String content = new String(Files.readAllBytes(Paths.get("www.facebook.com_Archive [25-02-18 17-53-11].har")));

            // Initialize ObjectMapper for JSON parsing
            ObjectMapper objectMapper = new ObjectMapper();

            // Parse the outer JSON to inspect its structure
            JsonNode rootNode = objectMapper.readTree(content);

            // Look for the 'entries' field in the 'log' structure
            JsonNode entriesNode = rootNode.path("log").path("entries");

            if (entriesNode.isArray()) {
                // Iterate through the entries to find relevant requests
                for (JsonNode entry : entriesNode) {
                    JsonNode requestNode = entry.path("request");
                    String url = requestNode.path("url").asText();

                    // Check if this request is related to the Graph API
                    if (url.contains("graphql")) {
                        System.out.println("Found GraphQL request URL: " + url);

                        // Extract the response body if available
                        JsonNode responseNode = entry.path("response");
                        if (!responseNode.isMissingNode()) {
                            JsonNode contentNode = responseNode.path("content");
                            if (!contentNode.isMissingNode()) {
                                String rawJsonText = contentNode.path("text").asText();
                                if (!rawJsonText.isEmpty()) {
                                    // Print the raw JSON and try to parse it further
                                    System.out.println("Raw JSON Text (partial):");
                                    System.out.println(rawJsonText.substring(0, Math.min(500, rawJsonText.length())));

                                    // Parse the response body to explore the structure
                                    JsonNode actualData = objectMapper.readTree(rawJsonText);
                                    System.out.println("Parsed Data (partial):");
                                    System.out.println(actualData.toString().substring(0, Math.min(500, actualData.toString().length())));

                                    // Continue searching for events or edges
                                    JsonNode eventsRoot = actualData.path("data").path("events");

                                    if (eventsRoot.isArray()) {
                                        for (JsonNode eventNode : eventsRoot) {
                                            // Safely extract event details
                                            String title = eventNode.path("name").asText("No Title Available");
                                            String dateTime = eventNode.path("date").asText("No Date Available");
                                            url = eventNode.path("eventUrl").asText("No URL Available");
                                            // Print event details
                                            System.out.println("Event: " + title);
                                            System.out.println("Date & Time: " + dateTime);
                                            System.out.println("URL: " + url);
                                            System.out.println("----------------------------");
                                        }
                                    } else {
                                        System.out.println("No events found in this response.");
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                System.out.println("No entries found in the HAR file.");
            }

        } catch (IOException e) {
            System.out.println("Error reading or parsing the HAR file: " + e.getMessage());
        }
    }
}
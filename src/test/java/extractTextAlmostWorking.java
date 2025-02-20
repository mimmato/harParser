import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class extractTextAlmostWorking {
    public static void main(String[] args) {
        String filePath = "www.facebook.com_Archive [25-02-19 16-28-34-2].har"; // Change this to your actual HAR file

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File(filePath));

            List<Event> events = new ArrayList<>();
            JsonNode entries = root.path("log").path("entries");

            if (entries.isArray()) {
                for (JsonNode entry : entries) {
                    JsonNode textNode = entry.path("response").path("content").path("text");

                    // Ensure the text is JSON and parse it
                    if (textNode.isTextual() && textNode.asText().startsWith("{")) {
                        JsonNode parsedJson = mapper.readTree(textNode.asText());
                        extractEvents(parsedJson, events);
                    }
                }
            }

            // Print extracted events
            for (Event event : events) {
                System.out.println(event);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String extractSocialContext(JsonNode socialContextNode) {
        if (socialContextNode.has("text")) {
            // Get the text field
            String text = socialContextNode.path("text").asText();
            // Replace middle dot (·) with a comma and space
            return text.replace("\u00b7", ", ");
        }
        return "N/A"; // Return a default value if not found
    }


    private static void extractEvents(JsonNode node, List<Event> events) {
        if (node.isObject()) {
            if (node.has("schema_context")) {
                // Found a new event, extract its details
                Event event = new Event();
                event.schemaContext = node.path("schema_context").asText();
                event.dayTimeSentence = node.path("day_time_sentence").asText();
                event.name = node.path("name").asText();
//                event.socialContext = node.path("social_context").asText();
//                event.eventPlace = node.path("event_place").asText();
                event.socialContext = extractSocialContext(node.path("social_context"));
                event.eventPlace = node.path("event_place").path("contextual_name").asText();

                event.eventUrl = node.path("eventUrl").asText();
                events.add(event);
            }
            // Recursively check all fields in the JSON
            for (JsonNode child : node) {
                extractEvents(child, events);
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                extractEvents(item, events);
            }
        }
    }

    // Event class to store extracted data
    static class Event {
        String schemaContext;
        String dayTimeSentence;
        String name;
        String socialContext;
        String eventPlace;
        String eventUrl;

        public String toString() {
            return "------------------------------------------"+
                    "\n  Date and Time: '" + dayTimeSentence + '\'' +
                    "\n  Event name: '" + name + '\'' +
                    "\n  People: '" + socialContext + '\'' +
                    "\n  Event Place: '" + eventPlace + '\'' +
                    "\n  Event URL: '" + eventUrl + '\'';
        }
    }
}
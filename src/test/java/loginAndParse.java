import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.json.JSONObject;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.*;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class loginAndParse {

        public static void main(String[] args) throws Exception {
            // Reading configuration
            File configFile = new File("config.json");
            String configData = new String(java.nio.file.Files.readAllBytes(configFile.toPath()));
            JSONObject config = new JSONObject(configData);

            String place = config.getString("Place");
            String keyword = config.getString("Keyword");
            String username = config.getJSONObject("Facebook credentials").getString("Email");
            String password = config.getJSONObject("Facebook credentials").getString("Password");

            // Selenium Setup
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            WebDriver driver = new ChromeDriver(options);

            // Navigate to Facebook login page
            driver.get("https://www.facebook.com/login");

            // Logging into Facebook
            driver.findElement(By.id("email")).sendKeys(username);
            driver.findElement(By.id("pass")).sendKeys(password);
            driver.findElement(By.id("loginbutton")).click();

            // Wait until page is fully loaded
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(By.id("__eqmc")));

            // Getting cookies
            Map<String, String> cookies = driver.manage().getCookies().stream()
                    .collect(HashMap::new, (m, v) -> m.put(v.getName(), v.getValue()), HashMap::putAll);

            // Extract the necessary fb_dtsg (anti-CSRF token)
            WebElement scriptTag = driver.findElement(By.id("__eqmc"));
            String scriptContent = scriptTag.getAttribute("innerHTML");
            JSONObject scriptJson = new JSONObject(scriptContent);
            String fb_dtsg = scriptJson.getString("f");

            driver.quit();

            // Sending request to Graph API
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.facebook.com/api/graphql/"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Cookie", cookies.entrySet().stream()
                            .map(entry -> entry.getKey() + "=" + entry.getValue())
                            .collect(Collectors.joining("; ")))
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "fb_dtsg=" + fb_dtsg + "&variables={\"count\":8,\"query\":\"" + place + "\"}"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Parsing the JSON response
            JSONObject jsonResponse = new JSONObject(response.body());
            String id_value = jsonResponse.getJSONObject("data")
                    .getJSONObject("node")
                    .getJSONArray("filter_values")
                    .getJSONObject(0)
                    .getJSONObject("node")
                    .getJSONObject("value_object")
                    .getString("id");

            System.out.println("ID Value: " + id_value);

            // Extract data from highlighted URLs (in a loop for pagination)
            boolean nextPage = true;
            String cursor = null;

            while (nextPage) {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("https://www.facebook.com/api/graphql/"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Cookie", cookies.entrySet().stream()
                                .map(entry -> entry.getKey() + "=" + entry.getValue())
                                .collect(Collectors.joining("; ")))
                        .POST(HttpRequest.BodyPublishers.ofString(
                                "fb_dtsg=" + fb_dtsg + "&cursor=" + cursor + "&variables={\"count\":5,\"text\":\"" + keyword + "\"}"))
                        .build();

                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                jsonResponse = new JSONObject(response.body());

                // Extract event data (URLs, titles, etc.)
                List<String> eventUrls = extractEventUrls(jsonResponse);

                // Check for pagination
                JSONObject pageData = jsonResponse.getJSONObject("data").getJSONObject("serpResponse").getJSONObject("results").getJSONObject("page_info");
                nextPage = pageData.getBoolean("has_next_page");
                cursor = pageData.getString("end_cursor");

                // Sleep to avoid rate-limiting
                TimeUnit.SECONDS.sleep(2);
            }
        }

        private static List<String> extractEventUrls(JSONObject jsonResponse) {
            // Extract the necessary URLs, titles, and other event details
            // Example using Jsoup to parse any HTML or data extracted from a page
            // Placeholder for actual event extraction logic
            return List.of();
        }
    }

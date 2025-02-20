import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.time.Duration;

public class openFacebook {

    @Test
    public void getFacebook() throws InterruptedException {
        WebDriverManager.chromedriver().setup();
        ChromeDriver webDriver = new ChromeDriver();
        webDriver.manage().window().maximize();
        webDriver.get("https://www.facebook.com/");

        try {
            // Find the "Decline optional cookies" button
            WebElement declineButton = webDriver.findElement(By.xpath("//div[@aria-label='Decline optional cookies' and @role='button']"));

            // Click the button
            declineButton.click();

            System.out.println("Clicked on 'Decline optional cookies'");

        } catch (Exception e) {
            System.out.println("Button not found or not clickable.");
            e.printStackTrace();
        }

        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
        try {
            // Locate Email Field and enter "test"
            WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
            emailField.sendKeys(""); // enter user

            // Locate Password Field and enter "test"
            WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pass")));
            passwordField.sendKeys(""); // enter pass

            // Locate and Click the Login Button
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@name='login']")));
            loginButton.click();

            System.out.println("Login attempted with test credentials.");

        } catch (Exception e) {
            System.out.println("Error locating elements.");
            e.printStackTrace();
        }
// Wait to manually approve login
        Thread.sleep(35000); // Wait 35 seconds for manual confirmation

// Locate and click "Always confirm that it's me"
        WebElement alwaysConfirmButton = webDriver.findElement(By.xpath("//button[contains(text(), 'Always confirm')]"));
        alwaysConfirmButton.click();




    }
}

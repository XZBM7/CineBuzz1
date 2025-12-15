package base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class BaseTest {

    public static WebDriver driver;

    public static String baseUrl = "http://127.0.0.1:5000";

    public static void setUp() {
        System.setProperty("webdriver.chrome.driver",
                "C:\\ChromeDriver\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-notifications");

        driver = new ChromeDriver(options);
    }

    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

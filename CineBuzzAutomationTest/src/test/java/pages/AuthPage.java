package pages;

import base.BasePage;
import org.openqa.selenium.WebDriver;

public class AuthPage extends BasePage {

    public AuthPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl, String path) {
        driver.get(baseUrl + path);
        pause();
    }

    public boolean redirectedToLogin() {
        return driver.getCurrentUrl().toLowerCase().contains("login");
    }

    public boolean redirectedToHome(String baseUrl) {
        String url = driver.getCurrentUrl();
        return url.equals(baseUrl + "/") || url.startsWith(baseUrl + "/?");
    }
}

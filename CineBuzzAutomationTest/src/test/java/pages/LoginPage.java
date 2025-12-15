package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class LoginPage extends BasePage {

    private final By emailField = By.id("email");
    private final By passwordField = By.id("password");
    private final By submitBtn = By.cssSelector("button[type='submit']");
    private final By alertBox = By.className("alert");

    WebDriverWait wait;

    public LoginPage(WebDriver driver) {
        super(driver);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField));
    }

    public void login(String email, String password) {
        WebElement emailEl = wait.until(ExpectedConditions.visibilityOfElementLocated(emailField));
        emailEl.clear();
        emailEl.sendKeys(email);

        WebElement passEl = driver.findElement(passwordField);
        passEl.clear();
        passEl.sendKeys(password);

        WebElement btn = driver.findElement(submitBtn);
        btn.click();
    }

    public String getFlash() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(alertBox)).getText().trim().toLowerCase();
        } catch (Exception e) {
            return "";
        }
    }
}
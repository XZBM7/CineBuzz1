package pages;
import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class RegisterPage extends BasePage {

    private final By nameField = By.id("name");
    private final By emailField = By.id("email");
    private final By passwordField = By.id("password");
    private final By confirmPasswordField = By.id("confirmPassword");
    private final By submitBtn = By.cssSelector("button[type='submit']");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/register");
        pause();
    }

    public void fill(String name, String email, String pass, String confirmPass) {
        type(nameField, name);
        type(emailField, email);
        type(passwordField, pass);
        type(confirmPasswordField, confirmPass);
    }

    public void submit() {
        click(submitBtn);
        pause();
    }
}

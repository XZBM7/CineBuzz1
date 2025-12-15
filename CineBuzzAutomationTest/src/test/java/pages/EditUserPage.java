package pages;

import base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class EditUserPage extends BasePage {

    private final By nameField = By.id("name");
    private final By phoneField = By.id("phone");
    private final By dobField = By.id("dob");
    private final By emailField = By.id("email");
    private final By saveButton = By.cssSelector("button[type='submit']");

    public EditUserPage(WebDriver driver) {
        super(driver);
    }

    public void waitForEditPage() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(nameField));
    }

    public void editName(String name) {
        WebElement el = driver.findElement(nameField);
        el.clear();
        el.sendKeys(name);
    }

    public void editPhone(String phone) {
        WebElement el = driver.findElement(phoneField);
        el.clear();
        el.sendKeys(phone);
    }

    public void editDob(String dob) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(dobField));
        WebElement el = driver.findElement(dobField);
        el.clear();
        el.sendKeys(dob);
    }

    public void editEmail(String email) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField));
        WebElement el = driver.findElement(emailField);
        el.clear();
        el.sendKeys(email);
    }

    public void clickSave() {
        WebElement btn = driver.findElement(saveButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getPageSource() {
        return driver.getPageSource();
    }
}
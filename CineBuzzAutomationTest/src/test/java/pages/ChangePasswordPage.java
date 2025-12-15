package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ChangePasswordPage extends BasePage {

    private final By currPassName = By.name("current_password");
    private final By newPassName = By.name("new_password");
    private final By confirmPassName = By.name("confirm_password");

    private final By currPassId = By.id("currentPassword");
    private final By newPassId = By.id("newPassword");
    private final By confirmPassId = By.id("confirmPassword");

    private final By changePassSubmitBtn = By.cssSelector("#changePasswordModal button[type='submit']");
    private final By changePasswordModal = By.id("changePasswordModal");

    public ChangePasswordPage(WebDriver driver) {
        super(driver);
    }

    public void changePasswordByName(String current, String newP, String confirmP) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(currPassName));
        driver.findElement(currPassName).sendKeys(current);
        driver.findElement(newPassName).sendKeys(newP);
        driver.findElement(confirmPassName).sendKeys(confirmP);
        driver.findElement(changePassSubmitBtn).click();
    }

    public void changePasswordById(String current, String newP, String confirmP) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(currPassId));
        driver.findElement(currPassId).sendKeys(current);
        driver.findElement(newPassId).sendKeys(newP);
        driver.findElement(confirmPassId).sendKeys(confirmP);
        driver.findElement(changePassSubmitBtn).click();
    }

    public boolean isChangePassModalDisplayed() {
        return driver.findElement(changePasswordModal).isDisplayed();
    }
}
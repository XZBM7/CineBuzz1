package pages;

import base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class DeleteAccountPage extends BasePage {

    private final By deletePassId = By.id("deletePassword");
    private final By deleteSubmitBtn = By.cssSelector("#deleteAccountModal button[type='submit']");

    // Admin specific
    private final By adminDeleteBtn = By.xpath("//button[contains(@class,'btn-danger') and contains(@class,'w-100')]");
    private final By adminDeleteConfirmBtn = By.xpath("//button[contains(text(),'Yes, Delete My Account')]");

    public DeleteAccountPage(WebDriver driver) {
        super(driver);
    }

    public void deleteAccount(String password) {
        WebElement passField = wait.until(ExpectedConditions.visibilityOfElementLocated(deletePassId));
        if (password.equals("WRONG")) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = 'WRONG';", passField);
        } else {
            passField.sendKeys(password);
        }
        driver.findElement(deleteSubmitBtn).click();
    }

    public void deleteAccountAdminFlow(String password) {
        WebElement deleteBtn = wait.until(ExpectedConditions.presenceOfElementLocated(adminDeleteBtn));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", deleteBtn);
        try { Thread.sleep(500); } catch (Exception ignored) {}
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);

        WebElement pwd = wait.until(ExpectedConditions.visibilityOfElementLocated(deletePassId));
        pwd.sendKeys(password);

        WebElement yesBtn = wait.until(ExpectedConditions.elementToBeClickable(adminDeleteConfirmBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", yesBtn);
    }
}
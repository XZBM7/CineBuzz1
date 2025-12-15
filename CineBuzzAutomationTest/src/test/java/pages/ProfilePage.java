package pages;

import base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.io.File;

public class ProfilePage extends BasePage {

    private final By editModalBtn = By.cssSelector("button[data-bs-target='#editProfileModal']");
    private final By changePassModalBtn = By.cssSelector("button[data-bs-target='#changePasswordModal']");
    private final By deleteAccountModalBtn = By.cssSelector("button[data-bs-target='#deleteAccountModal']");

    private final By profileImage = By.id("profileImage");
    private final By profilePicInput = By.id("profilePicInput");
    private final By profilePicForm = By.id("profilePicForm");

    private final By modalShow = By.cssSelector(".modal.show");
    private final By editProfileModal = By.id("editProfileModal");
    private final By changePasswordModal = By.id("changePasswordModal");
    private final By deleteAccountModal = By.id("deleteAccountModal");

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    public void goToProfile() {
        driver.get("http://127.0.0.1:5000/profile");
        wait.until(ExpectedConditions.urlContains("/profile"));
    }

    private void clickModalButtonSafely(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(modalShow));
        WebElement button = driver.findElement(locator);
        try {
            button.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
        }
    }

    public void openEditModal() {
        clickModalButtonSafely(editModalBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(editProfileModal));
    }

    public void openChangePassModal() {
        clickModalButtonSafely(changePassModalBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(changePasswordModal));
    }

    public void openDeleteAccountModal() {
        clickModalButtonSafely(deleteAccountModalBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(deleteAccountModal));
    }

    public void uploadPicture(String path) {
        WebElement fileInput = driver.findElement(profilePicInput);
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.display='block';", fileInput);

        File file = new File(path);
        if(!path.endsWith(".txt")) {
            try { if(!file.exists()) file.createNewFile(); } catch(Exception ignored){}
        }

        fileInput.sendKeys(path);
        driver.findElement(profilePicForm).submit();
    }

    public String getImageSrc() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(profileImage)).getAttribute("src");
    }

    public boolean isRecentBookingsSectionVisible() {
        return driver.getPageSource().contains("Recent Bookings");
    }

    public boolean isDeleteAccountSectionVisible() {
        return driver.getPageSource().contains("Delete Account");
    }

    public String getPageSource() {
        return driver.getPageSource();
    }

    public String getPageSourceLower() {
        return driver.getPageSource().toLowerCase();
    }
}
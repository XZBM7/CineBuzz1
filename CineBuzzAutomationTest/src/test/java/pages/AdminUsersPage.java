package pages;

import base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class AdminUsersPage extends BasePage {

    private final By tableRows = By.cssSelector("table tbody tr");
    private final By editButtonSelector = By.cssSelector("a.btn-outline-primary");
    private final By deleteButtonSelector = By.cssSelector("form button[type='submit']");

    public AdminUsersPage(WebDriver driver) {
        super(driver);
    }

    public void goToAdminUsers() {
        driver.get("http://127.0.0.1:5000/admin/users");
        wait.until(web -> ((JavascriptExecutor) web)
                .executeScript("return document.readyState").equals("complete"));
    }

    public List<WebElement> getUsersList() {
        return driver.findElements(tableRows);
    }

    public boolean isTableEmptyAndMessageShown() {
        List<WebElement> rows = getUsersList();
        return rows.size() == 1 && rows.get(0).getText().contains("No registered users found");
    }

    public void clickEditForUser(String email) {
        List<WebElement> rows = getUsersList();
        for (WebElement row : rows) {
            if (row.getText().contains(email)) {
                WebElement btn = row.findElement(editButtonSelector);
                btn.click();
                return;
            }
        }
        throw new NoSuchElementException("Edit button not found for user: " + email);
    }

    public void clickDeleteForUser(String email) {
        List<WebElement> rows = getUsersList();
        for (WebElement row : rows) {
            if (row.getText().contains(email)) {
                WebElement btn = row.findElement(deleteButtonSelector);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                return;
            }
        }
        throw new NoSuchElementException("Delete button not found for user: " + email);
    }

    public void acceptAlert() {
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {}
    }

    public boolean isUserListed(String email) {
        return driver.getPageSource().contains(email);
    }

    public void executeJsPostRequest(String url) {
        String jsPost = "fetch('" + url + "', { method: 'POST' })" +
                ".then(() => { window.location.href = 'http://127.0.0.1:5000/admin/users'; });";
        ((JavascriptExecutor) driver).executeScript(jsPost);
    }

    public String getPageSource() {
        return driver.getPageSource();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }
}
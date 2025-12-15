package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class HeaderPage extends BasePage {

    private final By dropdown = By.cssSelector("a.dropdown-toggle");
    private final By logoutBtn = By.xpath("//a[contains(text(),'Logout')]");

    public HeaderPage(WebDriver driver) {
        super(driver);
    }

    public void logout() {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(dropdown));
        pause();

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(logoutBtn));
        pause();
    }

    public boolean isUserLoggedIn() {
        return driver.findElements(dropdown).size() > 0;
    }
}

package pages;

import base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CinemaPage extends BasePage {

    private final By starsCinemaBtn = By.xpath("//h5[contains(text(),'Stars Cinema (Nasr City)')]/ancestor::button");

    public CinemaPage(WebDriver driver) {
        super(driver);
    }

    public void goToSelectCinema() {
        driver.get("http://127.0.0.1:5000/select-cinema");
    }

    public void selectStarsCinema() {
        WebElement cinema = wait.until(ExpectedConditions.elementToBeClickable(starsCinemaBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cinema);
    }

    //  ContextTests

    public void selectCinema(String cinemaName) {
        By cinemaButton = By.xpath("//button[.//h5[contains(text(),'" + cinemaName + "')]]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(cinemaButton));
        WebElement el = driver.findElement(cinemaButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        wait.until(ExpectedConditions.urlToBe("http://127.0.0.1:5000/"));
    }

    public void clearCinemaContext() {
        driver.get("http://127.0.0.1:5000/clear-cinema");
        wait.until(ExpectedConditions.urlContains("/select-cinema"));
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
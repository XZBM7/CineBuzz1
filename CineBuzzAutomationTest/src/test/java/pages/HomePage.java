package pages;

import base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class HomePage extends BasePage {

    private final By comingSoonTab = By.id("showComingSoon");
    private final By comingSoonContainer = By.xpath("(//div[@id='comingSoonContainer']//div[contains(@class,'movie-card')])[1]");
    private final By comingSoonBtn = By.xpath(".//button[contains(text(),'Coming Soon')]");

    private final By searchInput = By.id("movieSearch");
    private final By searchButton = By.id("searchButton");
    private final By resetButton = By.id("resetSearch");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void goToHome() {
        driver.get("http://127.0.0.1:5000/");
    }

    public void clickComingSoonTab() {
        wait.until(ExpectedConditions.elementToBeClickable(comingSoonTab)).click();
    }

    public WebElement getFirstComingSoonCard() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(comingSoonContainer));
    }

    public WebElement getComingSoonButton(WebElement card) {
        return card.findElement(comingSoonBtn);
    }

    // ContextTests

    public void searchMovie(String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        WebElement search = driver.findElement(searchInput);
        search.clear();
        search.sendKeys(text);
        driver.findElement(searchButton).click();
    }

    public void resetSearch() {
        driver.findElement(resetButton).click();
    }

    public boolean isVisibleMovie(String title) {
        try {
            WebElement card = driver.findElement(
                    By.xpath("//h5[contains(text(),'" + title + "')]/ancestor::div[contains(@class,'movie-item')]")
            );
            return card.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getPageSource() {
        return driver.getPageSource();
    }

    public void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }
}
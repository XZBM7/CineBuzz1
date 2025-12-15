package pages;

import base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public class MovieDetailsPage extends BasePage {

    private final By screeningsTable = By.id("screeningsTable");
    private final By dateFilter = By.id("dateFilter");

    public MovieDetailsPage(WebDriver driver) {
        super(driver);
    }

    public void goToMovie(String movieId) {
        driver.get("http://127.0.0.1:5000/movie/" + movieId);
    }

    public String getScreeningsTableText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(screeningsTable)).getText();
    }

    public String getPageSource() {
        return driver.getPageSource();
    }

    public void filterBySecondOption() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(dateFilter));
        WebElement filter = driver.findElement(dateFilter);
        Select select = new Select(filter);
        String value = select.getOptions().get(1).getAttribute("value");
        select.selectByValue(value);
    }

    public int getFilteredRowCount() {
        WebElement filter = driver.findElement(dateFilter);
        Select select = new Select(filter);
        String value = select.getOptions().get(1).getAttribute("value");
        By filteredRows = By.cssSelector("tr.screening-row[data-date='" + value + "']");
        wait.until(ExpectedConditions.visibilityOfElementLocated(filteredRows));
        return driver.findElements(filteredRows).size();
    }
}
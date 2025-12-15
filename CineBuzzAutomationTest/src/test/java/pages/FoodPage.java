package pages;

import base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class FoodPage extends BasePage {

    private final By checkoutBtn = By.id("checkoutBtn");
    private final By searchBar = By.id("foodSearch");
    private final By noResultsMessage = By.id("noResultsMessage");
    private final By foodOrderForm = By.id("foodOrderForm");

    private final By themeToggleBtn = By.id("themeToggle");

    public FoodPage(WebDriver driver) {
        super(driver);
    }

    public void goToFoodMenu() {
        driver.get("http://127.0.0.1:5000/food");
        wait.until(ExpectedConditions.urlContains("/food"));
    }

    public void goToFood() {
        goToFoodMenu();
    }

    public void setFoodItemQuantity(String dataName, int count) {
        By itemContainerLocator = By.xpath("//div[@data-name='" + dataName + "']");
        WebElement itemContainer = wait.until(ExpectedConditions.presenceOfElementLocated(itemContainerLocator));

        WebElement input = itemContainer.findElement(By.cssSelector(".quantity-input"));

        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        input.sendKeys("0");

        WebElement button = itemContainer.findElement(By.cssSelector(".increment"));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        for (int i = 0; i < Math.abs(count); i++) {
            js.executeScript("arguments[0].click();", button);
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        }

        wait.until(ExpectedConditions.attributeToBe(input, "value", String.valueOf(count)));
    }

    public void clickCheckout() {
        driver.findElement(checkoutBtn).click();
    }

    public void submitOrderForm() {
        driver.findElement(foodOrderForm).submit();
    }

    public boolean isCheckoutEnabled() {
        String disabledAttr = driver.findElement(checkoutBtn).getAttribute("disabled");
        return disabledAttr == null || !disabledAttr.contains("true");
    }

    public void searchFood(String text) {
        WebElement search = driver.findElement(searchBar);
        search.clear();
        search.sendKeys(text);
        if (!text.isEmpty()) {
            wait.until(ExpectedConditions.attributeToBe(searchBar, "value", text));
        } else {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].dispatchEvent(new Event('input'));", search);
            wait.until(ExpectedConditions.attributeToBe(searchBar, "value", ""));
        }
    }

    public boolean isNoResultsDisplayed() {
        try {
            return driver.findElement(noResultsMessage).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void waitForNoResultsToDisappear() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(noResultsMessage));
    }

    public boolean isFoodItemVisible(String dataName) {
        By locator = By.xpath("//div[@data-name='" + dataName + "']");
        return !driver.findElements(locator).isEmpty();
    }

    public String getPageSource() {
        return driver.getPageSource();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    //   methods for DarkLightModeTest
    public void appendSearchText(String text) {
        WebElement search = wait.until(ExpectedConditions.visibilityOfElementLocated(searchBar));
        search.sendKeys(text);
    }

    public void clickThemeToggle() {
        WebElement toggle = wait.until(ExpectedConditions.elementToBeClickable(themeToggleBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", toggle);
    }

    public void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }
}
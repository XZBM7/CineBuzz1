package pages;

import base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CheckoutPage extends BasePage {

    // =============================================================
    //                     FOOD CHECKOUT LOCATORS
    // =============================================================
    private final By cardNameInput = By.id("cardName");
    private final By cardNumberInput = By.id("cardNumber");
    private final By expiryDateInput = By.id("expiryDate");
    private final By cvvInput = By.id("cvv");
    private final By payButton = By.cssSelector(".btn-primary.btn-lg");
    private final By totalPriceHeader = By.xpath("//tfoot//th[last()]");

    // =============================================================
    //                   BOOKING (CINEMA) CHECKOUT LOCATORS
    // =============================================================
    private final By bookingCardNameInput = By.id("card-name");
    private final By bookingCardNumberInput = By.id("card-number");
    private final By bookingCardExpiryInput = By.id("card-expiry");
    private final By bookingCardCvvInput = By.id("card-cvv");
    private final By bookingConfirmBtn = By.id("confirm-btn");
    private final By bookingTotalPrice = By.id("total-price");

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    // =============================================================
    //                     FOOD CHECKOUT METHODS
    // =============================================================
    public void waitForCheckoutPage() {
        wait.until(ExpectedConditions.urlContains("/checkout/food"));
    }

    public void fillPaymentDetails(String name, String number, String expiry, String cvv) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(cardNameInput)).sendKeys(name);
        driver.findElement(cardNumberInput).sendKeys(number);
        driver.findElement(expiryDateInput).sendKeys(expiry);
        driver.findElement(cvvInput).sendKeys(cvv);
    }

    public void clearAndFillField(By locator, String value) {
        WebElement el = driver.findElement(locator);
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        if (value != null && !value.isEmpty()) {
            el.sendKeys(value);
        }
    }

    public void clickPay() {
        driver.findElement(payButton).click();
    }

    public String getTotalPrice() {
        return driver.findElement(totalPriceHeader).getText();
    }

    public String getValidationMessage(By fieldLocator) {
        WebElement button = driver.findElement(payButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].reportValidity();", button);

        WebElement field = driver.findElement(fieldLocator);
        return (String) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].validationMessage;", field);
    }

    // Getters for Food Locators
    public By getCardNameLocator() { return cardNameInput; }
    public By getCardNumberLocator() { return cardNumberInput; }
    public By getExpiryDateLocator() { return expiryDateInput; }
    public By getCvvLocator() { return cvvInput; }

    // =============================================================
    //                   BOOKING (CINEMA) CHECKOUT METHODS
    // =============================================================

    public void fillBookingPayment(String name, String number, String expiry, String cvv) {
        if(name != null) {
            WebElement el = driver.findElement(bookingCardNameInput);
            el.clear();
            el.sendKeys(name);
        }
        if(number != null) {
            WebElement el = driver.findElement(bookingCardNumberInput);
            el.clear();
            el.sendKeys(number);
        }
        if(expiry != null) {
            WebElement el = driver.findElement(bookingCardExpiryInput);
            el.clear();
            el.sendKeys(expiry);
        }
        if(cvv != null) {
            WebElement el = driver.findElement(bookingCardCvvInput);
            el.clear();
            el.sendKeys(cvv);
        }
    }

    public void clickConfirmBooking() {
        WebElement btn = driver.findElement(bookingConfirmBtn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public boolean isConfirmBookingEnabled() {
        return driver.findElement(bookingConfirmBtn).isEnabled();
    }

    public int getBookingUiTotal() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(bookingTotalPrice));
        return Integer.parseInt(driver.findElement(bookingTotalPrice).getText().trim());
    }

    // =============================================================
    //                     SHARED / COMMON METHODS
    // =============================================================

    public String getPageSource() {
        return driver.getPageSource();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
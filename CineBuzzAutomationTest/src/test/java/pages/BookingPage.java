package pages;

import base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class BookingPage extends BasePage {

    private final By bookBtnPulse = By.cssSelector("a.btn.btn-sm.btn-primary.pulse");
    private final By bookBtnNormal = By.cssSelector("a.btn.btn-sm.btn-primary");
    private final By seatMap = By.className("seat-map");

    private final By cardNameInput = By.id("card-name");
    private final By cardNumberInput = By.id("card-number");
    private final By cardExpiryInput = By.id("card-expiry");
    private final By cardCvvInput = By.id("card-cvv");
    private final By confirmBtn = By.id("confirm-btn");
    private final By totalPrice = By.id("total-price");

    public BookingPage(WebDriver driver) {
        super(driver);
    }

    public void goToMovie(String movieId) {
        driver.get("http://127.0.0.1:5000/movie/" + movieId);
    }

    public void clickBookTicket() {
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(bookBtnPulse));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        } catch (TimeoutException e) {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(bookBtnNormal));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public void waitForSeatMap() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(seatMap));
    }

    public void selectSeat(String seatValue) {
        WebElement seat = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[input[@value='" + seatValue + "']]//span")
        ));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", seat);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", seat);
    }

    public void scrollToSeatMap() {
        WebElement map = driver.findElement(seatMap);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", map);
    }

    public void forceSelectOccupiedSeat(String seatValue) {
        try {
            WebElement seat = driver.findElement(By.xpath("//label[input[@value='" + seatValue + "']]//span"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", seat);
        } catch (Exception ignored) {
            ((JavascriptExecutor) driver).executeScript(
                    "let input=document.createElement('input');" +
                            "input.type='hidden'; input.name='seats'; input.value='" + seatValue + "';" +
                            "document.getElementById('booking-form').appendChild(input);"
            );
        }
    }

    public void fillPayment(String name, String number, String expiry, String cvv) {
        if(name != null) {
            driver.findElement(cardNameInput).clear();
            driver.findElement(cardNameInput).sendKeys(name);
        }
        if(number != null) {
            driver.findElement(cardNumberInput).clear();
            driver.findElement(cardNumberInput).sendKeys(number);
        }
        if(expiry != null) {
            driver.findElement(cardExpiryInput).clear();
            driver.findElement(cardExpiryInput).sendKeys(expiry);
        }
        if(cvv != null) {
            driver.findElement(cardCvvInput).clear();
            driver.findElement(cardCvvInput).sendKeys(cvv);
        }
    }

    public void clickConfirm() {
        WebElement btn = driver.findElement(confirmBtn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public boolean isConfirmEnabled() {
        return driver.findElement(confirmBtn).isEnabled();
    }

    public boolean promoDetected() {
        String body = driver.findElement(By.tagName("body")).getText();
        return body.contains("Promotion") || body.contains("Promo") ||
                body.contains("خصم") || body.contains("discount") ||
                body.contains("FREE") || body.contains("offer") || body.contains("عرض");
    }

    public boolean isStillOnBookingPage() {
        return driver.getPageSource().contains("Booking Summary")
                || driver.getPageSource().contains("Book Seats")
                || driver.getCurrentUrl().contains("/book/");
    }

    public boolean isSeatMapPresent() {
        return driver.findElements(seatMap).size() > 0;
    }

    public int getUiTotal() {
        return Integer.parseInt(driver.findElement(totalPrice).getText().trim());
    }

    public void expireTimer() {
        ((JavascriptExecutor) driver).executeScript("window.remainingSeconds = 0;");
        ((JavascriptExecutor) driver).executeScript("window.location.href = '/select-cinema';");
    }

    public String getCurrentUrl() { return driver.getCurrentUrl(); }
    public String getPageSource() { return driver.getPageSource(); }
    public void sleep(long millis) { try { Thread.sleep(millis); } catch (Exception ignored) {} }
}
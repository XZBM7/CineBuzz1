package pages;

import base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import java.util.List;

public class AdminDashboardPage extends BasePage {


    private final By submitBtn = By.cssSelector("button[type='submit']");
    private final By deleteBtn = By.cssSelector("button[type='submit'], .btn-danger");

    private final By movieIdSelect = By.id("movie_id");
    private final By cinemaIdSelect = By.id("cinema_id");
    private final By dateInput = By.id("date");
    private final By timeInput = By.id("time");
    private final By hallSelect = By.id("hall");
    private final By screeningEditLinks = By.cssSelector("a[href*='/admin/screenings/edit/']");

    private final By cinemaNameInput = By.id("name");
    private final By cityInput = By.id("city");
    private final By countryInput = By.id("country");
    private final By addressInput = By.id("address");
    private final By cinemaEditLinks = By.xpath("(//a[contains(@href,'admin/cinemas/edit')])[last()]");

    private final By promoName = By.id("name");
    private final By promoDesc = By.id("description");
    private final By promoType = By.id("type");
    private final By promoMovie = By.id("movie_id");
    private final By promoActive = By.id("is_active");
    private final By promoEditLinks = By.cssSelector("a[href*='/admin/promotions/edit/']");

    private final By foodName = By.id("name");
    private final By foodDesc = By.id("description");
    private final By foodPrice = By.id("price");
    private final By foodCategory = By.id("category");
    private final By foodImage = By.id("image_url");
    private final By foodAvailable = By.id("is_available");
    private final By foodEditLinks = By.cssSelector("a[href*='admin/food/edit']");
    private final By foodDeleteForms = By.cssSelector("form[action*='admin/food/delete']");

    private final By bookingTable = By.cssSelector("table");
    private final By bookingRows = By.cssSelector("tbody tr");

    public AdminDashboardPage(WebDriver driver) {
        super(driver);
    }

    //                     NAVIGATION & UTILS

    public void goToAdminDashboard() {
        driver.get("http://127.0.0.1:5000/admin");
    }

    public void goToUrl(String url) {
        driver.get(url);
    }

    public void waitForUrlContains(String text) {
        wait.until(ExpectedConditions.urlContains(text));
    }

    public void waitForPageLoad() {
        wait.until(web -> ((JavascriptExecutor) web).executeScript("return document.readyState").equals("complete"));
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getPageSource() {
        return driver.getPageSource();
    }

    public void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }

    //                     AUTH / PERMISSION CHECKS


    public boolean isMyBookingsVisibleInNavbar() {
        String src = driver.getPageSource();
        return src.contains("href=\"/my_bookings\"") ||
                src.contains("url_for('my_bookings')") ||
                src.contains("My Bookings");
    }

    public boolean isAdminRedirectedToAdminPanel() {
        return driver.getCurrentUrl().contains("/admin");
    }

    public long executeJsPostRequest(String url) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = "var xhr = new XMLHttpRequest();" +
                "xhr.open('POST', '" + url + "', false);" +
                "xhr.send(null);" +
                "return xhr.status;";
        return (Long) js.executeScript(script);
    }

    //                     SCREENINGS METHODS

    public void fillScreeningForm(int movieIdx, int cinemaIdx, String date, String time, String hall) {
        new Select(driver.findElement(movieIdSelect)).selectByIndex(movieIdx);
        new Select(driver.findElement(cinemaIdSelect)).selectByIndex(cinemaIdx);
        type(dateInput, date);
        type(timeInput, time);
        new Select(driver.findElement(hallSelect)).selectByValue(hall);
    }

    public void editScreeningHall(String newHall) {
        WebElement hall = wait.until(ExpectedConditions.visibilityOfElementLocated(hallSelect));
        hall.clear();
        hall.sendKeys(newHall);
    }

    public String getLastScreeningEditUrl() {
        List<WebElement> links = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(screeningEditLinks));
        return links.get(links.size() - 1).getAttribute("href");
    }

    public String getScreeningHallValue() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(hallSelect)).getAttribute("value");
    }


    //                     CINEMAS METHODS

    public void fillCinemaForm(String name, String city, String country, String address) {
        type(cinemaNameInput, name);
        type(cityInput, city);
        type(countryInput, country);
        type(addressInput, address);
    }

    public String getLastCinemaEditUrl() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(cinemaEditLinks)).getAttribute("href");
    }

    public void editCinemaName(String newName) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(cinemaNameInput));
        el.clear();
        el.sendKeys(newName);
    }

    public String getCinemaNameValue() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(cinemaNameInput)).getAttribute("value");
    }

    //                     PROMOTIONS METHODS

    public void fillPromoForm(String name, String desc, String type, boolean active) {
        type(promoName, name);
        type(promoDesc, desc);
        new Select(driver.findElement(promoType)).selectByValue(type);

        Select movieSel = new Select(driver.findElement(promoMovie));
        for (WebElement op : movieSel.getOptions()) {
            if (op.isEnabled() && !op.getAttribute("value").isEmpty()) {
                op.click();
                break;
            }
        }

        WebElement cb = driver.findElement(promoActive);
        if (active && !cb.isSelected()) cb.click();
    }

    public String getLastPromoEditUrl() {
        List<WebElement> edits = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(promoEditLinks));
        return edits.get(edits.size() - 1).getAttribute("href");
    }

    public void togglePromoActive() {
        WebElement cb = wait.until(ExpectedConditions.visibilityOfElementLocated(promoActive));
        if (cb.isSelected()) cb.click();
    }

    public boolean isPromoActive() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(promoActive)).isSelected();
    }

    //                     FOOD METHODS

    public void fillFoodForm(String name, String desc, String price, String category, String img, boolean available) {
        type(foodName, name);
        type(foodDesc, desc);
        type(foodPrice, price);
        new Select(driver.findElement(foodCategory)).selectByVisibleText(category);
        type(foodImage, img);

        WebElement cb = driver.findElement(foodAvailable);
        if (available && !cb.isSelected()) ((JavascriptExecutor)driver).executeScript("arguments[0].click();", cb);
    }

    public String getLastFoodEditUrl() {
        List<WebElement> links = driver.findElements(foodEditLinks);
        return links.get(links.size() - 1).getAttribute("href");
    }

    public void editFoodPriceAndAvailability(String price, boolean toggleAvail) {
        WebElement p = wait.until(ExpectedConditions.visibilityOfElementLocated(foodPrice));
        p.clear();
        p.sendKeys(price);

        if(toggleAvail) {
            WebElement cb = driver.findElement(foodAvailable);
            if (cb.isSelected()) ((JavascriptExecutor)driver).executeScript("arguments[0].click();", cb);
        }
    }

    public void clickLastFoodDelete() {
        List<WebElement> forms = driver.findElements(foodDeleteForms);
        WebElement btn = forms.get(forms.size() - 1).findElement(submitBtn);
        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", btn);
    }

    //                     BOOKINGS METHODS

    public List<WebElement> getBookingRows() {
        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(bookingTable));
        return table.findElements(bookingRows);
    }

    //                     SHARED ACTIONS

    public void clickSubmit() {
        WebElement btn = driver.findElement(submitBtn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public void clickDelete() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(deleteBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public void acceptAlert() {
        try {
            wait.until(ExpectedConditions.alertIsPresent()).accept();
        } catch (Exception ignored) {}
    }
}
package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class MyBookingsPage extends BasePage {

    public MyBookingsPage(WebDriver driver) {
        super(driver);
    }

    public void goToMyBookings() {
        driver.get("http://127.0.0.1:5000/my-bookings");
    }

    public boolean validateMyBookingsContent() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        String src = driver.getPageSource();
        boolean hasBookings = src.contains("View Ticket")
                || src.contains("Seats:")
                || driver.findElements(By.cssSelector(".card")).size() > 0;
        boolean noBookings = src.contains("You don't have any bookings yet.");
        return hasBookings || noBookings;
    }
}
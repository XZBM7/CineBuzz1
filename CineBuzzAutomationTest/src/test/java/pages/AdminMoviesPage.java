
package pages;

import base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class AdminMoviesPage extends BasePage {

    private final By titleInput = By.id("title");
    private final By descInput = By.id("description");
    private final By durationInput = By.id("duration");
    private final By genreInput = By.id("genre");
    private final By statusSelect = By.id("status");
    private final By imageInput = By.id("image_url");
    private final By selectAllBtn = By.id("selectAllBtn");
    private final By cinemaCheckbox = By.cssSelector("input[name='cinema_ids']");
    private final By submitBtn = By.cssSelector("button[type='submit']");
    private final By errorAlert = By.cssSelector(".alert-danger");

    public AdminMoviesPage(WebDriver driver) {
        super(driver);
    }

    public void goToAddMovie() {
        driver.get("http://127.0.0.1:5000/admin/movies/add");
        wait.until(ExpectedConditions.visibilityOfElementLocated(titleInput));
    }

    public void fillMovieForm(String title, String desc, String duration, String genre, String status, String imageUrl) {
        if (title != null) type(titleInput, title);
        if (desc != null) type(descInput, desc);
        if (duration != null) type(durationInput, duration);
        if (genre != null) type(genreInput, genre);

        if (status != null) {
            new Select(driver.findElement(statusSelect)).selectByValue(status);
        }

        if (imageUrl != null) type(imageInput, imageUrl);
    }

    public void selectAllCinemas() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(selectAllBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        sleep(400);
    }

    public void selectFirstCinema() {
        WebElement checkbox = wait.until(ExpectedConditions.presenceOfElementLocated(cinemaCheckbox));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", checkbox);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
    }

    public void clickSubmit() {
        WebElement btn = driver.findElement(submitBtn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public boolean isValidationErrorDisplayed() {
        try {
            WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorAlert));
            return error.getText().toLowerCase().contains("required");
        } catch (Exception e) {
            String pageSource = driver.getPageSource().toLowerCase();
            return pageSource.contains("required") || pageSource.contains("please");
        }
    }

    public void goToEditMovie(String movieId) {
        driver.get("http://127.0.0.1:5000/admin/movies/edit/" + movieId);
        wait.until(ExpectedConditions.visibilityOfElementLocated(titleInput));
    }

    public void goToDeleteMovie(String movieId) {
        driver.get("http://127.0.0.1:5000/admin/movies/delete/" + movieId);
    }

    public void confirmDelete() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'], .btn-danger")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public boolean isMoviePresent(String text) {
        return driver.getPageSource().toLowerCase().contains(text.toLowerCase());
    }

    public void toggleCinemaCheckboxes() {
        List<WebElement> cinemas = driver.findElements(cinemaCheckbox);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (WebElement c : cinemas) {
            if (c.isSelected()) js.executeScript("arguments[0].click();", c);
        }
        if (cinemas.size() > 1) {
            js.executeScript("arguments[0].click();", cinemas.get(0));
            js.executeScript("arguments[0].click();", cinemas.get(1));
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }
}
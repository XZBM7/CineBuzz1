package pages;

import base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public class EditProfilePage extends BasePage {

    private final By nameInput = By.name("name");
    private final By phoneInput = By.name("phone");
    private final By addressInput = By.name("address");
    private final By cityInput = By.name("city");
    private final By countryInput = By.name("country");
    private final By dobInput = By.name("dob");
    private final By emailInput = By.name("email");
    private final By editSubmitBtn = By.cssSelector("#editProfileModal button[type='submit']");
    private final By successHeader = By.tagName("h3");

    public EditProfilePage(WebDriver driver) {
        super(driver);
    }

    public void editProfile(String name, String phone, String address, String city, String country) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
        type(nameInput, name);
        type(phoneInput, phone);
        type(addressInput, address);
        type(cityInput, city);

        WebElement countryEl = driver.findElement(countryInput);
        if (countryEl.getTagName().equalsIgnoreCase("select")) {
            Select select = new Select(countryEl);
            try {
                select.selectByVisibleText(country);
            } catch (NoSuchElementException e) {
                select.selectByValue(country);
            }
        } else {
            type(countryInput, country);
        }

        click(editSubmitBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(successHeader));
    }

    public void editDob(String dob) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(dobInput));
        type(dobInput, dob);
        click(editSubmitBtn);
    }

    public boolean isEmailReadOnly() {
        WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        return email.getAttribute("readonly") != null;
    }

    public void forceEditEmail(String newEmail) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("document.getElementsByName('email')[0].removeAttribute('readonly');");
        type(emailInput, newEmail);
        click(editSubmitBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(successHeader));
    }
}
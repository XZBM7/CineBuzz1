package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.LoginPage;
import pages.FoodPage;

import java.time.Duration;

public class DarkLightModeTest extends BaseTest {

    private LoginPage loginPage;
    private FoodPage foodPage;

    private static final String USER_EMAIL = "xz@a.com";
    private static final String USER_PASS = "X123456x";

    @BeforeClass
    public void classSetup() {
        setUp();
        loginPage = new LoginPage(driver);
        foodPage = new FoodPage(driver);
    }

    @AfterClass
    public void classTeardown() {
        tearDown();
    }

    @DataProvider(name = "searchData")
    public Object[][] getSearchData() {
        return new Object[][] {
                { "abcd", "TC-9.1" },
                { "foshar", "TC-9.2" },
                { "123", "TC-9.3" }
        };
    }

    public void loginAndGoToFood() {
        loginPage.open(baseUrl);
        loginPage.login(USER_EMAIL, USER_PASS);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));

        wait.until(web -> ((JavascriptExecutor) web).executeScript("return document.readyState").equals("complete"));

        if (driver.getCurrentUrl().contains("select-cinema")) {
            try {
                driver.findElement(By.cssSelector("a.btn, .card, a[href*='home']")).click();
                wait.until(ExpectedConditions.urlContains("home"));
            } catch (Exception e) {
                System.out.println("Warning: Auto-select cinema failed.");
            }
        }

        foodPage.goToFood();
    }

    @Test(dataProvider = "searchData")
    public void TC_9_Toggle_Modes_And_Search(String searchText, String testId) {
        try {
            loginAndGoToFood();

            foodPage.appendSearchText(searchText);
            foodPage.sleep(700);

            foodPage.clickThemeToggle();
            foodPage.sleep(700);

            foodPage.searchFood(searchText);
            foodPage.sleep(700);

            Assert.fail("[" + testId + "] FAILED: Forced failure after switching to Dark Mode and searching.");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("[" + testId + "] Unexpected exception: " + e.getMessage());
        }
    }
}
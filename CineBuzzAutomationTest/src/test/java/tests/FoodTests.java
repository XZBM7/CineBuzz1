package tests;

import base.BaseTest;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import pages.LoginPage;
import pages.FoodPage;
import pages.CheckoutPage;
import org.testng.Assert;
import org.testng.annotations.*;

public class FoodTests extends BaseTest {

    LoginPage loginPage;
    FoodPage foodPage;
    CheckoutPage checkoutPage;

    @BeforeClass
    public void classSetup() {
        setUp();
        loginPage = new LoginPage(driver);
        foodPage = new FoodPage(driver);
        checkoutPage = new CheckoutPage(driver);
    }

    @AfterClass
    public void classTeardown() {
        tearDown();
    }

    @DataProvider(name = "menuData")
    public Object[][] menuData() {
        return new Object[][]{
                { "xz@a.com", "X123456x", "foshar", "foshar", "$3.00" }
        };
    }

    @DataProvider(name = "checkoutData")
    public Object[][] checkoutData() {
        return new Object[][]{
                { "xz@a.com", "X123456x", "foshar", 3, "9.00", "foshar" }
        };
    }

    @DataProvider(name = "paymentSuccess")
    public Object[][] paymentSuccess() {
        return new Object[][]{
                { "xz@a.com", "X123456x", "foshar", 1,
                        "Test User", "1111222233334444", "12/26", "321" }
        };
    }

    @DataProvider(name = "invalidCard")
    public Object[][] invalidCard() {
        return new Object[][]{
                {
                        "xz@a.com", "X123456x",
                        "foshar", 3, "9.00",
                        "Test User",
                        "1111222233334444",
                        "",
                        "321",
                        "01/20",
                        "12"
                }
        };
    }

    @DataProvider(name = "searchData")
    public Object[][] searchData() {
        return new Object[][]{
                { "xz@a.com", "X123456x", "foshar" }
        };
    }

    @DataProvider(name = "apiData")
    public Object[][] apiData() {
        return new Object[][]{
                { "xz@a.com", "X123456x" }
        };
    }

    private void loginAndGoToFoodMenu(String email, String password) {
        driver.manage().deleteAllCookies();
        loginPage.open(baseUrl);
        loginPage.login(email, password);

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.or(
                        ExpectedConditions.urlContains("home"),
                        ExpectedConditions.urlContains("select-cinema"),
                        ExpectedConditions.urlContains("/")
                ));

        foodPage.goToFoodMenu();
    }

    @Test(priority = 1, dataProvider = "menuData")
    public void TC_5_1_viewFoodMenu(String email, String pass,
                                    String itemName, String displayName, String expectedPrice) {

        loginAndGoToFoodMenu(email, pass);

        Assert.assertTrue(foodPage.isFoodItemVisible(itemName));
        Assert.assertFalse(foodPage.getPageSource().contains("Old Soda"));
        Assert.assertTrue(foodPage.getPageSource().contains(expectedPrice));
    }

    @Test(priority = 2, dataProvider = "checkoutData")
    public void TC_5_2_submitItemsToCheckout(String email, String pass, String itemName,
                                             int quantity, String expectedTotal, String displayName) {

        loginAndGoToFoodMenu(email, pass);

        foodPage.setFoodItemQuantity(itemName, quantity);
        foodPage.clickCheckout();
        checkoutPage.waitForCheckoutPage();

        Assert.assertTrue(checkoutPage.getPageSource().contains(displayName));
        Assert.assertTrue(checkoutPage.getTotalPrice().contains(expectedTotal));
    }

    @Test(priority = 3, dataProvider = "menuData")
    public void TC_5_3_submitEmptyCart(String email, String pass,
                                       String itemName, String displayName, String expectedPrice) {

        loginAndGoToFoodMenu(email, pass);

        foodPage.setFoodItemQuantity(itemName, 0);

        Assert.assertFalse(foodPage.isCheckoutEnabled());

        foodPage.submitOrderForm();

        Assert.assertTrue(foodPage.getCurrentUrl().contains("/food"));
    }

    @Test(priority = 4, dataProvider = "paymentSuccess")
    public void TC_5_4_processPaymentHappyPath(String email, String pass, String itemName,
                                               int qty, String cardName, String cardNum,
                                               String exp, String cvv) {

        loginAndGoToFoodMenu(email, pass);

        foodPage.setFoodItemQuantity(itemName, qty);
        foodPage.clickCheckout();
        checkoutPage.waitForCheckoutPage();

        checkoutPage.fillPaymentDetails(cardName, cardNum, exp, cvv);
        checkoutPage.clickPay();

        String page = driver.getPageSource().toLowerCase();

        Assert.assertTrue(
                page.contains("order confirmed") ||
                        page.contains("thank you") ||
                        page.contains("my food orders")
        );
    }

    @Test(priority = 5, dataProvider = "invalidCard")
    public void TC_5_5_paymentFailsInvalidCardNumber(
            String email, String pass, String item, int qty, String expectedTotal,
            String cardName, String cardNum, String emptyExpiry, String cvv,
            String expiredDate, String invalidCvv) {

        TC_5_2_submitItemsToCheckout(email, pass, item, qty, expectedTotal, item);

        checkoutPage.fillPaymentDetails(cardName, "", emptyExpiry, cvv);
        checkoutPage.clearAndFillField(checkoutPage.getCardNumberLocator(), "1234");

        Assert.assertFalse(checkoutPage.getValidationMessage(checkoutPage.getCardNumberLocator()).isEmpty());
        Assert.assertTrue(checkoutPage.getCurrentUrl().contains("/checkout/food"));
    }

    @Test(priority = 6, dataProvider = "invalidCard")
    public void TC_5_6_paymentFailsExpiredCard(
            String email, String pass, String item, int qty, String expectedTotal,
            String cardName, String cardNum, String emptyExpiry, String cvv,
            String expiredDate, String invalidCvv) {

        TC_5_2_submitItemsToCheckout(email, pass, item, qty, expectedTotal, item);

        checkoutPage.fillPaymentDetails(cardName, cardNum, emptyExpiry, cvv);
        checkoutPage.clearAndFillField(checkoutPage.getExpiryDateLocator(), expiredDate);

        Assert.assertFalse(checkoutPage.getValidationMessage(checkoutPage.getExpiryDateLocator()).isEmpty());
        Assert.assertTrue(checkoutPage.getCurrentUrl().contains("/checkout/food"));
    }

    @Test(priority = 7, dataProvider = "invalidCard")
    public void TC_5_7_paymentFailsInvalidCvv(
            String email, String pass, String item, int qty, String expectedTotal,
            String cardName, String cardNum, String emptyExpiry, String cvv,
            String expiredDate, String invalidCvv) {

        TC_5_2_submitItemsToCheckout(email, pass, item, qty, expectedTotal, item);

        checkoutPage.fillPaymentDetails(cardName, cardNum, emptyExpiry, "");
        checkoutPage.clearAndFillField(checkoutPage.getCvvLocator(), invalidCvv);

        Assert.assertFalse(checkoutPage.getValidationMessage(checkoutPage.getCvvLocator()).isEmpty());
        Assert.assertTrue(checkoutPage.getCurrentUrl().contains("/checkout/food"));
    }

    @Test(priority = 8, dataProvider = "invalidCard")
    public void TC_5_8_paymentFailsEmptyFields(
            String email, String pass, String item, int qty, String expectedTotal,
            String cardName, String cardNum, String emptyExpiry, String cvv,
            String expiredDate, String invalidCvv) {

        TC_5_2_submitItemsToCheckout(email, pass, item, qty, expectedTotal, item);

        checkoutPage.clearAndFillField(checkoutPage.getCardNameLocator(), "");
        checkoutPage.clearAndFillField(checkoutPage.getCardNumberLocator(), "");
        checkoutPage.clearAndFillField(checkoutPage.getExpiryDateLocator(), "");
        checkoutPage.clearAndFillField(checkoutPage.getCvvLocator(), "");

        Assert.assertFalse(checkoutPage.getValidationMessage(checkoutPage.getCardNameLocator()).isEmpty());
        Assert.assertTrue(checkoutPage.getCurrentUrl().contains("/checkout/food"));
    }

    @Test(priority = 9, dataProvider = "searchData")
    public void TC_5_9_testFoodSearch(String email, String pass, String item) {

        loginAndGoToFoodMenu(email, pass);

        foodPage.searchFood(item);
        Assert.assertTrue(foodPage.getPageSource().toLowerCase().contains(item));

        foodPage.searchFood("burger");
        Assert.assertTrue(foodPage.isNoResultsDisplayed());

        foodPage.searchFood("");
        foodPage.waitForNoResultsToDisappear();

        Assert.assertFalse(foodPage.isNoResultsDisplayed());
        Assert.assertTrue(foodPage.getPageSource().toLowerCase().contains(item));
    }

    @Test(priority = 10, dataProvider = "apiData")
    public void TC_5_10_testFoodOrderApi(String email, String pass) {

        loginAndGoToFoodMenu(email, pass);

        driver.get(baseUrl + "/food/order");

        Assert.assertFalse(driver.getPageSource().toLowerCase().contains("not found"));
    }
}

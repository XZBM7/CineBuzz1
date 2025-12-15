package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.LoginPage;
import pages.AuthPage;

public class SecurityTests extends BaseTest {

    LoginPage loginPage;
    AuthPage auth;

    @BeforeMethod
    public void setupTest() {
        setUp();
        loginPage = new LoginPage(driver);
        auth = new AuthPage(driver);
    }

    @AfterMethod
    public void endTest() {
        tearDown();
    }

    @DataProvider(name = "protectedRoutes")
    public Object[][] getProtectedRoutes() {
        return new Object[][] {
                { "/profile", "TC-1.16" },
                { "/book/12345", "TC-1.17" },
                { "/admin", "TC-1.18" },
                { "/book/some_screening_id", "TC-1.20A" },
                { "/food", "TC-1.20B" },
                { "/my-food-orders", "TC-1.20C" },
                { "/my-bookings", "TC-1.20D" },
                { "/profile", "TC-1.20E" }
        };
    }

    @DataProvider(name = "validUserData")
    public Object[][] getValidUserData() {
        return new Object[][]{
                { "ibrahim@gmail.com", "X123456x" }
        };
    }

    @Test(dataProvider = "protectedRoutes")
    public void TC_Access_Protected_Routes_Logged_Out(String route, String testId) {
        auth.open(baseUrl, "/logout");
        auth.open(baseUrl, route);

        Assert.assertTrue(auth.redirectedToLogin(),
                "[" + testId + " FAIL] " + route + " accessible without login!");
    }

    @Test(dataProvider = "validUserData")
    public void TC_1_19_nonAdminAccessAdmin(String email, String password) {
        loginPage.open(baseUrl);
        loginPage.login(email, password);

        auth.open(baseUrl, "/admin");

        Assert.assertTrue(auth.redirectedToHome(baseUrl),
                "[TC-1.19 FAIL] Non-admin accessed /admin!");
    }
}

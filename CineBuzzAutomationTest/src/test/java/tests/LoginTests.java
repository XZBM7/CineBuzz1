package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.LoginPage;
import pages.HeaderPage;

public class LoginTests extends BaseTest {

    LoginPage loginPage;
    HeaderPage header;

    @BeforeMethod
    public void setupTest() {
        setUp();
        loginPage = new LoginPage(driver);
        header = new HeaderPage(driver);
    }

    @AfterMethod
    public void endTest() {
        tearDown();
    }

    @DataProvider(name = "negativeLoginData")
    public Object[][] getNegativeLoginData() {
        return new Object[][] {
                { "ibrahim@gmail.com", "WrongPass123", "invalid", "TC-1.12" },
                { "", "", "", "TC-1.13A" },
                { "ibrahim@gmail.com", "", "", "TC-1.13B" },
                { "invalid@wrong", "X123456x", "invalid", "TC-1.14" }
        };
    }

    @DataProvider(name = "validUserLogin")
    public Object[][] getValidUserLogin() {
        return new Object[][] {
                { "ibrahim@gmail.com", "X123456x" }
        };
    }

    @DataProvider(name = "validAdminLogin")
    public Object[][] getValidAdminLogin() {
        return new Object[][] {
                { "admin@cinema.com", "admin123" }
        };
    }

    @Test(dataProvider = "validUserLogin")
    public void TC_1_10_loginUser(String email, String password) {
        loginPage.open(baseUrl);
        loginPage.login(email, password);

        String url = driver.getCurrentUrl();
        boolean ok = url.equals(baseUrl + "/") || url.contains("select-cinema");

        Assert.assertTrue(ok,
                "[TC-1.10 FAIL] Expected redirect after user login. Got: " + url);
    }

    @Test(dataProvider = "validAdminLogin")
    public void TC_1_11_loginAdmin(String email, String password) {
        loginPage.open(baseUrl);
        loginPage.login(email, password);

        var cookie = driver.manage().getCookieNamed("session");
        Assert.assertNotNull(cookie,
                "[TC-1.11 FAIL] Session cookie missing!");

        String url = driver.getCurrentUrl();
        boolean redirected = url.contains("admin") || url.contains("dashboard");

        Assert.assertTrue(redirected,
                "[TC-1.11 FAIL] Admin login did not redirect! Got: " + url);
    }

    @Test(dataProvider = "negativeLoginData")
    public void TC_Negative_Login_Scenarios(String email, String password, String expectedSubstring, String testId) {
        loginPage.open(baseUrl);
        loginPage.login(email, password);

        boolean stayed = driver.getCurrentUrl().contains("login");
        String msg = loginPage.getFlash();

        Assert.assertTrue(stayed, "[" + testId + " FAIL] User was redirected unexpectedly.");

        if (expectedSubstring.isEmpty()) {
            Assert.assertTrue(msg.length() > 0,
                    "[" + testId + " FAIL] No validation message shown.");
        } else {
            Assert.assertTrue(msg.contains(expectedSubstring),
                    "[" + testId + " FAIL] Message expected to contain '" + expectedSubstring + "' but got: " + msg);
        }
    }

    @Test(dataProvider = "validUserLogin")
    public void TC_1_15_logout(String email, String password) {
        loginPage.open(baseUrl);
        loginPage.login(email, password);

        header.logout();

        String url = driver.getCurrentUrl();
        boolean redirected = url.contains("/login") || url.contains("/");

        Assert.assertTrue(redirected && !header.isUserLoggedIn(),
                "[TC-1.15 FAIL] Logout did not work!");
    }
}

package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.RegisterPage;

public class RegisterTests extends BaseTest {

    RegisterPage register;

    @BeforeMethod
    public void setupTest() {
        setUp();
        register = new RegisterPage(driver);
    }

    @AfterMethod
    public void endTest() {
        tearDown();
    }

    @DataProvider(name = "positiveRegisterData")
    public Object[][] getPositiveRegisterData() {
        String t = String.valueOf(System.currentTimeMillis());
        return new Object[][] {
                { "User " + t, "user" + t + "@test.com", "X123456x", "X123456x" }
        };
    }

    @DataProvider(name = "negativeRegisterData")
    public Object[][] getNegativeRegisterData() {
        String t = String.valueOf(System.currentTimeMillis());
        return new Object[][] {
                { "Test User 3", "user3@test.com", "Password123", "Password456", "", "TC-1.2" },
                { "User", "upper" + t + "@test.com", "password123", "password123", "", "TC-1.3" },
                { "User", "lower" + t + "@test.com", "PASSWORD123", "PASSWORD123", "", "TC-1.4" },
                { "User", "num" + t + "@test.com", "PasswordAbc", "PasswordAbc", "", "TC-1.5" },
                { "User", "short" + t + "@test.com", "Pass12", "Pass12", "", "TC-1.6" },

                { "User", "xz@a.com", "X123456x", "X123456x", "exist", "TC-1.7" },

                { "", "", "", "", "", "TC-1.8" }
        };
    }

    @DataProvider(name = "authRegisterData")
    public Object[][] getAuthRegisterData() {
        String t = String.valueOf(System.currentTimeMillis());
        return new Object[][] {
                { "User " + t, "auth" + t + "@test.com", "X123456x", "X123456x" }
        };
    }

    @Test(dataProvider = "positiveRegisterData")
    public void TC_1_1_successfulRegistration(String name, String email, String pass, String confirm) {
        register.open(baseUrl);
        register.fill(name, email, pass, confirm);
        register.submit();
        Assert.assertTrue(driver.getCurrentUrl().contains("select-cinema"));
    }

    @Test(dataProvider = "negativeRegisterData")
    public void TC_Negative_Register_Scenarios(String name, String email, String pass, String confirm, String expected, String testId) {
        register.open(baseUrl);
        register.fill(name, email, pass, confirm);
        register.submit();
        Assert.assertTrue(driver.getCurrentUrl().contains("register"));
        if (!expected.isEmpty()) {
            Assert.assertTrue(driver.getPageSource().toLowerCase().contains(expected));
        }
    }

    @Test(dataProvider = "authRegisterData")
    public void TC_1_9_authenticatedAccess(String name, String email, String pass, String confirm) {
        register.open(baseUrl);
        register.fill(name, email, pass, confirm);
        register.submit();

        Assert.assertTrue(driver.getCurrentUrl().contains("select-cinema"));

        driver.get(baseUrl + "/profile");
        Assert.assertFalse(driver.getCurrentUrl().contains("login"));

        driver.get(baseUrl + "/food");
        Assert.assertFalse(driver.getCurrentUrl().contains("login"));
    }
}

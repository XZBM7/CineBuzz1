package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.*;

public class AdminAuthorizationTest extends BaseTest {

    private LoginPage loginPage;
    private AdminDashboardPage adminDashboardPage;
    private ProfilePage profilePage;
    private DeleteAccountPage deleteAccountPage;

    @BeforeClass
    public void classSetup() {
        setUp();
        loginPage = new LoginPage(driver);
        adminDashboardPage = new AdminDashboardPage(driver);
        profilePage = new ProfilePage(driver);
        deleteAccountPage = new DeleteAccountPage(driver);
    }

    @AfterClass
    public void classTeardown() {
        tearDown();
    }

    private void performLogin(String email, String password) {
        loginPage.open(baseUrl);
        loginPage.login(email, password);
        adminDashboardPage.sleep(1500);
    }

    @DataProvider(name = "adminCredentials")
    public Object[][] adminCredentials() {
        return new Object[][] {
                {"admin@cinema.com", "admin123"}
        };
    }

    @DataProvider(name = "protectedUserRoutes")
    public Object[][] protectedUserRoutes() {
        return new Object[][] {
                {"admin@cinema.com", "admin123", "/"},
                {"admin@cinema.com", "admin123", "/movie/692e3fc8e3e33fcb79e66abb"},
                {"admin@cinema.com", "admin123", "/book/692e3f3fe3e33fcb79e66ab9"}
        };
    }

    @DataProvider(name = "basicUserCredentials")
    public Object[][] basicUserCredentials() {
        return new Object[][] {
                {"xz@a.com", "X123456x", "692e3fc8e3e33fcb79e66abb", "/admin", "/admin/movies/add", "/admin/movies/delete/"}
        };
    }

    @DataProvider(name = "adminLeakageData")
    public Object[][] adminLeakageData() {
        return new Object[][] {
                {"admin@cinema.com", "admin123", "xz@a.com", "xz", "gmail.com"}
        };
    }

    @Test(dataProvider = "adminCredentials")
    public void TC_8_1_adminNavbarDoesNotShowMyBookings(String adminEmail, String adminPass) {
        performLogin(adminEmail, adminPass);

        Assert.assertFalse(adminDashboardPage.isMyBookingsVisibleInNavbar());
    }

    @Test(dataProvider = "adminCredentials")
    public void TC_8_2_adminProfileShouldHideUserSections(String adminEmail, String adminPass) {
        performLogin(adminEmail, adminPass);

        profilePage.goToProfile();
        adminDashboardPage.sleep(1000);

        boolean recentBookingsVisible = profilePage.isRecentBookingsSectionVisible();
        boolean deleteSectionVisible = profilePage.isDeleteAccountSectionVisible();

        Assert.assertFalse(recentBookingsVisible);
        Assert.assertFalse(deleteSectionVisible);
    }

    @Test(priority = 999, dataProvider = "adminCredentials")
    public void TC_8_3_adminCannotDeleteOwnAccount(String adminEmail, String adminPass) {
        performLogin(adminEmail, adminPass);

        profilePage.goToProfile();
        adminDashboardPage.sleep(1000);

        deleteAccountPage.deleteAccountAdminFlow(adminPass);
        adminDashboardPage.sleep(2000);

        driver.get(baseUrl + "/logout");
        adminDashboardPage.sleep(1000);

        performLogin(adminEmail, adminPass);
        adminDashboardPage.sleep(1000);

        Assert.assertTrue(driver.getCurrentUrl().contains("/admin"));
    }

    @Test(dataProvider = "protectedUserRoutes")
    public void TC_8_4_adminCannotAccessBookingFlow(String adminEmail, String adminPass, String route) {
        performLogin(adminEmail, adminPass);

        adminDashboardPage.goToUrl(baseUrl + route);
        adminDashboardPage.sleep(1500);

        Assert.assertTrue(adminDashboardPage.isAdminRedirectedToAdminPanel());
    }

    @Test(dataProvider = "basicUserCredentials")
    public void TC_8_5_authZ_NormalUserCannotAccessAdmin(
            String userEmail,
            String userPass,
            String movieId,
            String adminPage,
            String adminAddMoviePage,
            String deleteBaseUrl
    ) {
        performLogin(userEmail, userPass);

        adminDashboardPage.goToUrl(baseUrl + adminPage);
        adminDashboardPage.sleep(1000);
        Assert.assertFalse(adminDashboardPage.isAdminRedirectedToAdminPanel());

        adminDashboardPage.goToUrl(baseUrl + adminAddMoviePage);
        adminDashboardPage.sleep(1000);
        Assert.assertFalse(adminDashboardPage.isAdminRedirectedToAdminPanel());

        String deleteUrl = deleteBaseUrl + movieId;
        long status = adminDashboardPage.executeJsPostRequest(deleteUrl);
        adminDashboardPage.sleep(500);

        Assert.assertNotEquals(status, 200L);
    }

    @Test(dataProvider = "adminLeakageData")
    public void TC_8_6_adminCannotAccessUserAccountPages(
            String adminEmail,
            String adminPass,
            String userEmail,
            String emailSubstring,
            String emailDomain
    ) {
        performLogin(adminEmail, adminPass);

        profilePage.goToProfile();
        adminDashboardPage.sleep(1500);

        String source = profilePage.getPageSource().toLowerCase();

        boolean adminProfileVisible = source.contains(adminEmail.toLowerCase());
        boolean userDataVisible =
                source.contains(userEmail.toLowerCase()) ||
                        source.contains(emailSubstring.toLowerCase());

        Assert.assertTrue(adminProfileVisible);
        Assert.assertFalse(userDataVisible);
    }
}
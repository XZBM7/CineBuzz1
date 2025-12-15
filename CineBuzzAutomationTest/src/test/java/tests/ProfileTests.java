package tests;

import base.BaseTest;
import pages.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import java.io.File;
import java.time.Duration;

public class ProfileTests extends BaseTest {

    LoginPage loginPage;
    ProfilePage profilePage;
    EditProfilePage editProfilePage;
    ChangePasswordPage changePasswordPage;
    DeleteAccountPage deleteAccountPage;

    private String currentPassword = "X123456x";

    @BeforeTest
    public void setup() {
        setUp();
        loginPage = new LoginPage(driver);
        profilePage = new ProfilePage(driver);
        editProfilePage = new EditProfilePage(driver);
        changePasswordPage = new ChangePasswordPage(driver);
        deleteAccountPage = new DeleteAccountPage(driver);
    }

    @AfterTest
    public void teardown() {
        tearDown();
    }


    @DataProvider(name = "editProfileData")
    public Object[][] editProfileData() {
        return new Object[][]{
                {"ibrahim@gmail.com", "Test User Edited", "01125700310", "Test Address 123", "Giza", "UK"}
        };
    }

    @DataProvider(name = "emailBackendData")
    public Object[][] emailBackendData() {
        return new Object[][]{
                {"ibrahim@gmail.com", "new_email_attempt@test.com"}
        };
    }

    @DataProvider(name = "validPictureData")
    public Object[][] validPictureData() {
        return new Object[][]{
                {"ibrahim@gmail.com", "C:\\Users\\HUAWEI\\Pictures\\watch-dogs-2-4k-sesvahbh6s63fbhv.jpg"}
        };
    }

    @DataProvider(name = "invalidPictureData")
    public Object[][] invalidPictureData() {
        return new Object[][]{
                {"ibrahim@gmail.com", "C:\\Users\\HUAWEI\\Pictures\\test.txt"}
        };
    }

    @DataProvider(name = "dobData")
    public Object[][] dobData() {
        return new Object[][]{
                {"ibrahim@gmail.com", "2015-01-01"}
        };
    }

    @DataProvider(name = "passwordChangeSuccess")
    public Object[][] passwordChangeSuccess() {
        return new Object[][]{
                {"ibrahim@gmail.com", "Z123456z"}
        };
    }

    @DataProvider(name = "passwordChangeNegative")
    public Object[][] passwordChangeNegative() {
        return new Object[][]{
                {"ibrahim@gmail.com", "WRONG_PASS", "Test1234T", "Test1234T", "incorrect"},
                {"ibrahim@gmail.com", "Z123456z", "A123456a", "A123456aa", "match"},
                {"ibrahim@gmail.com", "Z123456z", "abc", "abc", "match"},
                {"ibrahim@gmail.com", "Z123456z", "Z123456z", "Z123456z", "different"}
        };
    }

    @DataProvider(name = "deleteWrong")
    public Object[][] deleteWrong() {
        return new Object[][]{
                {"ibrahim@gmail.com", "WRONG"}
        };
    }

    @DataProvider(name = "deleteSuccess")
    public Object[][] deleteSuccess() {
        return new Object[][]{
                {"ibrahim@gmail.com"}
        };
    }


    public void login(String email, String password) {
        driver.manage().deleteAllCookies();
        loginPage.open(baseUrl);
        loginPage.login(email, password);

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.or(
                        ExpectedConditions.urlContains("home"),
                        ExpectedConditions.urlContains("profile"),
                        ExpectedConditions.urlContains("select-cinema")
                ));

        profilePage.goToProfile();
    }


    @Test(priority = 1, dataProvider = "editProfileData")
    public void TC_3_1_editProfileTextFields(String email, String name, String phone, String address, String city, String country) {
        login(email, currentPassword);
        profilePage.openEditModal();
        editProfilePage.editProfile(name, phone, address, city, country);
    }

    @Test(priority = 2, dataProvider = "editProfileData")
    public void TC_3_2_emailReadOnly_UI(String email, String n, String p, String a, String c, String co) {
        login(email, currentPassword);
        profilePage.openEditModal();
        Assert.assertTrue(editProfilePage.isEmailReadOnly());
    }

    @Test(priority = 3, dataProvider = "emailBackendData")
    public void TC_3_2_emailReadOnly_Backend(String email, String fakeEmail) {
        login(email, currentPassword);
        profilePage.openEditModal();
        editProfilePage.forceEditEmail(fakeEmail);
        Assert.assertFalse(profilePage.getPageSourceLower().contains(fakeEmail));
    }

    @Test(priority = 4, dataProvider = "validPictureData")
    public void TC_3_3_uploadValidProfilePicture(String email, String path) {
        login(email, currentPassword);

        String oldSrc = profilePage.getImageSrc();
        profilePage.uploadPicture(path);

        var session = driver.manage().getCookieNamed("session");
        boolean backendUpdated = session != null && !session.getValue().isEmpty();
        boolean imageChanged = !profilePage.getImageSrc().equals(oldSrc);

        Assert.assertTrue(backendUpdated && imageChanged,
                "Profile picture upload failed → Backend session not updated or image not updated.");
    }

    @Test(priority = 5, dataProvider = "invalidPictureData")
    public void TC_3_4_uploadInvalidExtension(String email, String path) {
        login(email, currentPassword);
        File invalid = new File(path);
        try { invalid.createNewFile(); } catch (Exception ignored) {}
        profilePage.uploadPicture(path);
        Assert.assertFalse(profilePage.getImageSrc().contains("test.txt"));
    }

    @Test(priority = 6)
    public void TC_3_5_uploadLargeFile() {
        // Placeholder test
        login("ibrahim@gmail.com", currentPassword);
        Assert.assertTrue(true);
    }

    @Test(priority = 7, dataProvider = "passwordChangeSuccess")
    public void TC_3_6_changePasswordSuccess(String email, String newPass) {
        login(email, currentPassword);

        profilePage.openChangePassModal();
        changePasswordPage.changePasswordByName(currentPassword, newPass, newPass);

        Assert.assertTrue(profilePage.getPageSourceLower().contains("success"));

        currentPassword = newPass;

        driver.navigate().refresh();
    }

    @Test(priority = 8, dataProvider = "passwordChangeNegative")
    public void TC_Negative_Password_Change_Scenarios(String email, String currentInput, String newPass, String confirm, String expectedKey) {
        login(email, currentPassword);

        profilePage.openChangePassModal();
        changePasswordPage.changePasswordById(currentInput, newPass, confirm);

        Assert.assertTrue(changePasswordPage.isChangePassModalDisplayed());
        Assert.assertTrue(driver.getCurrentUrl().contains("profile"));

        String page = profilePage.getPageSourceLower();
        Assert.assertFalse(page.contains("internal server error"));
        Assert.assertTrue(page.contains(expectedKey) ||
                page.contains("wrong") ||
                page.contains("must be different"));
    }

    @Test(priority = 9, dataProvider = "passwordChangeSuccess")
    public void TC_3_13_recentBookingsVisible(String email, String newPass) {
        login(email, currentPassword);
        Assert.assertTrue(profilePage.getPageSourceLower().contains("bookings"));
    }

    @Test(priority = 10, dataProvider = "dobData")
    public void TC_3_14_dobValidation(String email, String dob) {
        login(email, currentPassword);
        profilePage.openEditModal();
        editProfilePage.editDob(dob);

        int year = Integer.parseInt(dob.substring(0, 4));
        String page = profilePage.getPageSourceLower();

        if (year > 2012)
            Assert.assertTrue(page.contains("2012") || page.contains("invalid"));
        else if (year < 1960)
            Assert.assertTrue(page.contains("1960") || page.contains("invalid"));
        else
            Assert.assertFalse(page.contains("invalid"));
    }

    @Test(priority = 11, dataProvider = "deleteWrong")
    public void TC_3_12_deleteAccountWrongPassword(String email, String wrongPass) {
        login(email, currentPassword);
        profilePage.openDeleteAccountModal();
        deleteAccountPage.deleteAccount(wrongPass);
        String page = profilePage.getPageSourceLower();
        Assert.assertTrue(page.contains("wrong") || page.contains("incorrect"));
    }

    @Test(priority = 12, dataProvider = "deleteSuccess")
    public void TC_3_11_deleteAccountSuccess(String email) {
        login(email, currentPassword);
        profilePage.openDeleteAccountModal();
        deleteAccountPage.deleteAccount(currentPassword);

        driver.get(baseUrl + "/login");
        loginPage.login(email, currentPassword);

        Assert.assertTrue(driver.getCurrentUrl().contains("login"));
    }
}
package tests;

import base.BaseTest;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.*;

import java.util.List;

public class AdminUsers extends BaseTest {

    private LoginPage loginPage;
    private AdminUsersPage adminUsersPage;
    private EditUserPage editUserPage;

    @BeforeClass
    public void classSetup() {
        setUp();
        loginPage = new LoginPage(driver);
        adminUsersPage = new AdminUsersPage(driver);
        editUserPage = new EditUserPage(driver);
    }

    @AfterClass
    public void classTeardown() {
        tearDown();
    }

    private void loginAsAdmin(String email, String password) {
        loginPage.open(baseUrl);
        loginPage.login(email, password);
        adminUsersPage.sleep(1500);
    }

    @DataProvider(name = "viewUserListData")
    public Object[][] viewUserListData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", "No registered users found", "@"}
        };
    }

    @DataProvider(name = "editUserData")
    public Object[][] editUserData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", "xz@a.com", "Edited User", "123456"}
        };
    }

    @DataProvider(name = "dobValidationData")
    public Object[][] dobValidationData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", "xz@a.com", "1959-12-31", "2013-01-01", "1990-05-15", "/admin/users/edit", "1960", "2012", "/admin/users", "success", "updated"}
        };
    }

    @DataProvider(name = "duplicateEmailData")
    public Object[][] duplicateEmailData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", "xz@a.com", "user3@test.com", "/admin/users/edit", "email", "exists", "already"}
        };
    }

    @DataProvider(name = "adminEditProtectionData")
    public Object[][] adminEditProtectionData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", "123456789000000000000000", "/admin/users/edit/", "/admin/users", "admin", "cannot", "edit"}
        };
    }

    @DataProvider(name = "adminDeleteProtectionData")
    public Object[][] adminDeleteProtectionData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", "123456789000000000000000", "/admin/users/delete/", "/admin/users", "cannot", "delete", "admin"}
        };
    }

    @DataProvider(name = "userDeletionData")
    public Object[][] userDeletionData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", "fresh1763504960027@test.com", "X123456x", "/logout", "/movie/692e3f3fe3e33fcb79e66ab9"}
        };
    }

    @Test(dataProvider = "viewUserListData")
    public void TC_7_1_adminViewCustomerList(String adminEmail, String adminPass, String emptyTableMsg, String userSymbol) {
        try {
            loginAsAdmin(adminEmail, adminPass);
            adminUsersPage.goToAdminUsers();
            adminUsersPage.sleep(1000);

            if (adminUsersPage.isTableEmptyAndMessageShown()) {
                Assert.assertTrue(adminUsersPage.getPageSource().contains(emptyTableMsg));
                return;
            }

            boolean foundNormalUser = false;
            boolean adminShown = false;

            List<WebElement> rows = adminUsersPage.getUsersList();
            for (WebElement row : rows) {
                String txt = row.getText();

                if (txt.contains(adminEmail)) {
                    adminShown = true;
                }
                if (txt.contains(userSymbol) && !txt.contains(adminEmail)) {
                    foundNormalUser = true;
                }
            }

            Assert.assertFalse(adminShown);
            Assert.assertTrue(foundNormalUser);

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(dataProvider = "editUserData")
    public void TC_7_2_adminEditUser(String adminEmail, String adminPass, String targetEmail, String newName, String newPhone) {
        try {
            loginAsAdmin(adminEmail, adminPass);
            adminUsersPage.goToAdminUsers();
            adminUsersPage.sleep(1000);

            adminUsersPage.clickEditForUser(targetEmail);
            editUserPage.waitForEditPage();
            adminUsersPage.sleep(500);

            editUserPage.editName(newName);
            editUserPage.editPhone(newPhone);
            editUserPage.clickSave();
            adminUsersPage.sleep(1500);

            adminUsersPage.goToAdminUsers();
            Assert.assertTrue(adminUsersPage.isUserListed(targetEmail));

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(dataProvider = "dobValidationData")
    public void TC_7_3_adminDOBValidation(String adminEmail, String adminPass, String targetEmail, String oldDob, String futureDob, String validDob, String editUrlPart, String minYearErr, String maxYearErr, String successUrlPart, String successMsg1, String successMsg2) {
        try {
            loginAsAdmin(adminEmail, adminPass);
            adminUsersPage.goToAdminUsers();
            adminUsersPage.sleep(1000);

            adminUsersPage.clickEditForUser(targetEmail);
            adminUsersPage.sleep(500);

            editUserPage.editDob(oldDob);
            editUserPage.clickSave();
            adminUsersPage.sleep(1000);
            Assert.assertTrue(editUserPage.getCurrentUrl().contains(editUrlPart));
            Assert.assertTrue(editUserPage.getPageSource().contains(minYearErr));

            editUserPage.editDob(futureDob);
            editUserPage.clickSave();
            adminUsersPage.sleep(1000);
            Assert.assertTrue(editUserPage.getCurrentUrl().contains(editUrlPart));
            Assert.assertTrue(editUserPage.getPageSource().contains(maxYearErr));

            editUserPage.editDob(validDob);
            editUserPage.clickSave();
            adminUsersPage.sleep(1500);

            Assert.assertTrue(adminUsersPage.getCurrentUrl().contains(successUrlPart));
            Assert.assertTrue(adminUsersPage.getPageSource().contains(successMsg1)
                    || adminUsersPage.getPageSource().contains(successMsg2));

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(dataProvider = "duplicateEmailData")
    public void TC_7_4_adminEditUserEmailToExisting(String adminEmail, String adminPass, String targetEmail, String existingEmail, String editUrlPart, String err1, String err2, String err3) {
        try {
            loginAsAdmin(adminEmail, adminPass);
            adminUsersPage.goToAdminUsers();
            adminUsersPage.sleep(1000);

            adminUsersPage.clickEditForUser(targetEmail);
            adminUsersPage.sleep(500);

            editUserPage.editEmail(existingEmail);
            editUserPage.clickSave();
            adminUsersPage.sleep(1500);

            Assert.assertTrue(editUserPage.getCurrentUrl().contains(editUrlPart));
            Assert.assertTrue(
                    editUserPage.getPageSource().toLowerCase().contains(err1)
                            && editUserPage.getPageSource().toLowerCase().contains(err2)
                            || editUserPage.getPageSource().toLowerCase().contains(err3)
            );

            adminUsersPage.goToAdminUsers();
            Assert.assertTrue(adminUsersPage.isUserListed(targetEmail));

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(dataProvider = "adminEditProtectionData")
    public void TC_7_5_adminCannotEditAnotherAdmin(String adminEmail, String adminPass, String fakeAdminId, String editUrlBase, String listUrlPart, String err1, String err2, String err3) {
        try {
            loginAsAdmin(adminEmail, adminPass);

            driver.get(baseUrl + editUrlBase + fakeAdminId);
            adminUsersPage.sleep(1500);

            boolean redirected = adminUsersPage.getCurrentUrl().contains(listUrlPart);
            Assert.assertTrue(redirected);

            String src = adminUsersPage.getPageSource().toLowerCase();
            boolean hasError = src.contains(err1) && src.contains(err2) && src.contains(err3);
            Assert.assertTrue(hasError);

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(dataProvider = "adminDeleteProtectionData")
    public void TC_7_7_adminCannotDeleteAdmin(String adminEmail, String adminPass, String fakeAdminId, String deleteUrlBase, String listUrlPart, String err1, String err2, String err3) {
        try {
            loginAsAdmin(adminEmail, adminPass);

            String deleteUrl = baseUrl + deleteUrlBase + fakeAdminId;

            adminUsersPage.executeJsPostRequest(deleteUrl);
            adminUsersPage.sleep(2000);

            boolean redirected = adminUsersPage.getCurrentUrl().contains(listUrlPart);
            Assert.assertTrue(redirected);

            String src = adminUsersPage.getPageSource().toLowerCase();
            boolean errorDetected = src.contains(err1) && src.contains(err2) && src.contains(err3);
            Assert.assertTrue(errorDetected);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(dataProvider = "userDeletionData")
    public void TC_7_6_adminDeleteUserAndCascade(String adminEmail, String adminPass, String emailToDelete, String userPassword, String logoutUrl, String checkUrl) {
        try {
            loginAsAdmin(adminEmail, adminPass);
            adminUsersPage.goToAdminUsers();
            adminUsersPage.sleep(1000);

            adminUsersPage.clickDeleteForUser(emailToDelete);
            adminUsersPage.sleep(500);
            adminUsersPage.acceptAlert();
            adminUsersPage.sleep(2000);
            adminUsersPage.goToAdminUsers();
            adminUsersPage.sleep(1000);
            boolean stillListed = adminUsersPage.isUserListed(emailToDelete);
            Assert.assertFalse(stillListed, "User is still listed in the table!");

            driver.get(baseUrl + logoutUrl);
            adminUsersPage.sleep(1000);

            loginPage.open(baseUrl);
            loginPage.login(emailToDelete, userPassword);
            adminUsersPage.sleep(1000);

            driver.get(baseUrl + checkUrl);
            adminUsersPage.sleep(500);

            boolean accessDenied = driver.getCurrentUrl().contains("login") || !driver.getCurrentUrl().contains("/movie/");
            Assert.assertTrue(accessDenied, "Deleted user managed to access the system!");

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
package tests;

import base.BaseTest;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.*;

import java.util.List;

public class AdminTests extends BaseTest {

    private LoginPage loginPage;
    private AdminMoviesPage adminMoviesPage;
    private AdminDashboardPage adminDashboardPage;

    @BeforeClass
    public void classSetup() {
        setUp();
        loginPage = new LoginPage(driver);
        adminMoviesPage = new AdminMoviesPage(driver);
        adminDashboardPage = new AdminDashboardPage(driver);
    }

    @AfterClass
    public void classTeardown() {
        tearDown();
    }

    private void adminLogin(String email, String password) {
        loginPage.open(baseUrl);
        loginPage.login(email, password);
        adminDashboardPage.waitForUrlContains("/admin");
    }

    @DataProvider(name = "newMovieData")
    public Object[][] newMovieData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", "Automation Test Movie", "This is an automated movie added by Selenium.", "120", "Action", "now_showing", "https://picsum.photos/400/600", "/admin"}
        };
    }

    @DataProvider(name = "invalidTitleMovieData")
    public Object[][] invalidTitleMovieData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", null, "desc", "120", "Action", "https://picsum.photos/200", "/admin/movies/add"}
        };
    }

    @DataProvider(name = "noCinemaMovieData")
    public Object[][] noCinemaMovieData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", "No Cinema Movie", "desc", "120", "Drama", "https://picsum.photos/200", "/admin/movies/add"}
        };
    }

    @DataProvider(name = "successMovieData")
    public Object[][] successMovieData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", "Valid Movie Selenium", "good movie", "120", "Sci-Fi", "https://picsum.photos/300", "/admin"}
        };
    }

    @DataProvider(name = "editMovieData")
    public Object[][] editMovieData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", "691e0177e3b8780cdac512a9", "Updated Movie Title", "/admin"}
        };
    }

    @DataProvider(name = "deleteMovieData")
    public Object[][] deleteMovieData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", "692cf7054434b43d577b779a", "/admin", "http://127.0.0.1:5000/admin/movies"}
        };
    }

    @DataProvider(name = "screeningData")
    public Object[][] screeningData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", 0, 1, "2025-12-25", "21:00", "Hall 2", "Hall 5",
                        "http://127.0.0.1:5000/admin/screenings/add",
                        "http://127.0.0.1:5000/admin/screenings",
                        "http://127.0.0.1:5000/admin/screenings/delete/",
                        "/admin"}
        };
    }

    @DataProvider(name = "cinemaData")
    public Object[][] cinemaData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", "Test Cinema Branch", "Cairo", "Egypt", "Street 123, Nasr City", "Updated Cinema Branch",
                        "http://127.0.0.1:5000/admin/cinemas/add",
                        "http://127.0.0.1:5000/admin/cinemas",
                        "http://127.0.0.1:5000/admin/cinemas/delete/",
                        "/admin"}
        };
    }

    @DataProvider(name = "promotionData")
    public Object[][] promotionData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", "Test Promo Selenium", "Selenium promo description", "B1G1F", true,
                        "http://127.0.0.1:5000/admin/promotions/add",
                        "http://127.0.0.1:5000/admin/promotions",
                        "http://127.0.0.1:5000/admin/promotions/delete/",
                        "/admin"}
        };
    }

    @DataProvider(name = "foodData")
    public Object[][] foodData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", "Test Fries", "Automation test item", "25.50", "Snacks", "https://example.com/test.jpg", true, "30.00",
                        "http://127.0.0.1:5000/admin/food/add",
                        "http://127.0.0.1:5000/admin/food"}
        };
    }

    @DataProvider(name = "bookingViewData")
    public Object[][] bookingViewData() {
        return new Object[][]{
                {"admin@cinema.com", "admin123", "http://127.0.0.1:5000/admin/all-bookings",
                        "User Not Found", ".*@.*\\..*", "Movie Not Found", "Cinema Not Found", "hall", ".*[A-Z][0-9].*", ".*\\d+.*"}
        };
    }

    @Test(priority=2, dataProvider = "newMovieData")
    public void TC_6_1_adminAddNewMovie(String email, String pass, String title, String desc, String duration, String genre, String status, String imgUrl, String successUrlPart) {
        adminLogin(email, pass);
        adminMoviesPage.goToAddMovie();
        adminMoviesPage.fillMovieForm(title, desc, duration, genre, status, imgUrl);
        adminMoviesPage.selectAllCinemas();
        adminMoviesPage.selectFirstCinema();
        adminMoviesPage.clickSubmit();

        adminDashboardPage.waitForUrlContains(successUrlPart);
        Assert.assertTrue(adminMoviesPage.getCurrentUrl().contains(successUrlPart), "Failed: Admin was not redirected to " + successUrlPart);
    }

    @Test(priority=3, dataProvider = "invalidTitleMovieData")
    public void TC_6_2_A_emptyTitleShowsValidationError(String email, String pass, String title, String desc, String duration, String genre, String imgUrl, String errorUrlPart) {
        adminLogin(email, pass);
        adminMoviesPage.goToAddMovie();
        adminMoviesPage.fillMovieForm(title, desc, duration, genre, null, imgUrl);
        adminMoviesPage.selectFirstCinema();
        adminMoviesPage.clickSubmit();

        Assert.assertTrue(adminMoviesPage.getCurrentUrl().contains(errorUrlPart));
        Assert.assertTrue(adminMoviesPage.isValidationErrorDisplayed(), "Expected validation error message for empty title.");
    }

    @Test(priority=4, dataProvider = "noCinemaMovieData")
    public void TC_6_2_B_noCinemaSelectedShowsError(String email, String pass, String title, String desc, String duration, String genre, String imgUrl, String errorUrlPart) {
        adminLogin(email, pass);
        adminMoviesPage.goToAddMovie();
        adminMoviesPage.fillMovieForm(title, desc, duration, genre, null, imgUrl);
        adminMoviesPage.clickSubmit();

        Assert.assertTrue(adminMoviesPage.getCurrentUrl().contains(errorUrlPart), "Expected to stay on Add Movie page when no cinema is selected.");
    }

    @Test(priority=5, dataProvider = "successMovieData")
    public void TC_6_2_C_successAddMovie(String email, String pass, String title, String desc, String duration, String genre, String imgUrl, String successUrlPart) {
        adminLogin(email, pass);
        adminMoviesPage.goToAddMovie();
        adminMoviesPage.fillMovieForm(title, desc, duration, genre, null, imgUrl);
        adminMoviesPage.selectFirstCinema();
        adminMoviesPage.clickSubmit();

        adminDashboardPage.waitForUrlContains(successUrlPart);
        Assert.assertTrue(adminMoviesPage.isMoviePresent(title.toLowerCase()), "Movie should appear in admin dashboard list after adding.");
    }

    @Test(priority=6, dataProvider = "editMovieData")
    public void TC_6_3_editMovie(String email, String pass, String movieId, String newTitle, String successUrlPart) {
        adminLogin(email, pass);
        adminMoviesPage.goToEditMovie(movieId);
        adminMoviesPage.fillMovieForm(newTitle, null, null, null, null, null);
        adminMoviesPage.toggleCinemaCheckboxes();
        adminMoviesPage.clickSubmit();

        adminDashboardPage.waitForUrlContains(successUrlPart);
        Assert.assertTrue(adminMoviesPage.getCurrentUrl().contains(successUrlPart), "TC-6.3 PASSED: Movie updated successfully.");
    }

    @Test(priority=7, dataProvider = "deleteMovieData")
    public void TC_6_4_deleteMovie(String email, String pass, String movieId, String successUrlPart, String moviesListUrl) {
        adminLogin(email, pass);
        adminMoviesPage.goToDeleteMovie(movieId);
        adminMoviesPage.confirmDelete();

        adminDashboardPage.waitForUrlContains(successUrlPart);
        adminDashboardPage.goToUrl(moviesListUrl);
        Assert.assertFalse(adminMoviesPage.isMoviePresent(movieId), "TC-6.4 FAILED: Movie still appears after deletion.");
    }

    @Test(priority=8, dataProvider = "screeningData")
    public void TC_6_5_adminAddEditDeleteScreening(String email, String pass, int movieIndex, int cinemaIndex, String date, String time, String initialHall, String newHall, String addUrl, String listUrl, String deleteBaseUrl, String successUrlPart) {
        adminLogin(email, pass);
        adminDashboardPage.goToUrl(addUrl);
        adminDashboardPage.fillScreeningForm(movieIndex, cinemaIndex, date, time, initialHall);
        adminDashboardPage.clickSubmit();
        adminDashboardPage.waitForUrlContains(successUrlPart);

        adminDashboardPage.goToUrl(listUrl);
        String editUrl = adminDashboardPage.getLastScreeningEditUrl();
        String screeningId = editUrl.substring(editUrl.lastIndexOf('/') + 1);
        Assert.assertFalse(screeningId.isEmpty(), "Screening ID not detected!");

        adminDashboardPage.goToUrl(editUrl);
        adminDashboardPage.editScreeningHall(newHall);
        adminDashboardPage.clickSubmit();
        adminDashboardPage.waitForUrlContains(successUrlPart);

        adminDashboardPage.goToUrl(editUrl);
        Assert.assertEquals(adminDashboardPage.getScreeningHallValue(), newHall);

        adminDashboardPage.goToUrl(deleteBaseUrl + screeningId);
        adminDashboardPage.clickDelete();
        adminDashboardPage.waitForUrlContains(successUrlPart);

        adminDashboardPage.goToUrl(listUrl);
        Assert.assertFalse(adminDashboardPage.getPageSource().contains(screeningId), "Screening was NOT deleted!");
    }

    @Test(priority=9, dataProvider = "cinemaData")
    public void TC_6_6_adminAddEditDeleteCinema(String email, String pass, String name, String city, String country, String address, String newName, String addUrl, String listUrl, String deleteBaseUrl, String successUrlPart) {
        adminLogin(email, pass);
        adminDashboardPage.goToUrl(addUrl);
        adminDashboardPage.fillCinemaForm(name, city, country, address);
        adminDashboardPage.clickSubmit();
        adminDashboardPage.waitForUrlContains(successUrlPart);

        adminDashboardPage.goToUrl(listUrl);
        String editUrl = adminDashboardPage.getLastCinemaEditUrl();
        String cinemaId = editUrl.substring(editUrl.lastIndexOf('/') + 1);
        Assert.assertFalse(cinemaId.isEmpty(), "Cinema ID not detected!");

        adminDashboardPage.goToUrl(editUrl);
        adminDashboardPage.editCinemaName(newName);
        adminDashboardPage.clickSubmit();
        adminDashboardPage.waitForUrlContains(successUrlPart);

        adminDashboardPage.goToUrl(editUrl);
        Assert.assertEquals(adminDashboardPage.getCinemaNameValue(), newName);

        adminDashboardPage.goToUrl(deleteBaseUrl + cinemaId);
        adminDashboardPage.clickDelete();
        adminDashboardPage.acceptAlert();
        adminDashboardPage.waitForUrlContains(successUrlPart);

        adminDashboardPage.goToUrl(listUrl);
        Assert.assertFalse(adminDashboardPage.getPageSource().contains(cinemaId), "Cinema was NOT deleted!");
    }

    @Test(priority=10, dataProvider = "promotionData")
    public void TC_6_7_adminAddEditDeletePromotion(String email, String pass, String promoName, String promoDesc, String promoCode, boolean isActive, String addUrl, String listUrl, String deleteBaseUrl, String successUrlPart) {
        adminLogin(email, pass);
        adminDashboardPage.goToUrl(addUrl);
        adminDashboardPage.fillPromoForm(promoName, promoDesc, promoCode, isActive);
        adminDashboardPage.clickSubmit();
        adminDashboardPage.waitForUrlContains(successUrlPart);

        adminDashboardPage.goToUrl(listUrl);
        String editUrl = adminDashboardPage.getLastPromoEditUrl();
        String promoId = editUrl.substring(editUrl.lastIndexOf("/") + 1);
        Assert.assertFalse(promoId.isEmpty(), "Promo ID NOT FOUND!");

        adminDashboardPage.goToUrl(editUrl);
        adminDashboardPage.togglePromoActive();
        adminDashboardPage.clickSubmit();
        adminDashboardPage.waitForUrlContains(successUrlPart);

        adminDashboardPage.goToUrl(editUrl);
        Assert.assertFalse(adminDashboardPage.isPromoActive(), "Edit FAILED!");

        adminDashboardPage.goToUrl(deleteBaseUrl + promoId);
        adminDashboardPage.waitForUrlContains(successUrlPart);

        adminDashboardPage.goToUrl(listUrl);
        Assert.assertFalse(adminDashboardPage.getPageSource().contains(promoId), "Promotion was NOT deleted!");
    }

    @Test(priority=11, dataProvider = "foodData")
    public void TC_6_8_adminAddEditDeleteFoodItem(String email, String pass, String name, String desc, String price, String category, String imgUrl, boolean isVeg, String newPrice, String addUrl, String listUrl) {
        adminLogin(email, pass);
        adminDashboardPage.goToUrl(addUrl);
        adminDashboardPage.waitForPageLoad();
        adminDashboardPage.fillFoodForm(name, desc, price, category, imgUrl, isVeg);
        adminDashboardPage.clickSubmit();
        adminDashboardPage.sleep(150);

        adminDashboardPage.goToUrl(listUrl);
        adminDashboardPage.waitForPageLoad();
        String editUrl = adminDashboardPage.getLastFoodEditUrl();
        adminDashboardPage.goToUrl(editUrl);
        adminDashboardPage.waitForPageLoad();

        String foodId = adminDashboardPage.getCurrentUrl().replaceAll(".*/", "");
        adminDashboardPage.editFoodPriceAndAvailability(newPrice, true);
        adminDashboardPage.clickSubmit();
        adminDashboardPage.sleep(150);

        adminDashboardPage.goToUrl(listUrl);
        adminDashboardPage.waitForPageLoad();
        adminDashboardPage.clickLastFoodDelete();
        adminDashboardPage.acceptAlert();

        adminDashboardPage.goToUrl(listUrl);
        adminDashboardPage.waitForPageLoad();
        Assert.assertFalse(adminDashboardPage.getPageSource().contains(foodId), "Food Item NOT deleted!");
    }

    @Test(priority=1, dataProvider = "bookingViewData")
    public void TC_6_9_adminCanViewUserBookings(String email, String pass, String bookingsUrl, String userNotFoundMsg, String emailRegex, String movieNotFoundMsg, String cinemaNotFoundMsg, String hallKeyword, String seatRegex, String priceRegex) {
        adminLogin(email, pass);
        adminDashboardPage.goToUrl(bookingsUrl);

        List<WebElement> rows = adminDashboardPage.getBookingRows();
        Assert.assertTrue(rows.size() > 0, "No bookings found — at least one booking is required.");

        boolean fullDetailsFound = false;
        for (WebElement row : rows) {
            String text = row.getText();
            boolean hasUserName = !text.contains(userNotFoundMsg);
            boolean hasUserEmail = text.matches(emailRegex);
            boolean hasMovie = !text.contains(movieNotFoundMsg);
            boolean hasCinema = !text.contains(cinemaNotFoundMsg);
            boolean hasHall = text.toLowerCase().contains(hallKeyword);
            boolean hasSeats = text.matches(seatRegex);
            boolean hasPrice = text.matches(priceRegex);

            if (hasUserName && hasUserEmail && hasMovie && hasCinema && hasHall && hasSeats && hasPrice) {
                fullDetailsFound = true;
                break;
            }
        }
        Assert.assertTrue(fullDetailsFound, "No booking row contains full details.");
    }
}
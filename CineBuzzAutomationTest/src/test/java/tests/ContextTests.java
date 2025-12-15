package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.CinemaPage;
import pages.HomePage;
import pages.LoginPage;
import pages.MovieDetailsPage;

public class ContextTests extends BaseTest {

    LoginPage loginPage;
    CinemaPage cinemaPage;
    HomePage homePage;
    MovieDetailsPage movieDetailsPage;

    @BeforeClass
    public void classSetup() {
        setUp();
        loginPage = new LoginPage(driver);
        cinemaPage = new CinemaPage(driver);
        homePage = new HomePage(driver);
        movieDetailsPage = new MovieDetailsPage(driver);
    }

    @AfterClass
    public void classTeardown() {
        tearDown();
    }

    @DataProvider(name = "loginData")
    public Object[][] loginData() {
        return new Object[][]{
                { "xz@a.com", "X123456x" }
        };
    }

    @DataProvider(name = "cinemaData")
    public Object[][] cinemaData() {
        return new Object[][]{
                { "xz@a.com", "X123456x", "Stars Cinema (Nasr City)", "http://127.0.0.1:5000/" }
        };
    }

    @DataProvider(name = "clearCinemaData")
    public Object[][] clearCinemaData() {
        return new Object[][]{
                { "xz@a.com", "X123456x", "Stars Cinema (Nasr City)" }
        };
    }

    @DataProvider(name = "movieDetailsData")
    public Object[][] movieDetailsData() {
        return new Object[][]{
                { "xz@a.com", "X123456x", "Stars Cinema (Nasr City)", "692e41ade3e33fcb79e66ac2", "No Screenings Available" }
        };
    }

    @DataProvider(name = "filteredScreeningData")
    public Object[][] filteredScreeningData() {
        return new Object[][]{
                { "xz@a.com", "X123456x", "Stars Cinema (Nasr City)", "692e3fc8e3e33fcb79e66abb", "Stars Cinema", "Point 90 Cinema" }
        };
    }

    @DataProvider(name = "showtimeData")
    public Object[][] showtimeData() {
        return new Object[][]{
                { "xz@a.com", "X123456x", "Stars Cinema (Nasr City)", "692e3fc8e3e33fcb79e66abb" }
        };
    }

    @DataProvider(name = "movieSearchData")
    public Object[][] movieSearchData() {
        return new Object[][]{
                { "xz@a.com", "X123456x", "Stars Cinema (Nasr City)", "Avatar", "Avatar", new String[]{"Venom", "Superman", "F1"}, false },
                { "xz@a.com", "X123456x", "Stars Cinema (Nasr City)", "Venom", "Venom", new String[]{"Avatar", "Superman", "F1"}, false },
                { "xz@a.com", "X123456x", "Stars Cinema (Nasr City)", "Superman", "Superman", new String[]{"Avatar", "Venom", "F1"}, false },
                { "xz@a.com", "X123456x", "Stars Cinema (Nasr City)", "F1", "F1", new String[]{"Avatar", "Venom", "Superman"}, false },
                { "xz@a.com", "X123456x", "Stars Cinema (Nasr City)", "Zodiac", "", new String[]{"Avatar", "Venom", "Superman", "F1"}, true }
        };
    }

    @DataProvider(name = "resetSearchData")
    public Object[][] resetSearchData() {
        return new Object[][]{
                { "xz@a.com", "X123456x", "Stars Cinema (Nasr City)", new String[]{"Avatar", "Venom", "Superman", "F1"} }
        };
    }

    private void doLogin(String email, String pass) {
        driver.manage().deleteAllCookies();
        loginPage.open(baseUrl);
        loginPage.login(email, pass);
    }

    @Test(dataProvider = "loginData")
    public void TC_2_1_redirectToSelectCinema(String email, String pass) {
        doLogin(email, pass);
        Assert.assertTrue(cinemaPage.getCurrentUrl().contains("/select-cinema"));
    }

    @Test(dataProvider = "cinemaData")
    public void TC_2_2_selectCinemaAndHome(String email, String pass, String cinemaName, String expectedUrl) {
        doLogin(email, pass);
        cinemaPage.selectCinema(cinemaName);
        Assert.assertEquals(cinemaPage.getCurrentUrl(), expectedUrl);
    }

    @Test(dataProvider = "clearCinemaData")
    public void TC_2_3_clearCinemaContext(String email, String pass, String cinemaName) {
        doLogin(email, pass);
        cinemaPage.selectCinema(cinemaName);
        cinemaPage.clearCinemaContext();
        Assert.assertTrue(cinemaPage.getCurrentUrl().contains("/select-cinema"));
    }

    @Test(dataProvider = "movieDetailsData")
    public void TC_2_4_movieDetailsWrongCinemaEmptyScreenings(String email, String pass,
                                                              String cinemaName, String movieId, String expectedText) {
        doLogin(email, pass);
        cinemaPage.selectCinema(cinemaName);
        movieDetailsPage.goToMovie(movieId);
        Assert.assertTrue(movieDetailsPage.getScreeningsTableText().contains(expectedText));
    }

    @Test(dataProvider = "filteredScreeningData")
    public void TC_2_5_filteredScreenings(String email, String pass, String cinemaName,
                                          String movieId, String mustContain, String mustNotContain) {

        doLogin(email, pass);
        cinemaPage.selectCinema(cinemaName);
        movieDetailsPage.goToMovie(movieId);

        String page = movieDetailsPage.getPageSource();
        Assert.assertTrue(page.contains(mustContain));
        Assert.assertFalse(page.contains(mustNotContain));
    }

    @Test(dataProvider = "showtimeData")
    public void TC_2_6_showtimeFilter(String email, String pass, String cinemaName, String movieId) {
        doLogin(email, pass);
        cinemaPage.selectCinema(cinemaName);
        movieDetailsPage.goToMovie(movieId);
        Assert.assertTrue(movieDetailsPage.getPageSource().contains("Screenings"));
    }

    @Test(dataProvider = "showtimeData")
    public void TC_2_7_showtimeLinkCorrectScreening(String email, String pass, String cinemaName, String movieId) {
        doLogin(email, pass);
        cinemaPage.selectCinema(cinemaName);
        movieDetailsPage.goToMovie(movieId);
        movieDetailsPage.filterBySecondOption();
        Assert.assertTrue(movieDetailsPage.getFilteredRowCount() > 0);
    }

    @Test(dataProvider = "movieSearchData")
    public void TC_2_8_searchMovies(String email, String pass, String cinemaName, String searchTerm,
                                    String expectedVisible, String[] expectedHidden, boolean isNegative) {

        doLogin(email, pass);
        cinemaPage.selectCinema(cinemaName);

        homePage.searchMovie(searchTerm);
        homePage.sleep(500);

        if (isNegative) {
            Assert.assertTrue(homePage.getPageSource().toLowerCase().contains("no"));
        } else {
            Assert.assertTrue(homePage.isVisibleMovie(expectedVisible));
        }

        for (String hiddenMovie : expectedHidden) {
            Assert.assertFalse(homePage.isVisibleMovie(hiddenMovie));
        }
    }

    @Test(dependsOnMethods = "TC_2_8_searchMovies", dataProvider = "resetSearchData")
    public void TC_2_9_resetSearch(String email, String pass, String cinemaName, String[] movies) {

        doLogin(email, pass);
        cinemaPage.selectCinema(cinemaName);

        homePage.searchMovie("Zodiac");
        homePage.sleep(500);
        homePage.resetSearch();
        homePage.sleep(600);

        for (String movie : movies) {
            Assert.assertTrue(homePage.isVisibleMovie(movie));
        }
    }
}

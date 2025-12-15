package tests;

import base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.*;

import java.time.Duration;

public class BookingTest extends BaseTest {

    private LoginPage loginPage;
    private CinemaPage cinemaPage;
    private BookingPage bookingPage;
    private CheckoutPage checkoutPage;
    private MyBookingsPage myBookingsPage;
    private HomePage homePage;

    @BeforeClass
    public void classSetup() {
        setUp();
        loginPage = new LoginPage(driver);
        cinemaPage = new CinemaPage(driver);
        bookingPage = new BookingPage(driver);
        checkoutPage = new CheckoutPage(driver);
        myBookingsPage = new MyBookingsPage(driver);
        homePage = new HomePage(driver);
    }

    @AfterClass
    public void classTeardown() {
        tearDown();
    }

    @DataProvider(name = "promoData")
    public Object[][] getPromoData() {
        return new Object[][] {
                { "xz@a.com", "X123456x", "692e3fc8e3e33fcb79e66abb", new String[]{"H5"}, false, "TC-4.1", "Test User", "4111111111111111", "12/30", "123" },
                { "xz@a.com", "X123456x", "692e3fc8e3e33fcb79e66abb", new String[]{"H1", "H2"}, true, "TC-4.2", "Test User", "4111111111111111", "12/30", "123" },
                { "xz@a.com", "X123456x", "692e3fc8e3e33fcb79e66abb", new String[]{"G6", "G7", "G8"}, true, "TC-4.3", "Test User", "4111111111111111", "12/30", "123" }
        };
    }

    @DataProvider(name = "negativeBookingData")
    public Object[][] getNegativeBookingData() {
        return new Object[][] {
                { "xz@a.com", "X123456x", "692e3fc8e3e33fcb79e66abb", new String[]{}, false, "TC-4.4", "Test User", "4111111111111111", "12/30", "123" },
                { "xz@a.com", "X123456x", "692e3fc8e3e33fcb79e66abb", new String[]{"H5"}, true, "TC-4.5", "Test User", "4111111111111111", "12/30", "123" },
                { "xz@a.com", "X123456x", "692e3fc8e3e33fcb79e66abb", new String[]{"H5"}, true, "TC-4.6", "Test User", "4111111111111111", "12/30", "123" }
        };
    }

    @DataProvider(name = "pricingData")
    public Object[][] getPricingData() {
        return new Object[][] {
                { "xz@a.com", "X123456x", "692e3f3fe3e33fcb79e66ab9", new String[]{"F1", "F2"}, 20, "Regular Seats", "Test User", "4111111111111111", "12/30", "123" },
                { "xz@a.com", "X123456x", "692e3f3fe3e33fcb79e66ab9", new String[]{"J1", "J2"}, 30, "VIP Seats", "Test User", "4111111111111111", "12/30", "123" },
                { "xz@a.com", "X123456x", "692e3f3fe3e33fcb79e66ab9", new String[]{"F3", "J3"}, 25, "Mixed Seats", "Test User", "4111111111111111", "12/30", "123" }
        };
    }

    @DataProvider(name = "myBookingsData")
    public Object[][] getMyBookingsData() {
        return new Object[][] {
                { "xz@a.com", "X123456x" }
        };
    }

    @DataProvider(name = "moreThanSixSeatsData")
    public Object[][] getMoreThanSixSeatsData() {
        return new Object[][] {
                { "xz@a.com", "X123456x", "692e3fc8e3e33fcb79e66abb", new String[]{"A1", "A2", "A3", "A4", "A5", "A6", "A7"}, "Test User", "4111111111111111", "12/30", "123" }
        };
    }

    @DataProvider(name = "sixSeatsSuccessData")
    public Object[][] getSixSeatsSuccessData() {
        return new Object[][] {
                { "xz@a.com", "X123456x", "692e3fc8e3e33fcb79e66abb", new String[]{"K5"}, "Test User", "4111111111111111", "12/30", "123" }
        };
    }

    @DataProvider(name = "paymentValidationData")
    public Object[][] getPaymentValidationData() {
        return new Object[][] {
                { "xz@a.com", "X123456x", "692e3fc8e3e33fcb79e66abb", "C2", "User", "123", "00/00", "1", "Valid User", "4111111111111111", "12/30", "123" }
        };
    }

    @DataProvider(name = "timerData")
    public Object[][] getTimerData() {
        return new Object[][] {
                { "xz@a.com", "X123456x", "692e3fc8e3e33fcb79e66abb", "J4" }
        };
    }

    @DataProvider(name = "comingSoonData")
    public Object[][] getComingSoonData() {
        return new Object[][] {
                { "xz@a.com", "X123456x" }
        };
    }

    private void loginAndReachSeatSelection(String email, String pass, String movieId) {
        loginPage.open(baseUrl);
        loginPage.login(email, pass);

        bookingPage.sleep(1000);

        new WebDriverWait(driver, Duration.ofSeconds(10)).until(web -> ((JavascriptExecutor) web)
                .executeScript("return document.readyState").equals("complete"));

        cinemaPage.goToSelectCinema();
        cinemaPage.selectStarsCinema();

        bookingPage.sleep(500);
        bookingPage.goToMovie(movieId);
        bookingPage.clickBookTicket();

        bookingPage.sleep(1500);
    }



    @Test(dataProvider = "negativeBookingData")
    public void TC_Booking_Negative_Scenarios(String email, String pass, String movieId, String[] seats, boolean forceOccupied, String testId,
                                              String pName, String pCard, String pDate, String pCvv) {
        try {
            loginAndReachSeatSelection(email, pass, movieId);
            bookingPage.waitForSeatMap();

            if (forceOccupied && seats.length > 0) {
                bookingPage.forceSelectOccupiedSeat(seats[0]);
                bookingPage.sleep(500);
            } else {
                for (String seat : seats) {
                    bookingPage.selectSeat(seat);
                    bookingPage.sleep(300);
                }
            }

            checkoutPage.fillBookingPayment(pName, pCard, pDate, pCvv);
            bookingPage.sleep(500);
            checkoutPage.clickConfirmBooking();

            bookingPage.sleep(2000);

            Assert.assertTrue(bookingPage.isStillOnBookingPage(),
                    "User should remain on booking page for case: " + testId);
            System.out.println("✔ " + testId + " PASSED.");

        } catch (Exception e) {
            Assert.fail("Unexpected failure in " + testId + ": " + e.getMessage());
        }
    }

    @Test(dataProvider = "pricingData")
    public void TC_4_15_Pricing_Scenarios(String email, String pass, String movieId, String[] seats, int expectedTotal, String scenarioType,
                                          String pName, String pCard, String pDate, String pCvv) {
        try {
            loginAndReachSeatSelection(email, pass, movieId);
            bookingPage.waitForSeatMap();

            for (String seat : seats) {
                bookingPage.selectSeat(seat);
                bookingPage.sleep(600);
            }

            bookingPage.sleep(1000);

            Assert.assertEquals(checkoutPage.getBookingUiTotal(), expectedTotal,
                    "Incorrect total for " + scenarioType);

            checkoutPage.fillBookingPayment(pName, pCard, pDate, pCvv);

            System.out.println("✔ TC-4.15 PASSED — " + scenarioType);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Unexpected error: " + e.getMessage());
        }
    }

    @Test(dataProvider = "myBookingsData")
    public void TC_4_7_viewMyBookings(String email, String pass) {
        try {
            loginPage.open(baseUrl);
            loginPage.login(email, pass);
            bookingPage.sleep(1000);
            myBookingsPage.goToMyBookings();
            bookingPage.sleep(1000);

            Assert.assertTrue(myBookingsPage.validateMyBookingsContent(),
                    "Page did not display bookings nor 'no bookings' message.");
            System.out.println("✔ TC-4.7 PASSED — My Bookings displays correctly.");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Unexpected: " + e.getMessage());
        }
    }

    @Test(dataProvider = "moreThanSixSeatsData")
    public void TC_4_8_bookMoreThanSixSeats_negative(String email, String pass, String movieId, String[] seats,
                                                     String pName, String pCard, String pDate, String pCvv) {
        try {
            loginAndReachSeatSelection(email, pass, movieId);
            bookingPage.waitForSeatMap();

            for (String seat : seats) {
                bookingPage.selectSeat(seat);
                bookingPage.sleep(250);
            }

            checkoutPage.fillBookingPayment(pName, pCard, pDate, pCvv);
            bookingPage.sleep(500);
            checkoutPage.clickConfirmBooking();

            bookingPage.sleep(2000);

            Assert.assertTrue(bookingPage.isSeatMapPresent(),
                    "FAILED: Booking succeeded when selecting more than 6 seats!");
            System.out.println("✔ TC-4.8 PASSED — System correctly prevented booking more than 6 seats.");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
    }



    @Test(dataProvider = "paymentValidationData")
    public void TC_4_12_paymentFieldValidation(String email, String pass, String movieId, String seat,
                                               String invName, String invCard, String invDate, String invCvv,
                                               String valName, String valCard, String valDate, String valCvv) {
        try {
            loginAndReachSeatSelection(email, pass, movieId);
            bookingPage.waitForSeatMap();

            bookingPage.selectSeat(seat);
            bookingPage.sleep(500);

            checkoutPage.fillBookingPayment(invName, invCard, invDate, invCvv);
            bookingPage.sleep(500);

            Assert.assertFalse(checkoutPage.isConfirmBookingEnabled(),
                    "ERROR: Confirm button enabled on invalid payment!");

            checkoutPage.fillBookingPayment("", "", "", "");
            bookingPage.sleep(500);

            checkoutPage.fillBookingPayment(valName, valCard, valDate, valCvv);

            for (int i = 0; i < 10; i++) {
                if (checkoutPage.isConfirmBookingEnabled()) break;
                bookingPage.sleep(250);
            }

            Assert.assertTrue(checkoutPage.isConfirmBookingEnabled(),
                    "Confirm button did not enable on valid payment!");

            checkoutPage.clickConfirmBooking();

            boolean success = false;
            try {
                new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h2[contains(text(),'Booking Confirmed')]")));
                success = true;
            } catch (Exception ignored) {}

            if(!success) success = !bookingPage.getPageSource().contains("card-number");

            Assert.assertTrue(success, "FAILED: Valid payment did not reach confirmation page!");
            System.out.println("✔ TC-4.12 PASSED — Payment field validation working.");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Unexpected error: " + e.getMessage());
        }
    }

    @Test(dataProvider = "timerData")
    public void TC_4_13_bookingTimerExpires_redirectsUser(String email, String pass, String movieId, String seat) {
        try {
            loginAndReachSeatSelection(email, pass, movieId);
            bookingPage.waitForSeatMap();

            bookingPage.selectSeat(seat);
            bookingPage.sleep(500);

            bookingPage.expireTimer();
            bookingPage.sleep(1500);

            boolean redirected = !bookingPage.getCurrentUrl().contains("/book/") &&
                    !bookingPage.getPageSource().contains("seat-map");

            Assert.assertTrue(redirected, "FAILED: User not redirected after timer expiration!");
            System.out.println("✔ TC-4.13 PASSED — Timer expiration redirect simulation OK.");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Unexpected error: " + e.getMessage());
        }
    }

    @Test(dataProvider = "comingSoonData")
    public void TC_4_14_comingSoonBookButtonDoesNothing(String email, String pass) {
        try {
            loginPage.open(baseUrl);
            loginPage.login(email, pass);
            bookingPage.sleep(1000);

            cinemaPage.goToSelectCinema();
            cinemaPage.selectStarsCinema();
            homePage.goToHome();
            bookingPage.sleep(500);

            homePage.clickComingSoonTab();
            bookingPage.sleep(500);

            WebElement movieCard = homePage.getFirstComingSoonCard();
            WebElement comingSoonBtn = homePage.getComingSoonButton(movieCard);

            Assert.assertFalse(comingSoonBtn.isEnabled(), "ERROR: Coming Soon button is clickable!");
            System.out.println("✔ TC-4.14 PASSED — Coming Soon cannot be booked.");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Unexpected error: " + e.getMessage());
        }
    }
}
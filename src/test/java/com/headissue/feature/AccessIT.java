package com.headissue.feature;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.openqa.selenium.By.cssSelector;

@ExtendWith(ApplicationServerExtension.class)
class AccessIT {

    static WebDriver driver;
    static Given given;
    static When when;
    static Then then;

    @BeforeAll
    static void setUpClass() {
        driver = new ChromeDriver();
        driver.get("http://localhost:8080");
        given = new Given(driver);
        when = new When(driver);
        then = new Then(driver);
    }

    @AfterAll
    static void tearDownClass() {
        driver.quit();
    }

    @BeforeEach
    void setUp() {

    }

    @Test
    void whereTheyCanSeeThePdfWhenTheyProvideAnEmailAddress() {
        when.theyOpenTheTestPdfWithoutProvidingId();
        then.theyShouldSeeTheEmailForm();
        when.theySubmitAnEmail();
        then.theyShouldSeeThePdfCanvasAndControls();
        then.theyShouldSeeExamplePdfHasTwoPages();
    }

    private static class Given {
        private final WebDriver driver;

        public Given(WebDriver driver) {
            this.driver = driver;
        }
    }

    private static class When {
        private final WebDriver driver;

        public When(WebDriver driver) {
            this.driver = driver;
        }

        public void theyOpenTheTestPdfWithoutProvidingId() {
            driver.get("http://localhost:8080/docs/test");
        }

        public void theySubmitAnEmail() {
            driver.findElement(cssSelector("input[type='email']")).sendKeys("example@example.com", Keys.ENTER);
        }
    }

    private static class Then {
        private final WebDriver driver;

        public Then(WebDriver driver) {
            this.driver = driver;
        }

        public void theyShouldSeeTheEmailForm() {
            await().alias("see email input")
                    .atMost(5, SECONDS)
                    .until(() -> driver.findElements(cssSelector("input[type='email']")).size() > 0);
        }

        public void theyShouldSeeThePdfCanvasAndControls() {
            await().alias("see canvas")
                    .atMost(5, SECONDS)
                    .until(() -> driver.findElements(cssSelector("canvas")).size() > 0);
            await().alias("see buttons")
                    .atMost(5, SECONDS)
                    .until(() -> (driver.findElements(cssSelector("#next")).size() > 0) && (driver.findElements(cssSelector("#prev")).size() > 0));
        }

        public void theyShouldSeeExamplePdfHasTwoPages() {
            await().alias("see page 1 of 2")
                    .atMost(5, SECONDS)
                    .until(() -> (driver.findElement(cssSelector("#page_num")).getText().equals("1")) && (driver.findElement(cssSelector("#page_count")).getText().equals("2")));
        }
    }
}
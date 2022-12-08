package com.headissue.feature.share;

import com.headissue.feature.ApplicationServerExtension;
import com.headissue.feature.steps.Given;
import com.headissue.feature.steps.Then;
import com.headissue.feature.steps.When;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

@ExtendWith(ApplicationServerExtension.class)
class ShareIT {

    private static WebDriver driver;
    private static Given given;
    private static When when;
    private static Then then;

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
        if (driver != null) {
            driver.quit();
        }
    }


    @Test
    void whereUploadingPdfReturnsValidShareLink() {
        when.theyUploadPdf();
        then.theyShouldSeeTheShareLink();
        when.theyClickTheShareLink();
        then.theySeeThePromptToProvideEmailAddress();
    }

}
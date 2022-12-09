package com.headissue.feature.docs;

import com.headissue.feature.ApplicationServerExtension;
import com.headissue.feature.steps.Given;
import com.headissue.feature.steps.Then;
import com.headissue.feature.steps.When;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

@ExtendWith(ApplicationServerExtension.class)
class AccessIT {

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
    driver.quit();
  }

  @BeforeEach
  void setUp() {}

  @Test
  void whereTheyCanSeeThePdfWhenTheyProvideAnEmailAddress() {
    when.theyOpenTheTestPdfWithoutProvidingId();
    then.theySeeThePromptToProvideEmailAddress();
    when.theySubmitAnEmail();
    then.theyShouldSeeThePdfCanvasAndControls();
    then.theyShouldSeeExamplePdfHasTwoPages();
  }
}

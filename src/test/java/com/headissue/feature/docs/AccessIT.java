package com.headissue.feature.docs;

import com.headissue.feature.ApplicationServerExtension;
import com.headissue.feature.steps.Then;
import com.headissue.feature.steps.When;
import com.headissue.service.TestDataService;
import java.nio.file.Path;
import java.util.HashMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

@ExtendWith(ApplicationServerExtension.class)
class AccessIT {

  @TempDir private static Path tempDir;
  private static WebDriver driver;
  private static When when;
  private static Then then;

  @BeforeAll
  static void setUpClass() {
    ChromeOptions chromeOptions = new ChromeOptions();
    var prefs = new HashMap<String, Object>();
    prefs.put("download.default_directory", tempDir.toString());
    prefs.put("plugins.always_open_pdf_externally", true);
    chromeOptions.setExperimentalOption("prefs", prefs);

    driver = new ChromeDriver(chromeOptions);
    driver.get("http://localhost:8080");
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
    when.theyOpenTheTestPdf(TestDataService.defaultAccess);
    then.theySeeThePromptToProvideEmailAddress();
    when.theySubmitAnEmail();
    then.theyShouldSeeThatTheirEmailIsNotPartOfTheUrl();
    then.theyShouldSeeThePdfCanvasAndControls();
    then.theyShouldSeeExamplePdfHasTwoPages();
  }

  @Test
  void whereTheAccessIsExpired() {
    when.theyOpenTheTestPdf(TestDataService.expired);
    then.theySeeTheExpiryMessage();
  }

  @Test
  void wherePdfIsDownloadable() {
    when.theyOpenTheDownloadableTestPdfSuccessfully();
    then.theyShouldSeeItsDownloadable();
    when.theyDownloadIt();
    then.theyShouldSeeItsDownloaded(tempDir);
  }
}

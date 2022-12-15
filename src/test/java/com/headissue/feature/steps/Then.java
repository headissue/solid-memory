package com.headissue.feature.steps;

import static com.headissue.feature.Page.downloadButton;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.openqa.selenium.By.cssSelector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.hamcrest.core.StringContains;
import org.openqa.selenium.WebDriver;

public class Then {
  private final WebDriver driver;

  public Then(WebDriver driver) {
    this.driver = driver;
  }

  public void theySeeThePromptToProvideEmailAddress() {
    await()
        .alias("see email input")
        .atMost(5, SECONDS)
        .until(() -> driver.findElements(cssSelector("input[type='email']")).size() > 0);
  }

  public void theyShouldSeeThePdfCanvasAndControls() {
    await()
        .alias("see canvas")
        .atMost(5, SECONDS)
        .until(() -> driver.findElements(cssSelector("canvas")).size() > 0);
    await()
        .alias("see buttons")
        .atMost(5, SECONDS)
        .until(
            () ->
                (driver.findElements(cssSelector("#next")).size() > 0)
                    && (driver.findElements(cssSelector("#prev")).size() > 0));
  }

  public void theyShouldSeeExamplePdfHasTwoPages() {
    await()
        .alias("see page 1 of 2")
        .atMost(5, SECONDS)
        .until(
            () ->
                (driver.findElement(cssSelector("#page_num")).getText().equals("1"))
                    && (driver.findElement(cssSelector("#page_count")).getText().equals("2")));
  }

  public void theyShouldSeeTheShareLink() {
    await()
        .alias("see link with id 'access'")
        .atMost(5, SECONDS)
        .until(() -> (driver.findElements(cssSelector("#access")).size() > 0));
  }

  public void theyShouldSeeThatTheirEmailIsNotPartOfTheUrl() {
    assertThat(driver.getCurrentUrl(), not(StringContains.containsString("id")));
    assertThat(driver.getCurrentUrl(), not(StringContains.containsString("?")));
    assertThat(driver.getCurrentUrl(), not(StringContains.containsString("email")));
  }

  public void theyShouldSeeItsDownloadable() {
    assertThat(driver.findElements(downloadButton), hasSize(1));
  }

  public void theyShouldSeeItsNotDownloadable() {
    assertThat(driver.findElements(downloadButton), hasSize(0));
  }

  public void theyShouldSeeItsDownloaded(Path tempDir) {
    await()
        .alias("file downloaded to system")
        .atMost(1, SECONDS)
        .until(
            () -> {
              try (Stream<Path> list = Files.list(tempDir)) {
                return list.findAny().isPresent();
              }
            });
  }

  public void theySeeTheExpiryMessage() {
    await()
        .alias("text match")
        .atMost(2, SECONDS)
        .ignoreExceptions()
        .until(
            () -> {
              String body = driver.findElement(cssSelector("body")).getText();
              assertThat(body, StringContains.containsString("expired"));
              assertThat(body, StringContains.containsString("contact"));
              assertThat(body, StringContains.containsString("yours truly"));
              return true;
            });
  }
}

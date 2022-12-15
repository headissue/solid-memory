package com.headissue.feature.steps;

import static com.headissue.feature.Page.downloadButton;
import static org.openqa.selenium.By.cssSelector;

import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class When {
  private final WebDriver driver;

  public When(WebDriver driver) {
    this.driver = driver;
  }

  public void theyOpenTheTestPdfWithoutProvidingId() {
    driver.get("http://localhost:8080/00000000");
  }

  public void theySubmitAnEmail() {
    driver
        .findElement(cssSelector("input[type='email']"))
        .sendKeys("example@example.com", Keys.ENTER);
  }

  public void theyUploadPdf() {
    driver.navigate().to("http://localhost:8080/public/share");
    WebElement upload_file = driver.findElement(By.cssSelector("input[type='file']"));
    upload_file.sendKeys(
        Objects.requireNonNull(this.getClass().getResource("sample.pdf")).getPath());
    driver.findElement(By.cssSelector("button[type='submit']")).click();
  }

  public void theyClickTheShareLink() {
    driver.findElement(By.cssSelector("#access")).click();
  }

  public void theyOpenTheDownloadableTestPdfSuccessfully() {
    driver.get("http://localhost:8080/00000001");
    theySubmitAnEmail();
  }

  public void theyDownloadIt() {
    driver.findElement(downloadButton).click();
  }
}

package com.headissue.feature.steps;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.openqa.selenium.By.cssSelector;

public class When {
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

    public void theyUploadPdf() {
        driver.navigate().to("http://localhost:8080/public/share");
        WebElement upload_file = driver.findElement(By.cssSelector("input[type='file']"));
        upload_file.sendKeys(this.getClass().getResource("sample.pdf").getPath());
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }
}

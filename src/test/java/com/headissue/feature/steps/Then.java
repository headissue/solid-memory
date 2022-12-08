package com.headissue.feature.steps;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.openqa.selenium.By.cssSelector;

public class Then {
    private final WebDriver driver;

    public Then(WebDriver driver) {
        this.driver = driver;
    }

    public void theySeeThePromptToProvideEmailAddress() {
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

    public void theyShouldSeeTheShareLink() {
        await().alias("see link with id 'access'")
                .atMost(5, SECONDS)
                .until(() -> (driver.findElements(cssSelector("#access")).size() > 0));

    }

}

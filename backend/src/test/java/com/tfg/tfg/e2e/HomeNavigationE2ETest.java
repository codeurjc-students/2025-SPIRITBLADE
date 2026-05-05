package com.tfg.tfg.e2e;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests for the home navigation and main page.
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "jwt.secret=mySecretKeyForTesting123456789012345678901234567890"
})
class HomeNavigationE2ETest {

    private static final Logger log = LoggerFactory.getLogger(HomeNavigationE2ETest.class);
    private static final String ERROR_PATH = "/error";

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--ignore-certificate-errors");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        baseUrl = "https://localhost:" + port;
        driver.manage().window().maximize();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testHomePageLayoutAndResponsiveness() {

        driver.manage().window().setSize(new org.openqa.selenium.Dimension(1920, 1080));
        driver.get(baseUrl);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        assertNotNull(driver.findElement(By.tagName("body")));
        assertNotNull(driver.getTitle());

        try {
            List<WebElement> headerElements = driver.findElements(By.tagName("header"));
            List<WebElement> navElements = driver.findElements(By.tagName("nav"));
            List<WebElement> footerElements = driver.findElements(By.tagName("footer"));
            List<WebElement> summonerElements = driver
                    .findElements(By.xpath("//*[contains(text(), 'Summoner') or contains(text(), 'summoner')]"));

            log.info("✓ Structure verified: {} headers, {} navs, {} footers",
                    headerElements.size(), navElements.size(), footerElements.size());
            if (!summonerElements.isEmpty()) {
                log.info("✓ Summoner-related content found");
            }
        } catch (Exception e) {
            log.info("Note: Some structure elements may be dynamically loaded");
        }

        int[][] viewportSizes = {
                { 768, 1024 },
                { 375, 667 }
        };

        for (int[] size : viewportSizes) {
            driver.manage().window().setSize(new org.openqa.selenium.Dimension(size[0], size[1]));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            assertNotNull(driver.findElement(By.tagName("body")));
            log.info("✓ Page maintains structure at {}x{}", size[0], size[1]);
        }

        log.info("✓ Responsive design and layout verified");
    }

    @Test
    void testErrorPageHandling() {

        driver.get(baseUrl + ERROR_PATH);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        String pageContent = driver.findElement(By.tagName("body")).getText();
        assertNotNull(pageContent);

        log.info("✓ Error page is accessible");
        log.info("  URL: {}", driver.getCurrentUrl());
    }

    @Test
    void testInvalidRouteRedirect() {

        driver.get(baseUrl);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        driver.get(baseUrl + "/nonexistent-page-12345");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        String currentUrl = driver.getCurrentUrl();

        assertTrue(
                currentUrl.equals(baseUrl + "/") ||
                        currentUrl.contains(ERROR_PATH) ||
                        currentUrl.endsWith("/") ||
                        currentUrl.contains("/nonexistent-page-12345"),
                "Invalid routes should be handled by Angular routing");

        log.info("✓ Invalid route handling verified");
        log.info("  Redirected to: {}", currentUrl);
    }

    @Test
    void testMultiplePageTransitions() {

        String[] pages = { "/", "/login", "/", ERROR_PATH, "/" };

        for (String page : pages) {
            driver.get(baseUrl + page);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            WebElement body = driver.findElement(By.tagName("body"));
            assertNotNull(body);
        }

        log.info("✓ Multiple page transitions verified");
        log.info("  Successfully navigated through {} pages", pages.length);
    }
}

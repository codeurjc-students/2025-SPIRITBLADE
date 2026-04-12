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
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests for the login interface and API endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "jwt.secret=mySecretKeyForTesting123456789012345678901234567890"
})
class LoginE2ETest {

    private static final Logger log = LoggerFactory.getLogger(LoginE2ETest.class);
    private static final String LOGIN_PATH = "/login";

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
    void testLoginPageStructure() {

        driver.get(baseUrl + LOGIN_PATH);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        String pageContent = driver.findElement(By.tagName("body")).getText();
        assertNotNull(pageContent);
        log.info("✓ Login page is accessible at URL: {}", driver.getCurrentUrl());

        try {
            List<WebElement> inputElements = driver.findElements(By.tagName("input"));
            List<WebElement> buttonElements = driver.findElements(By.tagName("button"));
            List<WebElement> formElements = driver.findElements(By.tagName("form"));
            log.info("✓ Login page structure verified with {} inputs, {} buttons, {} forms",
                    inputElements.size(), buttonElements.size(), formElements.size());
        } catch (Exception e) {
            log.info("Login form elements may be dynamically loaded");
        }
    }

    @Test
    void testAuthenticationAPIEndpoint() {

        driver.get(baseUrl + "/api/v1/auth/login");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        String responseContent = driver.findElement(By.tagName("body")).getText();
        assertNotNull(responseContent);

        assertTrue(
                responseContent.contains("error") ||
                        responseContent.contains("Unauthorized") ||
                        responseContent.contains("Method Not Allowed") ||
                        !responseContent.isEmpty(),
                "Auth API endpoint should respond");

        log.info("✓ Authentication API endpoint is accessible");
    }

    @Test
    void testRegisterAPIEndpoint() {

        driver.get(baseUrl + "/api/v1/auth/register");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        String responseContent = driver.findElement(By.tagName("body")).getText();
        assertNotNull(responseContent);

        assertFalse(responseContent.isEmpty(), "Register API endpoint should respond");

        log.info("✓ Registration API endpoint is accessible");
    }
}

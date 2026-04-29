package hooks;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import base.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;

public class Hooks {

	public static WebDriver driver;
	Properties prop;

	@Before
	public void launchBrowser(Scenario scenario) throws Exception {

		System.out.println("CUCUMBER HOOK BEFORE EXECUTED");
		System.out.println("Scenario: " + scenario.getName());

		prop = new Properties();
		FileInputStream fis = new FileInputStream("src/resources/Config/config.properties");
		prop.load(fis);
		fis.close();

		/*
		 * Browser resolution priority: 1. -Dbrowser system property (set by Jenkins via
		 * pom.xml) 2. config.properties browser value This means local runs use
		 * config.properties, Jenkins runs use the parameter chosen in the pipeline UI.
		 */
		String browser = System.getProperty("browser");
		if (browser == null || browser.isBlank()) {
			browser = prop.getProperty("browser", "chrome").trim();
		}

		/*
		 * URL resolution priority: 1. -DbaseUrl system property (set by Jenkins) 2.
		 * BuyerloginUrl from config.properties
		 */
		String url = System.getProperty("baseUrl");
		if (url == null || url.isBlank()) {
			url = prop.getProperty("BuyerloginUrl");
		}

		System.out.println("[Hooks] Browser : " + browser);
		System.out.println("[Hooks] URL     : " + url);

		driver = DriverManager.initDriver(browser);
		driver.get(url);

		// Login — credentials from config.properties
		String email = prop.getProperty("EmailID", "admin@gmail.com");
		String password = prop.getProperty("password", "Blackg0ld123456#");

		driver.findElement(By.xpath("//input[@id='email']")).sendKeys(email);
		driver.findElement(By.xpath("//input[@id='password']")).sendKeys(password);
		driver.findElement(By.xpath("//button[@id='loginButton']")).click();

		System.out.println("[Hooks] Login complete");
	}

	@After
	public void closeBrowser(Scenario scenario) {
		System.out.println("CUCUMBER HOOK AFTER EXECUTED");
		System.out.println("Scenario status: " + scenario.getStatus());

		/*
		 * Screenshot on failure: - Attaches to Allure report (visible in Jenkins Allure
		 * page) - Also saves to target/screenshots/ (archived by Jenkins)
		 */
		if (scenario.isFailed() && driver != null) {
			try {
				byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

				// Attach to Allure report
				Allure.addAttachment("Failure screenshot — " + scenario.getName(), "image/png",
						new ByteArrayInputStream(screenshot), "png");

				// Save to disk for Jenkins archiveArtifacts
				String safeScenarioName = scenario.getName().replaceAll("[^a-zA-Z0-9_\\-]", "_");
				String screenshotPath = "target/screenshots/" + safeScenarioName + "_" + System.currentTimeMillis()
						+ ".png";
				new java.io.File("target/screenshots").mkdirs();
				Files.write(Paths.get(screenshotPath), screenshot);

				System.out.println("[Hooks] Screenshot saved: " + screenshotPath);

			} catch (IOException e) {
				System.out.println("[Hooks] Screenshot failed: " + e.getMessage());
			}
		}

		DriverManager.quitDriver();
	}
}

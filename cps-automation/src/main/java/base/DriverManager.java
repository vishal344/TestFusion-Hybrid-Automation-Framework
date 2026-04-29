package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class DriverManager {

	/*
	 * ThreadLocal ensures each parallel test thread gets its own WebDriver. This is
	 * the industry-standard pattern for parallel Selenium execution. For your
	 * current single-threaded setup it also works perfectly.
	 */
	private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

	public static WebDriver initDriver(String browser) {

		/*
		 * CI/CD PRIORITY ORDER: 1. System property -Dbrowser passed by Jenkins via
		 * pom.xml 2. Parameter passed directly from Hooks.java 3. Defaults to chrome
		 *
		 * isCI() detects Jenkins environment automatically. On Jenkins: headless mode
		 * is forced (no display available). On your local Eclipse: headed mode (you see
		 * the browser).
		 */
		String resolvedBrowser = System.getProperty("browser", browser);
		if (resolvedBrowser == null || resolvedBrowser.isBlank()) {
			resolvedBrowser = "chrome";
		}
		resolvedBrowser = resolvedBrowser.trim();

		boolean isCI = isRunningOnCI();

		if (resolvedBrowser.equalsIgnoreCase("chrome")) {
			WebDriverManager.chromedriver().setup();
			ChromeOptions opts = new ChromeOptions();

			if (isCI) {
				// Jenkins server has no display — must run headless
				opts.addArguments("--headless=new");
				opts.addArguments("--no-sandbox"); // required inside Docker/Jenkins
				opts.addArguments("--disable-dev-shm-usage"); // avoids /dev/shm OOM
				opts.addArguments("--disable-gpu");
				opts.addArguments("--window-size=1920,1080");
				opts.addArguments("--disable-extensions");
				System.out.println("[DriverManager] Chrome running HEADLESS (CI environment)");
			} else {
				opts.addArguments("--start-maximized");
				System.out.println("[DriverManager] Chrome running HEADED (local environment)");
			}

			driver.set(new ChromeDriver(opts));

		} else if (resolvedBrowser.equalsIgnoreCase("firefox")) {
			WebDriverManager.firefoxdriver().setup();
			FirefoxOptions opts = new FirefoxOptions();

			if (isCI) {
				opts.addArguments("--headless");
				System.out.println("[DriverManager] Firefox running HEADLESS (CI environment)");
			} else {
				System.out.println("[DriverManager] Firefox running HEADED (local environment)");
			}

			driver.set(new FirefoxDriver(opts));
		}

		if (!isCI) {
			driver.get().manage().window().maximize();
		}

		return driver.get();
	}

	public static WebDriver getDriver() {
		return driver.get();
	}

	public static void quitDriver() {
		if (driver.get() != null) {
			driver.get().quit();
			driver.remove(); // prevents ThreadLocal memory leak
		}
	}

	/**
	 * Detects if we are running inside a CI/CD environment. Jenkins automatically
	 * sets the CI=true environment variable. Also checks JENKINS_URL which Jenkins
	 * always sets.
	 */
	private static boolean isRunningOnCI() {
		return System.getenv("CI") != null || System.getenv("JENKINS_URL") != null
				|| System.getenv("BUILD_NUMBER") != null;
	}
}

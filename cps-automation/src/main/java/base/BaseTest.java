package base;


//  Currently we are using Hook file  important----- 


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class BaseTest {

    protected WebDriver driver;
    protected Properties prop;

    @Before
    public void setUp() throws IOException {

        prop = new Properties();
        FileInputStream fis =
                new FileInputStream("src/resources/Config/config.properties");
        prop.load(fis);

        // 1️⃣ Get browser from CI/CD (runtime)
        String browser = System.getProperty("browser");

        // 2️⃣ If CI/CD did NOT pass browser, use config.properties
        if (browser == null || browser.isEmpty()) {
            browser = prop.getProperty("browser");
        }

        String url = prop.getProperty("BuyerloginUrl");

        driver = DriverManager.initDriver(browser);
        driver.get(url);
    }

    @After
    public void tearDown() {
        DriverManager.quitDriver();
    }
}

package Pages;

import java.time.Duration;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Customer_page {

	WebDriver driver;

	public Customer_page(WebDriver driver) {
		this.driver = driver;

	}

	// Locators

	By adminmenu = By.xpath("//span[@class='menu-title'][normalize-space()='Admin']");
	By Customer = By.xpath("//span[contains(text(),'Customer List')]");
	By addCustomerbutton = By.xpath("//button[normalize-space()='Add Customer']");
	By fullname = By.xpath("//input[@id='fullName']");
	By Email = By.xpath("//input[@id='email']");
	By Phone = By.xpath("//input[@id='phone']");
	By City = By.xpath("//input[@id='city']");
	By Country = By.xpath("//input[@id='country']");
	By Zip = By.xpath("//input[@id='zip']");
	By Savebutton = By.xpath("//button[@id='saveButton']");

	public void clicksonAdminModule() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		wait.until(ExpectedConditions.elementToBeClickable(adminmenu)).click();
		wait.until(ExpectedConditions.elementToBeClickable(Customer)).click();
	}

	public void clicksonAddcustomerbutton() {

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
		wait.until(ExpectedConditions.elementToBeClickable(addCustomerbutton)).click();
	}

	public void enterdetails(String name, String phone, String email, String city, String country, String zip) {

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		wait.until(ExpectedConditions.visibilityOfElementLocated(fullname)).sendKeys(name);

		driver.findElement(Phone).sendKeys(phone);
		driver.findElement(Email).sendKeys(email);
		driver.findElement(City).sendKeys(city);
		driver.findElement(Country).sendKeys(country);
		driver.findElement(Zip).sendKeys(zip);

	}

	public void clickSave() {
		driver.findElement(Savebutton).click();

		 // ✅ wait for alert
	    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	    Alert alert = wait.until(ExpectedConditions.alertIsPresent());

	    // print alert (optional)
	    System.out.println("Alert message: " + alert.getText());

	    // ✅ accept alert (VERY IMPORTANT)
	    alert.accept();
	}

	public boolean isCustomerPresent(String name) {

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

		try {
			return wait
					.until(ExpectedConditions
							.visibilityOfElementLocated(By.xpath("//*[contains(text(),'" + name + "')]")))
					.isDisplayed();

		} catch (Exception e) {
			return false;
		}
	}

}

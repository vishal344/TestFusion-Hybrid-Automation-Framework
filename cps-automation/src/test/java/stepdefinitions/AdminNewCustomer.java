package stepdefinitions;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import Pages.Customer_page;
import hooks.Hooks;
import io.cucumber.java.en.*;

public class AdminNewCustomer {

	WebDriver driver;
	Customer_page page;

	String customerName;

	@Given("User is on Customer page")
	public void open_page() {
		driver = Hooks.driver;
		page = new Customer_page(driver);
		page.clicksonAdminModule();
	}

	@When("User clicks on add customer button")
	public void click_add() {
		page.clicksonAddcustomerbutton();
	}

	@When("User enters customer details")
	public void enter_details() {
		 customerName = "cust" + System.currentTimeMillis();

		page.enterdetails(customerName,  // Full Name ✅
				"9876543210", // Phone ✅
				"abcxyz@gmail.com", // Email ✅
				"Nagpur", // City ✅
				"India", // Country ✅
				"440001" // Zip (if required)
		);
	}

	@When("User clicks Save button for customer")
	public void click_save() {
		page.clickSave();
	}

	@Then("Customer should be added in list")
	public void verify() {

		System.out.println("Customer Name: " + customerName);

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

		// 🔍 Search customer first
		WebElement searchBox = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Search']")));
		searchBox.clear();
		searchBox.sendKeys(customerName);

		// ✅ Verify using better xpath
		boolean isPresent = wait
				.until(ExpectedConditions
						.visibilityOfElementLocated(By.xpath("//*[contains(text(),'" + customerName + "')]")))
				.isDisplayed();

		assert isPresent;
	}
}
package Pages;

import java.time.Duration;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import org.openqa.selenium.support.ui.WebDriverWait;

public class Create_RFQ_Popup_Page {

	WebDriver driver;
	WebDriverWait wait;

	public Create_RFQ_Popup_Page(WebDriver driver) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));

	}

	By Procurementmodule = By.xpath("//span[contains(text(),'Procurement')]");
	By RFQ = By.xpath("//span[contains(text(),'RFQ')]");
	By CreateRFQbutton = By.xpath("//button[normalize-space()='Create RFQ']");

	By PartProgramName = By.xpath("//input[@id='part_name']");
	By Buyer = By.xpath("//input[@id='buyer']");

	By Savebutton = By.xpath("//button[normalize-space()='Save']");

	// Drop down

	By Customerdropdown = By.xpath("//select[@id='customer-options']");
	By daysDropdown = By.xpath("//select[@id='time-options']");
	By expectedLaunchDropdown = By.xpath("//select[@id='launch-options']");

	By Reqdropdown = By.xpath("//select[@id='requestor-options']");

	// Date picker
	By receivedDate = By.xpath("//img[@onclick=\"openDatepicker('rfq_received_date')\"]");
	By quoteDueDate = By.xpath("//img[@onclick=\"openDatepicker('quote_due_date')\"]");
	By startDate = By.xpath("//img[@onclick=\"openDatepicker('start_date')\"]");

	/*
	 * public void ClickProcurementmodule() {
	 * 
	 * WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	 * wait.until(ExpectedConditions.elementToBeClickable(Procurementmodule)).click(
	 * );
	 * 
	 * 
	 * wait.until(ExpectedConditions.elementToBeClickable(RFQ)).click(); }
	 */

	public void ClickProcurementmodule() {

		// ✅ wait for page load
		wait.until(driver -> ((org.openqa.selenium.JavascriptExecutor) driver)
				.executeScript("return document.readyState").equals("complete"));

		// ✅ get element
		WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(Procurementmodule));

		// ✅ scroll to element
		((org.openqa.selenium.JavascriptExecutor) driver)
				.executeScript("arguments[0].scrollIntoView({block:'center'});", element);

		// ✅ try normal click
		try {
			element.click();
		} catch (Exception e) {

			// 🔥 fallback JS click (IMPORTANT)
			((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
		}

		// ✅ click RFQ
		WebElement rfqElement = wait.until(ExpectedConditions.elementToBeClickable(RFQ));

		rfqElement.click();
	}

	public void ClicksonCreateRFQbutton() {

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		wait.until(ExpectedConditions.elementToBeClickable(CreateRFQbutton)).click();
	}

	/*
	 * public void selectCustomer(String customer) { WebDriverWait wait = new
	 * WebDriverWait(driver, Duration.ofSeconds(20)); Select select = new
	 * Select(wait.until(ExpectedConditions.visibilityOfElementLocated(
	 * Customerdropdown))); select.selectByVisibleText(customer); }
	 */

	/*
	 * public void selectCustomer(String customer) {
	 * 
	 * WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	 * 
	 * // ✅ wait for dropdown fresh load
	 * wait.until(ExpectedConditions.presenceOfElementLocated(Customerdropdown));
	 * 
	 * // 🔥 re-fetch element (IMPORTANT) Select select = new
	 * Select(driver.findElement(Customerdropdown));
	 * 
	 * try { select.selectByVisibleText(customer); } catch
	 * (org.openqa.selenium.StaleElementReferenceException e) {
	 * 
	 * // 🔥 retry (core fix) select = new
	 * Select(driver.findElement(Customerdropdown));
	 * select.selectByVisibleText(customer); } }
	 */

	public void selectCustomer(String customer) {

		// Wait until the target option is present inside the dropdown
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//select[@id='customer-options']/option[normalize-space(text())='" + customer + "']")));

		// Now safely fetch and select
		for (int i = 0; i < 3; i++) {
			try {
				WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(Customerdropdown));
				Select select = new Select(dropdown);
				select.selectByVisibleText(customer);
				return;

			} catch (StaleElementReferenceException e) {
				System.out.println("Retrying due to stale element... attempt " + (i + 1));
			}
		}

		throw new RuntimeException("Failed to select customer: " + customer);
	}

	public void selectDays(String day) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		Select select = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(daysDropdown)));
		select.selectByVisibleText(day);
	}

	public void enterdetails(String PartPrograname) {

		driver.findElement(PartProgramName).sendKeys(PartPrograname);

		driver.findElement(Buyer).sendKeys("Vishalbuyer");

	}

	public void empname(String reqname) {
		Select select = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(Reqdropdown)));
		select.selectByVisibleText(reqname);

	}

	public void selectExpectedLaunch(String launch) {
		Select select = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(expectedLaunchDropdown)));
		select.selectByVisibleText(launch);
	}

	// 🔥 Common date picker handler
	public void selectDate(By locator, String day) {
		wait.until(ExpectedConditions.elementToBeClickable(locator)).click();

		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[normalize-space()='" + day + "']"))).click();
	}

	public void selectReceivedDate(String day) {
		selectDate(receivedDate, day);
	}

	public void selectStartDate(String day) {
		selectDate(startDate, day);
	}

	public void selectQuoteDueDate(String day) {

		selectDate(quoteDueDate, day);
	}

	public void clickSave() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		wait.until(ExpectedConditions.elementToBeClickable(Savebutton)).click();

		// handle alert
		try {
			Alert alert = wait.until(ExpectedConditions.alertIsPresent());
			System.out.println("Alert: " + alert.getText());
			alert.accept();
		} catch (Exception e) {
		}
	}

	public boolean isRFQCreated() {
		return driver.getPageSource().contains("RFQ");
	}

}

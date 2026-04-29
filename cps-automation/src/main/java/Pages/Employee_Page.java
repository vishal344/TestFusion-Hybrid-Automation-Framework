package Pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Employee_Page {

	WebDriver driver;

	public Employee_Page(WebDriver driver) {
		this.driver = driver;

	}

	// Locators

	By adminmenu = By.xpath("//span[@class='menu-title'][normalize-space()='Admin']");
	By addpermission = By.xpath("//span[@class='menu-title'][normalize-space()='Admin Permission'] ");
	By addemployeebtn = By.xpath("//button[normalize-space()='Add Employee']");
	By fullName = By.xpath("//input[@id='fullName']");
	By userName = By.xpath("//input[@id='userName']");
	By password = By.xpath("//input[@id='password']");
	By email = By.xpath("//input[@id='email']");
	By phone = By.xpath("//input[@id='phone']");
	By city = By.xpath("//input[@id='city']");
	By zip = By.xpath("//input[@id='zip']");
	By saveBtn = By.xpath("//button[@id='saveButton']");

	// Actions

	/*
	 * public void clicksonAdminModule() {
	 * 
	 * driver.findElement(adminmenu).click();
	 * driver.findElement(addpermission).click();
	 * 
	 * }
	 */

	public void clicksonAdminModule() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		wait.until(ExpectedConditions.elementToBeClickable(adminmenu)).click();
		wait.until(ExpectedConditions.elementToBeClickable(addpermission)).click();
	}

	public void clickAddEmployee() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		wait.until(ExpectedConditions.elementToBeClickable(addemployeebtn)).click();
		// driver.findElement(addemployeebtn).click();
	}

	public void enterDetails(String name, String user, String pass, String mail, String ph, String cityName,
			String zipCode) {

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		wait.until(ExpectedConditions.visibilityOfElementLocated(fullName)).sendKeys(name);
		driver.findElement(userName).sendKeys(user);
		driver.findElement(password).sendKeys(pass);
		driver.findElement(email).sendKeys(mail);
		driver.findElement(phone).sendKeys(ph);
		driver.findElement(city).sendKeys(cityName);
		driver.findElement(zip).sendKeys(zipCode);

	}

	public void selectRole(String role) {
		Select select = new Select(driver.findElement(By.xpath("//select[@id='role']")));
		select.selectByVisibleText(role);
	}

	/*
	 * public void clickSave() { driver.findElement(saveBtn).click(); }
	 * 
	 */

	public void clickSave() {
		driver.findElement(saveBtn).click();

		// wait for table reload
		new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.invisibilityOfElementLocated(saveBtn));
	}
	
	

	public boolean isEmployeePresent(String username) {

	    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

	    try {
	        return wait.until(ExpectedConditions.visibilityOfElementLocated(
	                By.xpath("//td[contains(text(),'" + username + "')]")
	        )).isDisplayed();

	    } catch (Exception e) {
	        return false;
	    }
	}
	
	
}

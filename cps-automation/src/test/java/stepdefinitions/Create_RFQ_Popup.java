package stepdefinitions;

import org.openqa.selenium.WebDriver;
import Pages.Create_RFQ_Popup_Page;
import hooks.Hooks;
import io.cucumber.java.en.*;

public class Create_RFQ_Popup {

	WebDriver driver;
	Create_RFQ_Popup_Page page;

	String partName = "Part_" + System.currentTimeMillis();

	@Given("User is clicks on RFQ Page")
	public void open_rfq_page() {
		driver = Hooks.driver;
		page = new Create_RFQ_Popup_Page(driver);
		page.ClickProcurementmodule();
	}

	@When("User click on create RFQ button")
	public void click_create_rfq() {
		page.ClicksonCreateRFQbutton();
	}

	/*
	 * @When("User selects Customer") public void select_customer() {
	 * page.selectCustomer("Dev"); // change based on your data }
	 */

	@When("User selects Customer {string}")
	public void select_customer(String customerName) {
		page.selectCustomer(customerName);
	}

	@When("User selects RFQ Received Date")
	public void select_received_date() {
		page.selectReceivedDate("11");
	}

	@When("User selects days")
	public void select_days() {
		page.selectDays("7 Days");
	}

	@When("User select Start date")
	public void select_start_date() {
		page.selectStartDate("11");
	}

	@When("User enters RFQ details")
	public void enter_details() {
		page.enterdetails(partName);
	}

	@When("User select Quote due date")
	public void select_due_date() {
		page.selectQuoteDueDate("10");
	}

	@When("User select Employee name")
	public void select_employee() {
		page.empname("Administrator");
	}

	@When("User select Expected Launch")
	public void selectExpectedLaunch() {
		page.selectExpectedLaunch("Q1");
	}

	@When("User clicks on save button")
	public void click_save() {
		page.clickSave();
	}

	@Then("RFQ should be in list")
	public void verify_rfq() {
		assert page.isRFQCreated();
	}
}

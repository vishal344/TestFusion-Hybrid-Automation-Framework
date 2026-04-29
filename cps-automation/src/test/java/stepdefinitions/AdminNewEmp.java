package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;

import Pages.Employee_Page;
import hooks.Hooks;

public class AdminNewEmp {

    WebDriver driver;
    Employee_Page page;

    String username = "user" + System.currentTimeMillis();

    @Given("User is on Employee page")
    public void open_page() {

        driver = Hooks.driver;                 // ✅ get driver from Hooks
        page = new Employee_Page(driver);      // ✅ pass correct driver

        page.clicksonAdminModule();            // now works
    }

    @When("User clicks Add Employee button")
    public void click_add() {
        page.clickAddEmployee();
    }

    @When("User enters employee details")
    public void enter_details() {
        page.enterDetails(
            "ABCXYZ Test",
            username,
            "Abcxyz@123",
            "abcxyz@gmail.com",
            "9876543210",
            "Nagpur",
            "440001"
        );
    }

    @When("User selects role {string}")
    public void select_Role(String role) {
        page.selectRole(role);
    }

    @When("User clicks Save button")
    public void click_save() {
        page.clickSave();
    }

    @Then("Employee should be added in list")
    public void verify() {
        assert page.isEmployeePresent(username);
    }
}
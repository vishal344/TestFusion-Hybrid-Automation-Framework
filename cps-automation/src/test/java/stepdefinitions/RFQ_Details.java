package stepdefinitions;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import Pages.RFQ_DetailsPage;
import hooks.Hooks;
import io.cucumber.java.en.*;

public class RFQ_Details {
    WebDriver driver;
    RFQ_DetailsPage page;

    @Given("User is on the RFQ details page")
    public void openPage() {
        driver = Hooks.driver;
        page = new RFQ_DetailsPage(driver);
        page.navigateToRFQPage();
    }

    @When("User clicks on {string} button")
    public void clickButton(String name) {
        switch (name) {
        case "Add Row":
            page.clickAddRow();
            break;
        case "Send Email":
            page.clickSendEmail();
            break;
        default:
            throw new RuntimeException("Unknown button: " + name);
        }
    }

    @When("User enters item details from Excel sheet {string}")
    public void enterData(String filePath) throws Exception {
        System.out.println("Reading Excel from: " + filePath);
        String[][] data = page.readExcelData(filePath);
        System.out.println("Total rows to process: " + data.length);
        page.discoverTdIndices();
        for (int i = 0; i < data.length; i++) {
            int excelRowIndex = i + 1;
            System.out.println("\n=== Processing row " + excelRowIndex + " | Part: " + data[i][0] + " ===");
            if (i > 0)
                page.clickAddRow();
            page.fillRowData(data[i], excelRowIndex);
        }
    }

    @When("User creates a new version")
    public void createNewVersion() {
        page.handleCreateVersionPopup();
    }

    @When("User adds a note {string}")
    public void addNote(String noteText) {
        page.handleNotesPopup(noteText);
    }

    @When("User selects vendors {string}")
    public void selectVendors(String vendorsCsv) {
        page.selectVendors(vendorsCsv.split(","));
    }

    @When("User clicks on vendor save button")
    public void clickVendorSaveButton() {
        page.clickVendorSaveButton();
    }

    @When("User sends the email")
    public void sendEmail() {
        page.clickSendEmailInCompose();
    }

    @Then("item should be added and email sent to vendor successfully")
    public void verify() {
        Assert.assertTrue(page.isEmailSentSuccessfully(), "Email was NOT sent successfully to vendor");
    }
}
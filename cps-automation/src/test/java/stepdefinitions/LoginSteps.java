package stepdefinitions;

import io.cucumber.java.en.Given;

public class LoginSteps  {

    @Given("user opens application")
    public void user_opens_application() {
        System.out.println("Application opened successfully");
    }
}

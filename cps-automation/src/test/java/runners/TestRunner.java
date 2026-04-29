package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/*
 * TestRunner — runs the full RFQ test suite.
 *
 * plugin additions for CI/CD:
 *   allure:target/allure-results   → generates Allure data picked up by Jenkins
 *   json:target/cucumber-reports/  → JSON for masterthought HTML report
 *   html:target/cucumber-reports/  → existing HTML report (kept)
 *   junit:target/surefire-reports/ → XML for Jenkins Quality Gate + JUnit graph
 *
 * Tags can be overridden at runtime:
 *   mvn test -Dcucumber.filter.tags="@Smoke"
 */
@CucumberOptions(
        features = "src/resources/feature",
        glue     = {"stepdefinitions", "hooks"},
        tags     = "@RFQDetailsModule",
        plugin   = {
                "pretty",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json",
                "junit:target/surefire-reports/cucumber-junit.xml",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        }
)
public class TestRunner extends AbstractTestNGCucumberTests {
}


package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/*
 * SmokeRunner — runs only @Smoke tagged scenarios (login.feature).
 * Used by testng-smoke.xml for fast sanity checks on every push.
 * Completes in ~2 minutes.
 */
@CucumberOptions(features = "src/resources/feature", glue = { "stepdefinitions", "hooks" }, tags = "@Smoke", plugin = {
		"pretty", "html:target/cucumber-reports/smoke.html", "json:target/cucumber-reports/smoke.json",
		"junit:target/surefire-reports/smoke-junit.xml", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" })
public class SmokeRunner extends AbstractTestNGCucumberTests {
}

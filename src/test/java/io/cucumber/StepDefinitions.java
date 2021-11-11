package io.cucumber;

import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import io.cucumber.Hook;
import com.perfecto.reportium.client.ReportiumClient;

import cucumber.api.java.en.Given;

public class StepDefinitions extends AbstractJunit {
	private WebDriver driver = getDriver();
	protected ReportiumClient reportiumClient = AbstractJunit.getReportiumClient();


	@Given("^I check perfecto$")
	public void check_perfecto() throws InterruptedException {
		reportiumClient.stepStart("browser navigate to perfecto");
		driver.get("https://www.perfecto.io");
		String aTitle = driver.getTitle();
		// compare the actual title with the expected title
		Assert.assertTrue("Title verified as expected",
				aTitle.equals("Web & Mobile App Testing | Continuous Testing | Perfecto"));
		reportiumClient.stepEnd();
	}
	
	@Given("^I fail perfecto test$")
	public void fail_perfecto() throws InterruptedException {
		reportiumClient.stepStart("browser navigate to perfecto");
		driver.get("https://www.perfecto.io");
		String aTitle = driver.getTitle();
		// compare the actual title with the expected title
		Assert.assertTrue("Title failed as expected",
				aTitle.equals("WRONG TITLE"));
		reportiumClient.stepEnd();
	}

}

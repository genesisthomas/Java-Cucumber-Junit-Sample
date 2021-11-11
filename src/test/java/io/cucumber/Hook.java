package io.cucumber;

import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

import com.perfecto.reportium.test.TestContext;
import com.perfecto.reportium.test.result.TestResultFactory;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;

public class Hook extends AbstractJunit {
	@Before
	public void setUp(Scenario scenario) throws MalformedURLException {
		initScenario();
		addScenario(scenario.getName());
		driver = createRemoteDriver();
		driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);
		setDriver(driver);
		setReportiumClient(createRemoteReportiumClient(driver));
		@SuppressWarnings({ "unchecked", "rawtypes" })
		TestContext testContext = new TestContext.Builder().withTestExecutionTags().withCustomFields().build();
		getReportiumClient().testStart(getScenario(), testContext);
	}

	@After
	public void tearDown(Scenario scenario) throws Exception {
		driver = getDriver();
		if (scenario.isFailed()) {
			Throwable error = getError(scenario);
			if (getReportiumClient() != null) {
				if (error != null && getReportiumClient() != null) {
					getReportiumClient().testStop(TestResultFactory.createFailure("An error occurred", error));
				} else {
					getReportiumClient().testStop(TestResultFactory.createFailure("An error occurred",
							new RuntimeException("Error while executing")));
				}
			}
		} else if (scenario.getStatus().toString().toLowerCase().equals("passed")) {
			if (getReportiumClient() != null) {
				getReportiumClient().testStop(TestResultFactory.createSuccess());
			}
		}
		System.out.println("Report URL: " + getReportiumClient().getReportUrl());
		driver.quit();
	}

}

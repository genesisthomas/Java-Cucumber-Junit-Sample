package io.cucumber;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;

import com.perfecto.reportium.client.ReportiumClient;
import com.perfecto.reportium.client.ReportiumClientFactory;
import com.perfecto.reportium.model.Job;
import com.perfecto.reportium.model.PerfectoExecutionContext;
import com.perfecto.reportium.model.Project;

import cucumber.api.Scenario;
import cucumber.runtime.ScenarioImpl;
import gherkin.formatter.model.Result;

/**
 * Base test for JUnit tests.
 * <p>
 * This class handles the connection to Perfecto reporting and seamlessly
 * reports test start and end to reduce boilerplate code in the actual test
 * cases.
 */
public class AbstractJunit {
	public static WebDriver driver;
	private static final ThreadLocal<WebDriver> threadLocal = new ThreadLocal<WebDriver>();
	private static final ThreadLocal<ReportiumClient> reportThreadLocal = new ThreadLocal<ReportiumClient>();
	private static HashMap<Integer, String> scenarios;

	protected static Throwable getError(Scenario scenario) throws Exception {
		Field field = FieldUtils.getField(((ScenarioImpl) scenario).getClass(), "stepResults", true);
		field.setAccessible(true);
		try {
			Throwable error = null;
			ArrayList<Result> results = (ArrayList<Result>) field.get(scenario);
			for (Result result : results) {
				if (result.getError() != null & result.getErrorMessage() != null) {
					error = result.getError();
				}
			}
			return error;
		} catch (Exception e) {
			System.out.println("Error while logging error: " + e);
		}
		return null;
	}

	public static WebDriver getDriver() {
		return threadLocal.get();
	}

	public static void setDriver(WebDriver driver) {
		threadLocal.set(driver);
	}

	public static void setReportiumClient(ReportiumClient report) {
		reportThreadLocal.set(report);
	}

	public static ReportiumClient getReportiumClient() {
		return reportThreadLocal.get();
	}

	public void initScenario() {
		if (scenarios == null)
			scenarios = new HashMap<Integer, String>();
	}

	protected String getScenario() {
		Thread currentThread = Thread.currentThread();
		int threadID = currentThread.hashCode();
		return scenarios.get(threadID);
	}

	protected void addScenario(String scenario) {
		Thread currentThread = Thread.currentThread();
		int threadID = currentThread.hashCode();
		System.out.println(currentThread + " : " + scenario);
		scenarios.put(threadID, scenario);
	}

	/**
	 * Creates a {@link org.openqa.selenium.remote.RemoteWebDriver} instance for
	 * executing the Selenium script
	 *
	 * @return Webdriver to use for this test
	 * @throws MalformedURLException
	 */
	protected static WebDriver createRemoteDriver() throws MalformedURLException {
		// Read connection details from env variables that are injected during the build
		// process
		// to prevent them from being leaked here
		String cloudName = System.getProperty("cloudName", "ps");
		String securityToken = System.getProperty("securityToken");
	
		DesiredCapabilities capabilities = new DesiredCapabilities("", "", Platform.ANY);

		capabilities.setCapability("securityToken", securityToken);
		capabilities.setCapability("platformName", "Windows");
		capabilities.setCapability("platformVersion", "11");
		capabilities.setCapability("browserName", "Chrome");
		capabilities.setCapability("browserVersion", "95");
		capabilities.setCapability("location", "AP Sydney");
		capabilities.setCapability("resolution", "1024x768");

		WebDriver driver = new org.openqa.selenium.remote.RemoteWebDriver(
				new HttpCommandExecutor(
						new URL("https://" + cloudName + ".perfectomobile.com/nexperience/perfectomobile/wd/hub")),
				capabilities);

		return driver;
	}

	protected static ReportiumClient createRemoteReportiumClient(WebDriver driver) {
		PerfectoExecutionContext perfectoExecutionContext = null;
		if (System.getProperty("reportium-job-name") != null) {
			String branch = System.getProperty("reportium-job-branch") == null ? "local"
					: System.getProperty("reportium-job-branch");
			perfectoExecutionContext = new PerfectoExecutionContext.PerfectoExecutionContextBuilder()
					.withProject(new Project("Automation", "1.0"))
					.withJob(new Job(System.getProperty("reportium-job-name"),
							Integer.parseInt(System.getProperty("reportium-job-number"))).withBranch(branch))
					.withWebDriver(driver).build();
		} else {
			perfectoExecutionContext = new PerfectoExecutionContext.PerfectoExecutionContextBuilder()
					.withProject(new Project("Automation", "1.0"))
					// .withJob(new Job(System.getProperty("JOB_NAME"),
					// Integer.parseInt(System.getProperty("JOB_NUMBER"))).withBranch("master"))
					.withWebDriver(driver).build();
		}
		return new ReportiumClientFactory().createPerfectoReportiumClient(perfectoExecutionContext);
	}
}
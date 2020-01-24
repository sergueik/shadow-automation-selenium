package io.github.sukgu;

import static java.lang.System.err;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assume;
import org.junit.Ignore;

// https://www.baeldung.com/junit-before-beforeclass-beforeeach-beforeall

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.ElementNotVisibleException;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.sukgu.Shadow;

// the tests will get skipped when run on Firefox
public class SettingsTest {

	private final static String baseUrl = "chrome://settings/";
	private static String urlLocator = null;
	private static String shadowLocator = null;
	private static String shadow2Locator = null;
	private static final boolean debug = Boolean.parseBoolean(getPropertyEnv("DEBUG", "false"));;

	private static boolean isCIBuild = checkEnvironment();

	private static WebDriver driver = null;
	public Actions actions;
	private static Shadow shadow = null;
	private static String browser = getPropertyEnv("BROWSER", getPropertyEnv("webdriver.driver", "chrome"));

	private static final BrowserChecker browserChecker = new BrowserChecker(browser);
	// export BROWSER=firefox or specify profile -Pfirefox to override

	@SuppressWarnings("unused")
	private static final boolean headless = Boolean.parseBoolean(getPropertyEnv("HEADLESS", "false"));

	@BeforeAll
	public static void injectShadowJS() {

		err.println("Launching " + browser);
		if (browser.equals("chrome")) {
			WebDriverManager.chromedriver().setup();
			driver = new ChromeDriver();
		}
		if (browser.equals("firefox")) {
			WebDriverManager.firefoxdriver().setup();
			driver = new FirefoxDriver();
		} // TODO: finish for other browsers
		shadow = new Shadow(driver);
		shadow.setDebug(debug);
	}

	@BeforeEach
	public void init() {
		driver.navigate().to("about:blank");
	}

	@Test
	public void test1() {
		urlLocator = "#basicPage > settings-section[page-title=\"Search engine\"]";
		shadowLocator = "#card";

		Assumptions.assumeTrue(browser.equals("chrome"));
		driver.navigate().to(baseUrl);
		List<WebElement> elements = shadow.findElements(urlLocator);
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		err.println(String.format("Located %d %s elements:", elements.size(), urlLocator));
		// NOTE: default toString() is not be particularly useful
		elements.stream().forEach(err::println);
		elements.stream().map(o -> o.getTagName()).forEach(err::println);
		elements.stream().map(o -> String.format("innerHTML: %s", o.getAttribute("innerHTML"))).forEach(err::println);
		elements.stream().map(o -> String.format("outerHTML: %s", o.getAttribute("outerHTML"))).forEach(err::println);
	}

	@Test
	public void test2() {
		Assumptions.assumeTrue(browserChecker.testingChrome());
		urlLocator = "#basicPage > settings-section[page-title=\"Search engine\"]";
		shadowLocator = "#card";
		driver.navigate().to(baseUrl);
		WebElement element = shadow.findElement(urlLocator);
		err.println(String.format("outerHTML: %s", element.getAttribute("outerHTML")));
		List<WebElement> elements = shadow.findElements(element, shadowLocator);
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		err.println(String.format("Found %d elements: ", elements.size()));
		elements.stream().map(o -> String.format("outerHTML: %s", o.getAttribute("outerHTML"))).forEach(err::println);
	}

	// @Disabled("Disabled until execptions   of element is addressed")
	@Test
	public void test3() {
		Assumptions.assumeTrue(browserChecker.testingChrome());
		urlLocator = "#basicPage > settings-section[page-title=\"Default browser\"]";
		shadowLocator = "settings-default-browser-page";
		shadow2Locator = "div#canBeDefaultBrowser";
		driver.navigate().to(baseUrl);
		WebElement element = shadow.findElement(urlLocator);
		err.println(String.format("outerHTML: %s", element.getAttribute("outerHTML")));
		try {
			actions = new Actions(driver);
			actions.moveToElement(element).build().perform();
			sleep(1000);
			actions.click().build().perform();
			sleep(1000);
			element.click();
		} catch (ElementClickInterceptedException e) {
			// element click intercepted: Element is not clickable at point

		}
		sleep(1000);
		// shadowLocator = "*"; // anything! - does not work either
		// NOTE: hanging the browser!
		try {
			WebElement element2 = shadow.findElement(element, shadowLocator);
			assertThat(element2, notNullValue());
			WebElement element3 = shadow.findElement(element2, shadow2Locator);
			assertThat(element3, notNullValue());
		} catch (ElementNotVisibleException e) {
			// Element with CSS settings-default-browser-page is not present on screen
		}
	}

	@AfterEach
	public void tearDown() {
	}

	@AfterAll
	public static void tearDownAll() {
		driver.close();
	}

	public static String getPropertyEnv(String name, String defaultValue) {
		String value = System.getProperty(name);
		if (debug) {
			err.println("system property " + name + " = " + value);
		}
		if (value == null || value.length() == 0) {
			value = System.getenv(name);
			if (debug) {
				err.println("system env " + name + " = " + value);
			}
			if (value == null || value.length() == 0) {
				value = defaultValue;
				if (debug) {
					err.println("default value  = " + value);
				}
			}
		}
		return value;
	}

	public static boolean checkEnvironment() {
		Map<String, String> env = System.getenv();
		boolean result = false;
		if (env.containsKey("TRAVIS") && env.get("TRAVIS").equals("true")) {
			result = true;
		}
		return result;
	}

	// origin: https://reflectoring.io/conditional-junit4-junit5-tests/
	// probably an overkill
	public static class BrowserChecker {
		private String browser;

		public BrowserChecker(String browser) {
			this.browser = browser;
		}

		public boolean testingChrome() {
			return (this.browser.equals("chrome"));
		}
	}

	public void sleep(Integer milliSeconds) {
		try {
			Thread.sleep((long) milliSeconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}

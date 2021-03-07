package io.github.sukgu;

import static java.lang.System.err;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.util.ArrayList;
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
public class ChromeSettingsTest extends BaseTest {

	private final static String baseUrl = "chrome://settings/";
	private static String urlLocator = null;
	private static String shadowLocator = null;
	private static String shadow2Locator = null;
	private List<WebElement> elements = new ArrayList<>();
	private WebElement element = null;
	private WebElement element2 = null;
	private WebElement element3 = null;

	private static final BrowserChecker browserChecker = new BrowserChecker(
			getBrowser());
	// export BROWSER=firefox or specify profile -Pfirefox to override

	@BeforeEach
	public void init() {
		driver.navigate().to("about:blank");
	}

	@Test
	public void test1() {
		urlLocator = "#basicPage > settings-section[page-title=\"Search engine\"]";
		shadowLocator = "#card";

		Assumptions.assumeTrue(getBrowser().equals("chrome"));
		driver.navigate().to(baseUrl);
		elements = shadow.findElements(urlLocator);
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		err.println(
				String.format("Located %d %s elements:", elements.size(), urlLocator));
		// NOTE: default toString() is not be particularly useful
		elements.stream().forEach(err::println);
		elements.stream().map(o -> o.getTagName()).forEach(err::println);
		elements.stream()
				.map(o -> String.format("innerHTML: %s", o.getAttribute("innerHTML")))
				.forEach(err::println);
		elements.stream()
				.map(o -> String.format("outerHTML: %s", o.getAttribute("outerHTML")))
				.forEach(err::println);
	}

	@Test
	public void test2() {
		Assumptions.assumeTrue(browserChecker.testingChrome());
		urlLocator = "#basicPage > settings-section[page-title=\"Search engine\"]";
		shadowLocator = "#card";
		driver.navigate().to(baseUrl);
		element = shadow.findElement(urlLocator);
		err.println(
				String.format("outerHTML: %s", element.getAttribute("outerHTML")));
		elements = shadow.findElements(element, shadowLocator);
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		err.println(String.format("Found %d elements: ", elements.size()));
		elements.stream()
				.map(o -> String.format("outerHTML: %s", o.getAttribute("outerHTML")))
				.forEach(err::println);
	}

	// @Disabled("Disabled until execptions of element is addressed")
	@Test
	public void test3() {
		Assumptions.assumeTrue(browserChecker.testingChrome());
		urlLocator = "#basicPage > settings-section[page-title=\"Default browser\"]";
		shadowLocator = "settings-default-browser-page";
		shadow2Locator = "div#canBeDefaultBrowser";
		driver.navigate().to(baseUrl);
		element = shadow.findElement(urlLocator);
		err.println(
				String.format("outerHTML: %s", element.getAttribute("outerHTML")));
		try {
			/*
			 * 
			 * actions.moveToElement(element).build().perform(); sleep(1000);
			 * actions.click().build().perform(); sleep(1000);
			 */
			element.click();
			sleep(1000);
		} catch (ElementClickInterceptedException e) {
			System.err.println("Exception (ignored): " + e.getMessage());
			// element click intercepted: Element is not clickable at point
		}
		// shadowLocator = "*";
		// anything! - does not work either
		try {
			element2 = shadow.findElement(element, shadowLocator);
			assertThat(element2, notNullValue());
			element3 = shadow.findElement(element2, shadow2Locator);
			assertThat(element3, notNullValue());
		} catch (ElementNotVisibleException e) {
			System.err.println("Exception (ignored): " + e.getMessage());
			// Element with CSS settings-default-browser-page is not present on screen
		}
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

}

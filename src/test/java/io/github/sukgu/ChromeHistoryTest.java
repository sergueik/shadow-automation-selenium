package io.github.sukgu;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assume;
import org.junit.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.sukgu.BaseTest;

// this test methods will get skipped when run on Firefox
public class ChromeHistoryTest extends BaseTest {

	private static final String url = "chrome://history/";
	private final String site = "www.wikipedia.org";

	private static List<WebElement> elements = new ArrayList<>();
	private static WebElement element = null;
	private static WebElement element2 = null;
	private static String html = null;

	// using Assumptions in @Before and @After is not the best practice
	@BeforeEach
	public void browse() {
		Assumptions.assumeTrue(BaseTest.browserChecker.testingChrome());
		Assumptions.assumeFalse(isCIBuild);
		driver.navigate().to(String.format("https://%s", site));
	}

	@Test
	public void test1() {
		Assumptions.assumeTrue(browserChecker.testingChrome());
		Assumptions.assumeFalse(isCIBuild);
		Assumptions.assumeFalse(headless);

		driver.navigate().to(url);
		element = driver.findElement(By.cssSelector("#history-app"));
		elements = shadow.getAllShadowElement(element, "#main-container #content");
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		element = elements.get(0);
		if (debug)
			System.err
					.println("Element(1) HTML: " + element.getAttribute("innerHTML"));
		element2 = element.findElement(By.cssSelector("#history"));

		elements = shadow.getAllShadowElement(element2,
				".history-cards history-item");
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		element2 = elements.get(0);
		assertThat(element2, notNullValue());
		if (debug)
			System.err
					.println("Element(2) HTML: " + element2.getAttribute("outerHTML"));
		elements = shadow.getAllShadowElement(element2, "#main-container");
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		element = elements.get(0);
		if (debug)
			System.err
					.println("Element(3) HTML: " + element.getAttribute("outerHTML"));
		elements = element.findElements(By.cssSelector("#date-accessed"));
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		element2 = elements.get(0);
		assertThat(element2, notNullValue());
		assertThat(element2.getText(), containsString("Today"));
		System.err.println("Element(4) text: " + element2.getText());
		elements = element.findElements(By.cssSelector("#title-and-domain"));
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		element2 = elements.get(0);
		assertThat(element2, notNullValue());
		if (debug)
			System.err
					.println("Element(5) HTML: " + element2.getAttribute("outerHTML"));
		elements = element2.findElements(By.cssSelector("#domain"));
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		element2 = elements.get(0);
		assertThat(element2.getText(), containsString(site));
		System.err.println("Element(6) text: " + element2.getText());
	}

}

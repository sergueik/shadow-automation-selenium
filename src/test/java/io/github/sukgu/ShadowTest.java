package io.github.sukgu;

import static java.lang.System.err;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// https://www.baeldung.com/junit-before-beforeclass-beforeeach-beforeall
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class ShadowTest extends BaseTest {

	private final static String baseUrl = "https://www.virustotal.com";
	private static final String urlLocator = "*[data-route='url']";

	@BeforeEach
	public void init() {
		driver.navigate().to(baseUrl);

	}

	@Test
	public void testJSInjection() {
		WebElement element = shadow.findElement(urlLocator);
		err.println(element);
		String pageSource = shadow.getDriver().getPageSource();
		assertThat(pageSource, notNullValue());
	}

	@Test
	public void testGetAllObject() {
		List<WebElement> elements = shadow.findElements(urlLocator);
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		err.println(String.format("Found %d elements:", elements.size()));
		/*
		 * elements.stream().forEach(err::println); elements.stream().map(o ->
		 * o.getTagName()).forEach(err::println); elements.stream() .map(o ->
		 * String.format("innerHTML: %s", o.getAttribute("innerHTML")))
		 * .forEach(err::println);
		 */
		elements.stream()
				.map(o -> String.format("outerHTML: %s", o.getAttribute("outerHTML")))
				.forEach(err::println);
	}

	@Test
	public void testAPICalls1() {
		WebElement element = shadow.findElements(urlLocator).stream()
				.filter(o -> o.getTagName().matches("div")).collect(Collectors.toList())
				.get(0);

		WebElement element1 = shadow.getNextSiblingElement(element);
		assertThat(element1, notNullValue());
		// TODO: compare siblings
	}

	@Test
	public void testAPICalls2() {
		WebElement element = shadow.findElements(urlLocator).stream()
				.filter(o -> o.getTagName().matches("div")).collect(Collectors.toList())
				.get(0);
		List<WebElement> elements = shadow.findElements(element, "img");
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
	}

	@Test
	public void testAPICalls3() {
		WebElement element = shadow.findElement(urlLocator);
		List<WebElement> elements = shadow.getSiblingElements(element);
		assertThat(elements, notNullValue());
		// assertThat(elements.size(), greaterThan(0));
	}

	@Disabled("Disabled until getChildElements javascript error: Illegal invocation is addressed")
	@Test
	public void testAPICalls4() {
		WebElement element = shadow.findElement(urlLocator);
		List<WebElement> elements = shadow.getChildElements(element);
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
	}

	@Test
	public void testAPICalls5() {
		List<WebElement> elements = shadow
				.findElements(shadow.findElement(urlLocator), "#wrapperLink");
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		err.println(String.format("Found %d elements: ", elements.size()));
		elements.stream()
				.map(o -> String.format("outerHTML: %s", o.getAttribute("outerHTML")))
				.forEach(err::println);
	}

}

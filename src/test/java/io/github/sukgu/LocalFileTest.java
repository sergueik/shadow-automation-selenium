package io.github.sukgu;

import static java.lang.System.err;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class LocalFileTest extends BaseTest {

	@AfterEach
	public void AfterMethod() {
		driver.get("about:blank");
	}

	@Test
	public void test1() {
		driver.navigate().to(getPageContent("index.html"));
		WebElement element = shadow.findElement("#container");
		List<WebElement> elements = shadow.getAllShadowElement(element, "#inside");
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		err.println(String.format("Located %d shadow document elements:", elements.size()));
		elements.stream().map(o -> String.format("outerHTML: %s", o.getAttribute("outerHTML"))).forEach(err::println);
	}

	@Test
	public void test2() {
		driver.navigate().to(getPageContent("button.html"));
		WebElement element = shadow.findElement("button");
		shadow.scrollTo(element);
		assertThat(element, notNullValue());
		err.println("outerTML: " + shadow.getAttribute(element, "outerHTML"));
		List<WebElement> elements = shadow.getChildElements(element);
		assertThat(elements, notNullValue());
		err.println(elements);
	}

	@Disabled("Disabled until com.google.common.collect.Maps$TransformedEntriesMap cannot be cast to org.openqa.selenium.WebElement is addressed")
	@Test
	public void test3() {
		driver.navigate().to(getPageContent("button.html"));
		WebElement element = shadow.findElement("button");
		shadow.scrollTo(element);
		assertThat(element, notNullValue());
		err.println("outerTML: " + shadow.getAttribute(element, "outerHTML"));
		List<WebElement> elements = shadow.getChildElements(element);
		assertThat(elements, notNullValue());
		err.println(elements);

		element = elements.get(0);
		err.println(element);
	}

	@Disabled("Disabled until getShadowElement javascript error: Cannot read property 'querySelector' of null is addressed")
	@Test
	public void test4() {

		driver.navigate().to(getPageContent("button.html"));
		WebElement parent = shadow.findElement("body");
		assertThat(parent, notNullValue());
		// Cannot read property 'querySelector' of null
		WebElement element = shadow.getShadowElement(parent, "button");
		assertThat(element, notNullValue());
	}

	@Disabled
	@Test
	public void test5() {
		driver.navigate().to(getPageContent("inner_html_example.html"));
		WebElement element = driver.findElement(By.cssSelector("body > div > h3"));
		assertThat(element, notNullValue());
		err.println("outerHTML: " + element.getAttribute("outerHTML"));
		err.println(String.format("Text: \"%s\"", element.getText()));
		// TODO: assert
		assertThat(element.getText(), is(""));
		WebElement parent = shadow.findElement("body > div");
		assertThat(parent, notNullValue());
		err.println("Parent outerHTML: " + parent.getAttribute("outerHTML"));
		err.println(String.format("Parent text(old API): \"%s\"", parent.getText()));
		element = null;
		element = shadow.getShadowElement(parent, "h3");
		assertThat(element, notNullValue());
		err.println("Got shadow element: " + element); // toString
		err.println("outerHTML (old API): " + element.getAttribute("outerHTML"));
		err.println("outerHTML (new API): " + shadow.getAttribute(element, "outerHTML"));
		err.println(String.format("Text(old API): \"%s\"", element.getText()));
		err.println("Text(new API): " + shadow.getAttribute(element, "value"));

		List<WebElement> elements = shadow.getAllShadowElement(parent, "h3");
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		err.println(String.format("Located %d shadow document elements:", elements.size()));
		elements.stream().map(e -> String.format("outerHTML (new API): %s", shadow.getAttribute(e, "outerHTML")))
				.forEach(err::println);
		elements.stream().map(e -> String.format("outerHTML (old API): %s", e.getAttribute("outerHTML")))
				.forEach(err::println);
		elements.stream().map(e -> String.format("Text (old API): \"%s\"", e.getText())).forEach(err::println);

	}

	@AfterAll
	public static void tearDownAll() {
		driver.close();
	}

}

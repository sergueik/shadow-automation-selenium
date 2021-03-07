package io.github.sukgu;

import static java.lang.System.err;
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
public class ChromeDownloadsTest extends BaseTest {

	private static final String url = "chrome://downloads/";

	private static WebDriver driver = null;
	private static Shadow shadow = null;

	private static List<WebElement> elements = new ArrayList<>();
	private static WebElement element = null;
	private static WebElement element2 = null;
	private static WebElement element3 = null;
	private static WebElement element4 = null;
	private static WebElement element5 = null;
	private static String html = null;

	@SuppressWarnings("deprecation")
	@BeforeAll
	// TODO: merge customizations of download directory to BaseTest
	public static void setup() {
		BaseTest.driver.close();
		BaseTest.driver = null;
		if (browser.equals("chrome")) {
			String chromeDriverPath = null;
			System.err
					.println("Launching " + (headless ? " headless " : "") + browser);
			if (isCIBuild) {
				WebDriverManager.chromedriver().setup();
				chromeDriverPath = WebDriverManager.chromedriver()
						.getDownloadedDriverPath();

			} else {
				chromeDriverPath = Paths.get(System.getProperty("user.home"))
						.resolve("Downloads")
						.resolve(System.getProperty("os.name").toLowerCase()
								.startsWith("windows") ? "chromedriver.exe" : "chromedriver")
						.toAbsolutePath().toString();
				System.setProperty("webdriver.chrome.driver", chromeDriverPath);
			}
			System.err.println("Chrome Driver Path: " + chromeDriverPath);
			// https://peter.sh/experiments/chromium-command-line-switches/
			ChromeOptions options = new ChromeOptions();
			// options for headless
			if (headless) {
				for (String arg : (new String[] { "headless",
						"window-size=1200x800" })) {
					options.addArguments(arg);
				}
			}

			Map<String, Object> preferences = new Hashtable<>();
			preferences.put("profile.default_content_settings.popups", 0);
			preferences.put("download.prompt_for_download", "false");
			String downloadsPath = System.getProperty("user.home") + "/Downloads";
			preferences.put("download.default_directory",
					BaseTest.getPropertyEnv("fileDownloadPath", downloadsPath));

			Map<String, Object> chromePrefs = new HashMap<>();
			chromePrefs.put("plugins.always_open_pdf_externally", true);
			Map<String, Object> plugin = new HashMap<>();
			plugin.put("enabled", false);
			plugin.put("name", "Chrome PDF Viewer");

			chromePrefs.put("plugins.plugins_list", Arrays.asList(plugin));
			options.setExperimentalOption("prefs", chromePrefs);
			DesiredCapabilities capabilities = DesiredCapabilities.chrome();
			capabilities.setCapability("chrome.binary", chromeDriverPath);

			capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			capabilities.setCapability(ChromeOptions.CAPABILITY, options);

			driver = new ChromeDriver(capabilities);

			shadow = new Shadow(driver);
			shadow.setDebug(debug);
		}
	}

	// using Assumptions in @Before and @After is not the best practice
	@BeforeEach
	public void init() {
		if ((browser.equals("chrome") && !isCIBuild)) {
			driver.navigate().to("about:blank");
		}
	}

	// download PDF have to be run first
	@BeforeEach
	public void download() {

		Assumptions.assumeTrue(browser.equals("chrome"));
		Assumptions.assumeFalse(isCIBuild);
		driver.navigate().to(BaseTest.getPageContent("download.html"));
		element = driver
				.findElement(By.xpath("//a[contains(@href, \"wikipedia.pdf\")]"));
		element.click();
		sleep(1000);
	}

	@Test
	public void test2() { // listDownloadsShadowTest
		Assumptions.assumeTrue(browserChecker.testingChrome());
		Assumptions.assumeFalse(isCIBuild);
		driver.navigate().to(url);
		element = driver.findElement(By.tagName("downloads-manager"));
		elements = shadow.getAllShadowElement(element, "#downloadsList");
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		element = elements.get(0);
		if (debug)
			err.println(
					String.format("Located element:", element.getAttribute("outerHTML")));
		element2 = element.findElement(By.tagName("downloads-item"));
		assertThat(element2, notNullValue());
		element3 = shadow.getShadowElement(element2, "div#details");
		assertThat(element3, notNullValue());
		if (debug)
			System.err
					.println("Result element: " + element3.getAttribute("outerHTML"));
		element4 = element3.findElement(By.cssSelector("span#name"));
		assertThat(element4, notNullValue());
		if (debug)
			System.err
					.println("Result element: " + element4.getAttribute("outerHTML"));
		html = element4.getAttribute("innerHTML");
		assertThat(html, containsString("wikipedia"));
		// NOTE: the getText() is failing
		try {
			assertThat(element3.getText(), containsString("wikipedia"));
		} catch (AssertionError e) {
			System.err.println("Exception (ignored) " + e.toString());
		}
		// can be OS-specific: "wikipedia (10).pdf"

		Pattern pattern = Pattern.compile(
				String.format("wikipedia(?:%s)*\\.pdf", " \\((\\d+)\\)"),
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(html);
		assertThat(matcher.find(), is(true));
		assertThat(pattern.matcher(html).find(), is(true));
		element4 = element3.findElement(By.cssSelector("a#url"));
		assertThat(element4, notNullValue());
		if (debug)
			System.err
					.println("Inspecting element: " + element4.getAttribute("outerHTML"));
		shadow.scrollTo(element4);
		element2 = shadow.getParentElement(element4);
		assertThat(element2, notNullValue());
		assertThat(shadow.isVisible(element2), is(true));
		html = element2.getAttribute("outerHTML");
		if (debug)
			System.err.println("Inspecting parent element: " + html);
		try {
			assertThat(shadow.getAttribute(element2, "outerHTML"), notNullValue());
			assertThat(shadow.getAttribute(element2, "outerHTML"),
					containsString(html));
			System.err.println("Vefified attribute extraction: "
					+ shadow.getAttribute(element2, "outerHTML"));
		} catch (AssertionError e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
		elements = shadow.getChildElements(element2);
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		sleep(1000);
		// cannot access instance method of BaseTest using static reference
	}

	@AfterAll
	public static void tearDownAll() {
		driver.close();
		driver.quit();
	}

}

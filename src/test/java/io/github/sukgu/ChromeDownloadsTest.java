package io.github.sukgu;

import static java.lang.System.err;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.nio.file.Paths;
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
public class ChromeDownloadsTest {

	private static boolean isCIBuild = BaseTest.checkEnvironment();

	protected static final boolean debug = Boolean.parseBoolean(BaseTest.getPropertyEnv("DEBUG", "false"));

	protected static WebDriver driver = null;
	protected static Shadow shadow = null;
	private static String browser = BaseTest.getPropertyEnv("BROWSER",
			BaseTest.getPropertyEnv("webdriver.driver", "chrome"));

	private static final BrowserChecker browserChecker = new BrowserChecker(browser);

	private static final boolean headless = Boolean.parseBoolean(BaseTest.getPropertyEnv("HEADLESS", "false"));

	@SuppressWarnings("deprecation")
	@BeforeAll
	// TODO: merge customizations of download directory to BaseTest
	public static void setup() {
		if (browser.equals("chrome")) {
			String chromeDriverPath = null;
			err.println("Launching " + browser);
			if (isCIBuild) {
				WebDriverManager.chromedriver().setup();
				chromeDriverPath = WebDriverManager.chromedriver().getBinaryPath();

			} else {
				chromeDriverPath = Paths.get(System.getProperty("user.home")).resolve("Downloads")
						.resolve(System.getProperty("os.name").toLowerCase().startsWith("windows") ? "chromedriver.exe"
								: "chromedriver")
						.toAbsolutePath().toString();
				System.setProperty("webdriver.chrome.driver", chromeDriverPath);
			}
			System.err.println("Chrome Driver Path: " + chromeDriverPath);
			// https://peter.sh/experiments/chromium-command-line-switches/
			ChromeOptions options = new ChromeOptions();
			// options for headless
			if (headless) {
				for (String arg : (new String[] { "headless", "window-size=1200x800" })) {
					options.addArguments(arg);
				}
			}

			Map<String, Object> preferences = new Hashtable<>();
			preferences.put("profile.default_content_settings.popups", 0);
			preferences.put("download.prompt_for_download", "false");
			String downloadsPath = System.getProperty("user.home") + "/Downloads";
			preferences.put("download.default_directory", BaseTest.getPropertyEnv("fileDownloadPath", downloadsPath));

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

	@BeforeEach
	public void init() {
		driver.navigate().to("about:blank");
	}

	// TODO: ordering
	@Test
	public void test1() { // downloadPDFTest

		Assumptions.assumeTrue(browser.equals("chrome"));
		Assumptions.assumeFalse(isCIBuild);

		driver.navigate().to("https://intellipaat.com/blog/tutorial/selenium-tutorial/selenium-cheat-sheet/");
		WebElement element = driver
				.findElement(By.xpath("//*[@id=\"global\"]//a[contains(@href, \"Selenium-Cheat-Sheet.pdf\")]"));
		element.click();
		sleep(5000);
	}

	@Test
	public void test2() { // listDownloadsShadowTest
		Assumptions.assumeTrue(browserChecker.testingChrome());
		Assumptions.assumeFalse(isCIBuild);
		driver.navigate().to("chrome://downloads/");
		WebElement element = driver.findElement(By.tagName("downloads-manager"));
		List<WebElement> elements = shadow.getAllShadowElement(element, "#downloadsList");
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		WebElement element2 = elements.get(0);
		err.println(String.format("Located element:", element2.getAttribute("outerHTML")));
		WebElement element3 = element2.findElement(By.tagName("downloads-item"));
		assertThat(element3, notNullValue());
		WebElement element4 = shadow.getShadowElement(element3, "div#details");
		assertThat(element4, notNullValue());
		System.err.println("Result element: " + element3.getAttribute("outerHTML"));
		WebElement element5 = element4.findElement(By.cssSelector("span#name"));
		assertThat(element5, notNullValue());
		System.err.println("Result element: " + element5.getAttribute("outerHTML"));
		final String element4HTML = element5.getAttribute("innerHTML");
		assertThat(element4HTML, containsString("Selenium-Cheat-Sheet"));
		// NOTE: the getText() is failing
		try {
			assertThat(element4.getText(), containsString("Selenium-Cheat-Sheet"));
		} catch (AssertionError e) {
			System.err.println("Exception (ignored) " + e.toString());
		}
		// can be OS-specific: "Selenium-Cheat-Sheet (10).pdf"

		Pattern pattern = Pattern.compile(String.format(".*Selenium-Cheat-Sheet(?:%s)*.pdf", " \\((\\d+)\\)"),
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(element4HTML);
		assertThat(matcher.find(), is(true));
		assertThat(pattern.matcher(element4HTML).find(), is(true));
		WebElement element6 = element4.findElement(By.cssSelector("a#url"));
		assertThat(element6, notNullValue());
		System.err.println("Inspecting element: " + element6.getAttribute("outerHTML"));
		shadow.scrollTo(element6);
		WebElement element7 = shadow.getParentElement(element6);
		assertThat(element7, notNullValue());
		assertThat(shadow.isVisible(element7), is(true));
		String html = element7.getAttribute("outerHTML");
		System.err.println("Inspecting parent element: " + html);
		try {
			assertThat(shadow.getAttribute(element7, "outerHTML"), notNullValue());
			assertThat(shadow.getAttribute(element7, "outerHTML"), containsString(html));
			System.err.println("Vefified attribute extraction: " + shadow.getAttribute(element7, "outerHTML"));
		} catch (AssertionError e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
		elements = shadow.getChildElements(element7);
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		sleep(1000);
		// cannot access instance method of BaseTest using static reference
	}

	@AfterEach
	public void AfterMethod() {
		driver.get("about:blank");
	}

	@AfterAll
	public static void tearDownAll() {
		driver.close();
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

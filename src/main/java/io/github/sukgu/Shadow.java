package io.github.sukgu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;

import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptException;

public class Shadow {

	WebDriver driver;
	WebDriverException exception;
	WebDriverWait wait;
	CommandExecutor executer;
	SessionId sessionId;
	ChromeDriver chromeDriver;
	FirefoxDriver firfoxDriver;
	InternetExplorerDriver ieDriver;
	RemoteWebDriver remoteWebDriver;
	private final String javascriptLibrary = convertJStoText().toString();
	private int implicitWait = 0;
	private int explicitWait = 0;
	private int pollingTime = 0;
	private boolean debug = false;

	public void setDebug(boolean value) {
		this.debug = value;
	}

	public Shadow(WebDriver driver) {

		if (driver instanceof ChromeDriver) {
			sessionId = ((ChromeDriver) driver).getSessionId();
			chromeDriver = (ChromeDriver) driver;
		} else if (driver instanceof FirefoxDriver) {
			sessionId = ((FirefoxDriver) driver).getSessionId();
			firfoxDriver = (FirefoxDriver) driver;
		} else if (driver instanceof InternetExplorerDriver) {
			sessionId = ((InternetExplorerDriver) driver).getSessionId();
			ieDriver = (InternetExplorerDriver) driver;
		} else if (driver instanceof RemoteWebDriver) {
			sessionId = ((RemoteWebDriver) driver).getSessionId();
			remoteWebDriver = (RemoteWebDriver) driver;
		}
		this.driver = driver;
	}

	private Object injectShadowExecuter(String javascript) {
		if (chromeDriver != null) {
			JavascriptExecutor js = (JavascriptExecutor) chromeDriver;
			waitForPageLoaded();
			return js.executeScript(javascript);
		} else if (firfoxDriver != null) {
			waitForPageLoaded();
			return firfoxDriver.executeScript(javascript);
		} else if (ieDriver != null) {
			waitForPageLoaded();
			return ieDriver.executeScript(javascript);
		} else if (remoteWebDriver != null) {
			JavascriptExecutor js = (JavascriptExecutor) remoteWebDriver;
			waitForPageLoaded();
			return js.executeScript(javascript);
		} else {
			return null;
		}
	}

	private Object injectShadowExecuter(String javascript, WebElement element) {
		if (chromeDriver != null) {
			JavascriptExecutor js = (JavascriptExecutor) chromeDriver;
			waitForPageLoaded();
			return js.executeScript(javascript, element);
		} else if (firfoxDriver != null) {
			waitForPageLoaded();
			return firfoxDriver.executeScript(javascript, element);
		} else if (ieDriver != null) {
			waitForPageLoaded();
			return ieDriver.executeScript(javascript, element);
		} else if (remoteWebDriver != null) {
			JavascriptExecutor js = (JavascriptExecutor) remoteWebDriver;
			waitForPageLoaded();
			return js.executeScript(javascript, element);
		} else {
			return null;
		}
	}

	private Object executerGetObject(String script) {
		String javascript = javascriptLibrary;
		javascript += script;
		if (debug) {
			System.err.println("Executing: " + javascript);
		}
		try {
			return injectShadowExecuter(javascript);
		} catch (JavascriptException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	private Object executerGetObject(String script, WebElement element) {
		String javascript = javascriptLibrary;
		javascript += script;
		if (debug) {
			System.err.println("Executing: " + javascript + " on" + element);
		}
		return injectShadowExecuter(javascript, element);
	}

	private StringBuilder convertJStoText() {
		InputStream in = getClass().getResourceAsStream("/querySelector.js");
		BufferedReader reader = null;
		// File jsFile = new File("querySelector.js");
		// BufferedReader reader = null;
		StringBuilder text = new StringBuilder();
		// reader = new BufferedReader(new FileReader(jsFile));
		reader = new BufferedReader(new InputStreamReader(in));
		if (reader != null) {
			try {
				while (reader.ready()) {
					text.append(reader.readLine());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return text;
	}

	private void fixLocator(SearchContext context, String cssLocator, WebElement element) {
		if (element instanceof RemoteWebElement) {
			try {
				@SuppressWarnings("rawtypes")
				Class[] parameterTypes = new Class[] { SearchContext.class, String.class, String.class };
				Method m = element.getClass().getDeclaredMethod("setFoundBy", parameterTypes);
				m.setAccessible(true);
				Object[] parameters = new Object[] { context, "cssSelector", cssLocator };
				m.invoke(element, parameters);
			} catch (Exception fail) {
				// fail("Something bad happened when fixing locator");
			}
		}
	}

	private void waitForPageLoaded() {
		ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").toString()
						.equals("complete");
			}
		};
		try {
			Thread.sleep(1000);
			WebDriverWait wait = new WebDriverWait(driver, 30);
			wait.until(expectation);
		} catch (Throwable error) {
			// Assertions.fail("Timeout waiting for Page Load Request to complete.");
		}
	}

	// wait methods on shadow objects
	public void setImplicitWait(int seconds) {
		this.implicitWait = seconds;
	}

	public void setExplicitWait(int seconds, int pollingTime) throws Exception {
		if (pollingTime > seconds) {
			throw new Exception("pollingTime can't be greater than wait time");
		}
		this.explicitWait = seconds;
		this.pollingTime = pollingTime;
	}

	private boolean isPresent(WebElement element) {
		boolean present = false;
		try {
			present = (Boolean) executerGetObject("return isVisible(arguments[0]);", element);
		} catch (JavascriptException ex) {

		}
		return present;
	}

	public WebElement findElement(String cssSelector) {
		WebElement element = null;
		boolean visible = false;

		if (implicitWait > 0) {
			try {
				Thread.sleep(implicitWait * 1000);
			} catch (InterruptedException e) {

			}
			element = (WebElement) executerGetObject(
					String.format("return getObject(\"%s\");", cssSelector.replaceAll("\"", "\\\\\"")));
			fixLocator(driver, cssSelector, element);
			visible = isPresent(element);
		}

		if (explicitWait > 0) {
			element = (WebElement) executerGetObject(
					String.format("return getObject(\"%s\");", cssSelector.replaceAll("\"", "\\\\\"")));
			fixLocator(driver, cssSelector, element);
			visible = isPresent(element);

			for (int i = 0; i < explicitWait && !visible;) {
				try {
					Thread.sleep(pollingTime * 1000);
					element = (WebElement) executerGetObject(
							String.format("return getObject(\"%s\");", cssSelector.replaceAll("\"", "\\\\\"")));
					fixLocator(driver, cssSelector, element);
					visible = isPresent(element);
					i = i + pollingTime;
				} catch (InterruptedException e) {

				}
			}
		}

		if (explicitWait == 0 && implicitWait == 0) {
			element = (WebElement) executerGetObject(
					String.format("return getObject(\"%s\");", cssSelector.replaceAll("\"", "\\\\\"")));
			fixLocator(driver, cssSelector, element);
		}

		if (!isPresent(element)) {
			throw new ElementNotVisibleException("Element with CSS " + cssSelector + " is not present on screen");
		}

		return element;

	}

	public WebElement findElement(WebElement parent, String cssSelector) {
		WebElement element = null;
		boolean visible = false;

		if (implicitWait > 0) {
			try {
				Thread.sleep(implicitWait * 1000);
			} catch (InterruptedException e) {

			}
			element = (WebElement) executerGetObject(
					String.format("return getObject(\"%s\", arguments[0]);", cssSelector.replaceAll("\"", "\\\\\"")),
					parent);

			fixLocator(driver, cssSelector, element);
			visible = isPresent(element);
		}

		if (explicitWait > 0) {
			element = (WebElement) executerGetObject(
					String.format("return getObject(\"%s\", arguments[0]);", cssSelector.replaceAll("\"", "\\\\\"")),
					parent);
			fixLocator(driver, cssSelector, element);
			visible = isPresent(element);

			for (int i = 0; i < explicitWait && !visible;) {
				try {
					Thread.sleep(pollingTime * 1000);
					element = (WebElement) executerGetObject(String.format("return getObject(\"%s\", arguments[0]);",
							cssSelector.replaceAll("\"", "\\\\\"")), parent);
					fixLocator(driver, cssSelector, element);
					visible = isPresent(element);
					i = i + pollingTime;
				} catch (InterruptedException e) {

				}
			}

		}

		if (explicitWait == 0 && implicitWait == 0) {
			element = (WebElement) executerGetObject(
					String.format("return getObject(\"%s\", arguments[0]);", cssSelector.replaceAll("\"", "\\\\\"")),
					parent);
			fixLocator(driver, cssSelector, element);
		}

		if (!isPresent(element)) {
			throw new ElementNotVisibleException("Element with CSS " + cssSelector + " is not present on screen");
		}

		return element;
	}

	@SuppressWarnings("unchecked")
	public List<WebElement> findElements(String cssSelector) {
		if (implicitWait > 0) {
			try {
				Thread.sleep(implicitWait * 1000);
			} catch (InterruptedException e) {

			}
		}
		List<WebElement> element = null;
		Object object = executerGetObject(
				String.format("return getAllObject(\"%s\");", cssSelector.replaceAll("\"", "\\\\\"")));
		if (object != null && object instanceof List<?>) {
			element = (List<WebElement>) object;
		}
		for (WebElement webElement : element) {
			fixLocator(driver, cssSelector, webElement);
		}
		return element;
	}

	@SuppressWarnings("unchecked")
	public List<WebElement> findElements(WebElement parent, String cssSelector) {
		if (implicitWait > 0) {
			try {
				Thread.sleep(implicitWait * 1000);
			} catch (InterruptedException e) {

			}
		}
		List<WebElement> element = null;
		Object object = executerGetObject(
				String.format("return getAllObject(\"%s\", arguments[0]);", cssSelector.replaceAll("\"", "\\\\\"")),
				parent);
		if (object != null && object instanceof List<?>) {
			element = (List<WebElement>) object;
		}
		for (WebElement webElement : element) {
			fixLocator(driver, cssSelector, webElement);
		}
		return element;
	}

	public WebElement getShadowElement(WebElement parent, String selector) {
		if (implicitWait > 0) {
			try {
				Thread.sleep(implicitWait * 1000);
			} catch (InterruptedException e) {

			}
		}
		WebElement element = null;
		element = (WebElement) executerGetObject(
				String.format("return getShadowElement(arguments[0],\"%s\");", selector), parent);
		fixLocator(driver, selector, element);
		return element;
	}

	@SuppressWarnings("unchecked")
	public List<WebElement> getAllShadowElement(WebElement parent, String selector) {
		if (implicitWait > 0) {
			try {
				Thread.sleep(implicitWait * 1000);
			} catch (InterruptedException e) {

			}
		}
		List<WebElement> elements = null;
		Object object = executerGetObject(String.format("return getAllShadowElement(arguments[0],\"%s\");", selector),
				parent);
		if (object != null && object instanceof List<?>) {
			elements = (List<WebElement>) object;
		}
		for (WebElement element : elements) {
			fixLocator(driver, selector, element);
		}
		return elements;
	}

	public WebElement getParentElement(WebElement element) {
		if (implicitWait > 0) {
			try {
				Thread.sleep(implicitWait * 1000);
			} catch (InterruptedException e) {

			}
		}
		return (WebElement) executerGetObject("return getParentElement(arguments[0]);", element);
	}

	@SuppressWarnings("unchecked")
	public List<WebElement> getChildElements(WebElement parent) {
		if (implicitWait > 0) {
			try {
				Thread.sleep(implicitWait * 1000);
			} catch (InterruptedException e) {

			}
		}
		List<WebElement> elements = null;
		Object object = executerGetObject("return getChildElements(arguments[0]);", parent);
		if (object != null && object instanceof List<?>) {
			elements = (List<WebElement>) object;
		}
		return elements;
	}

	@SuppressWarnings("unchecked")
	public List<WebElement> getSiblingElements(WebElement element) {
		if (implicitWait > 0) {
			try {
				Thread.sleep(implicitWait * 1000);
			} catch (InterruptedException e) {

			}
		}
		List<WebElement> elements = null;
		Object object = executerGetObject("return getSiblingElements(arguments[0]);", element);
		if (object != null && object instanceof List<?>) {
			elements = (List<WebElement>) object;
		}
		return elements;
	}

	public WebElement getSiblingElement(WebElement element, String selector) {
		if (implicitWait > 0) {
			try {
				Thread.sleep(implicitWait * 1000);
			} catch (InterruptedException e) {

			}
		}
		return (WebElement) executerGetObject(String.format("return getSiblingElement(arguments[0],\"%s\");", selector),
				element);
	}

	public WebElement getNextSiblingElement(WebElement element) {
		return (WebElement) executerGetObject("return getNextSiblingElement(arguments[0]);", element);
	}

	public WebElement getPreviousSiblingElement(WebElement element) {
		return (WebElement) executerGetObject("return getPreviousSiblingElement(arguments[0]);", element);
	}

	public boolean isVisible(WebElement element) {
		return (Boolean) executerGetObject("return isVisible(arguments[0]);", element);
	}

	public boolean isChecked(WebElement element) {
		return (Boolean) executerGetObject("return isChecked(arguments[0]);", element);
	}

	public boolean isDisabled(WebElement element) {
		return (Boolean) executerGetObject("return isDisabled(arguments[0]);", element);
	}

	public String getAttribute(WebElement element, String attribute) {
		return (String) executerGetObject(String.format("return getAttribute(arguments[0],\"%s\");", attribute),
				element);
	}

	public void selectCheckbox(WebElement parentElement, String label) {
		executerGetObject(String.format("return selectCheckbox(\"%s\",arguments[0]);", label), parentElement);
	}

	public void selectCheckbox(String label) {
		executerGetObject(String.format("return selectCheckbox(\"%s\");", label));
	}

	public void selectRadio(WebElement parentElement, String label) {
		executerGetObject(String.format("return selectRadio(\"%s\",arguments[0]);", label), parentElement);
	}

	public void selectRadio(String label) {
		executerGetObject(String.format("return selectRadio(\"%s\");", label));
	}

	public void selectDropdown(WebElement parentElement, String label) {
	}

	public void scrollTo(WebElement element) {
		executerGetObject("return scrollTo(arguments[0]);", element);
	}

}

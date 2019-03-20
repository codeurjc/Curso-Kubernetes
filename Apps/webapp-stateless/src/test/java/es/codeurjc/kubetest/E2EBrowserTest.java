package es.codeurjc.kubetest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class E2EBrowserTest {

	private WebDriver browser;

	@Before
	public void setup() {
		WebDriverManager.chromedriver().setup();
		browser = new ChromeDriver();
	}

	@After
	public void teardown() {
		if (browser != null) {
			browser.quit();
		}
	}

	private void loadPage() {

		String url = System.getProperty("weburl");
		if (url == null) {
			url = "http://localhost:8080/";
		}

		browser.get(url);
	}

	private String extractColor() {
		return browser.findElement(By.tagName("body")).getAttribute("bgcolor");
	}

	@Test
	public void test() throws InterruptedException {

		String userColor = null;

		for (int i = 0; i < 5; i++) {

			loadPage();

			String color = extractColor();

			if (userColor == null) {				
				userColor = color;				
			} else {
				assertThat(userColor).isEqualTo(color)
						.withFailMessage("El color del usuario ha cambiado");
			}

			System.out.println("Loaded page " + (i+1) +" time(s)");

			Thread.sleep(2000);
		}
	}

}

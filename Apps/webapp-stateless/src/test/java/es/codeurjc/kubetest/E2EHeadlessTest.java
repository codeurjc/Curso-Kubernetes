package es.codeurjc.kubetest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class E2EHeadlessTest {

	private HttpClientContext httpClientContext;
	private String page;

	public void loadPage(String url) throws Exception {

		if (httpClientContext == null) {
			CookieStore cookieStore = new BasicCookieStore();
			httpClientContext = HttpClientContext.create();
			httpClientContext.setCookieStore(cookieStore);
		}

		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(url);
		CloseableHttpResponse response1 = httpclient.execute(httpget, httpClientContext);
		this.page = EntityUtils.toString(response1.getEntity(), "UTF-8");
	}
	
	public void loadPage() throws Exception {
		
		String url = System.getProperty("weburl");
		if (url == null) {
			url = "http://localhost:8080/";
		}
		
		loadPage(url);		
	}
	
	public String extractColor() {
	
		final Pattern pattern = Pattern.compile("<body bgcolor=\"(.+?)\">", Pattern.DOTALL);
		final Matcher matcher = pattern.matcher(page);
		matcher.find();
		return matcher.group(1);
	}

	@Test
	public void test() throws Exception {

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

			System.out.println("Loaded page " + (i+1) +" time(s) with color "+color);

			Thread.sleep(2000);
		}
	}

}

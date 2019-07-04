package be.ugent.equatic.core;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
abstract public class BrowserTest extends ApplicationTest {

    protected HtmlUnitDriver driver;

    @Before
    public void setUp() {
        driver = new HtmlUnitDriver(BrowserVersion.CHROME) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                final WebClient webClient = super.modifyWebClient(client);
                webClient.setCssErrorHandler(new SilentCssErrorHandler());
                return webClient;
            }
        };
        driver.setJavascriptEnabled(true);
    }

    @After
    public void destroy() {
        if (driver != null) {
            driver.close();
        }
    }
}

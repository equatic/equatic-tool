package be.ugent.equatic.core;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.github.dandelion.core.web.DandelionFilter;
import org.junit.Before;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebConnection;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import be.ugent.equatic.filter.DandelionServletFilter;

import javax.annotation.Resource;
import javax.servlet.ServletException;

@WebAppConfiguration
abstract public class MockMvcTest extends ApplicationTest {

    @Resource
    private WebApplicationContext wac;

    @Resource
    private FilterChainProxy springSecurityFilterChain;

    protected MockMvc mockMvc;

    protected HtmlUnitDriver driver;

    @Before
    public void setUp() throws ServletException {
        DandelionFilter dandelionFilter = new DandelionFilter();
        dandelionFilter.init(new MockFilterConfig());

        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(springSecurityFilterChain)
                .addFilter(dandelionFilter)
                .addFilter(new DandelionServletFilter())
                .build();

        /*
         * https://github.com/spring-projects/spring-test-htmlunit/issues/40
         */
        driver = new HtmlUnitDriver(BrowserVersion.CHROME) {
            @Override
            protected WebClient modifyWebClient(final WebClient client) {
                final WebClient webClient = super.modifyWebClient(client);
                webClient.setWebConnection(new MockMvcWebConnection(mockMvc, new WebClient(), ""));
                webClient.setCssErrorHandler(new SilentCssErrorHandler());
                return webClient;
            }
        };
        driver.setJavascriptEnabled(true);
    }
}

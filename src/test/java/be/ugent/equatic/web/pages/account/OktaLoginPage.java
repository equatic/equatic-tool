package be.ugent.equatic.web.pages.account;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import be.ugent.equatic.web.pages.AbstractPage;

import java.net.MalformedURLException;
import java.net.URL;

public class OktaLoginPage extends AbstractPage {

    @FindBy(name = "username")
    private WebElement username;

    @FindBy(name = "password")
    private WebElement password;


    public OktaLoginPage(WebDriver driver) throws Exception {
        super(driver);
    }

    public URL getUrl() throws MalformedURLException {
        return new URL("https://dev-472907.oktapreview.com/app/usosdev472907_equaticlocalhost8080_1/exk5i2auor62BMS8V0h7/sso/saml");
    }

    public <T extends AbstractPage> T logInThroughFederatedIdp(String username, String password,
                                                               Class<T> resultPageClass) throws Exception {
        wait.until(ExpectedConditions.visibilityOf(this.username));

        this.username.sendKeys(username);
        this.password.sendKeys(password);
        try {
            WebDriverWait wait = new WebDriverWait(driver, 5);
            WebElement logIn = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("login")));
            logIn.click();
        } catch (TimeoutException exception) {
            logPageSource();
            throw exception;
        }

        return PageFactory.initElements(driver, resultPageClass);
    }
}

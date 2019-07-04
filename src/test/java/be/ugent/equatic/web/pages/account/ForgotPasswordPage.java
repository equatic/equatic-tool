package be.ugent.equatic.web.pages.account;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import be.ugent.equatic.web.AccountController;
import be.ugent.equatic.web.pages.AbstractPage;

public class ForgotPasswordPage extends AbstractPage {

    @FindBy(id = "input-email")
    private WebElement email;

    @FindBy(id = "button-reset-password")
    private WebElement buttonResetPassword;

    public static String getRelativeUrl() {
        return AccountController.VIEW_FORGOT_PASSWORD;
    }

    public ForgotPasswordPage(WebDriver driver) throws Exception {
        super(driver);
    }

    public ForgotPasswordPage resetPasswordForEmail(String email) {
        this.email.sendKeys(email);
        buttonResetPassword.click();

        return PageFactory.initElements(driver, ForgotPasswordPage.class);
    }
}

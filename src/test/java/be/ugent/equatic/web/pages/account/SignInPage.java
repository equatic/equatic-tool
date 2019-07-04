package be.ugent.equatic.web.pages.account;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.User;
import be.ugent.equatic.web.AccountController;
import be.ugent.equatic.web.pages.AbstractPage;

public class SignInPage extends AbstractPage {

    @FindBy(id = "input-username")
    private WebElement username;

    @FindBy(id = "input-password")
    private WebElement password;

    @FindBy(id = "button-signin")
    private WebElement buttonSignIn;

    @FindBy(id = "button-register")
    private WebElement buttonRegister;

    @FindBy(id = "link-forgot-password")
    private WebElement linkForgotPassword;

    public static String getRelativeUrl() {
        return AccountController.VIEW_LOGIN;
    }

    public SignInPage(WebDriver webDriver) throws Exception {
        super(webDriver);
    }

    public OktaLoginPage chooseToSignInThroughFederatedIdp(Institution institution) throws Exception {
        selectInstitutionByVisibleText(institution);
        buttonSignIn.click();

        return PageFactory.initElements(driver, OktaLoginPage.class);
    }

    public <T extends AbstractPage> T signInThroughDatabase(Institution institution, User user, String password,
                                                            Class<T> resultPageClass) {
        selectInstitutionByVisibleText(institution);
        this.username.sendKeys(user.getUsername());
        this.password.sendKeys(password);
        buttonSignIn.click();

        return PageFactory.initElements(driver, resultPageClass);
    }

    public RegistrationPage clickRegister() {
        buttonRegister.click();

        return PageFactory.initElements(driver, RegistrationPage.class);
    }

    public static SignInPage to(WebDriver driver) throws Exception {
        return AbstractPage.to(driver, SignInPage.class);
    }

    public static <T extends AbstractPage> T signInUser(User user, WebDriver driver, Class<T> resultPageClass)
            throws Exception {
        SignInPage signInPage = to(driver, SignInPage.class);
        return signInPage.signInThroughDatabase(user.getInstitution(), user, DEFAULT_PASSWORD, resultPageClass);
    }

    public ForgotPasswordPage forgotPasswordForInstitution(Institution institution) {
        selectInstitutionByVisibleText(institution);
        linkForgotPassword.click();

        return PageFactory.initElements(driver, ForgotPasswordPage.class);
    }

    public WebElement getButtonSignIn() {
        return buttonSignIn;
    }

    public WebElement getButtonRegister() {
        return buttonRegister;
    }
}

package be.ugent.equatic.web;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.support.PageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import be.ugent.equatic.core.BrowserTest;
import be.ugent.equatic.domain.User;
import be.ugent.equatic.util.UserUtil;
import be.ugent.equatic.web.pages.account.OktaLoginPage;
import be.ugent.equatic.web.pages.account.RegistrationPage;
import be.ugent.equatic.web.pages.account.SignInPage;
import be.ugent.equatic.web.pages.institutionaluser.InstitutionReport;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Configuration
@PropertySource("classpath:test.properties")
public class AccountControllerFederatedIdPTest extends BrowserTest {

    private User oktaUser;

    @Value("${test.okta.username}")
    public String username;

    @Value("${test.okta.password}")
    public String password;

    @Value("${test.okta.federatedName}")
    public String federatedName;

    @Autowired
    private JavaMailSenderImpl mailSender;

    private GreenMail testSmtp;

    @Before
    public void setUpUser() {
        oktaUser = UserUtil.getUser(federatedName, okta);
    }

    @Before
    public void setUpMail() {
        testSmtp = new GreenMail(ServerSetupTest.SMTP);
        testSmtp.start();

        mailSender.setPort(3025);
        mailSender.setHost("localhost");
    }

    @After
    public void cleanup() {
        testSmtp.stop();
    }

    @Ignore
    @Test
    public void newUserRegistersAfterSignIn() throws Exception {
        SignInPage signInPage = SignInPage.to(driver);

        OktaLoginPage oktaLoginPage = signInPage.chooseToSignInThroughFederatedIdp(okta);
        RegistrationPage registrationPage = oktaLoginPage.logInThroughFederatedIdp(username, password,
                RegistrationPage.class);

        assertThat(registrationPage.getUsername().getAttribute("value"), is(oktaUser.getUsername()));
        assertThat(registrationPage.getUsername().getAttribute("readonly"), notNullValue());
        assertThat(registrationPage.getSelectedInstitutionDisplayName(), is(okta.getDisplayName()));
        assertThat(registrationPage.isInstitutionSelectorReadonly(), is(true));
        assertThat(registrationPage.getPassword().isEnabled(), is(false));
        assertThat(registrationPage.getConfirmPassword().isEnabled(), is(false));

        registrationPage.register();
        List<String> errorMessages = getMessages("User.firstname.NotEmpty", "User.lastname.NotEmpty",
                "User.email.NotEmpty");
        assertThat(registrationPage.getErrors(), containsInAnyOrder(errorMessages.toArray()));

        registrationPage.getFirstname().sendKeys(oktaUser.getFirstname());
        registrationPage.getLastname().sendKeys(oktaUser.getLastname());
        registrationPage.getEmail().sendKeys(oktaUser.getEmail());
        registrationPage.register();

        assertThat(registrationPage.getSuccessMessage().getText(), is(
                getMessage("AccountController.register.confirmation", new String[]{oktaUser.getEmail()})));
        User createdUser = userService.findByUsernameAndInstitution(oktaUser.getUsername(), oktaUser.getInstitution());
        assertThat(createdUser, notNullValue());
    }

    @Ignore
    @Test
    public void registerButtonShouldRedirectToSignIn() throws Exception {
        SignInPage signInPage = SignInPage.to(driver);

        signInPage.selectInstitutionByVisibleText(okta);
        signInPage.getButtonRegister().click();

        PageFactory.initElements(driver, OktaLoginPage.class);
    }

    @Ignore
    @Test
    public void registeredUserBeforeActivationCannotSignIn() throws Exception {
        oktaUser.setEmailConfirmed(true);
        userService.save(oktaUser);

        SignInPage signInPage = SignInPage.to(driver);
        OktaLoginPage oktaLoginPage = signInPage.chooseToSignInThroughFederatedIdp(okta);
        signInPage = oktaLoginPage.logInThroughFederatedIdp(username, password, SignInPage.class);

        assertThat(signInPage.getErrorMessage().getText(),
                is(getMessage("AbstractUserDetailsAuthenticationProvider.disabled")));
    }

    @Ignore
    @Test
    public void activatedUserCanSignIn() throws Exception {
        oktaUser.setEmailConfirmed(true);
        oktaUser.setActivated(true);
        userService.save(oktaUser);

        SignInPage signInPage = SignInPage.to(driver);
        OktaLoginPage oktaLoginPage = signInPage.chooseToSignInThroughFederatedIdp(okta);
        oktaLoginPage.logInThroughFederatedIdp(username, password, InstitutionReport.class);
    }
}

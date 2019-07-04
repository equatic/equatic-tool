package be.ugent.equatic.web;

import com.google.common.base.Joiner;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.web.servlet.MvcResult;
import be.ugent.equatic.config.WebSecurityConfig;
import be.ugent.equatic.core.MockMvcTest;
import be.ugent.equatic.domain.Authority;
import be.ugent.equatic.domain.User;
import be.ugent.equatic.util.UserUtil;
import be.ugent.equatic.web.admin.superadmin.AdminManagementController;
import be.ugent.equatic.web.pages.AbstractPage;
import be.ugent.equatic.web.pages.account.ForgotPasswordPage;
import be.ugent.equatic.web.pages.account.RegistrationPage;
import be.ugent.equatic.web.pages.account.ResetPasswordPage;
import be.ugent.equatic.web.pages.account.SignInPage;
import be.ugent.equatic.web.pages.institutionaluser.InstitutionReport;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AccountControllerTest extends MockMvcTest {

    private static final String NO_SUCH_EMAIL = "wrong";
    private static final String NEW_PASSWORD = "new password";
    private static final String WRONG_PASSWORD = "wrong";

    @Autowired
    private JavaMailSenderImpl mailSender;

    private GreenMail testSmtp;

    private User newUser;

    @Before
    public void setUpUser() {
        newUser = UserUtil.getUser("newUser", ghentUniversity);
        newUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
    }

    @Before
    public void setUpMail() {
        testSmtp = new GreenMail(ServerSetupTest.SMTP);
        testSmtp.start();

        mailSender.setPort(MAIL_TEST_PORT);
        mailSender.setHost(MAIL_TEST_HOST);
    }

    @After
    public void cleanup() {
        testSmtp.stop();
    }

    @Test
    public void signInWithCorrectCredentials() throws Exception {
        SignInPage signInPage = SignInPage.to(driver);

        assertThat(signInPage.getButtonSignIn().isEnabled(), is(false));
        assertThat(signInPage.getButtonRegister().isEnabled(), is(false));

        signInPage.signInThroughDatabase(ghentUniversity, ghentUser, DEFAULT_PASSWORD, InstitutionReport.class);
    }

    @Test
    public void signInWithUsernameCapitalized() throws Exception {
        SignInPage signInPage = SignInPage.to(driver);

        assertThat(signInPage.getButtonSignIn().isEnabled(), is(false));
        assertThat(signInPage.getButtonRegister().isEnabled(), is(false));

        ghentUser.setUsername(ghentUser.getUsername().toUpperCase());
        signInPage.signInThroughDatabase(ghentUniversity, ghentUser, DEFAULT_PASSWORD, InstitutionReport.class);
    }

    @Test
    public void trySignInWithIncorrectCredentials() throws Exception {
        SignInPage signInPage = SignInPage.to(driver);

        signInPage = signInPage.signInThroughDatabase(ghentUniversity, ghentUser, WRONG_PASSWORD, SignInPage.class);

        assertThat(signInPage.getErrorMessage().getText(),
                is(getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials")));
    }

    @Test
    public void trySingInWithIncorrectInstitution() throws Exception {
        SignInPage signInPage = SignInPage.to(driver);

        userService.deleteByInstitution(ghentUniversity);
        institutionService.delete(ghentUniversity);
        signInPage = signInPage.signInThroughDatabase(ghentUniversity, ghentUser, DEFAULT_PASSWORD, SignInPage.class);

        assertThat(signInPage.getErrorMessage().getText(),
                is(getMessage("DaoAuthenticationProvider.internalException")));
    }

    @Test
    public void cannotSignInBeforeEmailConfirmationAndActivation() throws Exception {
        userService.save(newUser);

        SignInPage signInPage = SignInPage.to(driver);

        signInPage.signInThroughDatabase(ghentUniversity, newUser, DEFAULT_PASSWORD, SignInPage.class);

        assertThat(signInPage.getErrorMessage().getText(),
                is(getMessage("AbstractUserDetailsAuthenticationProvider.disabled")));

        newUser = userService.findById(newUser.getId());
        newUser.setEmailConfirmed(true);
        userService.save(newUser);

        signInPage.signInThroughDatabase(ghentUniversity, newUser, DEFAULT_PASSWORD, SignInPage.class);

        assertThat(signInPage.getErrorMessage().getText(),
                is(getMessage("AbstractUserDetailsAuthenticationProvider.disabled")));

        newUser = userService.findById(newUser.getId());
        newUser.setActivated(true);
        userService.save(newUser);

        signInPage.signInThroughDatabase(ghentUniversity, newUser, DEFAULT_PASSWORD, InstitutionReport.class);
    }

    @Test
    public void userRegistration() throws Exception {
        SignInPage signInPage = SignInPage.to(driver);

        signInPage.selectInstitutionByVisibleText(ghentUniversity);
        RegistrationPage registrationPage = signInPage.clickRegister();

        assertThat(registrationPage.getSelectedInstitutionDisplayName(), is(ghentUniversity.getDisplayName()));
        assertThat(registrationPage.isInstitutionSelectorReadonly(), is(true));

        /*
         * Don't fill any fields (institution is already selected).
         */
        registrationPage = registrationPage.register();
        List<String> errorMessages = getMessages("User.username.NotEmpty", "User.firstname.NotEmpty",
                "User.lastname.NotEmpty", "User.email.NotEmpty", "User.rawPassword.NotNull");
        assertThat(registrationPage.getErrors(), containsInAnyOrder(errorMessages.toArray()));

        /*
         * Try username and e-mail that already exist.
         */
        registrationPage.fillWithUserData(ghentUser);
        registrationPage = registrationPage.register();
        assertThat(registrationPage.getErrors(),
                containsInAnyOrder(getMessage("User.username.Unique"), getMessage("User.email.Unique")));

        /*
         * Try username and e-mail (but this time both in uppercase!) that already exist.
         */
        ghentUser.setUsername(ghentUser.getUsername().toUpperCase());
        ghentUser.setEmail(ghentUser.getEmail().toUpperCase());
        registrationPage.fillWithUserData(ghentUser);
        registrationPage = registrationPage.register();
        assertThat(registrationPage.getErrors(),
                containsInAnyOrder(getMessage("User.username.Unique"), getMessage("User.email.Unique")));

        /*
         * Try not matching passwords.
         */
        registrationPage.fillWithUserData(newUser);
        WebElement confirmPassword = registrationPage.getConfirmPassword();
        confirmPassword.clear();
        confirmPassword.sendKeys(WRONG_PASSWORD);
        registrationPage = registrationPage.register();
        assertThat(registrationPage.getErrors(), contains(getMessage("User.confirmRawPassword.Match")));

        /*
         * Register successfully.
         */
        registrationPage.fillWithUserData(newUser);
        registrationPage = registrationPage.register();
        assertThat(registrationPage.getSuccessMessage().getText(), is(
                getMessage("AccountController.register.confirmation", new String[]{newUser.getEmail()})));

        assertThat(registrationPage.isRegistrationBoxPresent(), is(false));

        User createdUser = userService.findByUsernameAndInstitution(newUser.getUsername(), newUser.getInstitution());
        assertThat(createdUser.isEmailConfirmed(), is(false));

        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(1));
        Message message = messages[0];

        assertThat(message.getRecipients(Message.RecipientType.TO),
                is(new InternetAddress[]{new InternetAddress(createdUser.getEmail())}));
        assertThat(message.getSubject(), is(getMessage("NotificationService.emailConfirmationNeeded.subject",
                new String[]{mailProperties.getSubjectPrefix()})));
        URL confirmationUrl = AccountController.getEmailConfirmationUrl(createdUser, AbstractPage.getAppRootUrl());
        String messageBody = GreenMailUtil.getBody(message).replaceAll("\r", "");
        assertThat(messageBody, is(getMessage("NotificationService.emailConfirmationNeeded.body",
                new String[]{confirmationUrl.toString(), ghentUniversity.getDisplayName(),
                        Joiner.on(", ").join(userService.getInstitutionalAdminNames(ghentUniversity))})));
    }

    @Test
    public void emailConfirmation() throws Exception {
        userService.save(newUser);

        assertThat(newUser.isEmailConfirmed(), is(false));

        URL confirmationUrl = AccountController.getEmailConfirmationUrl(newUser, AbstractPage.getAppRootUrl());
        driver.get(confirmationUrl.toString());
        RegistrationPage registrationPage = PageFactory.initElements(driver, RegistrationPage.class);

        assertThat(registrationPage.getSuccessMessage().getText(),
                is(getMessage("AccountController.register.emailConfirmed")));

        assertThat(registrationPage.isRegistrationBoxPresent(), is(false));

        newUser = userService.findById(newUser.getId());
        assertThat(newUser.isEmailConfirmed(), is(true));
        assertThat(newUser.getToken(), isEmptyOrNullString());

        /*
         * Check if notification was sent to institutional admins.
         */
        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(2));
        Message messageToAdmin = messages[0];
        Message messageToSecondAdmin = messages[1];

        assertThat(messageToAdmin.equals(messageToSecondAdmin), is(true));
        InternetAddress[] adminEmails = {
                new InternetAddress(ghentAdmin.getEmail()), new InternetAddress(ghentAdmin2.getEmail())};
        assertThat(Arrays.asList(messageToAdmin.getRecipients(Message.RecipientType.TO)),
                containsInAnyOrder(adminEmails));
        assertThat(messageToAdmin.getSubject(), is(getMessage("NotificationService.newUser.subject")));
        String messageBody = GreenMailUtil.getBody(messageToAdmin).replaceAll("\r", "");
        assertThat(messageBody, is(getMessage("NotificationService.newUser.body",
                new String[]{newUser.getUsername(), newUser.getFirstname(), newUser.getLastname(), newUser.getEmail()})));

        /*
         * Try the confirmation URL once more.
         */
        driver.get(confirmationUrl.toString());
        registrationPage = PageFactory.initElements(driver, RegistrationPage.class);

        assertThat(registrationPage.getWarningMessage().getText(),
                is(getMessage("AccountController.register.confirmationWrong")));
    }

    /**
     * Super admins instead of institutional admins should be notified if first institutional admin confirms e-mail.
     *
     * @throws Exception possibily: javax.mail.internet.AddressException, javax.mail.MessagingException
     *                   or java.net.MalformedURLException
     */
    @Test
    public void emailConfirmationIfFirstAdmin() throws Exception {
        User firstAdmin = UserUtil.getUser("grazAdmin", grazUniversity, Authority.ROLE_ADMIN_INSTITUTIONAL);
        User superAdmin2 = UserUtil.getUser("superAdmin2", warsawUniversity, Authority.ROLE_ADMIN_SUPER);
        userService.save(firstAdmin, superAdmin2);

        assertThat(firstAdmin.isEmailConfirmed(), is(false));

        URL confirmationUrl = AccountController.getEmailConfirmationUrl(firstAdmin, AbstractPage.getAppRootUrl());
        driver.get(confirmationUrl.toString());
        RegistrationPage registrationPage = PageFactory.initElements(driver, RegistrationPage.class);

        assertThat(registrationPage.getSuccessMessage().getText(),
                is(getMessage("AccountController.register.emailConfirmed")));

        assertThat(registrationPage.isRegistrationBoxPresent(), is(false));

        firstAdmin = userService.findById(firstAdmin.getId());
        assertThat(firstAdmin.isEmailConfirmed(), is(true));
        assertThat(firstAdmin.getToken(), isEmptyOrNullString());

        /*
         * Check if notification was sent to institutional admins.
         */
        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(2));
        Message messageToSuperAdmin = messages[0];
        Message messageToSuperAdmin2 = messages[1];

        assertThat(messageToSuperAdmin.equals(messageToSuperAdmin2), is(true));
        InternetAddress[] superAdminEmails =
                {new InternetAddress(superAdmin.getEmail()), new InternetAddress(superAdmin2.getEmail())};
        assertThat(Arrays.asList(messageToSuperAdmin.getRecipients(Message.RecipientType.TO)),
                containsInAnyOrder(superAdminEmails));
        assertThat(messageToSuperAdmin.getSubject(), is(getMessage("NotificationService.adminConfirmedEmail.subject")));
        URL adminsList = AdminManagementController.getAdminsListUrl(AbstractPage.getAppRootUrl());
        String messageBody = GreenMailUtil.getBody(messageToSuperAdmin).replaceAll("\r", "");
        assertThat(messageBody, is(getMessage("NotificationService.adminConfirmedEmail.body",
                new String[]{firstAdmin.getDisplayName(), adminsList.toString()})));
    }

    @Test
    public void userSignInIfInstitutionUsesFederatedIdPShouldNotSucceed() throws Exception {
        User oktaUser = UserUtil.getUser("user", okta);
        oktaUser.setEmailConfirmed(true);
        oktaUser.setActivated(true);
        userService.save(oktaUser);

        MvcResult result = mockMvc.perform(post(AccountController.VIEW_LOGIN)
                .param("institution", String.valueOf(okta.getId()))
                .param("username", oktaUser.getUsername())
                .param("password", "").with(csrf()))
                .andExpect(status().isFound()).andReturn();

        assertThat(result.getResponse().getRedirectedUrl(), startsWith((WebSecurityConfig.SAML_LOGIN_URL)));
    }

    @Test
    public void userSignInIfPasswordIsNullShouldNotSucceed() throws Exception {
        User userWoPassword = UserUtil.getUser("user", ghentUniversity);
        userWoPassword.setEmailConfirmed(true);
        userWoPassword.setActivated(true);
        userService.save(userWoPassword);

        SignInPage signInPage = SignInPage.to(driver);

        signInPage.signInThroughDatabase(ghentUniversity, userWoPassword, "", SignInPage.class);

        assertThat(signInPage.getErrorMessage().getText(),
                is(getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials")));
    }

    @Test
    public void tryResetPasswordForNotExistingUser() throws Exception {
        SignInPage signInPage = SignInPage.to(driver);

        ForgotPasswordPage forgotPasswordPage = signInPage.forgotPasswordForInstitution(ghentUniversity);
        forgotPasswordPage.resetPasswordForEmail(NO_SUCH_EMAIL);

        assertThat(forgotPasswordPage.getErrorMessage().getText(),
                is(getMessage("equatic.emailNotFoundAtInstitution", new String[]{NO_SUCH_EMAIL})));
    }

    @Test
    public void tryResetPasswordForUserWoConfirmedEmail() throws Exception {
        userService.save(newUser);

        SignInPage signInPage = SignInPage.to(driver);

        ForgotPasswordPage forgotPasswordPage = signInPage.forgotPasswordForInstitution(ghentUniversity);
        forgotPasswordPage.resetPasswordForEmail(newUser.getEmail());

        assertThat(forgotPasswordPage.getErrorMessage().getText(),
                is(getMessage("AccountController.forgotPassword.usersEmailNotConfirmed")));
    }

    @Test
    public void forgotPassword() throws Exception {
        SignInPage signInPage = SignInPage.to(driver);

        ForgotPasswordPage forgotPasswordPage = signInPage.forgotPasswordForInstitution(ghentUniversity);
        forgotPasswordPage.resetPasswordForEmail(ghentUser.getEmail().toUpperCase()); // Should work for upper case too!

        assertThat(forgotPasswordPage.getSuccessMessage().getText(),
                is(getMessage("AccountController.forgotPassword.confirmation", new String[]{ghentUser.getEmail()})));

        User ghentUser = userService.findById(this.ghentUser.getId());
        assertThat(ghentUser.getToken(), notNullValue());

        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(1));
        Message message = messages[0];

        assertThat(message.getRecipients(Message.RecipientType.TO),
                is(new InternetAddress[]{new InternetAddress(ghentUser.getEmail())}));
        assertThat(message.getSubject(), is(getMessage("NotificationService.forgotPassword.subject",
                new String[]{mailProperties.getSubjectPrefix()})));
        URL passwordResetUrl = AccountController.getPasswordResetUrl(ghentUser, AbstractPage.getAppRootUrl());
        String messageBody = GreenMailUtil.getBody(message).replaceAll("\r", "");
        assertThat(messageBody, is(getMessage("NotificationService.forgotPassword.body",
                new String[]{passwordResetUrl.toString()})));
    }

    @Test
    public void resetPassword() throws Exception {
        URL passwordResetUrl = AccountController.getPasswordResetUrl(ghentUser, AbstractPage.getAppRootUrl());
        driver.get(passwordResetUrl.toString());
        ResetPasswordPage resetPasswordPage = PageFactory.initElements(driver, ResetPasswordPage.class);

        /*
         * Don't fill any fields.
         */
        resetPasswordPage = resetPasswordPage.resetPassword();
        List<String> errorMessages = getMessages("User.rawPassword.NotNull");
        assertThat(resetPasswordPage.getErrors(), containsInAnyOrder(errorMessages.toArray()));

        /*
         * Try not matching passwords.
         */
        resetPasswordPage.getPassword().sendKeys(NEW_PASSWORD);
        resetPasswordPage.getConfirmPassword().sendKeys(WRONG_PASSWORD);
        resetPasswordPage = resetPasswordPage.resetPassword();
        assertThat(resetPasswordPage.getErrors(), contains(getMessage("User.confirmRawPassword.Match")));

        /*
         * Reset password successfully.
         */
        resetPasswordPage.getPassword().sendKeys(NEW_PASSWORD);
        resetPasswordPage.getConfirmPassword().sendKeys(NEW_PASSWORD);
        resetPasswordPage = resetPasswordPage.resetPassword();
        assertThat(resetPasswordPage.getSuccessMessage().getText(), is(
                getMessage("AccountController.resetPassword.confirmation", new String[]{newUser.getEmail()})));

        ghentUser = userService.findById(ghentUser.getId());
        assertThat(passwordEncoder.matches(NEW_PASSWORD, ghentUser.getPassword()), is(true));
        assertThat(ghentUser.getToken(), isEmptyOrNullString());

        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(1));
        Message message = messages[0];

        assertThat(message.getRecipients(Message.RecipientType.TO),
                is(new InternetAddress[]{new InternetAddress(ghentUser.getEmail())}));
        assertThat(message.getSubject(), is(getMessage("NotificationService.passwordReset.subject",
                new String[]{mailProperties.getSubjectPrefix()})));
        String messageBody = GreenMailUtil.getBody(message).replaceAll("\r", "");
        assertThat(messageBody, is(getMessage("NotificationService.passwordReset.body")));

        /*
         * Try the reset password URL once more.
         */
        driver.get(passwordResetUrl.toString());
        resetPasswordPage = PageFactory.initElements(driver, ResetPasswordPage.class);

        resetPasswordPage.getPassword().sendKeys(NEW_PASSWORD);
        resetPasswordPage.getConfirmPassword().sendKeys(NEW_PASSWORD);
        resetPasswordPage = resetPasswordPage.resetPassword();
        assertThat(resetPasswordPage.getWarningMessage().getText(), is(
                getMessage("AccountController.resetPassword.wrongToken")));
    }
}

package be.ugent.equatic.web;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import be.ugent.equatic.core.MockMvcTest;
import be.ugent.equatic.domain.Authority;
import be.ugent.equatic.domain.Role;
import be.ugent.equatic.domain.User;
import be.ugent.equatic.service.NotificationService;
import be.ugent.equatic.util.UserUtil;
import be.ugent.equatic.web.pages.AbstractPage;
import be.ugent.equatic.web.pages.account.SignInPage;
import be.ugent.equatic.web.pages.superadmin.AdminsPage;
import be.ugent.equatic.web.pages.superadmin.CreateAdminPage;
import be.ugent.equatic.web.pages.superadmin.EditAdminPage;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AdminManagementControllerTest extends MockMvcTest {

    @Autowired
    private JavaMailSenderImpl mailSender;

    private GreenMail testSmtp;

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
    public void adminsListShouldBeComplete() throws Exception {
        AdminsPage adminsPage = SignInPage.signInUser(superAdmin, driver, AdminsPage.class);

        assertThat(adminsPage.getUsernameList(),
                containsInAnyOrder(ghentAdmin.getUsername(), ghentAdmin2.getUsername(), warsawAdmin.getUsername(),
                        oktaAdmin.getUsername()));
    }

    @Test
    public void createInstitutionalAdmin() throws Exception {
        AdminsPage adminsPage = SignInPage.signInUser(superAdmin, driver, AdminsPage.class);

        CreateAdminPage createAdminPage = adminsPage.clickCreate();

        createAdminPage = createAdminPage.submitCreateExpectError();
        assertThat(createAdminPage.getErrors(), containsInAnyOrder(getMessage("User.username.NotEmpty"),
                getMessage("User.firstname.NotEmpty"), getMessage("User.lastname.NotEmpty"),
                getMessage("User.institution.NotNull"), getMessage("User.email.NotEmpty")));

        User grazAdmin = UserUtil.getUser("grazAdmin", grazUniversity, Authority.ROLE_ADMIN_INSTITUTIONAL);
        testAdminCreation(createAdminPage, grazAdmin);
    }

    @Test
    public void createNationalAdmin() throws Exception {
        AdminsPage adminsPage = SignInPage.signInUser(superAdmin, driver, AdminsPage.class);

        CreateAdminPage createAdminPage = adminsPage.clickCreate();

        User polandAdmin = UserUtil.getUser("polandAdmin", polandNationalAgency, Authority.ROLE_ADMIN_NATIONAL);
        testAdminCreation(createAdminPage, polandAdmin);
    }

    private void testAdminCreation(CreateAdminPage createAdminPage, User admin)
            throws MessagingException, MalformedURLException {
        createAdminPage.selectInstitutionByVisibleText(admin.getInstitution());
        createAdminPage.getUsername().sendKeys(admin.getUsername());
        createAdminPage.getFirstname().sendKeys(admin.getFirstname());
        createAdminPage.getLastname().sendKeys(admin.getLastname());
        createAdminPage.getEmail().sendKeys(admin.getEmail());

        AdminsPage adminsPage = createAdminPage.submitCreateExpectSuccess();

        String createConfirmationMessage = getMessage("AdminManagementController.create.confirmation",
                new String[]{admin.getUsername(), admin.getEmail()});
        String emailConfirmationNeededMessage = getMessage("equatic.admin.emailConfirmationNeeded",
                new String[]{admin.getEmail()});
        assertThat(adminsPage.getSuccessMessage().getText(),
                is(createConfirmationMessage + " " + emailConfirmationNeededMessage));

        User createdAdmin = userService.findByUsernameAndInstitution(admin.getUsername(), admin.getInstitution());
        assertThat(createdAdmin, is(admin));

        checkEmailConfirmationNeededNotification(createdAdmin);
    }

    private void checkEmailConfirmationNeededNotification(User admin) throws MessagingException, MalformedURLException {
        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(1));
        Message message = messages[0];

        assertThat(message.getRecipients(Message.RecipientType.TO),
                is(new InternetAddress[]{new InternetAddress(admin.getEmail())}));
        assertThat(message.getSubject(), is(getMessage("NotificationService.emailConfirmationNeeded.subject",
                new String[]{mailProperties.getSubjectPrefix()})));
        URL confirmationUrl = AccountController.getEmailConfirmationUrl(admin, AbstractPage.getAppRootUrl());
        String messageBody = GreenMailUtil.getBody(message).replaceAll("\r", "");
        assertThat(messageBody, is(getMessage("NotificationService.emailConfirmationNeededAdmin.body",
                new String[]{admin.getUsername(), confirmationUrl.toString(),
                        admin.getInstitution().getDisplayName()})));
    }

    @Test
    public void editAdminExcludingEmail() throws Exception {
        AdminsPage adminsPage = SignInPage.signInUser(superAdmin, driver, AdminsPage.class);

        User adminToEdit = ghentAdmin;
        EditAdminPage createEditAdminPage = adminsPage.editUser(adminToEdit, driver, EditAdminPage.class);

        assertThat(createEditAdminPage.getSelectedInstitutionDisplayName(),
                is(adminToEdit.getInstitution().getDisplayName()));
        assertThat(createEditAdminPage.isInstitutionSelectorReadonly(), is(true));

        String addedString = "edited";
        adminToEdit.setUsername(adminToEdit.getUsername() + addedString);
        adminToEdit.setFirstname(adminToEdit.getFirstname() + addedString);
        adminToEdit.setLastname(adminToEdit.getLastname() + addedString);

        createEditAdminPage.getUsername().sendKeys(addedString);
        createEditAdminPage.getFirstname().sendKeys(addedString);
        createEditAdminPage.getLastname().sendKeys(addedString);

        adminsPage = createEditAdminPage.submitEditExpectSuccess();

        String editConfirmationMessage = getMessage("equatic.admin.edit.confirmation", null);
        assertThat(adminsPage.getSuccessMessage().getText(), is(editConfirmationMessage));

        User editedAdmin = userService.findById(adminToEdit.getId());
        assertThat(adminToEdit, is(editedAdmin));

        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(0));
    }

    @Test
    public void tryEditIncorrectAdmin() throws Exception {
        AdminsPage adminsPage = SignInPage.signInUser(superAdmin, driver, AdminsPage.class);

        userService.delete(ghentAdmin);
        EditAdminPage createEditAdminPage = adminsPage.editUser(ghentAdmin, driver, EditAdminPage.class);

        assertThat(createEditAdminPage.getWarningMessage().getText(),
                is(getMessage("equatic.UserNotFoundException.byId", new String[]{ghentAdmin.getId().toString()})));
    }

    @Test
    public void editAdminEmail() throws Exception {
        AdminsPage adminsPage = SignInPage.signInUser(superAdmin, driver, AdminsPage.class);

        User adminToEdit = ghentAdmin;
        EditAdminPage createEditAdminPage = adminsPage.editUser(adminToEdit, driver, EditAdminPage.class);

        String newEmail = "newEmail@institution.com";
        adminToEdit.setEmail(newEmail);
        adminToEdit.setEmailConfirmed(false);
        adminToEdit.setActivated(false);

        createEditAdminPage.getEmail().clear();
        createEditAdminPage.getEmail().sendKeys(newEmail);

        adminsPage = createEditAdminPage.submitEditExpectSuccess();

        String editConfirmationMessage = getMessage("equatic.admin.edit.confirmation", null);
        String emailConfirmationNeededMessage = getMessage("equatic.admin.emailConfirmationNeeded",
                new String[]{adminToEdit.getEmail()});

        assertThat(adminsPage.getSuccessMessage().getText(),
                is(editConfirmationMessage + " " + emailConfirmationNeededMessage));

        User editedAdmin = userService.findById(adminToEdit.getId());
        assertThat(adminToEdit, is(editedAdmin));

        checkEmailConfirmationNeededNotification(editedAdmin);
    }

    @Test
    public void removeInstitutionalAdminPrivilege() throws Exception {
        AdminsPage adminsPage = SignInPage.signInUser(superAdmin, driver, AdminsPage.class);

        User adminToRemovePrivilege = ghentAdmin;
        adminsPage = adminsPage.removeAdminPrivilege(adminToRemovePrivilege, driver);

        String confirmationMessage = getMessage("AdminManagementController.removePrivilege.confirmation",
                new String[]{adminToRemovePrivilege.getUsername()});
        assertThat(adminsPage.getSuccessMessage().getText(), is(confirmationMessage));

        assertThat(adminsPage.getUsernameList(), not(contains(adminToRemovePrivilege.getUsername())));

        User adminAfterRemovingPrivilege = userService.findById(adminToRemovePrivilege.getId());
        adminToRemovePrivilege.setAdminRoles(Collections.<Role>emptyList());
        assertThat(adminAfterRemovingPrivilege, is(adminToRemovePrivilege));

        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(1));
        Message message = messages[0];

        assertThat(message.getRecipients(Message.RecipientType.TO),
                is(new InternetAddress[]{new InternetAddress(adminToRemovePrivilege.getEmail())}));
        assertThat(message.getSubject(), is(getMessage("NotificationService.removePrivilege.subject",
                new String[]{mailProperties.getSubjectPrefix(), adminToRemovePrivilege.getInstitution().getDisplayName()})));
        String messageBody = GreenMailUtil.getBody(message).replaceAll("\r", "");
        assertThat(messageBody, is(getMessage("NotificationService.removePrivilege.body",
                new String[]{adminToRemovePrivilege.getInstitution().getDisplayName()})));
    }

    @Test
    public void adminWithFederatedIdpShouldBeActivatedAndNotified() throws Exception {
        User inactiveAdminWithFederatedIdp = oktaAdmin;
        testAdminActivation(inactiveAdminWithFederatedIdp);

        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(1));
        Message message = messages[0];

        assertThat(message.getRecipients(Message.RecipientType.TO),
                is(new InternetAddress[]{new InternetAddress(inactiveAdminWithFederatedIdp.getEmail())}));
        assertThat(message.getSubject(), is(getMessage("NotificationService.accountActivation.subject",
                new String[]{mailProperties.getSubjectPrefix()})));
        URL signInUrl = AccountController.getSignInUrl(AbstractPage.getAppRootUrl());
        String messageBody = GreenMailUtil.getBody(message).replaceAll("\r", "");
        assertThat(messageBody, is(getMessage("NotificationService.accountActivation.body",
                new String[]{inactiveAdminWithFederatedIdp.getUsername(), NotificationService.getByWho(superAdmin),
                        signInUrl.toString()})));
    }

    @Test
    public void adminWoFederatedIdpShouldBeActivatedAndAbleToResetPassword() throws Exception {
        User inactiveAdminWoFederatedIdp = ghentAdmin2;
        testAdminActivation(inactiveAdminWoFederatedIdp);

        inactiveAdminWoFederatedIdp = userService.findById(inactiveAdminWoFederatedIdp.getId());

        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(1));
        Message message = messages[0];

        assertThat(message.getRecipients(Message.RecipientType.TO),
                is(new InternetAddress[]{new InternetAddress(inactiveAdminWoFederatedIdp.getEmail())}));
        assertThat(message.getSubject(),
                is(getMessage("NotificationService.accountActivation.subject",
                        new String[]{mailProperties.getSubjectPrefix()})));
        URL passwordResetUrl = AccountController.getPasswordResetUrl(inactiveAdminWoFederatedIdp,
                AbstractPage.getAppRootUrl());
        String messageBody = GreenMailUtil.getBody(message).replaceAll("\r", "");
        assertThat(messageBody, is(getMessage("NotificationService.accountActivationAndPasswordReset.body",
                new String[]{inactiveAdminWoFederatedIdp.getUsername(), NotificationService.getByWho(superAdmin),
                        passwordResetUrl.toString()})));
    }

    private void testAdminActivation(User admin) throws Exception {
        AdminsPage adminsPage = SignInPage.signInUser(superAdmin, driver, AdminsPage.class);

        adminsPage = adminsPage.activateUser(admin, driver, AdminsPage.class);
        assertThat(adminsPage.getSuccessMessage().getText(),
                is(getMessage("InstitutionalAdminController.activate.confirmation", new String[]{admin.getUsername()})));

        admin = userService.findById(admin.getId());
        assertThat(admin.isActivated(), is(true));
    }

    @Test
    public void adminShouldBeDeactivatedAndNotified() throws Exception {
        AdminsPage adminsPage = SignInPage.signInUser(superAdmin, driver, AdminsPage.class);

        User activeAdmin = ghentAdmin;
        adminsPage = adminsPage.deactivateUser(activeAdmin, driver, AdminsPage.class);
        assertThat(adminsPage.getSuccessMessage().getText(),
                is(getMessage("InstitutionalAdminController.deactivate.confirmation",
                        new String[]{activeAdmin.getUsername()})));

        activeAdmin = userService.findById(activeAdmin.getId());
        assertThat(activeAdmin.isActivated(), is(false));

        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(1));
        Message message = messages[0];

        assertThat(message.getRecipients(Message.RecipientType.TO),
                is(new InternetAddress[]{new InternetAddress(activeAdmin.getEmail())}));
        assertThat(message.getSubject(), is(getMessage("NotificationService.accountDeactivation.subject",
                new String[]{mailProperties.getSubjectPrefix()})));
        URL signInUrl = AccountController.getSignInUrl(AbstractPage.getAppRootUrl());
        String messageBody = GreenMailUtil.getBody(message).replaceAll("\r", "");
        assertThat(messageBody, is(getMessage("NotificationService.accountDeactivation.body",
                new String[]{"", signInUrl.toString()})));
    }
}

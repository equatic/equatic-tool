package be.ugent.equatic.web;

import com.google.common.base.Joiner;
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
import be.ugent.equatic.security.DatabaseUserDetails;
import be.ugent.equatic.service.NotificationService;
import be.ugent.equatic.util.UserUtil;
import be.ugent.equatic.web.admin.UserManagementController;
import be.ugent.equatic.web.pages.AbstractPage;
import be.ugent.equatic.web.pages.account.SignInPage;
import be.ugent.equatic.web.pages.institutionaladmin.EditUserPage;
import be.ugent.equatic.web.pages.institutionaladmin.UsersPage;
import be.ugent.equatic.web.pages.superadmin.AdminsPage;
import be.ugent.equatic.web.pages.superadmin.SelectInstitutionPage;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import java.net.URL;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserManagementControllerTest extends MockMvcTest {

    @Autowired
    private JavaMailSenderImpl mailSender;

    private GreenMail testSmtp;

    private static final int NO_SUCH_USER_ID = 0;

    private User emailNotConfirmedUser;
    private User ghentInactiveUser;
    private User warsawInactiveUser;

    private String ghentId;

    @Before
    public void setUpUser() {
        emailNotConfirmedUser = UserUtil.getUser("emailNotConfirmed", ghentUniversity);

        ghentInactiveUser = UserUtil.getUser("inactive", ghentUniversity);
        ghentInactiveUser.setEmailConfirmed(true);
        ghentInactiveUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));

        warsawInactiveUser = UserUtil.getUser("inactive", warsawUniversity);
        warsawInactiveUser.setEmailConfirmed(true);
        warsawInactiveUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));

        userService.save(emailNotConfirmedUser, ghentInactiveUser, warsawInactiveUser);

        ghentId = String.valueOf(ghentUniversity.getId());
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
    public void usersListShouldBeComplete() throws Exception {
        UsersPage usersPage = SignInPage.signInUser(ghentAdmin, driver, UsersPage.class);

        assertThat(usersPage.getUsernameList(), containsInAnyOrder(
                ghentUser.getUsername(), emailNotConfirmedUser.getUsername(), ghentInactiveUser.getUsername(),
                ghentAdmin.getUsername(), ghentAdmin2.getUsername()));
    }

    @Test
    public void superAdminShouldGetSelectedInstitutionsUserList() throws Exception {
        UsersPage usersPage = getInstitutionalAdminPageForSuperAdmin();

        assertThat(usersPage.getUsernameList(), containsInAnyOrder(
                warsawUser.getUsername(), warsawInactiveUser.getUsername(), warsawAdmin.getUsername()));
    }

    private UsersPage getInstitutionalAdminPageForSuperAdmin() throws Exception {
        AdminsPage adminsPage = SignInPage.signInUser(superAdmin, driver, AdminsPage.class);
        SelectInstitutionPage selectInstitutionPage = adminsPage.selectActionFromMenu(
                adminsPage.getInstitutionalAdminMenu(), "/admin/institutional/users", SelectInstitutionPage.class);
        return selectInstitutionPage.selectInstitution(warsawUniversity, UsersPage.class);
    }

    @Test
    public void userShouldBeActivatedAndNotified() throws Exception {
        UsersPage usersPage = SignInPage.signInUser(ghentAdmin, driver, UsersPage.class);

        usersPage = usersPage.activateUser(ghentInactiveUser, driver, UsersPage.class);
        assertThat(usersPage.getSuccessMessage().getText(),
                is(getMessage("InstitutionalAdminController.activate.confirmation",
                        new String[]{ghentInactiveUser.getUsername()})));

        ghentInactiveUser = userService.findById(ghentInactiveUser.getId());
        assertThat(ghentInactiveUser.isActivated(), is(true));

        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(1));
        Message message = messages[0];

        assertThat(message.getRecipients(Message.RecipientType.TO),
                is(new InternetAddress[]{new InternetAddress(ghentInactiveUser.getEmail())}));
        assertThat(message.getSubject(), is(getMessage("NotificationService.accountActivation.subject",
                new String[]{mailProperties.getSubjectPrefix()})));
        URL signInUrl = AccountController.getSignInUrl(AbstractPage.getAppRootUrl());
        String messageBody = GreenMailUtil.getBody(message).replaceAll("\r", "");
        assertThat(messageBody, is(getMessage("NotificationService.accountActivation.body",
                new String[]{ghentInactiveUser.getUsername(), NotificationService.getByWho(ghentAdmin),
                        signInUrl.toString()})));
    }

    @Test
    public void addInstitutionalAdminPrivilege() throws Exception {
        UsersPage usersPage = SignInPage.signInUser(ghentAdmin, driver, UsersPage.class);

        User userToAddPrivilege = ghentUser;
        usersPage = usersPage.addAdminPrivilege(userToAddPrivilege, driver);

        String confirmationMessage = getMessage("AdminManagementController.addPrivilege.confirmation",
                new String[]{userToAddPrivilege.getUsername()});
        assertThat(usersPage.getSuccessMessage().getText(), is(confirmationMessage));

        User userAfterAddingPrivilege = userService.findById(userToAddPrivilege.getId());
        Role institutionalAdminRole = new Role(Authority.ROLE_ADMIN_INSTITUTIONAL, userAfterAddingPrivilege);
        userToAddPrivilege.setAdminRoles(Collections.singletonList(institutionalAdminRole));
        assertThat(userAfterAddingPrivilege, is(userToAddPrivilege));

        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(1));
        Message message = messages[0];

        assertThat(message.getRecipients(Message.RecipientType.TO),
                is(new InternetAddress[]{new InternetAddress(userToAddPrivilege.getEmail())}));
        assertThat(message.getSubject(), is(getMessage("NotificationService.addPrivilege.subject",
                new String[]{mailProperties.getSubjectPrefix(), userToAddPrivilege.getInstitution().getDisplayName()})));
        String messageBody = GreenMailUtil.getBody(message).replaceAll("\r", "");
        assertThat(messageBody, is(getMessage("NotificationService.addPrivilege.body",
                new String[]{userToAddPrivilege.getInstitution().getDisplayName()})));
    }

    @Test
    public void removeInstitutionalAdminPrivilege() throws Exception {
        UsersPage usersPage = SignInPage.signInUser(ghentAdmin, driver, UsersPage.class);

        User adminToRemovePrivilege = ghentAdmin2;
        usersPage = usersPage.removeAdminPrivilege(adminToRemovePrivilege, driver);

        String confirmationMessage = getMessage("AdminManagementController.removePrivilege.confirmation",
                new String[]{adminToRemovePrivilege.getUsername()});
        assertThat(usersPage.getSuccessMessage().getText(), is(confirmationMessage));

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
    public void superAdminCanActivateUser() throws Exception {
        UsersPage usersPage = getInstitutionalAdminPageForSuperAdmin();

        usersPage.activateUser(warsawInactiveUser, driver, UsersPage.class);
        assertThat(usersPage.getSuccessMessage().getText(),
                is(getMessage("InstitutionalAdminController.activate.confirmation",
                        new String[]{warsawInactiveUser.getUsername()})));

        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(1));
        Message message = messages[0];

        URL signInUrl = AccountController.getSignInUrl(AbstractPage.getAppRootUrl());
        String messageBody = GreenMailUtil.getBody(message).replaceAll("\r", "");
        assertThat(messageBody, is(getMessage("NotificationService.accountActivation.body",
                new String[]{warsawInactiveUser.getUsername(), NotificationService.getByWho(superAdmin),
                        signInUrl.toString()})));
    }

    @Test
    public void editUserExcludingEmail() throws Exception {
        UsersPage usersPage = SignInPage.signInUser(ghentAdmin, driver, UsersPage.class);

        User userToEdit = ghentUser;
        EditUserPage editUserPage = usersPage.editUser(userToEdit, driver, EditUserPage.class);

        assertThat(editUserPage.getSelectedInstitutionDisplayName(), is(userToEdit.getInstitution().getDisplayName()));
        assertThat(editUserPage.isInstitutionSelectorReadonly(), is(true));

        String addedString = "edited";
        userToEdit.setUsername(userToEdit.getUsername() + addedString);
        userToEdit.setFirstname(userToEdit.getFirstname() + addedString);
        userToEdit.setLastname(userToEdit.getLastname() + addedString);

        editUserPage.getUsername().sendKeys(addedString);
        editUserPage.getFirstname().sendKeys(addedString);
        editUserPage.getLastname().sendKeys(addedString);

        usersPage = editUserPage.submitEditExpectSuccess();

        String editConfirmationMessage = getMessage("equatic.admin.edit.confirmation", null);
        assertThat(usersPage.getSuccessMessage().getText(), is(editConfirmationMessage));

        User editedUser = userService.findById(userToEdit.getId());
        assertThat(userToEdit, is(editedUser));

        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(0));
    }

    @Test
    public void editUserEmail() throws Exception {
        UsersPage usersPage = SignInPage.signInUser(ghentAdmin, driver, UsersPage.class);

        User userToEdit = ghentUser;
        EditUserPage editUserPage = usersPage.editUser(userToEdit, driver, EditUserPage.class);

        String newEmail = "newEmail@institution.com";
        userToEdit.setEmail(newEmail);
        userToEdit.setEmailConfirmed(false);
        userToEdit.setActivated(false);

        editUserPage.getEmail().clear();
        editUserPage.getEmail().sendKeys(newEmail);

        usersPage = editUserPage.submitEditExpectSuccess();

        String editConfirmationMessage = getMessage("equatic.admin.edit.confirmation", null);
        String emailConfirmationNeededMessage = getMessage("equatic.admin.emailConfirmationNeeded",
                new String[]{userToEdit.getEmail()});

        assertThat(usersPage.getSuccessMessage().getText(),
                is(editConfirmationMessage + " " + emailConfirmationNeededMessage));

        User editedUser = userService.findById(userToEdit.getId());
        assertThat(userToEdit, is(editedUser));

        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(1));
        Message message = messages[0];

        assertThat(message.getRecipients(Message.RecipientType.TO),
                is(new InternetAddress[]{new InternetAddress(editedUser.getEmail())}));
        assertThat(message.getSubject(), is(getMessage("NotificationService.emailConfirmationNeeded.subject",
                new String[]{mailProperties.getSubjectPrefix()})));
        URL confirmationUrl = AccountController.getEmailConfirmationUrl(editedUser, AbstractPage.getAppRootUrl());
        String messageBody = GreenMailUtil.getBody(message).replaceAll("\r", "");
        assertThat(messageBody, is(getMessage("NotificationService.emailConfirmationNeeded.body",
                new String[]{confirmationUrl.toString(), ghentUniversity.getDisplayName(),
                        Joiner.on(", ").join(userService.getInstitutionalAdminNames(ghentUniversity))})));
    }

    @Test
    public void userActivationForWrongUserIdShouldFail() throws Exception {
        mockMvc.perform(post(UserManagementController.VIEW_USER, UserManagementController.AdminType.institutional,
                String.valueOf(NO_SUCH_USER_ID))
                .with(csrf()).param("institution", ghentId).param("activate", "")
                .with(user(new DatabaseUserDetails(ghentAdmin))))
                .andExpect(status().isNotFound());
    }

    @Test
    public void userActivationForUserFromDifferentInstitutionShouldFail() throws Exception {
        mockMvc.perform(post(UserManagementController.VIEW_USER, UserManagementController.AdminType.institutional,
                String.valueOf(warsawUser.getId()))
                .with(csrf()).param("institution", ghentId).param("activate", "")
                .with(user(new DatabaseUserDetails(ghentAdmin))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void userActivationForUserWoConfirmedEmailShouldFail() throws Exception {
        mockMvc.perform(post(UserManagementController.VIEW_USER, UserManagementController.AdminType.institutional,
                String.valueOf(emailNotConfirmedUser.getId()))
                .with(csrf()).param("institution", ghentId).param("activate", "")
                .with(user(new DatabaseUserDetails(ghentAdmin))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void userActivationForUserThatIsActivatedShouldFail() throws Exception {
        mockMvc.perform(post(UserManagementController.VIEW_USER, UserManagementController.AdminType.institutional,
                String.valueOf(ghentUser.getId()))
                .with(csrf()).param("institution", ghentId).param("activate", "")
                .with(user(new DatabaseUserDetails(ghentAdmin))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void userShouldBeDeactivatedAndNotified() throws Exception {
        UsersPage usersPage = SignInPage.signInUser(ghentAdmin, driver, UsersPage.class);

        usersPage = usersPage.deactivateUser(ghentUser, driver, UsersPage.class);
        assertThat(usersPage.getSuccessMessage().getText(),
                is(getMessage("InstitutionalAdminController.deactivate.confirmation", new String[]{ghentUser.getUsername()})));

        ghentUser = userService.findById(ghentUser.getId());
        assertThat(ghentUser.isActivated(), is(false));

        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(1));
        Message message = messages[0];

        assertThat(message.getRecipients(Message.RecipientType.TO),
                is(new InternetAddress[]{new InternetAddress(ghentUser.getEmail())}));
        assertThat(message.getSubject(), is(getMessage("NotificationService.accountDeactivation.subject",
                new String[]{mailProperties.getSubjectPrefix()})));
        String messageBody = GreenMailUtil.getBody(message).replaceAll("\r", "");
        assertThat(messageBody, is(getMessage("NotificationService.accountDeactivation.body",
                new String[]{" by " + ghentAdmin.getDisplayName()})));
    }

    @Test
    public void superAdminCanDeactivateUser() throws Exception {
        UsersPage usersPage = getInstitutionalAdminPageForSuperAdmin();

        usersPage.deactivateUser(warsawUser, driver, UsersPage.class);
        assertThat(usersPage.getSuccessMessage().getText(),
                is(getMessage("InstitutionalAdminController.deactivate.confirmation", new String[]{warsawUser.getUsername()})));

        Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(1));
        Message message = messages[0];

        String messageBody = GreenMailUtil.getBody(message).replaceAll("\r", "");
        assertThat(messageBody,
                is(getMessage("NotificationService.accountDeactivation.body", new String[]{""})));
    }

    @Test
    public void userDeactivationForWrongUserIdShouldFail() throws Exception {
        mockMvc.perform(post(UserManagementController.VIEW_USER, UserManagementController.AdminType.institutional,
                String.valueOf(NO_SUCH_USER_ID))
                .with(csrf()).param("institution", ghentId).param("deactivate", "")
                .with(user(new DatabaseUserDetails(ghentAdmin))))
                .andExpect(status().isNotFound());
    }

    @Test
    public void userDeactivationForUserFromDifferentInstitutionShouldFail() throws Exception {
        mockMvc.perform(post(UserManagementController.VIEW_USER, UserManagementController.AdminType.institutional,
                String.valueOf(warsawUser.getId()))
                .with(csrf()).param("institution", ghentId).param("deactivate", "")
                .with(user(new DatabaseUserDetails(ghentAdmin))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void userDeactivationForUserWoConfirmedEmailShouldFail() throws Exception {
        mockMvc.perform(post(UserManagementController.VIEW_USER, UserManagementController.AdminType.institutional,
                String.valueOf(emailNotConfirmedUser.getId()))
                .with(csrf()).param("institution", ghentId).param("deactivate", "")
                .with(user(new DatabaseUserDetails(ghentAdmin))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void userDeactivationForUserThatIsNotActivatedShouldFail() throws Exception {
        mockMvc.perform(post(UserManagementController.VIEW_USER, UserManagementController.AdminType.institutional,
                String.valueOf(ghentInactiveUser.getId()))
                .with(csrf()).param("institution", ghentId).param("deactivate", "")
                .with(user(new DatabaseUserDetails(ghentAdmin))))
                .andExpect(status().isForbidden());
    }
}

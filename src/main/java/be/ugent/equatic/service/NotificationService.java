package be.ugent.equatic.service;

import com.google.common.base.Joiner;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import be.ugent.equatic.config.MailProperties;
import be.ugent.equatic.domain.*;
import be.ugent.equatic.exception.DataSheetInternalErrorException;
import be.ugent.equatic.util.UrlUtil;
import be.ugent.equatic.web.AccountController;
import be.ugent.equatic.web.admin.superadmin.AdminManagementController;
import be.ugent.equatic.web.util.SearchedInstitution;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class NotificationService {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Autowired
    private MailProperties mailProperties;

    @Autowired
    private DataSheetService dataSheetService;

    /**
     * Sends an e-mail to user to inform him that his account has been activated.
     *
     * @param user   user who's account is being activated
     * @param admin  admin who has activated the account
     * @param locale the Locale
     */
    void sendActivationNotification(User user, User admin, Locale locale, HttpServletRequest request)
            throws MalformedURLException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getReplyAddress());
        message.setTo(user.getEmail());
        message.setSubject(
                messageSource.getMessage("NotificationService.accountActivation.subject",
                        new String[]{mailProperties.getSubjectPrefix()}, locale));

        URL signInUrl = AccountController.getSignInUrl(UrlUtil.getRootUrlFromRequest(request));

        message.setText(messageSource.getMessage("NotificationService.accountActivation.body",
                new String[]{user.getUsername(), getByWho(admin), signInUrl.toString()}, locale));

        mailSender.send(message);
    }

    /**
     * Sends an e-mail to user to inform him that his account has been deactivated.
     *
     * @param user   user who's account is being deactivated
     * @param admin  admin who has deactivated the account
     * @param locale the Locale
     */

    void sendDeactivationNotification(User user, User admin, Locale locale) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getReplyAddress());
        message.setTo(user.getEmail());
        message.setSubject(
                messageSource.getMessage("NotificationService.accountDeactivation.subject",
                        new String[]{mailProperties.getSubjectPrefix()}, locale));

        message.setText(messageSource.getMessage("NotificationService.accountDeactivation.body",
                new String[]{getByWho(admin)}, locale));

        mailSender.send(message);
    }

    public static String getByWho(User admin) {
        String byWho = "";
        if (!admin.isSuperAdmin()) {
            byWho = " by " + admin.getDisplayName();
        }
        return byWho;
    }

    void sendNotFoundInstitutionsNotification(User admin, DataSheet dataSheet, Institution institution,
                                              Set<SearchedInstitution> notFoundInstitutions, Locale locale) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);

            messageHelper.setFrom(mailProperties.getReplyAddress());
            List<User> superAdmins = userService.findSuperAdmins();
            messageHelper.setTo(superAdmins.stream().map(User::getEmail).toArray(String[]::new));
            messageHelper.setSubject(
                    messageSource.getMessage("NotificationService.institutionNotFound.subject",
                            new String[]{mailProperties.getSubjectPrefix()}, locale));

            String dataSheetName = messageSource.getMessage(
                    "equatic.admin.uploadData." + dataSheet.getCode().name(), null, locale);
            messageHelper.setText(messageSource.getMessage("NotificationService.institutionNotFound.body",
                    new String[]{dataSheetName, institution.getDisplayName(), admin.getDisplayName(), admin.getEmail()},
                    locale));

            Sheet notFoundInstitutionsSheet = getSearchedInstitutionsSheet(notFoundInstitutions);
            Workbook workbook = notFoundInstitutionsSheet.getWorkbook();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            messageHelper.addAttachment("notFoundInstitutions.xls", new ByteArrayResource(outputStream.toByteArray()),
                    "application/vnd.ms-excel;charset=UTF-8");

            mailSender.send(message);
        } catch (MessagingException | IOException exception) {
            throw new DataSheetInternalErrorException(exception);
        }
    }

    private Sheet getSearchedInstitutionsSheet(Set<SearchedInstitution> searchedInstitutions) {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        DataSheet institutionsDataSheet = dataSheetService.findByCode(DataSheetCode.INSTITUTIONS);
        List<DataSheetColumn> dataSheetColumns = institutionsDataSheet.getCurrentColumns();

        Row columnsRow = sheet.createRow(0);

        int celln = 0;
        for (DataSheetColumn dataSheetColumn : dataSheetColumns) {
            columnsRow.createCell(celln).setCellValue(dataSheetColumn.getTitle());
            celln++;
        }

        int rown = 1;
        for (SearchedInstitution searchedInstitution : searchedInstitutions) {
            Row row = sheet.createRow(rown);

            celln = 0;
            for (DataSheetColumn dataSheetColumn : dataSheetColumns) {
                String cellValue = null;

                switch (dataSheetColumn.getCode()) {
                    case PIC:
                        cellValue = searchedInstitution.getPic();
                        break;

                    case ERASMUS_CODE:
                        cellValue = searchedInstitution.getErasmusCode();
                        break;

                    case NAME:
                        cellValue = searchedInstitution.getLegalName();
                        break;

                    case COUNTRY_CODE:
                        cellValue = searchedInstitution.getCountryCode();
                        break;
                }

                row.createCell(celln).setCellValue(cellValue);
                celln++;
            }

            rown++;
        }

        return sheet;
    }

    /**
     * Sends an e-mail to user to inform that he has successfuly reset his password.
     *
     * @param user   the User
     * @param locale the Locale
     */
    public void sendPasswordHasBeenResetNotification(User user, Locale locale) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getReplyAddress());
        message.setTo(user.getEmail());
        message.setSubject(messageSource.getMessage("NotificationService.passwordReset.subject",
                new String[]{mailProperties.getSubjectPrefix()}, locale));
        message.setText(messageSource.getMessage("NotificationService.passwordReset.body", null, locale));

        mailSender.send(message);
    }

    /**
     * Sends an e-mail to user to inform that he can reset his/her password by clicking a link.
     *
     * @param user    the User
     * @param locale  the Locale
     * @param request the HttpServletRequest
     * @throws MalformedURLException when the URL is malformed
     */
    public void sendPasswordResetNotification(User user, Locale locale, HttpServletRequest request)
            throws MalformedURLException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getReplyAddress());
        message.setTo(user.getEmail());
        message.setSubject(messageSource.getMessage("NotificationService.forgotPassword.subject",
                new String[]{mailProperties.getSubjectPrefix()}, locale));

        URL passwordResetUrl = AccountController.getPasswordResetUrl(user, UrlUtil.getRootUrlFromRequest(request));

        message.setText(messageSource.getMessage("NotificationService.forgotPassword.body",
                new String[]{passwordResetUrl.toString()}, locale));

        mailSender.send(message);
    }

    /**
     * Sends an e-mail to user to inform that his/her account has been activated and he should reset the password.
     *
     * @param target  the User
     * @param admin   the Admin
     * @param locale  the Locale
     * @param request the HttpServletRequest
     * @throws MalformedURLException when the URL is malformed
     */
    void sendActivationAndPasswordResetNotification(User target, User admin, Locale locale,
                                                    HttpServletRequest request) throws MalformedURLException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getReplyAddress());
        message.setTo(target.getEmail());
        message.setSubject(messageSource.getMessage("NotificationService.accountActivation.subject",
                new String[]{mailProperties.getSubjectPrefix()}, locale));

        URL passwordResetUrl = AccountController.getPasswordResetUrl(target, UrlUtil.getRootUrlFromRequest(request));

        message.setText(messageSource.getMessage("NotificationService.accountActivationAndPasswordReset.body",
                new String[]{target.getUsername(), getByWho(admin), passwordResetUrl.toString()}, locale));

        mailSender.send(message);
    }

    /**
     * Sends an e-mail to the institutional administrators of user's institution that a new user has registered.
     *
     * @param user    the User
     * @param locale  the Locale
     * @param request the HttpServletRequest
     * @throws MalformedURLException when the URL is malformed
     */
    public void sendNewUserNotification(User user, Locale locale, HttpServletRequest request)
            throws MalformedURLException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getReplyAddress());
        List<User> institutionalAdmins = userService.findInstitutionalAdmins(user.getInstitution());
        message.setTo(institutionalAdmins.stream().map(User::getEmail).toArray(String[]::new));
        message.setSubject(messageSource.getMessage("NotificationService.newUser.subject",
                new String[]{mailProperties.getSubjectPrefix()}, locale));

        message.setText(messageSource.getMessage("NotificationService.newUser.body",
                new String[]{user.getUsername(), user.getFirstname(), user.getLastname(), user.getEmail()}, locale));

        mailSender.send(message);
    }

    /**
     * Sends an e-mail to the super administrators that a new admin has confirmed his/her e-mail.
     *
     * @param admin   the User
     * @param locale  the Locale
     * @param request the HttpServletRequest
     * @throws MalformedURLException when the URL is malformed
     */
    public void sendAdminCreatedBySuperAdminNotification(User admin, Locale locale, HttpServletRequest request)
            throws MalformedURLException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getReplyAddress());
        List<User> superAdmins = userService.findSuperAdmins();
        message.setTo(superAdmins.stream().map(User::getEmail).toArray(String[]::new));
        message.setSubject(messageSource.getMessage("NotificationService.adminConfirmedEmail.subject",
                new String[]{mailProperties.getSubjectPrefix()}, locale));

        URL adminsListUrl = AdminManagementController.getAdminsListUrl(UrlUtil.getRootUrlFromRequest(request));

        message.setText(messageSource.getMessage("NotificationService.adminConfirmedEmail.body",
                new String[]{admin.getDisplayName(), adminsListUrl.toString()}, locale));

        mailSender.send(message);
    }

    /**
     * Sends an e-mail to user to inform that he has to confirm his e-mail by clicking a link.
     *
     * @param user    the User
     * @param locale  the Locale
     * @param request the HttpServletRequest
     * @throws MalformedURLException when the URL is malformed
     */
    public void sendConfirmationNeededEmail(User user, Locale locale, HttpServletRequest request)
            throws MalformedURLException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getReplyAddress());
        message.setTo(user.getEmail());
        message.setSubject(
                messageSource.getMessage("NotificationService.emailConfirmationNeeded.subject",
                        new String[]{mailProperties.getSubjectPrefix()}, locale));

        URL emailConfirmationUrl =
                AccountController.getEmailConfirmationUrl(user, UrlUtil.getRootUrlFromRequest(request));
        Institution institution = user.getInstitution();

        message.setText(messageSource.getMessage("NotificationService.emailConfirmationNeeded.body",
                new String[]{emailConfirmationUrl.toString(), institution.getDisplayName(),
                        Joiner.on(", ").join(userService.getInstitutionalAdminNames(institution))}, locale));

        mailSender.send(message);
    }

    /**
     * Sends an e-mail to admin to inform that he has to confirm his e-mail by clicking a link.
     *
     * @param admin   the User
     * @param locale  the Locale
     * @param request the HttpServletRequest
     * @throws MalformedURLException when the URL is malformed
     */
    public void sendConfirmationNeededAdminEmail(User admin, Locale locale, HttpServletRequest request)
            throws MalformedURLException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getReplyAddress());
        message.setTo(admin.getEmail());
        message.setSubject(
                messageSource.getMessage("NotificationService.emailConfirmationNeeded.subject",
                        new String[]{mailProperties.getSubjectPrefix()}, locale));

        URL emailConfirmationUrl =
                AccountController.getEmailConfirmationUrl(admin, UrlUtil.getRootUrlFromRequest(request));
        Institution institution = admin.getInstitution();

        message.setText(messageSource.getMessage("NotificationService.emailConfirmationNeededAdmin.body",
                new String[]{admin.getUsername(), emailConfirmationUrl.toString(), institution.getDisplayName()},
                locale));

        mailSender.send(message);
    }

    /**
     * Sends an e-mail to admin to inform that he is no longer an admin of an institution.
     *
     * @param admin  the admin
     * @param locale the Locale
     */
    public void sendRemovedPrivilegeNotification(User admin, Locale locale) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getReplyAddress());
        message.setTo(admin.getEmail());
        String institutionDisplayName = admin.getInstitution().getDisplayName();
        message.setSubject(messageSource.getMessage("NotificationService.removePrivilege.subject",
                new String[]{mailProperties.getSubjectPrefix(), institutionDisplayName}, locale));

        message.setText(messageSource.getMessage("NotificationService.removePrivilege.body",
                new String[]{institutionDisplayName}, locale));

        mailSender.send(message);
    }

    /**
     * Sends an e-mail to admin to inform that he is now a institutional admin.
     *
     * @param admin  the admin
     * @param locale the Locale
     */
    public void sendAddedPrivilegeNotification(User admin, Locale locale) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getReplyAddress());
        message.setTo(admin.getEmail());
        String institutionDisplayName = admin.getInstitution().getDisplayName();
        message.setSubject(messageSource.getMessage("NotificationService.addPrivilege.subject",
                new String[]{mailProperties.getSubjectPrefix(), institutionDisplayName}, locale));

        message.setText(messageSource.getMessage("NotificationService.addPrivilege.body",
                new String[]{institutionDisplayName}, locale));

        mailSender.send(message);
    }
}

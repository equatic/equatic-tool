package be.ugent.equatic.web;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import be.ugent.equatic.config.WebSecurityConfig;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.User;
import be.ugent.equatic.exception.FederatedAuthenticationException;
import be.ugent.equatic.exception.UserNotFoundException;
import be.ugent.equatic.service.InstitutionService;
import be.ugent.equatic.service.NotificationService;
import be.ugent.equatic.service.UserService;
import be.ugent.equatic.validation.PasswordValidator;
import be.ugent.equatic.validation.UserAdditionalValidator;
import be.ugent.equatic.web.util.Message;
import be.ugent.equatic.web.util.PasswordForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

/**
 * Controls all operations on user accounts: sign in, registration, e-mail confirmation.
 */
@Controller
public class AccountController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private NotificationService notificationService;

    public static final String VIEW_PATH = "/account";

    public static final String VIEW_LOGIN = VIEW_PATH + "/login";
    public static final String VIEW_LOGOUT = VIEW_PATH + "/logout";
    public static final String VIEW_REGISTER = VIEW_PATH + "/register";
    private static final String VIEW_CONFIRMATION = VIEW_PATH + "/confirmation";
    public static final String VIEW_FORGOT_PASSWORD = VIEW_PATH + "/forgotPassword";
    public static final String VIEW_RESET_PASSWORD = VIEW_PATH + "/resetPassword";
    private static final String AJAX_ACTIVE_INSTITUTIONS = VIEW_PATH + "/institutions/ajax";

    private static final String SESSION_USERNAME = "username";

    /**
     * Sign in page.
     * Also handles authentication failures and logout.
     *
     * @param session the HttpSession
     * @param model   the Model
     * @param logout  remark: this is an optional request param
     * @param locale  the Locale
     * @return String
     */
    @RequestMapping(value = VIEW_LOGIN, method = RequestMethod.GET)
    public String login(HttpSession session, Model model, @RequestParam(required = false) String logout,
                        Locale locale, RedirectAttributes redirect) {
        Exception exception = (Exception) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

        if (exception instanceof FederatedAuthenticationException) {
            exception = (Exception) ExceptionUtils.getRootCause(exception);
            if (exception instanceof UserNotFoundException) {
                /*
                 * If user successfully signed in through federated IdP but wasn't found then he has to register.
                 */
                UserNotFoundException userNotFoundException = (UserNotFoundException) exception;
                /*
                 * Save username and institution ID to use it on the registration page.
                 */
                session.setAttribute(SESSION_USERNAME, userNotFoundException.getUsername());
                redirect.addAttribute("firstname", userNotFoundException.getFirstName());
                redirect.addAttribute("lastname", userNotFoundException.getLastName());
                redirect.addAttribute("email", userNotFoundException.getEmail());
                redirect.addAttribute("institution", userNotFoundException.getInstitution().getId());

                return "redirect:" + VIEW_REGISTER;
            }
        }

        if (logout != null) {
            model.addAttribute("message",
                    Message.success(messageSource.getMessage("AccountController.logout.confirmation", null, locale)));
        }
        if (exception != null) {
            model.addAttribute("message", Message.danger(exception.getMessage()));
        }
        return VIEW_LOGIN;
    }

    @RequestMapping(value = AJAX_ACTIVE_INSTITUTIONS, method = RequestMethod.GET)
    public @ResponseBody
    List<Institution> ajaxActiveInstitutionsList() {
        return institutionService.findActive();
    }

    /**
     * Handles register button by checking if selected institution uses federated IdP and redirecting appropriately.
     * <p>
     * If institution doesn't use federated IdP then displays registration form.
     * If institution uses federated IdP then redirects to federated sign in page.
     * </p>
     *
     * @param institutionId the institution's ID
     * @param session       user's session
     * @param redirect      the RedirectAttributes
     * @param user          user data
     * @param model         the model
     * @return String
     */
    @RequestMapping(value = VIEW_REGISTER, method = RequestMethod.GET, params = "institution")
    public String registerAtInstitution(@RequestParam("institution") Long institutionId, HttpSession session,
                                        RedirectAttributes redirect, @ModelAttribute User user, Model model) {
        Institution institution = institutionService.findById(institutionId);
        if (institution.isWithFederatedIdP()) {
            String username = (String) session.getAttribute(SESSION_USERNAME);
            if (username != null) {
                user.setUsername(username);
                model.addAttribute("user", user);

                return VIEW_REGISTER;
            } else {
                redirect.addAttribute("idp", institution.getIdpEntityId());

                return "redirect:" + WebSecurityConfig.SAML_LOGIN_URL;
            }
        } else {
            user.setInstitution(institution);

            return VIEW_REGISTER;
        }
    }

    /**
     * Confirms user's e-mail if the token is correct.
     *
     * @param token   token sent to the user by e-mail
     * @param model   the Model
     * @param request the HttpServletRequest
     * @param locale  the Locale
     * @return String
     * @throws MalformedURLException when the URL is malformed
     */
    @RequestMapping(value = VIEW_REGISTER, method = RequestMethod.GET, params = "token")
    public String confirmEmail(@RequestParam String token, Model model, HttpServletRequest request, Locale locale)
            throws MalformedURLException {
        try {
            User user = userService.findByToken(token);
            user.setEmailConfirmed(true);
            user.setToken(null);
            userService.save(user);

            if (userService.hasBeenCreatedBySuperAdmin(user)) {
                notificationService.sendAdminCreatedBySuperAdminNotification(user, locale, request);
            } else {
                notificationService.sendNewUserNotification(user, locale, request);
            }

            String emailConfirmedMessage = messageSource.getMessage("AccountController.register.emailConfirmed", null,
                    locale);
            model.addAttribute("message", Message.success(emailConfirmedMessage));
        } catch (UserNotFoundException exception) {
            String wrongConfirmationMessage = messageSource.getMessage("AccountController.register.confirmationWrong",
                    null, locale);
            model.addAttribute("message", Message.warning(wrongConfirmationMessage));
        }

        return VIEW_CONFIRMATION;
    }

    /**
     * Registers user account and sends him a notification.
     *
     * @param user    the User
     * @param result  the BindingResult
     * @param model   the Model
     * @param request the HttpServletRequest
     * @param locale  the Locale
     * @return String
     * @throws MalformedURLException when the URL is malformed
     */
    @RequestMapping(value = VIEW_REGISTER, method = RequestMethod.POST)
    public String register(@Valid User user, BindingResult result, Model model, HttpServletRequest request,
                           Locale locale)
            throws MalformedURLException {
        UserAdditionalValidator userAdditionalValidator = new UserAdditionalValidator(userService);
        userAdditionalValidator.validate(user, result);

        if (result.hasErrors()) {
            return VIEW_REGISTER;
        }

        user.generateToken();
        this.userService.save(user);

        notificationService.sendConfirmationNeededEmail(user, locale, request);

        String confirmationNeededMessage = messageSource.getMessage("AccountController.register.confirmation",
                new String[]{user.getEmail()}, locale);
        model.addAttribute("message", Message.success(confirmationNeededMessage));
        return VIEW_CONFIRMATION;
    }

    /**
     * Displays forgot password form.
     *
     * @param institutionId the institution's ID
     * @param model         the model
     * @return String
     */
    @RequestMapping(value = VIEW_FORGOT_PASSWORD, method = RequestMethod.GET, params = "institution")
    public String forgotPasswordAtInstitution(@RequestParam("institution") Long institutionId, Model model) {
        Institution institution = institutionService.findByIdNotWithFederatedIdP(institutionId);
        model.addAttribute("institution", institution);

        return VIEW_FORGOT_PASSWORD;
    }

    /**
     * Handles forgot password form.
     * <p>
     * Sends an e-mail with a link to reset password action.
     * Displays e-mail sent confirmation
     * </p>
     *
     * @param institution user's institution
     * @param email       user's email
     * @param model       the Model
     * @param locale      the Locale
     * @return String
     */
    @RequestMapping(value = VIEW_FORGOT_PASSWORD, method = RequestMethod.POST)
    public String forgotPassword(@RequestParam("institution") Institution institution,
                                 @RequestParam("email") String email, RedirectAttributes redirect, Model model,
                                 HttpServletRequest request, Locale locale) throws MalformedURLException {
        try {
            User user = userService.findByEmailIgnoreCaseAndInstitution(email, institution);

            if (!user.isEmailConfirmed()) {
                String usersEmailNotConfirmedMessage = messageSource.getMessage(
                        "AccountController.forgotPassword.usersEmailNotConfirmed", null, locale);
                redirect.addFlashAttribute("message", Message.danger(usersEmailNotConfirmedMessage));
                redirect.addAttribute("institution", institution.getId());

                return "redirect:" + VIEW_FORGOT_PASSWORD;
            }

            user.generateToken();
            userService.save(user);

            notificationService.sendPasswordResetNotification(user, locale, request);

            String resetPasswordLinkSentMessage = messageSource.getMessage(
                    "AccountController.forgotPassword.confirmation", new String[]{user.getEmail()}, locale);
            model.addAttribute("message", Message.success(resetPasswordLinkSentMessage));

            return VIEW_CONFIRMATION;
        } catch (UserNotFoundException exception) {
            String userNotFoundMessage = messageSource.getMessage("equatic.emailNotFoundAtInstitution",
                    new String[]{email}, locale);
            redirect.addFlashAttribute("message", Message.danger(userNotFoundMessage));
            redirect.addAttribute("institution", institution.getId());

            return "redirect:" + VIEW_FORGOT_PASSWORD;
        }
    }

    /**
     * Displays password reset form.
     *
     * @param passwordForm form for new password and it's confirmation
     * @param token        token sent to the user by e-mail
     * @param model        the Model
     * @return String
     */
    @RequestMapping(value = VIEW_RESET_PASSWORD, method = RequestMethod.GET, params = "token")
    private String resetPassword(@ModelAttribute PasswordForm passwordForm, @RequestParam String token, Model model)
            throws MalformedURLException {
        model.addAttribute("token", token);

        return VIEW_RESET_PASSWORD;
    }

    /**
     * Resets user's password.
     *
     * @param passwordForm form with new password and it's confirmation
     * @param token        token sent to the user by e-mail
     * @param result       form validation results
     * @param model        the Model
     * @param locale       the Locale
     * @return String
     */
    @RequestMapping(value = VIEW_RESET_PASSWORD, method = RequestMethod.POST, params = "token")
    public String resetPassword(@Valid PasswordForm passwordForm, @RequestParam String token, BindingResult result,
                                Model model, Locale locale)
            throws MalformedURLException {
        try {
            PasswordValidator validator = new PasswordValidator();
            validator.validate(passwordForm, result);

            if (result.hasErrors()) {
                return resetPassword(passwordForm, token, model);
            }

            User user = userService.findByToken(token);
            user.setPassword(passwordEncoder.encode(passwordForm.getRawPassword()));
            user.setToken(null);
            userService.save(user);

            notificationService.sendPasswordHasBeenResetNotification(user, locale);

            String resetPasswordMessage = messageSource.getMessage("AccountController.resetPassword.confirmation", null,
                    locale);
            model.addAttribute("message", Message.success(resetPasswordMessage));
        } catch (UserNotFoundException exception) {
            String wrongTokenMessage = messageSource.getMessage("AccountController.resetPassword.wrongToken", null,
                    locale);
            model.addAttribute("message", Message.warning(wrongTokenMessage));
        }

        return VIEW_CONFIRMATION;
    }

    public static URL getEmailConfirmationUrl(User user, URL rootUrl) throws MalformedURLException {
        return new URL(rootUrl + VIEW_REGISTER + "?token=" + user.getToken());
    }

    public static URL getPasswordResetUrl(User user, URL rootUrl) throws MalformedURLException {
        return new URL(rootUrl + VIEW_RESET_PASSWORD + "?token=" + user.getToken());
    }

    public static URL getSignInUrl(URL rootUrl)
            throws MalformedURLException {
        return new URL(rootUrl + VIEW_LOGIN);
    }
}

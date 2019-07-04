package be.ugent.equatic.web.admin.superadmin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import be.ugent.equatic.domain.Authority;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.Role;
import be.ugent.equatic.domain.User;
import be.ugent.equatic.exception.AccessDeniedException;
import be.ugent.equatic.exception.ResourceNotFoundException;
import be.ugent.equatic.exception.UserNotFoundException;
import be.ugent.equatic.service.InstitutionService;
import be.ugent.equatic.service.NotificationService;
import be.ugent.equatic.service.UserActivationManagementService;
import be.ugent.equatic.service.UserService;
import be.ugent.equatic.web.util.Message;
import be.ugent.equatic.web.util.MessageType;
import be.ugent.equatic.web.util.PrincipalUtil;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Controller
public class AdminManagementController extends SuperAdminController {

    public static final String VIEW_ADMINS_LIST = VIEW_PATH + "/admins";
    public static final String VIEW_CREATE_ADMIN = VIEW_ADMINS_LIST + "/create";
    private static final String VIEW_ADMIN = VIEW_ADMINS_LIST + "/{user}";
    public static final String VIEW_EDIT_ADMIN = VIEW_ADMIN + "/edit";

    private static final String AJAX_INSTITUTIONS_WO_ADMINS = VIEW_PATH + "/institutionsWoAdmins/ajax";

    @Autowired
    private UserService userService;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private UserActivationManagementService userActivationManagementService;

    @Autowired
    private NotificationService notificationService;

    @RequestMapping(value = VIEW_ADMINS_LIST, method = RequestMethod.GET)
    public String admins(Model model) {
        model.addAttribute("admins", userService.findAdmins());

        return VIEW_ADMINS_LIST;
    }

    /**
     * Displays admin create form.
     *
     * @param user  the User
     * @param model the Model
     * @return String
     */
    @RequestMapping(value = VIEW_CREATE_ADMIN, method = RequestMethod.GET)
    private String createForm(@ModelAttribute User user, Model model) {
        model.addAttribute("create", true);

        return "admin/userCreateEdit";
    }

    @RequestMapping(value = AJAX_INSTITUTIONS_WO_ADMINS, method = RequestMethod.GET)
    public @ResponseBody
    List<Institution> ajaxInstitutionsWoAdminsList() {
        return institutionService.findWithoutAdmins();
    }

    /**
     * Creates admin account and sends him a notification.
     *
     * @param user    the Admin
     * @param result  the BindingResult
     * @param model   the Model
     * @param request the HttpServletRequest
     * @param locale  the Locale
     * @return String
     * @throws MalformedURLException when the URL is malformed
     */
    @RequestMapping(value = VIEW_CREATE_ADMIN, method = RequestMethod.POST)
    public String create(@Valid User user, BindingResult result, RedirectAttributes redirect, Model model,
                         HttpServletRequest request, Locale locale)
            throws MalformedURLException {
        if (result.hasErrors()) {
            return createForm(user, model);
        }

        user.generateToken();

        Authority authority;
        if (user.getInstitution().isVirtual()) {
            authority = Authority.ROLE_ADMIN_NATIONAL;
        } else {
            authority = Authority.ROLE_ADMIN_INSTITUTIONAL;
        }
        Role adminRole = new Role(authority, user);
        user.setAdminRoles(Collections.singletonList(adminRole));

        this.userService.save(user);

        notificationService.sendConfirmationNeededAdminEmail(user, locale, request);

        String createConfirmationMessage = messageSource.getMessage("AdminManagementController.create.confirmation",
                new String[]{user.getUsername()}, locale);
        String emailConfirmationNeededMessage = messageSource.getMessage(
                "equatic.admin.emailConfirmationNeeded", new String[]{user.getEmail()}, locale);
        redirect.addFlashAttribute("message",
                Message.success(createConfirmationMessage + " " + emailConfirmationNeededMessage));

        return "redirect:" + VIEW_ADMINS_LIST;
    }

    /**
     * Displays admin edit form.
     *
     * @param user  the Admin
     * @param model the Model
     * @return String
     */
    @RequestMapping(value = VIEW_EDIT_ADMIN, method = RequestMethod.GET)
    public String editForm(@ModelAttribute User user, @PathVariable(value = "user") long userId, Model model) {
        if (user.getId() == null) {
            throw new UserNotFoundException(userId);
        }

        model.addAttribute("institution", user.getInstitution());
        model.addAttribute("create", false);

        return "admin/userCreateEdit";
    }

    /**
     * Edits admin account.
     *
     * @param user    the Admin
     * @param result  the BindingResult
     * @param model   the Model
     * @param request the HttpServletRequest
     * @param locale  the Locale
     * @return String
     * @throws MalformedURLException when the URL is malformed
     */
    @RequestMapping(value = VIEW_EDIT_ADMIN, method = RequestMethod.POST)
    public String edit(@Valid User user, BindingResult result, RedirectAttributes redirect, Model model,
                       HttpServletRequest request, Locale locale)
            throws MalformedURLException {
        if (result.hasErrors()) {
            return createForm(user, model);
        }

        userService.edit(user);

        if (user.isEmailChanged()) {
            notificationService.sendConfirmationNeededAdminEmail(user, locale, request);
        }

        String confirmationMessage = messageSource.getMessage("equatic.admin.edit.confirmation", null, locale);
        if (user.isEmailChanged()) {
            confirmationMessage += " " + messageSource.getMessage("equatic.admin.emailConfirmationNeeded",
                    new String[]{user.getEmail()}, locale);
        }
        redirect.addFlashAttribute("message", Message.success(confirmationMessage));

        return "redirect:" + VIEW_ADMINS_LIST;
    }

    /**
     * Admin activation action. Sends a notification to admin after his account is successfully activated.
     *
     * @param adminId   ID of the admin
     * @param redirect  the RedirectAttributes
     * @param locale    the Locale
     * @param principal the Principal
     * @return String
     * @throws AccessDeniedException     when access is denied
     * @throws ResourceNotFoundException when resource is not found
     */
    @RequestMapping(value = VIEW_ADMIN, params = "activate", method = RequestMethod.POST)
    public String activate(@PathVariable("user") long adminId, HttpServletRequest request,
                           RedirectAttributes redirect, Locale locale, Principal principal)
            throws AccessDeniedException, ResourceNotFoundException, MalformedURLException {
        User superAdmin = PrincipalUtil.getUser(principal);
        User admin = userService.findById(adminId);

        Message successMessage = userActivationManagementService.activateUser(admin, superAdmin, request, locale);

        redirect.addFlashAttribute("message", successMessage);

        return "redirect:" + VIEW_ADMINS_LIST;
    }

    /**
     * Admin deactivation action. Sends a notification to admin after his account is deactivated.
     *
     * @param adminId   ID of the admin
     * @param redirect  the RedirectAttributes
     * @param locale    the Locale
     * @param principal the Principal
     * @return String
     * @throws AccessDeniedException     when access is denied
     * @throws ResourceNotFoundException when resource is not found
     */
    @RequestMapping(value = VIEW_ADMIN, params = "deactivate", method = RequestMethod.POST)
    public String deactivate(@PathVariable("user") long adminId, RedirectAttributes redirect, Locale locale,
                             Principal principal)
            throws AccessDeniedException, ResourceNotFoundException, MalformedURLException {
        User superAdmin = PrincipalUtil.getUser(principal);
        User admin = userService.findById(adminId);

        Message successMessage = userActivationManagementService.deactivateUser(admin, superAdmin, locale);

        redirect.addFlashAttribute("message", successMessage);

        return "redirect:" + VIEW_ADMINS_LIST;
    }

    /**
     * Removes admin privilege from user. Sends a notification to admin that he is no longer an admin.
     *
     * @param adminId  ID of the admin
     * @param redirect the RedirectAttributes
     * @param locale   the Locale
     * @return String
     * @throws AccessDeniedException     when access is denied
     * @throws ResourceNotFoundException when resource is not found
     */
    @RequestMapping(value = VIEW_ADMIN, params = "remove-privilege", method = RequestMethod.POST)
    public String removePrivilege(@PathVariable("user") long adminId, RedirectAttributes redirect, Locale locale)
            throws AccessDeniedException, ResourceNotFoundException, MalformedURLException {
        User admin = userService.findById(adminId);

        admin.removeAuthority(Authority.ROLE_ADMIN_INSTITUTIONAL);
        admin.removeAuthority(Authority.ROLE_ADMIN_NATIONAL);
        userService.save(admin);

        notificationService.sendRemovedPrivilegeNotification(admin, locale);

        String confirmationMessage = messageSource.getMessage("AdminManagementController.removePrivilege.confirmation",
                new String[]{admin.getUsername()}, locale);

        redirect.addFlashAttribute("message", new Message(confirmationMessage, MessageType.success));

        return "redirect:" + VIEW_ADMINS_LIST;
    }

    public static URL getAdminsListUrl(URL rootUrl) throws MalformedURLException {
        return new URL(rootUrl + VIEW_ADMINS_LIST);
    }
}

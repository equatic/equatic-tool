package be.ugent.equatic.web.admin;

import be.ugent.equatic.web.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import be.ugent.equatic.domain.Authority;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.User;
import be.ugent.equatic.exception.AccessDeniedException;
import be.ugent.equatic.exception.InstitutionNotSelectedBySuperAdminException;
import be.ugent.equatic.exception.ResourceNotFoundException;
import be.ugent.equatic.exception.UserNotFoundException;
import be.ugent.equatic.service.InstitutionService;
import be.ugent.equatic.service.NotificationService;
import be.ugent.equatic.service.UserActivationManagementService;
import be.ugent.equatic.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.MalformedURLException;
import java.security.Principal;
import java.util.Locale;

/**
 * Controls user management actions for institutional and national administrator.
 */
@Controller
public class UserManagementController {

    public static final String VIEW_USERS_LIST = "/admin/{type}/users";
    public static final String VIEW_USER = VIEW_USERS_LIST + "/{user}";
    public static final String VIEW_EDIT_USER = VIEW_USER + "/edit";

    public enum AdminType {
        institutional,
        national
    }

    @Autowired
    protected InstitutionService institutionService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserActivationManagementService userActivationManagementService;

    @Autowired
    protected MessageSource messageSource;

    @ModelAttribute("menu")
    public Menu getMenu(@PathVariable AdminType type) {
        return new Menu(type == AdminType.institutional ? MenuItem.INSTITUTIONAL_ADMIN_MENU : MenuItem.NATIONAL_ADMIN_MENU);
    }

    @ModelAttribute("institution")
    public Institution getRequestedInstitution(@RequestParam(required = false) Long instId,
                                               @PathVariable AdminType type, Principal principal)
            throws InstitutionNotSelectedBySuperAdminException {
        User admin = PrincipalUtil.getUser(principal);
        return institutionService.getRequestedInstitution(instId, admin, type == AdminType.national);
    }

    @ModelAttribute("admin")
    public User getAdmin(Principal principal) {
        return PrincipalUtil.getUser(principal);
    }

    /**
     * Page for managing users.
     *
     * @param institution institution who's users are displayed
     * @param admin       admin who is displaying users
     * @param model       the Model
     * @return String
     */
    @RequestMapping(value = VIEW_USERS_LIST, method = RequestMethod.GET)
    public String usersList(@ModelAttribute Institution institution, @ModelAttribute("admin") User admin, Model model) {
        model.addAttribute("users", userService.findUsersByInstitution(institution));
        model.addAttribute("admin", admin);

        return "admin/users";
    }

    /**
     * User activation action. Sends a notification to user after his account is successfully activated.
     *
     * @param institution institution who's user is activated
     * @param admin       admin who is activating the user
     * @param userId      ID of the user
     * @param request     the HttpServletRequest
     * @param redirect    the RedirectAttributes
     * @param locale      the Locale
     * @return String
     * @throws AccessDeniedException     when access is denied
     * @throws ResourceNotFoundException when resource is not found
     */
    @RequestMapping(value = VIEW_USER, params = "activate", method = RequestMethod.POST)
    public String activate(@ModelAttribute Institution institution, @ModelAttribute("admin") User admin,
                           @PathVariable("user") long userId, HttpServletRequest request, RedirectAttributes redirect,
                           Locale locale)
            throws AccessDeniedException, ResourceNotFoundException, MalformedURLException {
        User user = userService.findById(userId);

        checkUserCanBeChanged(institution, user, admin);

        Message successMessage = userActivationManagementService.activateUser(user, admin, request, locale);

        redirect.addFlashAttribute("message", successMessage);
        redirect.addAttribute("instId", institution.getId());

        return "redirect:" + VIEW_USERS_LIST;
    }

    /**
     * User deactivation action. Sends a notification to user after his account is deactivated.
     *
     * @param institution institution who's user is deactivated
     * @param admin       admin who is deactivating the user
     * @param userId      ID of the user
     * @param redirect    the RedirectAttributes
     * @param locale      the Locale
     * @return String
     * @throws AccessDeniedException     when access is denied
     * @throws ResourceNotFoundException when resource is not found
     */
    @RequestMapping(value = VIEW_USER, params = "deactivate", method = RequestMethod.POST)
    public String deactivate(@ModelAttribute Institution institution, @ModelAttribute("admin") User admin,
                             @PathVariable("user") long userId, RedirectAttributes redirect, Locale locale)
            throws AccessDeniedException, ResourceNotFoundException {
        User user = userService.findById(userId);

        checkUserCanBeChanged(institution, user, admin);

        Message successMessage = userActivationManagementService.deactivateUser(user, admin, locale);

        redirect.addFlashAttribute("message", successMessage);
        redirect.addAttribute("instId", institution.getId());

        return "redirect:" + VIEW_USERS_LIST;
    }

    /**
     * Adds admin privilege to user. Sends a notification to admin that he is now a institutional admin.
     *
     * @param institution institution of the user who's privilege is added
     * @param admin       admin who is adding the privilege
     * @param adminId     ID of the admin who's privilege is added
     * @param redirect    the RedirectAttributes
     * @param locale      the Locale
     * @return String
     * @throws AccessDeniedException when access is denied
     * @throws UserNotFoundException when admin is not found
     */
    @RequestMapping(value = VIEW_USER, params = "add-privilege", method = RequestMethod.POST)
    public String addPrivilege(@ModelAttribute Institution institution, @ModelAttribute("admin") User admin,
                               @PathVariable("user") long adminId, RedirectAttributes redirect, Locale locale)
            throws AccessDeniedException, UserNotFoundException {
        User targetAdmin = userService.findById(adminId);

        checkUserCanBeChanged(institution, targetAdmin, admin);

        targetAdmin.addAuthority(getAuthority(institution));
        userService.save(targetAdmin);

        notificationService.sendAddedPrivilegeNotification(targetAdmin, locale);

        String confirmationMessage = messageSource.getMessage("AdminManagementController.addPrivilege.confirmation",
                new String[]{targetAdmin.getUsername()}, locale);

        redirect.addFlashAttribute("message", new Message(confirmationMessage, MessageType.success));
        redirect.addAttribute("instId", institution.getId());

        return "redirect:" + VIEW_USERS_LIST;
    }

    private Authority getAuthority(@ModelAttribute Institution institution) {
        return institution.isVirtual() ? Authority.ROLE_ADMIN_NATIONAL : Authority.ROLE_ADMIN_INSTITUTIONAL;
    }

    /**
     * Removes admin privilege from user. Sends a notification to admin that he is no longer a institutional admin.
     *
     * @param institution institution of the admin who's privilege is removed
     * @param admin       admin who is removing the privilege
     * @param adminId     ID of the admin who's privilege is removed
     * @param redirect    the RedirectAttributes
     * @param locale      the Locale
     * @return String
     * @throws AccessDeniedException when access is denied
     * @throws UserNotFoundException when admin is not found
     */
    @RequestMapping(value = VIEW_USER, params = "remove-privilege", method = RequestMethod.POST)
    public String removePrivilege(@ModelAttribute Institution institution, @ModelAttribute("admin") User admin,
                                  @PathVariable("user") long adminId, RedirectAttributes redirect, Locale locale)
            throws AccessDeniedException, UserNotFoundException {
        User targetAdmin = userService.findById(adminId);

        checkUserCanBeChanged(institution, targetAdmin, admin);

        targetAdmin.removeAuthority(getAuthority(institution));
        userService.save(targetAdmin);

        notificationService.sendRemovedPrivilegeNotification(targetAdmin, locale);

        String confirmationMessage = messageSource.getMessage("AdminManagementController.removePrivilege.confirmation",
                new String[]{targetAdmin.getUsername()}, locale);

        redirect.addFlashAttribute("message", new Message(confirmationMessage, MessageType.success));
        redirect.addAttribute("instId", institution.getId());

        return "redirect:" + VIEW_USERS_LIST;
    }

    /**
     * Displays user edit form.
     *
     * @param user  the User
     * @param model the Model
     * @return String
     */
    @RequestMapping(value = VIEW_EDIT_USER, method = RequestMethod.GET)
    private String editForm(@ModelAttribute Institution institution, @ModelAttribute("admin") User admin,
                            @ModelAttribute User user, @PathVariable(value = "user") long userId, Model model) {
        if (user.getId() == null) {
            throw new UserNotFoundException(userId);
        }

        checkUserCanBeChanged(institution, user, admin);

        model.addAttribute("create", false);

        return "admin/userCreateEdit";
    }

    /**
     * Edits user account.
     *
     * @param user    the User
     * @param result  the BindingResult
     * @param model   the Model
     * @param request the HttpServletRequest
     * @param locale  the Locale
     * @return String
     * @throws MalformedURLException when the URL is malformed
     */
    @RequestMapping(value = VIEW_EDIT_USER, method = RequestMethod.POST)
    public String edit(@ModelAttribute Institution institution, Principal principal,
                       @Valid User user, BindingResult result, RedirectAttributes redirect,
                       Model model, HttpServletRequest request, Locale locale) throws MalformedURLException {
        User admin = PrincipalUtil.getUser(principal);

        if (result.hasErrors()) {
            return editForm(institution, admin, user, user.getId(), model);
        }

        userService.edit(user);

        if (user.isEmailChanged()) {
            notificationService.sendConfirmationNeededEmail(user, locale, request);
        }

        String confirmationMessage = messageSource.getMessage("equatic.admin.edit.confirmation", null, locale);
        if (user.isEmailChanged()) {
            confirmationMessage += " " + messageSource.getMessage("equatic.admin.emailConfirmationNeeded",
                    new String[]{user.getEmail()}, locale);
        }
        redirect.addFlashAttribute("message", Message.success(confirmationMessage));
        redirect.addAttribute("instId", institution.getId());

        return "redirect:" + VIEW_USERS_LIST;
    }

    /**
     * @param institution Institution whose user accounts are managed
     * @param user        User who is to be changed
     * @param admin       Admin who is doing the change
     * @throws AccessDeniedException when the access is denied
     */
    private void checkUserCanBeChanged(Institution institution, User user, User admin)
            throws AccessDeniedException {
        if (!user.getInstitution().equals(institution)) {
            throw new AccessDeniedException("User from wrong institution");
        }
        if (user.equals(admin)) {
            throw new AccessDeniedException("You are not allowed to change your own account");
        }
        if (user.isSuperAdmin()) {
            throw new AccessDeniedException("You cannot change super admin account");
        }
    }
}

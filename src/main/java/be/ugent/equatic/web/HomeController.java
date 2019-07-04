package be.ugent.equatic.web;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import be.ugent.equatic.domain.Authority;
import be.ugent.equatic.web.admin.UserManagementController;
import be.ugent.equatic.web.admin.superadmin.AdminManagementController;
import be.ugent.equatic.web.user.institutional.InstitutionReportController;
import be.ugent.equatic.web.user.national.NationalUserController;

import java.security.Principal;
import java.util.Collection;

/**
 * Controls user redirection to their starting pages.
 * Takes into account user roles.
 */
@Controller
public class HomeController {

    private static final String VIEW_HOME = "/";

    /**
     * Redirects authenticated user to an appropriate starting page for his role.
     *
     * @param principal the Principal
     * @return String
     */
    @RequestMapping(value = VIEW_HOME)
    public String home(Principal principal, RedirectAttributes redirect) {
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) principal;
        Collection<GrantedAuthority> authorities = authenticationToken.getAuthorities();

        if (authorities.contains(Authority.ROLE_ADMIN_SUPER)) {
            return "redirect:" + AdminManagementController.VIEW_ADMINS_LIST;
        } else if (authorities.contains(Authority.ROLE_ADMIN_NATIONAL)) {
            redirect.addAttribute("type", UserManagementController.AdminType.national);
            return "redirect:" + UserManagementController.VIEW_USERS_LIST;
        } else if (authorities.contains(Authority.ROLE_ADMIN_INSTITUTIONAL)) {
            redirect.addAttribute("type", UserManagementController.AdminType.institutional);
            return "redirect:" + UserManagementController.VIEW_USERS_LIST;
        } else if (authorities.contains(Authority.ROLE_USER_NATIONAL)) {
            return "redirect:" + NationalUserController.VIEW_REPORT;
        } else if (authorities.contains(Authority.ROLE_USER_INSTITUTIONAL)) {
            return "redirect:" + InstitutionReportController.VIEW_INSTITUTION_REPORT;
        } else {
            throw new RuntimeException("Wrong user authorities");
        }
    }
}

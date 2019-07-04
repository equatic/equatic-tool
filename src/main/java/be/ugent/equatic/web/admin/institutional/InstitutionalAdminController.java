package be.ugent.equatic.web.admin.institutional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.User;
import be.ugent.equatic.exception.InstitutionNotSelectedBySuperAdminException;
import be.ugent.equatic.service.InstitutionService;
import be.ugent.equatic.web.util.Menu;
import be.ugent.equatic.web.util.MenuItem;
import be.ugent.equatic.web.util.PrincipalUtil;

import java.security.Principal;

abstract public class InstitutionalAdminController {

    public static final String VIEW_PATH = "/admin/institutional";

    @Autowired
    protected InstitutionService institutionService;

    @Autowired
    protected MessageSource messageSource;

    @ModelAttribute("menu")
    public Menu getMenu() {
        return new Menu(MenuItem.INSTITUTIONAL_ADMIN_MENU);
    }

    @ModelAttribute("institution")
    public Institution getRequestedInstitution(@RequestParam(required = false) Long instId,
                                               Principal principal) throws InstitutionNotSelectedBySuperAdminException {
        User admin = PrincipalUtil.getUser(principal);
        return institutionService.getRequestedInstitution(instId, admin, false);
    }
}

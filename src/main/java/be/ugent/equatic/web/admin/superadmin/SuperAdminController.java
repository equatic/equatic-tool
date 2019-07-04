package be.ugent.equatic.web.admin.superadmin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.ModelAttribute;
import be.ugent.equatic.web.util.Menu;
import be.ugent.equatic.web.util.MenuItem;

abstract public class SuperAdminController {

    public static final String VIEW_PATH = "/admin/super";

    @Autowired
    protected MessageSource messageSource;

    @ModelAttribute("menu")
    public Menu getMenu() {
        return new Menu(MenuItem.SUPER_ADMIN_MENU);
    }
}

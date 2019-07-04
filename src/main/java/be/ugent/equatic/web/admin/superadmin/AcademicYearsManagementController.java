package be.ugent.equatic.web.admin.superadmin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.service.AcademicYearService;
import be.ugent.equatic.web.util.Message;

import java.util.Locale;

@Controller
public class AcademicYearsManagementController extends SuperAdminController {

    private static final String VIEW_LIST = VIEW_PATH + "/academicYears";
    private static final String VIEW_CREATE_NEXT = VIEW_LIST + "/createNext";

    @Autowired
    private AcademicYearService academicYearService;

    @RequestMapping(value = VIEW_LIST, method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("academicYears", academicYearService.findAll());

        return VIEW_LIST;
    }

    @RequestMapping(value = VIEW_CREATE_NEXT, method = RequestMethod.POST)
    public String create(RedirectAttributes redirect, Locale locale) {
        AcademicYear nextAcademicYear = academicYearService.createNextAcademicYear();
        academicYearService.save(nextAcademicYear);

        String createConfirmationMessage =
                messageSource.getMessage("AcademicYearManagementController.create.confirmation",
                        new String[]{nextAcademicYear.getAcademicYear()}, locale);
        redirect.addFlashAttribute("message", Message.success(createConfirmationMessage));

        return "redirect:" + VIEW_LIST;
    }
}
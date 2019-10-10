package be.ugent.equatic.web.admin.superadmin;

import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.service.InstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class InstitutionSelectionController extends SuperAdminController {

    public static final String VIEW_SELECT_INSTITUTION = VIEW_PATH + "/selectInstitution";
    private static final String AJAX_ACTIVE_INSTITUTIONS = VIEW_SELECT_INSTITUTION + "/ajax";

    @Autowired
    InstitutionService institutionService;

    @RequestMapping(value = VIEW_SELECT_INSTITUTION, params = "nextAction", method = RequestMethod.GET)
    public String selectInstitution(@RequestParam String nextAction, @RequestParam boolean virtual,
                                    @RequestParam(required = false, defaultValue = "false") boolean selfAssessment,
                                    Model model) {
        model.addAttribute("nextAction", nextAction);
        model.addAttribute("virtual", virtual);
        model.addAttribute("selfAssessment", selfAssessment);

        return VIEW_SELECT_INSTITUTION;
    }

    @RequestMapping(value = AJAX_ACTIVE_INSTITUTIONS, method = RequestMethod.GET, params = "virtual")
    public @ResponseBody
    List<Institution> ajaxActiveInstitutionsList(@RequestParam boolean virtual) {
        return institutionService.findActive(virtual);
    }
}

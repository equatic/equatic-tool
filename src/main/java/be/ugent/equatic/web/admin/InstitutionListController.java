package be.ugent.equatic.web.admin;

import com.github.dandelion.datatables.core.ajax.DataSet;
import com.github.dandelion.datatables.core.ajax.DatatablesCriterias;
import com.github.dandelion.datatables.core.ajax.DatatablesResponse;
import com.github.dandelion.datatables.extras.spring3.ajax.DatatablesParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.service.InstitutionService;
import be.ugent.equatic.web.util.Menu;

@Controller
public class InstitutionListController {

    private static final String VIEW_INSTITUTIONS_LIST = "/admin/institutions";

    @Autowired
    private InstitutionService institutionService;

    @ModelAttribute("menu")
    public Menu getMenu() {
        return new Menu();
    }

    @RequestMapping(value = VIEW_INSTITUTIONS_LIST, method = RequestMethod.GET)
    public String institutions() {
        return VIEW_INSTITUTIONS_LIST;
    }

    @RequestMapping(value = VIEW_INSTITUTIONS_LIST + "/ajax")
    public @ResponseBody
    DatatablesResponse<Institution> findAll(@DatatablesParams DatatablesCriterias criterias) {
        DataSet<Institution> dataSet = institutionService.findInstitutionsNotVirtualWithDatatablesCriterias(criterias);
        return DatatablesResponse.build(dataSet, criterias);
    }
}

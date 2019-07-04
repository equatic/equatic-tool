package be.ugent.equatic.web.user.national;

import be.ugent.equatic.service.*;
import be.ugent.equatic.web.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.Country;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.User;
import be.ugent.equatic.exception.InstitutionNotSelectedBySuperAdminException;

import java.security.Principal;
import java.util.List;

@Controller
public class NationalUserController {

    public static final String VIEW_PATH = "/user/national";
    public static final String VIEW_REPORT = VIEW_PATH + "/report";

    @Autowired
    protected InstitutionService institutionService;

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private AcademicYearsOptionValidator academicYearsOptionValidator;

    @Autowired
    private AcademicYearsOptionService academicYearsOptionService;

    @Autowired
    private DataSheetRowService dataSheetRowService;

    @ModelAttribute("menu")
    public Menu getMenu() {
        return new Menu(MenuItem.NATIONAL_USER_MENU);
    }

    @ModelAttribute("institution")
    public Institution getRequestedInstitution(@RequestParam(required = false) Long instId,
                                               Principal principal) throws InstitutionNotSelectedBySuperAdminException {
        User admin = PrincipalUtil.getUser(principal);
        return institutionService.getRequestedInstitution(instId, admin, true);
    }

    @ModelAttribute("allAcademicYears")
    public List<AcademicYear> getAllAcademicYears() {
        return academicYearService.findAll();
    }

    @ModelAttribute("allCountries")
    public List<Country> getAllCountries() {
        return countryService.findAll();
    }

    @RequestMapping(value = VIEW_REPORT, method = RequestMethod.GET)
    public String report(@ModelAttribute Institution institution,
                         @ModelAttribute("options") NationalUserReportOptions options, BindingResult result,
                         Model model) {
        academicYearsOptionService.initializeAcademicYearsOption(options);

        academicYearsOptionValidator.validate(options, result);

        Country country = options.getCountryChosen();
        if (country == null) {
            result.rejectValue("countryChosen", "NationalUserReportOptions.countryNotSelected");
        }

        List<NationalUserReportRow> reportRows = null;
        if (!result.hasErrors()) {
            List<AcademicYear> academicYears = academicYearService.findYearBetween(options.getAcademicYearFrom(),
                    options.getAcademicYearTo());

            List<Institution> institutionsTo = institutionService.getInstitutionsForCountry(country);
            List<Institution> institutionsFrom = institutionService.getInstitutionsForCountry(institution.getCountry());
            reportRows = dataSheetRowService.getNationalUserReportRows(institutionsTo, institutionsFrom, academicYears);
        }
        model.addAttribute("reportRows", reportRows);

        return VIEW_REPORT;
    }
}

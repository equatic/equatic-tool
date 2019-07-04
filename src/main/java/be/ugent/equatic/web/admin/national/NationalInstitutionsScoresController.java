package be.ugent.equatic.web.admin.national;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import be.ugent.equatic.domain.Country;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.User;
import be.ugent.equatic.exception.InstitutionNotSelectedBySuperAdminException;
import be.ugent.equatic.exception.InstitutionsScoresValidationException;
import be.ugent.equatic.indicator.Score;
import be.ugent.equatic.web.user.institutional.InstitutionsScoresController;
import be.ugent.equatic.web.util.InstitutionsScoresOptions;
import be.ugent.equatic.web.util.Menu;
import be.ugent.equatic.web.util.MenuItem;
import be.ugent.equatic.web.util.PrincipalUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

@Controller
public class NationalInstitutionsScoresController extends InstitutionsScoresController {

    public static final String VIEW_PATH = "/admin/national";
    private static final String VIEW_INSTITUTIONS_SCORES = VIEW_PATH + "/institutionsScores";

    @ModelAttribute("menu")
    public Menu getMenu() {
        return new Menu(MenuItem.NATIONAL_ADMIN_MENU);
    }

    @Override
    @ModelAttribute("institution")
    public Institution getRequestedInstitution(@RequestParam(required = false) Long instId,
                                               Principal principal) throws InstitutionNotSelectedBySuperAdminException {
        User admin = PrincipalUtil.getUser(principal);
        return institutionService.getRequestedInstitution(instId, admin, true);
    }

    @Override
    @RequestMapping(value = VIEW_INSTITUTIONS_SCORES, method = RequestMethod.GET)
    public String institutionsScores(@ModelAttribute Institution institution,
                                     @Valid @ModelAttribute("options") InstitutionsScoresOptions options,
                                     BindingResult result, Model model) {
        Map<Country, List<Score>> resultMap;
        try {
            resultMap = getCountryScoresMap(institution, options, result);
        } catch (InstitutionsScoresValidationException e) {
            resultMap = Collections.emptyMap();
        }

        model.addAttribute("countriesScoresMap", resultMap);
        model.addAttribute("mode", options.getMode());
        model.addAttribute("type", "national");

        return "admin/institutionsScores";
    }

    private Map<Country, List<Score>> getCountryScoresMap(Institution institution,
                                                          InstitutionsScoresOptions options, BindingResult result)
            throws InstitutionsScoresValidationException {
        institutionsScoresOptionsService.initializeInstitutionsScoresOptions(options);

        academicYearsOptionValidator.validate(options, result);

        if (result.hasErrors()) {
            throw new InstitutionsScoresValidationException();
        } else {
            List<Institution> institutions = institutionService.getInstitutionsForCountry(institution.getCountry());
            return institutionsScoresService.getCountriesScoresMap(institutions, options);
        }
    }

    @Override
    @RequestMapping(value = VIEW_INSTITUTIONS_SCORES, method = RequestMethod.GET, params = "export")
    public void institutionsScoresExport(@ModelAttribute Institution institution, @ModelAttribute("options")
            InstitutionsScoresOptions options, BindingResult result, HttpServletResponse response, Locale locale)
            throws IOException, InstitutionsScoresValidationException {
        Map<Country, List<Score>> countryScoresMap = getCountryScoresMap(institution, options, result);
        Map<Country, List<Score>> sortedCountryScoresMap = new TreeMap<>(countryScoresMap);

        Sheet sheet = getCountryScoresSheet(options, locale, sortedCountryScoresMap);

        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"country-scores.xls\"");

        ServletOutputStream out = response.getOutputStream();
        sheet.getWorkbook().write(out);
        out.flush();
    }

    private Sheet getCountryScoresSheet(InstitutionsScoresOptions options, Locale locale,
                                        Map<Country, List<Score>> countryScoresMap) {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        CellStyle cellDoubleStyle = getCellDoubleStyle(workbook);

        Row columnsRow = sheet.createRow(0);
        columnsRow.createCell(0).setCellValue(messageSource.getMessage("equatic.country", null, locale));

        int scoreFirstCelln = 1;
        createColumnsRowCells(options, columnsRow, scoreFirstCelln);

        int rown = 1;
        for (Map.Entry<Country, List<Score>> countryScoresMapEntry : countryScoresMap.entrySet()) {
            Row row = sheet.createRow(rown);

            Country country = countryScoresMapEntry.getKey();
            List<Score> scores = countryScoresMapEntry.getValue();

            row.createCell(0).setCellValue(country.getName());

            createScoreCells(row, scores, scoreFirstCelln, cellDoubleStyle);
            rown++;
        }
        return sheet;
    }
}

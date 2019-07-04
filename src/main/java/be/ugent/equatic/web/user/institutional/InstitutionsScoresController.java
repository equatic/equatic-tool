package be.ugent.equatic.web.user.institutional;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import be.ugent.equatic.cluster.ClusterCode;
import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.exception.InstitutionsScoresValidationException;
import be.ugent.equatic.indicator.IndicatorCode;
import be.ugent.equatic.indicator.Score;
import be.ugent.equatic.service.*;
import be.ugent.equatic.util.BroadIsced;
import be.ugent.equatic.web.util.AcademicYearsOptionValidator;
import be.ugent.equatic.web.util.InstitutionsScoresMode;
import be.ugent.equatic.web.util.InstitutionsScoresOptions;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Controller
public class InstitutionsScoresController extends InstitutionalUserController {

    private static final String VIEW_INSTITUTIONS_SCORES =
            InstitutionalUserController.VIEW_PATH + "/institutionsScores";

    private static final int ERASMUS_CODE_CELLN = 0;
    private static final int INSTITUTION_CELLN = 1;
    private static final int COUNTRY_CELLN = 2;

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private DataSheetRowService dataSheetRowService;

    @Autowired
    private IscedService iscedService;

    @Autowired
    protected AcademicYearsOptionValidator academicYearsOptionValidator;

    @Autowired
    protected InstitutionsScoresOptionsService institutionsScoresOptionsService;

    @Autowired
    protected InstitutionsScoresService institutionsScoresService;

    @ModelAttribute("allAcademicYears")
    public List<AcademicYear> getAllAcademicYears() {
        return academicYearService.findAll();
    }

    @ModelAttribute("allIndicatorCodes")
    public List<IndicatorCode> getAllIndicatorCodes() {
        return Arrays.asList(IndicatorCode.values());
    }

    @ModelAttribute("allBroadIsceds")
    public Map<String, BroadIsced> getAllBroadIsceds() {
        return iscedService.getBroadIscedMap();
    }

    @ModelAttribute("allPartnerInstitutions")
    public List<Institution> getAllPartnerInstitutions(@ModelAttribute Institution institution) {
        return dataSheetRowService.getPartnerInstitutions(institution);
    }

    @ModelAttribute("allModes")
    public List<InstitutionsScoresMode> getAllModes() {
        return Arrays.asList(InstitutionsScoresMode.values());
    }

    @ModelAttribute("allClusterCodes")
    private ClusterCode[] getAllClusterCodes() {
        return ClusterCode.values();
    }

    @RequestMapping(value = VIEW_INSTITUTIONS_SCORES)
    public String institutionsScores(@ModelAttribute Institution institution,
                                     @ModelAttribute("options") InstitutionsScoresOptions options,
                                     BindingResult result, Model model) {
        Map<Institution, List<Score>> institutionsScoresMap;
        try {
            institutionsScoresMap = getInstitutionsScoresMap(institution, options, result);
        } catch (InstitutionsScoresValidationException exception) {
            institutionsScoresMap = Collections.emptyMap();
        }

        model.addAttribute("institutionsScoresMap", institutionsScoresMap);
        model.addAttribute("mode", options.getMode());
        model.addAttribute("type", "institutional");

        return "user/institutional/institutionsScores";
    }

    private Map<Institution, List<Score>> getInstitutionsScoresMap(Institution institution,
                                                                   InstitutionsScoresOptions options,
                                                                   BindingResult result) throws
            InstitutionsScoresValidationException {
        institutionsScoresOptionsService.initializeInstitutionsScoresOptions(options);

        academicYearsOptionValidator.validate(options, result);

        if (result.hasErrors()) {
            throw new InstitutionsScoresValidationException();
        } else {
            return institutionsScoresService.getInstitutionsScoresMap(Collections.singletonList(institution),
                    options);
        }
    }

    @RequestMapping(value = VIEW_INSTITUTIONS_SCORES, params = "export")
    public void institutionsScoresExport(@ModelAttribute Institution institution,
                                         @ModelAttribute("options") InstitutionsScoresOptions options,
                                         BindingResult result, HttpServletResponse response, Locale locale)
            throws IOException, InstitutionsScoresValidationException {
        Map<Institution, List<Score>> institutionsScoresMap = getInstitutionsScoresMap(institution, options, result);
        Map<Institution, List<Score>> sortedInstitutionsScoreMap = new TreeMap<>(institutionsScoresMap);

        Sheet sheet = getInstitutionsScoresSheet(options, locale, sortedInstitutionsScoreMap);

        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"institutions-scores.xls\"");

        ServletOutputStream out = response.getOutputStream();
        sheet.getWorkbook().write(out);
        out.flush();
    }

    private Sheet getInstitutionsScoresSheet(InstitutionsScoresOptions options,
                                             Locale locale, Map<Institution, List<Score>> institutionsScoresMap) {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        CellStyle cellDoubleStyle = getCellDoubleStyle(workbook);

        Row columnsRow = sheet.createRow(0);
        columnsRow.createCell(ERASMUS_CODE_CELLN).setCellValue(
                messageSource.getMessage("equatic.erasmusCode", null, locale));
        columnsRow.createCell(INSTITUTION_CELLN).setCellValue(
                messageSource.getMessage("equatic.institution", null, locale));
        columnsRow.createCell(COUNTRY_CELLN).setCellValue(messageSource.getMessage("equatic.country", null, locale));

        int scoreFirstCelln = COUNTRY_CELLN + 1;
        createColumnsRowCells(options, columnsRow, scoreFirstCelln);

        int rown = 1;
        for (Map.Entry<Institution, List<Score>> institutionsScoresMapEntry : institutionsScoresMap.entrySet()) {
            Row row = sheet.createRow(rown);

            Institution partnerInstitution = institutionsScoresMapEntry.getKey();
            List<Score> scores = institutionsScoresMapEntry.getValue();

            row.createCell(ERASMUS_CODE_CELLN).setCellValue(partnerInstitution.getErasmusCode());
            row.createCell(INSTITUTION_CELLN).setCellValue(partnerInstitution.getDisplayName());
            row.createCell(COUNTRY_CELLN).setCellValue(partnerInstitution.getCountry().getName());

            createScoreCells(row, scores, scoreFirstCelln, cellDoubleStyle);
            rown++;
        }
        return sheet;
    }

    protected void createColumnsRowCells(InstitutionsScoresOptions options, Row columnsRow, int scoreFirstCelln) {
        int celln = scoreFirstCelln;
        switch (options.getMode()) {
            case DETAILED:
                for (IndicatorCode indicatorCode : options.getIndicatorCodes()) {
                    columnsRow.createCell(celln).setCellValue(indicatorCode.getIndicator().getName());
                    celln++;
                }
                break;

            case CLUSTERS:
                for (ClusterCode clusterCode : getAllClusterCodes()) {
                    columnsRow.createCell(celln).setCellValue(clusterCode.getCluster().getName());
                    celln++;
                }
                break;
        }
    }

    protected CellStyle getCellDoubleStyle(Workbook workbook) {
        CellStyle cellDoubleStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        cellDoubleStyle.setDataFormat(createHelper.createDataFormat().getFormat("0.00"));
        return cellDoubleStyle;
    }

    protected void createScoreCells(Row row, List<Score> scores, int firstCelln, CellStyle cellDoubleStyle) {
        int celln = firstCelln;
        for (Score score : scores) {
            if (score != null) {
                Cell cell = row.createCell(celln);
                cell.setCellValue(score.getValue());
                cell.setCellStyle(cellDoubleStyle);
            }
            celln++;
        }
    }
}

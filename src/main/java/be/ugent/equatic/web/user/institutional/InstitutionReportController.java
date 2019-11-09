package be.ugent.equatic.web.user.institutional;

import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.ScoreInterpretation;
import be.ugent.equatic.indicator.Explanation;
import be.ugent.equatic.indicator.IndicatorCode;
import be.ugent.equatic.indicator.Score;
import be.ugent.equatic.service.*;
import be.ugent.equatic.util.BroadIsced;
import be.ugent.equatic.web.util.AcademicYearsOptionValidator;
import be.ugent.equatic.web.util.InstitutionsScoresOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class InstitutionReportController extends InstitutionalUserController {

    public static final String VIEW_INSTITUTION_REPORT = VIEW_PATH + "/institutionReport";

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private DataSheetRowService dataSheetRowService;

    @Autowired
    private IscedService iscedService;

    @Autowired
    private AcademicYearsOptionValidator academicYearsOptionValidator;

    @Autowired
    private InstitutionsScoresOptionsService institutionsScoresOptionsService;

    @Autowired
    private InstitutionsScoresService institutionsScoresService;

    @Autowired
    private ScoreInterpretationService scoreInterpretationService;

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

    @RequestMapping(value = InstitutionReportController.VIEW_INSTITUTION_REPORT)
    public String institutionReport(@ModelAttribute Institution institution,
                                    @ModelAttribute("options") InstitutionsScoresOptions options,
                                    @RequestParam(required = false, defaultValue = "false") boolean selfAssessment,
                                    BindingResult result, Model model) {
        institutionsScoresOptionsService.initializeInstitutionsScoresOptions(options);

        academicYearsOptionValidator.validate(options, result);

        List<Institution> institutions = options.getInstitutions();

        if (selfAssessment) {
            institutions = Collections.singletonList(institution);
            options.setInstitutions(institutions);
            options.setIndicatorCodes(
                    Arrays.asList(IndicatorCode.SUPPORT_AND_FACILITIES, IndicatorCode.ACADEMIC_QUALITY,
                            IndicatorCode.COURSE_CATALOGUE_INFORMATION, IndicatorCode.EXCHANGE_OF_ECTS_DOCUMENTS));

            model.addAttribute("selfAssessment", true);
        }

        if (institutions == null || institutions.size() != 1) {
            result.rejectValue("institutions", "InstitutionsScoresOptions.institutionNotSelected");
        }

        List<Score> institutionScores = null;
        if (!result.hasErrors()) {
            Institution institutionSelected = institutions.get(0);
            model.addAttribute("institutionSelected", institutionSelected);

            // We need to ask for all partner institutions to calculate to statistical relevance
            InstitutionsScoresOptions institutionsScoresOptions =
                    new InstitutionsScoresOptions(options.getAcademicYearFrom(), options.getAcademicYearTo(),
                            options.getIndicatorCodes(), options.getIsceds(), null, options.getMode());
            Map<Institution, List<Score>> resultMap = institutionsScoresService.getInstitutionsScoresMap(
                    Collections.singletonList(institution), institutionsScoresOptions);
            institutionScores = resultMap.get(institutionSelected);

            List<Explanation> explanations =
                    institutionsScoresService.getExplanations(institution, options, selfAssessment);
            model.addAttribute("explanations", explanations);
        }

        Map<String, ScoreInterpretation> scoreInterpretationMap =
                scoreInterpretationService.getScoreInterpretationMap(institution);
        model.addAttribute("institutionScores", institutionScores);
        model.addAttribute("mode", options.getMode());
        model.addAttribute("scoreInterpretationMap", scoreInterpretationMap);

        return VIEW_INSTITUTION_REPORT;
    }
}

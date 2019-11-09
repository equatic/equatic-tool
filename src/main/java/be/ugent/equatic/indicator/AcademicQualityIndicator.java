package be.ugent.equatic.indicator;

import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.DataSheetColumnCode;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.Isced;
import be.ugent.equatic.service.DataSheetRowService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AcademicQualityIndicator extends IndicatorFromScalarTuples {

    private static final DataSheetColumnCode[] INDICATOR_COLUMN_CODES = new DataSheetColumnCode[]{
            DataSheetColumnCode.SMS_QUALITY_CONTENT, DataSheetColumnCode.SMS_QUALITY_TEACHING,
            DataSheetColumnCode.SMS_QUALITY_LEARNING, DataSheetColumnCode.SMP_QUALITY
    };

    @Override
    public String getName() {
        return "Academic quality";
    }

    @Override
    public String getDescription() {
        return "The indicator is the result of the answers on the questions on quality of learning and teaching from " +
                "the Erasmus+ participant report.";
    }

    @Override
    public Explanation getInstitutionReportExplanation(Institution institution, List<AcademicYear> academicYears,
                                                       List<Isced> isceds, Institution partnerInstitution,
                                                       DataSheetRowService dataSheetRowService,
                                                       boolean selfAssessment) {
        Optional<Double> learningSubscore =
                getSubscoreFromDefaultValues(institution, academicYears, isceds, partnerInstitution,
                        dataSheetRowService, DataSheetColumnCode.SMS_QUALITY_LEARNING);
        Optional<Double> teachingSubscore =
                getSubscoreFromDefaultValues(institution, academicYears, isceds, partnerInstitution,
                        dataSheetRowService, DataSheetColumnCode.SMS_QUALITY_TEACHING);
        Optional<Double> contentSubscore =
                getSubscoreFromDefaultValues(institution, academicYears, isceds, partnerInstitution,
                        dataSheetRowService, DataSheetColumnCode.SMS_QUALITY_CONTENT);
        Optional<Double> academicSupportSubscore =
                getSubscoreFromDefaultValues(institution, academicYears, isceds, partnerInstitution,
                        dataSheetRowService, DataSheetColumnCode.SMP_QUALITY);

        return new Explanation(
                new String[]{"The indicator on academic quality is based on the feedback of " +
                        (selfAssessment ? "incoming" : "outgoing") + " students to " +
                        partnerInstitution.getDisplayName() + ". In the context of this indicator, academic quality is " +
                        "the combination of three elements: degree of learning support; quality of teaching methods and " +
                        "quality of the course content<sup>2</sup>.",
                        "<small><sup>2</sup> In the feedback from trainee students academic support is taken into account " +
                                "for this indicator.</small>"},
                new String[]{
                        String.format("Degree of learning support sub-score: <strong>%s</strong> (25%% of total score)",
                                getSubScoreOrNA(learningSubscore)),
                        String.format(
                                "Quality of teaching methods sub-score: <strong>%s</strong> (25%% of total score)",
                                getSubScoreOrNA(teachingSubscore)),
                        String.format(
                                "Quality of the course content sub-score: <strong>%s</strong> (25%% of total score)",
                                getSubScoreOrNA(contentSubscore)),
                        String.format("Academic support sub-score: <strong>%s</strong> (25%% of total score)",
                                getSubScoreOrNA(academicSupportSubscore))});
    }

    private static String getSubScoreOrNA(Optional<Double> subscoreOptional) {
        return subscoreOptional.map(aDouble -> String.valueOf(aDouble.intValue())).orElse("N/A");
    }

    private Optional<Double> getSubscoreFromDefaultValues(Institution institution, List<AcademicYear> academicYears,
                                                          List<Isced> isceds, Institution partnerInstitution,
                                                          DataSheetRowService dataSheetRowService,
                                                          DataSheetColumnCode columnCode) {
        List<Object[]> scores =
                dataSheetRowService.calculateScoresFromDefaultValues(Collections.singletonList(institution),
                        academicYears, isceds, Collections.singletonList(columnCode),
                        Collections.singletonList(partnerInstitution));
        if (scores.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(rescale((Double) scores.get(0)[1]));
    }

    @Override
    public List<Object[]> getPartnerInstitutionsScores(List<Institution> institutions, List<AcademicYear> academicYears,
                                                       List<Isced> isceds, List<Institution> filteredInstitutions,
                                                       DataSheetRowService dataSheetRowService) {
        return dataSheetRowService.calculateScoresFromDefaultValues(institutions, academicYears, isceds,
                Arrays.asList(INDICATOR_COLUMN_CODES), filteredInstitutions);
    }

    @Override
    public Double rescale(Double rawScore) {
        return rawScore != null ? 25.0 * (rawScore - 1.0) : null;
    }

    @Override
    protected Double rescaleStandardError(double standardError) {
        return 25.0 * standardError;
    }
}

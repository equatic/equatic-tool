package be.ugent.equatic.indicator;

import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.Isced;
import be.ugent.equatic.service.DataSheetRowService;

import java.util.List;
import java.util.Optional;

public class SupportAndFacilitiesIndicator extends IndicatorFromScalarTuples {

    @Override
    public String getName() {
        return "Support and facilities";
    }

    @Override
    public String getDescription() {
        return "This indicator combines four elements extracted from the Erasmus+ participant reports: the level of " +
                "satisfaction with support arrangements provided by the receiving partner institution; the level of " +
                "satisfaction with how the receiving institution dealt with questions, complaints or problems; " +
                "the level of satisfaction with assistance in finding accommodation; and how the student facilities " +
                "were rated. Support questions count for 80% of the final score, the one on facilities for 20%.";
    }

    @Override
    protected int getSubscoreCount() {
        return 2;
    }

    @Override
    protected Double[] getSubscoreWeights(Double[] variance) {
        if (variance[0] == null) {
            return new Double[]{0.0, 1.0};
        } else if (variance[1] == null) {
            return new Double[]{1.0, 0.0};
        } else {
            return new Double[]{0.5, 0.5};
        }
    }

    @Override
    public Explanation getInstitutionReportExplanation(Institution institution, List<AcademicYear> academicYears,
                                                       List<Isced> isceds, Institution partnerInstitution,
                                                       DataSheetRowService dataSheetRowService,
                                                       boolean selfAssessment) {
        Optional<Double> supportSubscore = dataSheetRowService.getSupportSubscore(institution, academicYears, isceds,
                partnerInstitution);
        Optional<Double> facilitiesSubscore = dataSheetRowService.getFacilitiesSubscore(institution, academicYears,
                isceds, partnerInstitution);

        return new Explanation(
                new String[]{
                        "Support and facilities at the host institution gives an indication of " +
                                "the satisfaction of " + (selfAssessment ? "incoming" : "outgoing") +
                                " students with the support and facilities at " +
                                partnerInstitution.getDisplayName() +
                                ". The score for this indicator is based on feedback from " +
                                (selfAssessment ? "incoming" : "outgoing") + " students.",
                        "To calculate the indicator score the support-questions count for 80% towards the final score, " +
                                "the facilities count for 20% (if this information is available<sup>1</sup>).",
                        "<small><sup>1</sup> For institutions that only host trainee students, facilities will not be taken " +
                                "into account as there is no question on this topic in the questionnaire for " +
                                "trainee students.</small>"},
                new String[]{
                        String.format(
                                "Indicator sub-score on support question: <strong>%s</strong> (80%% of total score)",
                                getRescaledSubscore(supportSubscore)),
                        String.format(
                                "Indicator sub-score on facilities question: <strong>%s</strong> (20%% of total score)",
                                getRescaledSubscore(facilitiesSubscore))});
    }

    private String getRescaledSubscore(Optional<Double> subscoreOptional) {
        return subscoreOptional.map(subscore -> String.valueOf(rescale(subscore).intValue())).orElse("N/A");
    }

    @Override
    public List<Object[]> getPartnerInstitutionsScores(List<Institution> institutions,
                                                       List<AcademicYear> academicYears,
                                                       List<Isced> isceds,
                                                       List<Institution> filteredInstitutions,
                                                       DataSheetRowService dataSheetRowService) {
        return dataSheetRowService.calculateSupportAndFacilitiesScores(institutions, academicYears, isceds,
                filteredInstitutions);
    }

    @Override
    public Double rescale(Double rawScore) {
        return 25.0 * (rawScore - 1.0);
    }

    @Override
    protected Double rescaleStandardError(double standardError) {
        return 25.0 * standardError;
    }
}

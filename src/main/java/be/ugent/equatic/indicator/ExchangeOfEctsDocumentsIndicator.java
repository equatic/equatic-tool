package be.ugent.equatic.indicator;

import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.Isced;
import be.ugent.equatic.service.DataSheetRowService;

import java.util.List;

public class ExchangeOfEctsDocumentsIndicator extends IndicatorFromScalarTuples {

    @Override
    public String getName() {
        return "Exchange of mobility documents";
    }

    @Override
    public String getDescription() {
        return "The exchange of mobility documents indicator assesses whether the Learning Agreement was signed by " +
                "the host institution (25% of the final score) and whether the transcript of records was received " +
                "in time (25% of the final score). For this indicator it is also taken into account whether " +
                "a grading table is available in the Egracons tool (50% of the final score).";
    }

    @Override
    protected int getSubscoreCount() {
        return 2;
    }

    @Override
    protected Double[] getSubscoreWeights(Double[] variance) {
        return new Double[]{0.5, 0.5};
    }

    @Override
    public Explanation getInstitutionReportExplanation(Institution institution, List<AcademicYear> academicYears,
                                                       List<Isced> isceds, Institution partnerInstitution,
                                                       DataSheetRowService dataSheetRowService, boolean selfAssessment) {
        boolean isGradingTableAvailable = dataSheetRowService.isGradingTableAvailable(partnerInstitution,
                academicYears);
        Integer laSignedPercentage =
                dataSheetRowService.getLaSignedPercentage(institution, academicYears, isceds, partnerInstitution);
        Integer torOnTimePercentage =
                dataSheetRowService.getTorOnTimePercentage(institution, academicYears, isceds, partnerInstitution);

        return new Explanation(
                new String[]{"For the indicator on the exchange of mobility documents, student feedback was " +
                        "taken into account for half of the total score. Two elements are taken into account here: " +
                        "was the learning agreement signed by the host institution and did students receive " +
                        "their transcript of records in time?",
                        partnerInstitution.getDisplayName() + (isGradingTableAvailable ?
                                " has uploaded a grading table in the Egracons tool (50% of the total score)." :
                                " has not yet uploaded a grading table in the Egracons tool (50% of the total score) " +
                                        "so the maximum score it could get on this indicator is 50.")},
                new String[]{
                        String.format("Learning agreement signed by host: <strong>%d%%</strong> (25%% of total score)",
                                laSignedPercentage),
                        String.format(
                                "Transcript of records received in time: <strong>%d%%</strong> (25%% of total score)",
                                torOnTimePercentage)});
    }

    @Override
    public List<Object[]> getPartnerInstitutionsScores(List<Institution> institutions,
                                                       List<AcademicYear> academicYears,
                                                       List<Isced> isceds,
                                                       List<Institution> filteredInstitutions,
                                                       DataSheetRowService dataSheetRowService) {
        return dataSheetRowService.calculateExchangeOfEctsDocumentsScores(institutions, academicYears, isceds,
                filteredInstitutions);
    }

    @Override
    public Double rescale(Double rawScore) {
        return rawScore * 100.0;
    }
}

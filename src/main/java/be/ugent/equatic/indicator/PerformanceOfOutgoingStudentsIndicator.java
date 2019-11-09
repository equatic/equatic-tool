package be.ugent.equatic.indicator;

import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.DataSheetCode;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.Isced;
import be.ugent.equatic.service.DataSheetRowService;

import java.util.List;

public class PerformanceOfOutgoingStudentsIndicator extends IndicatorFromScalarTuples {

    @Override
    public String getName() {
        return "Performance of outgoing students";
    }

    @Override
    public String getDescription() {
        return "This indicator combines the study load and study success of outgoing exchange students to the " +
                "partner institution. Study load refers to the number of credits taken per semester. Study success " +
                "is the ratio between the number of credits taken and the number of credits successfully completed.";
    }

    @Override
    public Explanation getInstitutionReportExplanation(Institution institution, List<AcademicYear> academicYears,
                                                       List<Isced> isceds, Institution partnerInstitution,
                                                       DataSheetRowService dataSheetRowService, boolean selfAssessment) {
        Double averageCreditsPerDay = dataSheetRowService.getAverageCreditsPerDay(institution, academicYears, isceds,
                partnerInstitution, DataSheetCode.STUDENTS_OUTGOING);
        Integer studySuccessPercentage = dataSheetRowService.getStudySuccessPercentage(institution, academicYears,
                isceds, partnerInstitution, DataSheetCode.STUDENTS_OUTGOING);

        return new Explanation(
                new String[]{String.format(
                        "This indicator performance of outgoing students gives an insight in how well outgoing students to " +
                                "%1$s perform by assessing their study success combined with the study load. The average number of " +
                                "credits outgoing students take per day is %2$.2f ECTS (30 ECTS per semester equals " +
                                "about 0.20 per day). The study success of the outgoing students is %3$d%%.",
                        partnerInstitution.getDisplayName(), averageCreditsPerDay, studySuccessPercentage),
                        "To calculate the indicator score the completed credits are divided by 0.20 * the number of days."},
                new String[]{
                        String.format("Study success: <strong>%d%%</strong>", studySuccessPercentage),
                        String.format("Study load: <strong>%.2f</strong>", averageCreditsPerDay)});
    }

    @Override
    public List<Object[]> getPartnerInstitutionsScores(List<Institution> institutions,
                                                       List<AcademicYear> academicYears,
                                                       List<Isced> isceds,
                                                       List<Institution> filteredInstitutions,
                                                       DataSheetRowService dataSheetRowService) {
        return dataSheetRowService.calculateStudentsCreditsDaysScores(institutions, academicYears, isceds,
                filteredInstitutions, DataSheetCode.STUDENTS_OUTGOING);
    }

    @Override
    public Double rescale(Double rawScore) {
        return 100.0 * rawScore;
    }
}

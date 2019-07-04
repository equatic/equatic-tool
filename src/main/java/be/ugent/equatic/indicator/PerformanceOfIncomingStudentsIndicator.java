package be.ugent.equatic.indicator;

import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.DataSheetCode;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.Isced;
import be.ugent.equatic.service.DataSheetRowService;

import java.util.List;

public class PerformanceOfIncomingStudentsIndicator extends IndicatorFromScalarTuples {

    @Override
    public String getName() {
        return "Performance of incoming students";
    }

    @Override
    public String getDescription() {
        return "This indicator combines the study load and study success of incoming exchange students from the " +
                "partner institution. Study load refers to the number of credits taken per semester. Study success " +
                "is the ratio between the number of credits taken and the number of credits successfully completed.";
    }

    @Override
    public Explanation getInstitutionReportExplanation(Institution institution, List<AcademicYear> academicYears,
                                                       List<Isced> isceds, Institution partnerInstitution,
                                                       DataSheetRowService dataSheetRowService) {
        Double averageCreditsPerDay = dataSheetRowService.getAverageCreditsPerDay(institution, academicYears, isceds,
                partnerInstitution, DataSheetCode.STUDENTS_INCOMING);
        Integer studySuccessPercentage = dataSheetRowService.getStudySuccessPercentage(institution, academicYears,
                isceds, partnerInstitution, DataSheetCode.STUDENTS_INCOMING);

        return new Explanation(
                new String[]{String.format(
                        "This indicator tells us something about the performance of the incoming students from " +
                                "%1$s by assessing their study success combined with the study load. The average number of " +
                                "credits incoming students from %1$s take per day is %2$.2f ECTS (30 ECTS per semester equals " +
                                "about 0.20 per day). The study success of the incoming students is %3$d%%.",
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
                filteredInstitutions, DataSheetCode.STUDENTS_INCOMING);
    }

    @Override
    public Double rescale(Double rawScore) {
        return 100.0 * rawScore;
    }
}

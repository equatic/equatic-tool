package be.ugent.equatic.indicator;

import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.DataSheetCode;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.Isced;
import be.ugent.equatic.service.DataSheetRowService;

import java.util.List;

public class MobilityRateIndicator extends IndicatorFromScalarTuples {

    @Override
    public String getName() {
        return "Mobility rate";
    }

    @Override
    public String getDescription() {
        return "The mobility rate indicator is based on the mobility numbers of both students and staff (incoming and " +
                "outgoing) and the balance between incoming and outgoing mobility flows.";
    }

    @Override
    public Explanation getInstitutionReportExplanation(Institution institution, List<AcademicYear> academicYears,
                                                       List<Isced> isceds, Institution partnerInstitution,
                                                       DataSheetRowService dataSheetRowService, boolean selfAssessment) {
        Integer outgoingStudents = dataSheetRowService.getDataSheetRowsCount(institution, academicYears, isceds,
                partnerInstitution, DataSheetCode.STUDENTS_OUTGOING);
        Integer outgoingStaff = dataSheetRowService.getDataSheetRowsCount(institution, academicYears, isceds,
                partnerInstitution, DataSheetCode.STAFF_OUTGOING);
        Integer incomingStudents = dataSheetRowService.getDataSheetRowsCount(institution, academicYears, isceds,
                partnerInstitution, DataSheetCode.STUDENTS_INCOMING);
        Integer incomingStaff = dataSheetRowService.getDataSheetRowsCount(institution, academicYears, isceds,
                partnerInstitution, DataSheetCode.STAFF_INCOMING);

        return new Explanation(
                new String[]{String.format(
                        "The mobility rate indicator is based on all incoming and outgoing student and staff " +
                                "mobilities with %s for the selected academic years. 50%% of the " +
                                "total score reflects whether there has been a mobility in each academic year (yes or no). " +
                                "The other half of the score is based on the overall balance between incoming and outgoing " +
                                "mobility flows for both students and staff.", partnerInstitution.getDisplayName())},
                new String[]{
                        String.format("Outgoing: %d (students) - %d (staff)", outgoingStudents, outgoingStaff),
                        String.format("Incoming: %d (students) - %d (staff)", incomingStudents, incomingStaff)});
    }

    @Override
    public List<Object[]> getPartnerInstitutionsScores(List<Institution> institutions, List<AcademicYear> academicYears,
                                                       List<Isced> isceds, List<Institution> filteredInstitutions,
                                                       DataSheetRowService dataSheetRowService) {
        Integer academicYearsWithMobilityCount = dataSheetRowService.getAcademicYearsWithMobilityCount(institutions,
                academicYears, isceds);

        return dataSheetRowService.calculateMobilityRateScores(institutions, academicYears, isceds,
                filteredInstitutions, academicYearsWithMobilityCount);
    }

    @Override
    public Double rescale(Double rawScore) {
        return rawScore;
    }
}

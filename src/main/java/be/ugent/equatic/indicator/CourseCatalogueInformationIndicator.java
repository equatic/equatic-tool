package be.ugent.equatic.indicator;

import be.ugent.equatic.domain.*;
import be.ugent.equatic.service.DataSheetRowService;

import java.util.List;

public class CourseCatalogueInformationIndicator extends IndicatorFromScalarTuples {

    @Override
    public String getName() {
        return "Course catalogue information";
    }

    @Override
    public String getDescription() {
        return "This indicator refers to the information in the course catalogue at the receiving partner " +
                "institution. It is based on the feedback from students in the Erasmus+ participant reports. " +
                "Students indicate whether the course catalogue was: up to date (+4); available in time (+3); " +
                "complete (+2) and/or published on the website (+1).";
    }

    @Override
    public Explanation getInstitutionReportExplanation(Institution institution, List<AcademicYear> academicYears,
                                                       List<Isced> isceds, Institution partnerInstitution,
                                                       DataSheetRowService dataSheetRowService, boolean selfAssessment) {
        int allRowsCount =
                dataSheetRowService.countRowsWithColumn(institution, academicYears, isceds, partnerInstitution,
                        DataSheetColumnCode.SMS_COURSE_CATALOGUE);
        int rowsWithPublishedCount =
                dataSheetRowService.countRowsWithColumnAndValue(institution, academicYears, isceds, partnerInstitution,
                        DataSheetColumnCode.SMS_COURSE_CATALOGUE, DataSheetValueCode.COURSE_CATALOGUE_PUBLISHED);
        int rowsWithCompleteCount =
                dataSheetRowService.countRowsWithColumnAndValue(institution, academicYears, isceds, partnerInstitution,
                        DataSheetColumnCode.SMS_COURSE_CATALOGUE, DataSheetValueCode.COURSE_CATALOGUE_COMPLETE);
        int rowsWithInTimeCount =
                dataSheetRowService.countRowsWithColumnAndValue(institution, academicYears, isceds, partnerInstitution,
                        DataSheetColumnCode.SMS_COURSE_CATALOGUE, DataSheetValueCode.COURSE_CATALOGUE_IN_TIME);
        int rowsWithUpToDateCount =
                dataSheetRowService.countRowsWithColumnAndValue(institution, academicYears, isceds, partnerInstitution,
                        DataSheetColumnCode.SMS_COURSE_CATALOGUE, DataSheetValueCode.COURSE_CATALOGUE_UP_TO_DATE);

        return new Explanation(
                new String[]{"This indicator evaluates the availability of information necessary to complete " +
                        "the learning agreement. It is based on the feedback from students. They indicate whether " +
                        "the course catalogue was: up to date (+4); available in time (+3); complete (+2); " +
                        "published on the website (+1)."},
                new String[]{
                        String.format("Published on the website: %d%%",
                                allRowsCount > 0 ? rowsWithPublishedCount * 100 / allRowsCount : 0),
                        String.format("Complete: %d%%",
                                allRowsCount > 0 ? rowsWithCompleteCount * 100 / allRowsCount : 0),
                        String.format("Available in time: %d%%",
                                allRowsCount > 0 ? rowsWithInTimeCount * 100 / allRowsCount : 0),
                        String.format("Up to date: %d%%",
                                allRowsCount > 0 ? rowsWithUpToDateCount * 100 / allRowsCount : 0)});
    }

    @Override
    public List<Object[]> getPartnerInstitutionsScores(List<Institution> institutions, List<AcademicYear> academicYears,
                                                       List<Isced> isceds, List<Institution> filteredInstitutions,
                                                       DataSheetRowService dataSheetRowService) {
        return dataSheetRowService.calculateCourseCatalogueInformationScores(institutions, academicYears, isceds,
                filteredInstitutions);
    }

    @Override
    public Double rescale(Double rawScore) {
        return 10.0 * rawScore;
    }
}

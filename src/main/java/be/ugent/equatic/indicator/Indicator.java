package be.ugent.equatic.indicator;

import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.Isced;
import be.ugent.equatic.service.DataSheetRowService;

import java.util.List;
import java.util.Map;

public interface Indicator {

    String getName();

    String getDescription();

    Explanation getInstitutionReportExplanation(Institution institution, List<AcademicYear> academicYears,
                                                List<Isced> isceds, Institution partnerInstitution,
                                                DataSheetRowService dataSheetRowService, boolean selfAssessment);

    Map<Long, Score> getPartnerInstitutionsScoreMap(List<Institution> institutions,
                                                    List<AcademicYear> academicYears, List<Isced> isceds,
                                                    List<Institution> filteredInstitutions,
                                                    DataSheetRowService dataSheetRowService);

    Double rescale(Double rawScore);
}

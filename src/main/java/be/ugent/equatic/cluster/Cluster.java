package be.ugent.equatic.cluster;

import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.Isced;
import be.ugent.equatic.indicator.Score;
import be.ugent.equatic.service.DataSheetRowService;

import java.util.List;
import java.util.Map;

public interface Cluster {

    String getName();

    Map<Long, Score> getPartnerInstitutionsScoreMap(List<Institution> institutions,
                                                    List<AcademicYear> academicYears, List<Isced> isceds,
                                                    List<Institution> filteredInstitutions,
                                                    DataSheetRowService dataSheetRowService);
}

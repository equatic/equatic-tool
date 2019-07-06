package be.ugent.equatic.indicator;

import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.Isced;
import be.ugent.equatic.service.DataSheetRowService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EducationalCooperationIndicator implements Indicator {

    @Override
    public String getName() {
        return "Education cooperation";
    }

    @Override
    public String getDescription() {
        return "This indicator takes into account projects and double/joint/multiple degree programmes with partner" +
                "institution.";
    }

    @Override
    public Explanation getInstitutionReportExplanation(Institution institution, List<AcademicYear> academicYears,
                                                       List<Isced> isceds, Institution partnerInstitution,
                                                       DataSheetRowService dataSheetRowService) {
        return new Explanation(
                new String[]{"The total score is a combination of a subscore for projects and a subscore for" +
                        "programmes where both elements get the same weight in the calculation of the indicator" +
                        "score. For the subscore on projects the institution with the highest number of projects gets" +
                        "the highest score. For double/joint/multiple degrees programmes both the number of" +
                        "programmes and the number of graduates in those programmes are taken into account."},
                new String[]{
                        String.format("Joint projects: %d",
                                dataSheetRowService.countEducationalProjects(institution, academicYears, isceds,
                                        partnerInstitution)),
                        String.format("Double/Joint/Multiple degrees programmes: %d",
                                dataSheetRowService.countJointProgrammes(institution, academicYears, isceds,
                                        partnerInstitution)),
                        String.format("Graduates Double/Joint/Multiple degrees: %d",
                                dataSheetRowService.countJointProgrammesGraduates(institution, academicYears, isceds,
                                        partnerInstitution))});
    }

    @Override
    public Map<Long, Score> getPartnerInstitutionsScoreMap(List<Institution> institutions,
                                                           List<AcademicYear> academicYears,
                                                           List<Isced> isceds,
                                                           List<Institution> filteredInstitutions,
                                                           DataSheetRowService dataSheetRowService) {
        List<Object[]> partnerInstitutionsScores =
                dataSheetRowService.calculateEducationalCooperationScores(institutions, academicYears, isceds,
                        filteredInstitutions);

        Map<Long, Score> partnerInstitutionsScoreMap = new HashMap<>();
        for (Object[] entry : partnerInstitutionsScores) {
            Long institutionId = (Long) entry[0];
            Double score = (Double) entry[1];

            partnerInstitutionsScoreMap.put(institutionId, new Score(score));
        }

        return partnerInstitutionsScoreMap;
    }

    @Override
    public Double rescale(Double rawScore) {
        return null;
    }
}

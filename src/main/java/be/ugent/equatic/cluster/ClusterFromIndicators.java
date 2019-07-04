package be.ugent.equatic.cluster;

import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.Isced;
import be.ugent.equatic.indicator.Indicator;
import be.ugent.equatic.indicator.IndicatorCode;
import be.ugent.equatic.indicator.Score;
import be.ugent.equatic.service.DataSheetRowService;

import java.util.*;

public abstract class ClusterFromIndicators implements Cluster {

    /**
     * @return indicator codes the cluster consists of
     */
    abstract protected IndicatorCode[] indicatorCodes();

    @Override
    public Map<Long, Score> getPartnerInstitutionsScoreMap(List<Institution> institutions,
                                                           List<AcademicYear> academicYears, List<Isced> isceds,
                                                           List<Institution> filteredInstitutions,
                                                           DataSheetRowService dataSheetRowService) {
        List<Map<Long, Score>> scoreMapList = new ArrayList<>();
        for (IndicatorCode indicatorCode : indicatorCodes()) {
            Indicator indicator = indicatorCode.getIndicator();
            Map<Long, Score> scoreMap = indicator.getPartnerInstitutionsScoreMap(institutions, academicYears, isceds,
                    filteredInstitutions, dataSheetRowService);
            scoreMapList.add(scoreMap);
        }

        Map<Long, Institution> partnerInstitutionMap = dataSheetRowService.getPartnerInstitutionMap(institutions,
                academicYears, filteredInstitutions);

        Map<Long, Score> resultMap = new HashMap<>();
        for (Long partnerInstitutionId : partnerInstitutionMap.keySet()) {
            OptionalDouble average = scoreMapList.stream()
                    .map(scoreMap -> scoreMap.get(partnerInstitutionId)).filter(Objects::nonNull)
                    .mapToDouble(Score::getValue).average();

            if (average.isPresent()) {
                resultMap.put(partnerInstitutionId, new Score(average.getAsDouble()));
            }
        }
        return resultMap;
    }
}

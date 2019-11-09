package be.ugent.equatic.service;

import be.ugent.equatic.cluster.Cluster;
import be.ugent.equatic.cluster.ClusterCode;
import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.Country;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.indicator.Explanation;
import be.ugent.equatic.indicator.Indicator;
import be.ugent.equatic.indicator.IndicatorCode;
import be.ugent.equatic.indicator.Score;
import be.ugent.equatic.web.util.InstitutionsScoresMode;
import be.ugent.equatic.web.util.InstitutionsScoresOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InstitutionsScoresService {

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private DataSheetRowService dataSheetRowService;

    public Map<Institution, List<Score>> getInstitutionsScoresMap(List<Institution> institutions,
                                                                  InstitutionsScoresOptions options) {
        List<AcademicYear> academicYears = academicYearService.findYearBetween(options.getAcademicYearFrom(),
                options.getAcademicYearTo());

        InstitutionsScoresMode mode = options.getMode();
        List<Map<Long, Score>> institutionsScoresMapList = getInstitutionsScoresMapList(institutions, options,
                academicYears, mode);

        Map<Long, Institution> partnerInstitutionMap = dataSheetRowService.getPartnerInstitutionMap(institutions,
                academicYears, options.getInstitutions());

        Map<Institution, List<Score>> resultMap = new HashMap<>();
        for (Map.Entry<Long, Institution> partnerInstitutionEntry : partnerInstitutionMap.entrySet()) {
            Long institutionId = partnerInstitutionEntry.getKey();
            Institution partnerInstitution = partnerInstitutionEntry.getValue();

            List<Score> scores = institutionsScoresMapList.stream()
                    .map(institutionsScoresMap -> institutionsScoresMap.get(institutionId))
                    .collect(Collectors.toList());

            if (!scores.stream().allMatch(Objects::isNull)) {
                if (mode == InstitutionsScoresMode.CLUSTERS) {
                    if (scores.stream().allMatch(Objects::nonNull)) {
                        OptionalDouble average = scores.stream().mapToDouble(Score::getValue).average();
                        scores.add(new Score(average.getAsDouble()));
                    } else {
                        scores.add(null);
                    }
                }

                resultMap.put(partnerInstitution, scores);
            }
        }
        return resultMap;
    }

    public Map<Country, List<Score>> getCountriesScoresMap(List<Institution> institutions,
                                                           InstitutionsScoresOptions options) {
        List<AcademicYear> academicYears = academicYearService.findYearBetween(options.getAcademicYearFrom(),
                options.getAcademicYearTo());

        InstitutionsScoresMode mode = options.getMode();
        List<Map<Long, Score>> institutionsScoresMapList = getInstitutionsScoresMapList(institutions, options,
                academicYears, mode);

        Map<Country, List<Institution>> partnerInstitutionIdsByCountryMap = dataSheetRowService
                .partnerInstitutionsByCountryMap(institutions, academicYears);

        Map<Country, List<Score>> resultMap = new HashMap<>();
        for (Map.Entry<Country, List<Institution>> countryEntry : partnerInstitutionIdsByCountryMap.entrySet()) {
            Country country = countryEntry.getKey();
            List<Long> partnerInstitutionIds = countryEntry.getValue().stream().map(Institution::getId)
                    .collect(Collectors.toList());

            List<Score> scores = institutionsScoresMapList.stream()
                    .map(institutionsScoresMap -> {
                        OptionalDouble countryAverage = institutionsScoresMap.entrySet().stream().filter(
                                institutionScore -> partnerInstitutionIds.contains(institutionScore.getKey()))
                                .map(Map.Entry::getValue).mapToDouble(Score::getValue).average();
                        return countryAverage.isPresent() ? new Score(countryAverage.getAsDouble()) : null;
                    })
                    .collect(Collectors.toList());

            if (!scores.stream().allMatch(Objects::isNull)) {
                if (mode == InstitutionsScoresMode.CLUSTERS) {
                    if (scores.stream().allMatch(Objects::nonNull)) {
                        OptionalDouble average = scores.stream().mapToDouble(Score::getValue).average();
                        scores.add(new Score(average.getAsDouble()));
                    } else {
                        scores.add(null);
                    }
                }

                resultMap.put(country, scores);
            }
        }

        return resultMap;
    }

    private List<Map<Long, Score>> getInstitutionsScoresMapList(List<Institution> institutions,
                                                                InstitutionsScoresOptions options,
                                                                List<AcademicYear> academicYears,
                                                                InstitutionsScoresMode mode) {
        switch (mode) {
            case DETAILED:
                return getInstitutionsScoresMapList(institutions, options, academicYears);

            case CLUSTERS:
                return getInstitutionsClusterScoresMapList(institutions, options, academicYears);

            default:
                throw new RuntimeException("Incorrect option: " + mode);
        }
    }

    private List<Map<Long, Score>> getInstitutionsScoresMapList(List<Institution> institutions,
                                                                InstitutionsScoresOptions options,
                                                                List<AcademicYear> academicYears) {
        List<Map<Long, Score>> institutionsScoresMapList = new ArrayList<>();
        for (IndicatorCode indicatorCode : options.getIndicatorCodes()) {
            Indicator indicator = indicatorCode.getIndicator();
            Map<Long, Score> institutionsScoreMap = indicator.getPartnerInstitutionsScoreMap(institutions,
                    academicYears, options.getIsceds(), options.getInstitutions(), dataSheetRowService);
            institutionsScoresMapList.add(institutionsScoreMap);
        }
        return institutionsScoresMapList;
    }

    private List<Map<Long, Score>> getInstitutionsClusterScoresMapList(List<Institution> institutions,
                                                                       InstitutionsScoresOptions options,
                                                                       List<AcademicYear> academicYears) {
        List<Map<Long, Score>> institutionsScoresMapList = new ArrayList<>();
        for (ClusterCode clusterCode : ClusterCode.values()) {
            Cluster cluster = clusterCode.getCluster();
            Map<Long, Score> institutionsScoreMap = cluster.getPartnerInstitutionsScoreMap(institutions,
                    academicYears, options.getIsceds(), options.getInstitutions(), dataSheetRowService);
            institutionsScoresMapList.add(institutionsScoreMap);
        }
        return institutionsScoresMapList;
    }

    public List<Explanation> getExplanations(Institution institution, InstitutionsScoresOptions options,
                                             boolean selfAssessment) {
        List<Explanation> explanations = new ArrayList<>();
        List<AcademicYear> academicYears = academicYearService.findYearBetween(options.getAcademicYearFrom(),
                options.getAcademicYearTo());
        List<Institution> partnerInstitutions = options.getInstitutions();
        if (partnerInstitutions == null || partnerInstitutions.size() != 1) {
            throw new RuntimeException("Expected one institution in filter but got: " + partnerInstitutions);
        }
        Institution partnerInstitution = partnerInstitutions.get(0);

        for (IndicatorCode indicatorCode : options.getIndicatorCodes()) {
            Indicator indicator = indicatorCode.getIndicator();
            Explanation explanation = indicator.getInstitutionReportExplanation(institution,
                    academicYears, options.getIsceds(), partnerInstitution, dataSheetRowService, selfAssessment);
            explanations.add(explanation);
        }

        return explanations;
    }
}

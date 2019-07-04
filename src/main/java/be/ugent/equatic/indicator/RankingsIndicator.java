package be.ugent.equatic.indicator;

import be.ugent.equatic.domain.*;
import be.ugent.equatic.service.DataSheetRowService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RankingsIndicator implements Indicator {

    @Override
    public String getName() {
        return "Rankings";
    }

    @Override
    public String getDescription() {
        return "The indicator on rankings is based on the three major rankings ARWU, THE and QS. Institutions that " +
                "are ranked in at least two rankings in top-500 will get a score on this indicator.";
    }

    @Override
    public Explanation getInstitutionReportExplanation(Institution institution, List<AcademicYear> academicYears,
                                                       List<Isced> isceds, Institution partnerInstitution,
                                                       DataSheetRowService dataSheetRowService) {
        AcademicYear academicYear =
                dataSheetRowService.getMostRecentAcademicYearByDataSheetCode(DataSheetCode.RANKINGS);

        String arwuPosition =
                dataSheetRowService.getRankingPosition(partnerInstitution, DataSheetColumnCode.RANKING_AWRU_POSITION,
                        academicYear);
        String thePosition =
                dataSheetRowService.getRankingPosition(partnerInstitution, DataSheetColumnCode.RANKING_THE_POSITION,
                        academicYear);
        String qsPosition =
                dataSheetRowService.getRankingPosition(partnerInstitution, DataSheetColumnCode.RANKING_QS_POSITION,
                        academicYear);

        Explanation explanation =
                new Explanation(new String[]{"The rankings indicator takes three major rankings into account: " +
                        "the Academic Ranking of World Universities (ARWU), the World University Rankings from " +
                        "Times Higher Education (THE) and the QS University Rankings (QS). Institutions that appear " +
                        "in the top-500 of at least two of the rankings will get an indicator score based on their " +
                        "average position in the rankings. The highest ranked institution gets the highest indicator score."},
                        new String[]{
                                String.format("ARWU: %s <br/> THE: %s <br/> QS: %s", arwuPosition, thePosition,
                                        qsPosition)});
        explanation.hideReferenceGroup();

        return explanation;
    }

    @Override
    public Map<Long, Score> getPartnerInstitutionsScoreMap(List<Institution> institutions,
                                                           List<AcademicYear> academicYears, List<Isced> isceds,
                                                           List<Institution> filteredInstitutions,
                                                           DataSheetRowService dataSheetRowService) {
        AcademicYear academicYear =
                dataSheetRowService.getMostRecentAcademicYearByDataSheetCode(DataSheetCode.RANKINGS);

        if (academicYear == null) {
            return new HashMap<>();
        }

        List<Object[]> partnerInstitutionsRankingAverages =
                dataSheetRowService.getPartnerInstitutionsRankingAverages(academicYear);

        Map<Double, Integer> averageCountsMap = getAverageCountsMap(partnerInstitutionsRankingAverages);

        double scorePerRank = 100.0 / partnerInstitutionsRankingAverages.size();

        Map<Double, Double> averageScoreMap = getAverageScoreMap(averageCountsMap, scorePerRank);

        Map<Long, Score> partnerInstitutionsScoreMap = new HashMap<>();
        for (Object[] partnerInstitutionRankingAverage : partnerInstitutionsRankingAverages) {
            long partnerInstitutionId = (long) partnerInstitutionRankingAverage[0];
            Double average = (Double) partnerInstitutionRankingAverage[1];

            if (average != null && (filteredInstitutions == null || filteredInstitutions.stream().anyMatch(
                    institution -> institution.getId() == partnerInstitutionId))) {
                partnerInstitutionsScoreMap.put(partnerInstitutionId, new Score(averageScoreMap.get(average)));
            }
        }

        return partnerInstitutionsScoreMap;
    }

    private Map<Double, Integer> getAverageCountsMap(List<Object[]> partnerInstitutionsRankingAverages) {
        Map<Double, Integer> averageCountsMap = new HashMap<>();
        for (Object[] partnerInstitutionRankingAverage : partnerInstitutionsRankingAverages) {
            Double average = (Double) partnerInstitutionRankingAverage[1];
            if (average != null) {
                int count = (averageCountsMap.containsKey(average)) ? averageCountsMap.get(average) + 1 : 1;
                averageCountsMap.put(average, count);
            }
        }
        return averageCountsMap;
    }

    private Map<Double, Double> getAverageScoreMap(Map<Double, Integer> averageCountsMap, double scorePerRank) {
        Map<Double, Integer> sortedAverageCountsMap = new TreeMap<>(averageCountsMap);

        double currentScore = 100;

        Map<Double, Double> averageScoreMap = new HashMap<>();
        for (Map.Entry<Double, Integer> entry : sortedAverageCountsMap.entrySet()) {
            double average = entry.getKey();
            int count = entry.getValue();

            double score;
            if (count == 1) {
                score = currentScore;
                currentScore -= scorePerRank;
            } else {
                score = (2 * currentScore - scorePerRank * (count - 1)) / 2;
                currentScore -= scorePerRank * count;
            }

            averageScoreMap.put(average, score);
        }
        return averageScoreMap;
    }

    @Override
    public Double rescale(Double rawScore) {
        return null;
    }
}

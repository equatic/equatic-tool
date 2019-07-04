package be.ugent.equatic.indicator;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.Isced;
import be.ugent.equatic.service.DataSheetRowService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.StrictMath.sqrt;

abstract public class IndicatorFromScalarTuples implements Indicator {

    @Override
    public Map<Long, Score> getPartnerInstitutionsScoreMap(List<Institution> institutions,
                                                           List<AcademicYear> academicYears,
                                                           List<Isced> isceds,
                                                           List<Institution> filteredInstitutions,
                                                           DataSheetRowService dataSheetRowService) {
        List<Object[]> partnerInstitutionsScores = getPartnerInstitutionsScores(institutions, academicYears, isceds,
                filteredInstitutions, dataSheetRowService);

        int subscoreCount = getSubscoreCount();
        Double[] subscoreVariances = getSubscoreVariances(partnerInstitutionsScores, subscoreCount);

        Double correlation = null;
        if (subscoreCount > 1) {
            correlation = getCorrelation(partnerInstitutionsScores, subscoreCount);
        }

        Map<Long, Score> partnerInstitutionsScoreMap = new HashMap<>();
        for (Object[] entry : partnerInstitutionsScores) {
            Long institutionId = (Long) entry[0];
            Double rawScore = (Double) entry[1];

            if (rawScore != null) {
                Double score = rescale(rawScore);
                Long studentsCounts = (Long) entry[2];

                partnerInstitutionsScoreMap.put(institutionId, new Score(score, studentsCounts,
                        rescaleStandardError(getStandardError(subscoreVariances, studentsCounts, correlation))));
            }
        }

        return partnerInstitutionsScoreMap;
    }

    protected Double rescaleStandardError(double standardError) {
        return rescale(standardError);
    }

    protected int getSubscoreCount() {
        return 1;
    }

    private static double getCorrelation(List<Object[]> partnerInstitutionsScores, int subscoreCount) {
        if (subscoreCount > 2) {
            throw new RuntimeException("Only two subscores are currently handled");
        }
        double[] subscore1 = new double[partnerInstitutionsScores.size()];
        double[] subscore2 = new double[partnerInstitutionsScores.size()];

        int i = 0;
        for (Object[] entry : partnerInstitutionsScores) {
            Double rawScore = (Double) entry[1];
            Long studentsCounts = (Long) entry[2];

            if (rawScore != null && studentsCounts > 1 && entry[4] != null && entry[6] != null) {
                subscore1[i] = (Double) entry[4];
                subscore2[i] = (Double) entry[6];
                i++;
            }
        }

        if (subscore1.length < 2) {
            return 0.0;
        }

        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
        return pearsonsCorrelation.correlation(subscore1, subscore2);
    }

    private double getStandardError(Double[] variance, Long studentsCounts, Double correlation) {
        if (correlation == null) {
            return sqrt(variance[0] / studentsCounts);
        } else {
            Double[] subscoreWeights = getSubscoreWeights(variance);
            Double w1 = subscoreWeights[0];
            Double w2 = subscoreWeights[1];
            Double se1 = sqrt(variance[0] / studentsCounts);
            Double se2 = sqrt(variance[1] / studentsCounts);

            return Math.pow(w1, 2) * Math.pow(se1, 2) + Math.pow(w2, 2) * Math.pow(se2,
                    2) + 2 * w1 * w2 * correlation * se1 * se2;
        }
    }

    protected Double[] getSubscoreWeights(Double[] variance) {
        throw new RuntimeException("Method should not be called or should be implemented");
    }

    private static Double[] getSubscoreVariances(List<Object[]> partnerInstitutionsScores, int subscoreCount) {
        // Sum of square roots of mean to score difference for all partner institutions with more than one observation
        Double[] subscoreSumSquareDiffValid = new Double[subscoreCount];
        Arrays.fill(subscoreSumSquareDiffValid, 0.0);

        Long partnerInstitutionsValid = 0L;
        Long studentCountsValid = 0L;

        for (Object[] entry : partnerInstitutionsScores) {
            Double rawScore = (Double) entry[1];
            Long studentsCounts = (Long) entry[2];

            if (rawScore != null && entry.length > 3 && studentsCounts > 1) {
                for (int subscoreIndex = 0; subscoreIndex < subscoreCount; subscoreIndex++) {
                    Double subscoreSumSquareDiff = (Double) entry[3 + 2 * subscoreIndex];
                    if (subscoreSumSquareDiff != null) {
                        subscoreSumSquareDiffValid[subscoreIndex] += subscoreSumSquareDiff;
                    }
                }
                partnerInstitutionsValid++;
                studentCountsValid += studentsCounts;
            }
        }

        Double[] subscoreVariances = new Double[subscoreCount];
        for (int subscoreIndex = 0; subscoreIndex < subscoreCount; subscoreIndex++) {
            subscoreVariances[subscoreIndex] =
                    subscoreSumSquareDiffValid[subscoreIndex] / (studentCountsValid - partnerInstitutionsValid);
        }

        return subscoreVariances;
    }

    /**
     * @return tuples (institutionId, score, studentsCount)
     */
    abstract protected List<Object[]> getPartnerInstitutionsScores(List<Institution> institutions,
                                                                   List<AcademicYear> academicYears,
                                                                   List<Isced> isceds,
                                                                   List<Institution> filteredInstitutions,
                                                                   DataSheetRowService dataSheetRowService);
}

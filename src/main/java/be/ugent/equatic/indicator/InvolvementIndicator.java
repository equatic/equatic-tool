package be.ugent.equatic.indicator;

import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.Isced;
import be.ugent.equatic.service.DataSheetRowService;
import be.ugent.equatic.util.BroadIscedStat;
import be.ugent.equatic.util.IscedStat;

import java.util.*;

public class InvolvementIndicator implements Indicator {

    @Override
    public String getName() {
        return "Involvement";
    }

    @Override
    public String getDescription() {
        return "The indicator denotes the involvement of different programmes (based on ISCED narrow field) " +
                "and faculties/departments (based on ISCED broad field) with a certain partner institutions " +
                "based on existing agreements.";
    }

    @Override
    public Explanation getInstitutionReportExplanation(Institution institution, List<AcademicYear> academicYears,
                                                       List<Isced> isceds, Institution partnerInstitution,
                                                       DataSheetRowService dataSheetRowService, boolean selfAssessment) {
        Map<Long, IscedStat> partnerInstitutionsIscedStatMap = dataSheetRowService.getPartnerInstitutionsIscedStatMap(
                Collections.singletonList(institution), academicYears, isceds,
                Collections.singletonList(partnerInstitution));
        IscedStat iscedStat = partnerInstitutionsIscedStatMap.get(partnerInstitution.getId());

        if (iscedStat == null) {
            return null;
        }

        String[] paragraphs = {String.format("The indicator indicates the involvement with %1$s and is based " +
                        "on the number of different study fields (using ISCED narrow field) and faculties/departments " +
                        "(using ISCED broad field) that have an agreement with %1$s. For calculating this indicator " +
                        "the institution with the most number of faculties/departments and study fields " +
                        "gets the highest score. Consequently the score reflects the involvement " +
                        "compared to other partner institutions of %2$s.", partnerInstitution.getDisplayName(),
                institution.getDisplayName())};
        Explanation explanation = new Explanation(paragraphs,
                new String[]{String.format("Broad ISCED: %d", iscedStat.getBroadIscedCount()), String.format(
                        "Narrow ISCED: %d", iscedStat.getNarrowIscedCount())});
        explanation.hideReferenceGroup();

        return explanation;
    }

    @Override
    public Map<Long, Score> getPartnerInstitutionsScoreMap(List<Institution> institutions,
                                                           List<AcademicYear> academicYears, List<Isced> isceds,
                                                           List<Institution> filteredInstitutions,
                                                           DataSheetRowService dataSheetRowService) {
        Map<Integer, BroadIscedStat> broadIscedStatMap = dataSheetRowService.getBroadIscedStatMap(institutions,
                academicYears, isceds);

        Map<Long, IscedStat> partnerInstitutionsIscedStatMap = dataSheetRowService.getPartnerInstitutionsIscedStatMap(
                institutions, academicYears, isceds, filteredInstitutions);

        Optional<Integer> maxBroadIscedCountOptional = broadIscedStatMap.keySet().stream().max(Integer::compare);

        Map<Long, Score> partnerInstitutionsScoreMap = new HashMap<>();

        if (!maxBroadIscedCountOptional.isPresent() || maxBroadIscedCountOptional.get() == 0) {
            return partnerInstitutionsScoreMap;
        }
        Integer maxBroadIscedCount = maxBroadIscedCountOptional.get();

        for (Map.Entry<Long, IscedStat> iscedStatEntry : partnerInstitutionsIscedStatMap.entrySet()) {
            Long partnerInstitutionId = iscedStatEntry.getKey();
            int broadIscedCount = iscedStatEntry.getValue().getBroadIscedCount();
            int narrowIscedCount = iscedStatEntry.getValue().getNarrowIscedCount();

            double score;
            if (maxBroadIscedCount > 1) {
                double scorePerBroadIsced = 100.0 / maxBroadIscedCount;
                if (broadIscedCount == 1) {
                    BroadIscedStat broadIscedStat = broadIscedStatMap.get(1);
                    double divisor = (double) broadIscedStat.getMaxNarrowIscedCount()
                            - broadIscedStat.getMinNarrowIscedCount() + 2;
                    double multiplier = (double) narrowIscedCount - broadIscedStat.getMinNarrowIscedCount() + 1;
                    score = multiplier * scorePerBroadIsced / divisor;
                } else {
                    BroadIscedStat broadIscedStat = broadIscedStatMap.get(broadIscedCount);
                    double divisor = broadIscedStat.getMaxNarrowIscedCount() - broadIscedStat.getMinNarrowIscedCount();
                    if (broadIscedCount < maxBroadIscedCount) {
                        divisor += 1;
                    }

                    if (divisor == 0) {
                        score = 100;
                    } else {
                        double basis = ((double) broadIscedCount - 1) * scorePerBroadIsced;
                        double multiplier = (double) narrowIscedCount - broadIscedStat.getMinNarrowIscedCount();
                        score = basis + multiplier * scorePerBroadIsced / divisor;
                    }
                }
            } else {
                BroadIscedStat singleBroadIscedStat = broadIscedStatMap.get(1);
                score = ((double) narrowIscedCount * 100) / singleBroadIscedStat.getMaxNarrowIscedCount();
            }

            partnerInstitutionsScoreMap.put(partnerInstitutionId, new Score(score));
        }

        return partnerInstitutionsScoreMap;
    }

    @Override
    public Double rescale(Double rawScore) {
        return null;
    }
}

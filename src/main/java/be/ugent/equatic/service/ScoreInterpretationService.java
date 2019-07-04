package be.ugent.equatic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.ScoreInterpretation;
import be.ugent.equatic.domain.ScoreInterpretationRepository;
import be.ugent.equatic.indicator.IndicatorCode;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScoreInterpretationService {

    @Autowired
    private ScoreInterpretationRepository scoreInterpretationRepository;

    @Transactional(readOnly = true)
    public Map<String, ScoreInterpretation> getScoreInterpretationMap(Institution institution) {
        List<ScoreInterpretation> scoreInterpretations = scoreInterpretationRepository.findByInstitution(institution);

        Map<String, ScoreInterpretation> scoreInterpretationMap = new HashMap<>();
        for (ScoreInterpretation scoreInterpretation : scoreInterpretations) {
            scoreInterpretationMap.put(scoreInterpretation.getIndicatorCode().name(), scoreInterpretation);
        }

        for (IndicatorCode indicatorCode : IndicatorCode.values()) {
            if (!scoreInterpretationMap.containsKey(indicatorCode.name())) {
                scoreInterpretationMap.put(indicatorCode.name(), new ScoreInterpretation(institution, indicatorCode));
            }
        }

        return scoreInterpretationMap;
    }

    @Transactional
    public List<ScoreInterpretation> save(Collection<ScoreInterpretation> scoreInterpretation) {
        return scoreInterpretationRepository.save(scoreInterpretation);
    }
}

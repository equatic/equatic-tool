package be.ugent.equatic.web.util;

import be.ugent.equatic.domain.ScoreInterpretation;

import javax.validation.Valid;
import java.util.Map;

public class ScoreInterpretationsForm {

    @Valid
    private Map<String, ScoreInterpretation> scoreInterpretationMap;

    public ScoreInterpretationsForm(
            Map<String, ScoreInterpretation> scoreInterpretationMap) {
        this.scoreInterpretationMap = scoreInterpretationMap;
    }

    public Map<String, ScoreInterpretation> getScoreInterpretationMap() {
        return scoreInterpretationMap;
    }

    public void setScoreInterpretationMap(
            Map<String, ScoreInterpretation> scoreInterpretationMap) {
        this.scoreInterpretationMap = scoreInterpretationMap;
    }
}

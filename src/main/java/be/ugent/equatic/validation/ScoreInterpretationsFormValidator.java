package be.ugent.equatic.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import be.ugent.equatic.domain.ScoreInterpretation;
import be.ugent.equatic.web.util.ScoreInterpretationsForm;

public class ScoreInterpretationsFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return ScoreInterpretationsForm.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ScoreInterpretationsForm scoreInterpretationsForm = (ScoreInterpretationsForm) target;

        for (ScoreInterpretation scoreInterpretation : scoreInterpretationsForm.getScoreInterpretationMap().values()) {
            if (scoreInterpretation.getFairScoreMin() >= scoreInterpretation.getGoodScoreMin()) {
                errors.rejectValue(
                        "scoreInterpretationMap[" + scoreInterpretation.getIndicatorCode().getName() + "].fairScoreMin",
                        "ScoreInterpretation.fairScore.higherEqualGoodScore");
            }
        }
    }
}

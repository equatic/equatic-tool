package be.ugent.equatic.web.admin.institutional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.exception.InstitutionNotSelectedBySuperAdminException;
import be.ugent.equatic.service.ScoreInterpretationService;
import be.ugent.equatic.validation.ScoreInterpretationsFormValidator;
import be.ugent.equatic.web.util.Message;
import be.ugent.equatic.web.util.ScoreInterpretationsForm;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Locale;

@Controller
public class ScoreInterpretationController extends InstitutionalAdminController {

    private static final String VIEW_SCORE_INTERPRETATION = VIEW_PATH + "/scoreInterpretation";

    private final ScoreInterpretationsFormValidator scoreInterpretationsFormValidator =
            new ScoreInterpretationsFormValidator();

    @Autowired
    private ScoreInterpretationService scoreInterpretationService;

    @ModelAttribute("scoreInterpretationsForm")
    public ScoreInterpretationsForm getScoreInterpretationsForm(@ModelAttribute Institution institution) {
        return new ScoreInterpretationsForm(scoreInterpretationService.getScoreInterpretationMap(institution));
    }

    @RequestMapping(value = VIEW_SCORE_INTERPRETATION, method = RequestMethod.GET)
    public String scoreInterpretation() {
        return VIEW_SCORE_INTERPRETATION;
    }

    @RequestMapping(value = VIEW_SCORE_INTERPRETATION, method = RequestMethod.POST)
    public String editScoreInterpretation(@Valid ScoreInterpretationsForm scoreInterpretationsForm,
                                          BindingResult result, RedirectAttributes redirect, Locale locale,
                                          @RequestParam(required = false) Long instId,
                                          Principal principal) throws InstitutionNotSelectedBySuperAdminException {
        scoreInterpretationsFormValidator.validate(scoreInterpretationsForm, result);

        if (result.hasErrors()) {
            return VIEW_SCORE_INTERPRETATION;
        }

        scoreInterpretationService.save(scoreInterpretationsForm.getScoreInterpretationMap().values());

        String confirmationMessage =
                messageSource.getMessage("equatic.admin.institutional.scoreInterpretation.confirmation", null, locale);
        redirect.addFlashAttribute("message", Message.success(confirmationMessage));
        redirect.addAttribute("instId", getRequestedInstitution(instId, principal));

        return "redirect:" + VIEW_SCORE_INTERPRETATION;
    }
}

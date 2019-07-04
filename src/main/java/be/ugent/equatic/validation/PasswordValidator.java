package be.ugent.equatic.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import be.ugent.equatic.web.util.RawPasswordAndConfirm;

/**
 * Validates password and confirm password fields.
 */
public class PasswordValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return RawPasswordAndConfirm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RawPasswordAndConfirm form = (RawPasswordAndConfirm) target;

        if (form.getRawPassword() == null || form.getRawPassword().isEmpty()) {
            errors.rejectValue("rawPassword", "User.rawPassword.NotNull");
        } else if (form.getConfirmRawPassword() == null
                || !form.getRawPassword().equals(form.getConfirmRawPassword())) {
            errors.rejectValue("confirmRawPassword", "User.confirmRawPassword.Match");
        }
    }
}

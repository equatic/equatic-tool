package be.ugent.equatic.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.User;
import be.ugent.equatic.exception.UserNotFoundException;
import be.ugent.equatic.service.UserService;

/**
 * Makes additional validations of user entity.
 * The rest of the validation is done by entity annotations (JSR-303).
 */
public class UserAdditionalValidator implements Validator {

    private UserService userService;

    public UserAdditionalValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;
        Institution institution = user.getInstitution();

        if (institution != null && institution.getIdpEntityId() == null) {
            PasswordValidator passwordValidator = new PasswordValidator();
            passwordValidator.validate(user, errors);

            validateUserUnique(user, institution, errors);

            validateEmailUnique(user, institution, errors);
        }
    }

    private void validateUserUnique(User user, Institution institution, Errors errors) {
        try {
            userService.findByUsernameIgnoreCaseAndInstitution(user.getUsername(), institution);
        } catch (UserNotFoundException e) {
            return;
        }
        errors.rejectValue("username", "User.username.Unique");
    }

    private void validateEmailUnique(User user, Institution institution, Errors errors) {
        try {
            userService.findByEmailIgnoreCaseAndInstitution(user.getEmail(), institution);
        } catch (UserNotFoundException e) {
            return;
        }
        errors.rejectValue("email", "User.email.Unique");
    }
}

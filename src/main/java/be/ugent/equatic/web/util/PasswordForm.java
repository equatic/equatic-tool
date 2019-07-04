package be.ugent.equatic.web.util;

import javax.validation.constraints.Size;

/**
 * Form for changing user's password.
 */
public class PasswordForm implements RawPasswordAndConfirm {

    @Size(max = 100)
    private String rawPassword;

    @Size(max = 100)
    private String confirmRawPassword;

    public String getRawPassword() {
        return rawPassword;
    }

    public void setRawPassword(String rawPassword) {
        this.rawPassword = rawPassword;
    }

    public String getConfirmRawPassword() {
        return confirmRawPassword;
    }

    public void setConfirmRawPassword(String confirmRawPassword) {
        this.confirmRawPassword = confirmRawPassword;
    }
}

package be.ugent.equatic.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * Extends UsernamePasswordAuthenticationFilter to handle additional institution selector.
 *
 * @see org.springframework.security.authentication.UsernamePasswordAuthenticationToken
 */
public class InstitutionUsernamePasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private Long institutionId;

    /**
     * Stores additional institution ID.
     *
     * @param username      username
     * @param password      password
     * @param institutionId ID of the institution
     */
    InstitutionUsernamePasswordAuthenticationToken(String username, String password, Long institutionId) {
        super(username, password);
        this.institutionId = institutionId;
    }

    public Long getInstitutionId() {
        return institutionId;
    }
}

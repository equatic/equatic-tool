package be.ugent.equatic.domain;

import org.springframework.security.core.GrantedAuthority;

/**
 * List of user roles in eQuATIC.
 */
public enum Authority implements GrantedAuthority {
    /**
     * Institutional user.
     */
    ROLE_USER_INSTITUTIONAL,
    /**
     * National user.
     */
    ROLE_USER_NATIONAL,
    /**
     * Institutional administrator.
     */
    ROLE_ADMIN_INSTITUTIONAL,
    /**
     * National administrator.
     */
    ROLE_ADMIN_NATIONAL,
    /**
     * Super administrator of the eQuATIC tool.
     */
    ROLE_ADMIN_SUPER;

    public String getAuthority() {
        return name();
    }
}

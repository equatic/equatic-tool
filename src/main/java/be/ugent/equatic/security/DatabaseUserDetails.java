package be.ugent.equatic.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import be.ugent.equatic.domain.Authority;
import be.ugent.equatic.domain.Role;
import be.ugent.equatic.domain.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides user details by collecting user roles.
 */
public class DatabaseUserDetails implements UserDetails {

    private User user;

    private List<GrantedAuthority> authorities = new ArrayList<>();

    /**
     * Constructs user details based on the roles stored in the database.
     * <p>
     * <h3>Implementation notes</h3>
     * Only admin roles are stored in database so we add appropriate user role if user has no administration roles.
     *
     * @param user the User to construct details for
     */
    public DatabaseUserDetails(User user) {
        super();
        this.user = user;

        authorities = user.getAdminRoles().stream().map(Role::getRole).collect(Collectors.toList());
        if (authorities.isEmpty()) {
            Authority authority = user.getInstitution().isVirtual() ? Authority.ROLE_USER_NATIONAL : Authority.ROLE_USER_INSTITUTIONAL;
            authorities = Collections.singletonList(authority);
        }
    }

    /**
     * User is enabled (can sign in) if he has confirmed the e-mail and had his account activated by a institutional admin.
     *
     * @return true if user can sign in
     */
    @Override
    public boolean isEnabled() {
        return getUser().isEmailConfirmed() && getUser().isActivated();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return getUser().getPassword();
    }

    @Override
    public String getUsername() {
        return getUser().getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public User getUser() {
        return user;
    }

}
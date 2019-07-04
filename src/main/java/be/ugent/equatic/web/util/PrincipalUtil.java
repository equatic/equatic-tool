package be.ugent.equatic.web.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import be.ugent.equatic.domain.User;
import be.ugent.equatic.security.DatabaseUserDetails;

import java.security.Principal;

public class PrincipalUtil {

    /**
     * Retrieves user details from the principal object.
     * Assumes that the principal is an authentication token.
     *
     * @param principal the principal as an authentication token
     * @return User
     */
    public static User getUser(Principal principal) {
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) principal;
        return ((DatabaseUserDetails) authenticationToken.getPrincipal()).getUser();
    }
}

package be.ugent.equatic.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.exception.InstitutionNotFoundException;
import be.ugent.equatic.exception.UserNotFoundException;
import be.ugent.equatic.service.DatabaseUserDetailsService;
import be.ugent.equatic.service.InstitutionService;

/**
 * Authentication provider for the local eQuATIC accounts.
 * <p>
 * This is a modified version of org.springframework.security.authentication.dao.DaoAuthenticationProvider
 * that apart from username needs also institution ID to retrieve the user.
 * </p>
 */
public class DaoAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DatabaseUserDetailsService databaseUserDetailsService;

    @Autowired
    private InstitutionService institutionService;

    /**
     * Implementation copied from Spring but salt already set to null as it is not used.
     *
     * @param userDetails    UserDetails
     * @param authentication UsernamePasswordAuthenticationToken
     * @throws AuthenticationException when authentication fails
     */
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            logger.debug("Authentication failed: no credentials provided");

            //noinspection deprecation
            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }

        String presentedPassword = authentication.getCredentials().toString();

        if (!passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
            logger.debug("Authentication failed: password does not match stored value");

            //noinspection deprecation
            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
    }

    /**
     * Implementation copied from Spring but some additional checks were omitted for simplicity.
     * <p>
     * Uses {@link InstitutionUsernamePasswordAuthenticationToken} to get the username and institution ID.
     * <p>
     * Chosen institution must not by using federated IdP for the authentication to succeed.
     *
     * @param username       username of user to retrieve
     * @param authentication UsernamePasswordAuthenticationToken
     * @return UserDetails
     * @throws AuthenticationException when authentication fails
     */
    protected final UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        UserDetails loadedUser;
        InstitutionUsernamePasswordAuthenticationToken authenticationToken =
                (InstitutionUsernamePasswordAuthenticationToken) authentication;

        try {
            Long institutionId = authenticationToken.getInstitutionId();
            Institution institution = institutionService.findByIdNotWithFederatedIdP(institutionId);

            loadedUser = this.databaseUserDetailsService.loadUserByUsernameAndInstitution(username, institution);
        } catch (UserNotFoundException exception) {
            throw new InternalAuthenticationServiceException(messages.getMessage(
                    "equatic.usernameNotFoundAtInstitution", new String[]{username}, "User not found"), exception);
        } catch (InstitutionNotFoundException exception) {
            throw new InternalAuthenticationServiceException(messages.getMessage(
                    "DaoAuthenticationProvider.internalException", null, "Internal exception"), exception);
        }

        return loadedUser;
    }
}

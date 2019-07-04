package be.ugent.equatic.service;

import org.opensaml.saml2.core.Attribute;
import org.opensaml.xml.XMLObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.stereotype.Service;
import be.ugent.equatic.config.SamlProperties;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.exception.InstitutionNotFoundException;
import be.ugent.equatic.exception.UserNotFoundException;

import java.util.Locale;

/**
 * Loads user details from SAML credentials.
 *
 * @see org.springframework.security.saml.userdetails.SAMLUserDetailsService
 */
@Service
public class SAMLUserDetailsService implements org.springframework.security.saml.userdetails.SAMLUserDetailsService {

    @Autowired
    private DatabaseUserDetailsService databaseUserDetailsService;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SamlProperties samlProperties;

    /**
     * Searches for the user at institution by SAML username and entity ID fields.
     *
     * @throws UsernameNotFoundException if user or institution were not found
     * @throws DisabledException         if user's account is disabled
     */
    @Override
    public UserDetails loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException, DisabledException {
        Locale locale = LocaleContextHolder.getLocale();
        String username = credential.getAttributeAsString(samlProperties.getAttribute().getUsername());

        try {
            Institution institution = institutionService.findByIdpEntityId(credential.getRemoteEntityID());

            UserDetails user = databaseUserDetailsService.loadUserByUsernameAndInstitution(username, institution);

            if (!user.isEnabled()) {
                throw new DisabledException(
                        messageSource.getMessage("AbstractUserDetailsAuthenticationProvider.disabled", null, locale));
            }

            return user;
        } catch (InstitutionNotFoundException exception) {
            throw new UsernameNotFoundException(
                    messageSource.getMessage("SAMLUserDetailsService.institutionNotFound", null, locale), exception);
        } catch (UserNotFoundException exception) {
            exception.setFirstName(credential.getAttributeAsString(samlProperties.getAttribute().getFirstName()));
            exception.setLastName(credential.getAttributeAsString(samlProperties.getAttribute().getLastName()));
            exception.setEmail(credential.getAttributeAsString(samlProperties.getAttribute().getEmail()));

            throw new UsernameNotFoundException(
                    messageSource.getMessage("equatic.usernameNotFoundAtInstitution", new String[]{username}, locale),
                    exception);
        }
    }
}

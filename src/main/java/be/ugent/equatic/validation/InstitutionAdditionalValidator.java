package be.ugent.equatic.validation;

import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.ParserPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.service.InstitutionService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Makes additional validation of institution's fields: PIC, IdP entityID and IdP metadata URL.
 */
@Service
public class InstitutionAdditionalValidator implements Validator {

    private static final Logger log = Logger.getLogger(InstitutionAdditionalValidator.class.getName());

    @Autowired
    private ParserPool parserPool;

    @Autowired
    private InstitutionService institutionService;

    @Override
    public boolean supports(Class<?> clazz) {
        return Institution.class.equals(clazz);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void validate(Object target, Errors errors) {
        Institution institution = (Institution) target;

        String pic = institution.getPic();

        if (pic != null && !pic.isEmpty() && !pic.matches("[0-9]{9}")) {
            errors.rejectValue("pic", "Institution.pic.Format");
        }

        String idpMetadataUrl = institution.getIdpMetadataUrl();
        String idpEntityId = institution.getIdpEntityId();
        if (idpMetadataUrl != null && !idpMetadataUrl.isEmpty()) {
            try {
                HTTPMetadataProvider metadataProvider = new HTTPMetadataProvider(idpMetadataUrl, 5000);
                metadataProvider.setParserPool(parserPool);
                metadataProvider.refresh();
            } catch (MetadataProviderException e) {
                log.log(Level.WARNING, "Incorrect IdP metadata URL: " + idpMetadataUrl, e);
                errors.rejectValue("idpMetadataUrl", "Institution.idpMetadataUrl.Correct");
            }
            if (idpEntityId.isEmpty()) {
                errors.rejectValue("idpEntityId", "Institution.idpEntityId.NotEmptyIfIdpMetadataSet");
            }
        } else {
            if (idpEntityId != null && !idpEntityId.isEmpty()) {
                errors.rejectValue("idpMetadataUrl", "Institution.idpMetadataUrl.NotEmptyIfIdpEntityIdSet");
            }
        }

        String url = institution.getUrl();

        if (url != null) {
            try {
                List<Institution> institutions = institutionService.findByUrlSimilar(new URL(url));

                if (!institutions.isEmpty()) {
                    if (institutions.size() > 1 || !institutions.get(0).equals(institution)) {
                        errors.rejectValue("url", "Institution.url.Unique");
                    }
                }
            } catch (MalformedURLException e) {
                // Ignore if URL is malformed as it is checked by @URL
            }
        }
    }
}

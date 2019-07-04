package be.ugent.equatic.service;

import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.ParserPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.stereotype.Service;
import be.ugent.equatic.domain.Institution;

@Service
public class SAMLMetadataService {

    @Autowired
    private ParserPool parserPool;

    @Autowired
    private ExtendedMetadata extendedMetadata;

    public MetadataProvider getMetadataProvider(Institution institution) throws MetadataProviderException {
        @SuppressWarnings("deprecation")
        HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(institution.getIdpMetadataUrl(), 5000);
        httpMetadataProvider.setParserPool(parserPool);
        ExtendedMetadataDelegate extendedMetadataDelegate =
                new ExtendedMetadataDelegate(httpMetadataProvider, extendedMetadata);
        extendedMetadataDelegate.setMetadataTrustCheck(false);
        extendedMetadataDelegate.setMetadataRequireSignature(false);
        return extendedMetadataDelegate;
    }
}

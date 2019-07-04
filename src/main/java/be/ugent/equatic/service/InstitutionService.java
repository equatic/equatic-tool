package be.ugent.equatic.service;

import com.github.dandelion.datatables.core.ajax.DataSet;
import com.github.dandelion.datatables.core.ajax.DatatablesCriterias;
import org.hibernate.Query;
import org.hibernate.Session;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import be.ugent.equatic.domain.*;
import be.ugent.equatic.exception.InstitutionNotFoundException;
import be.ugent.equatic.exception.InstitutionNotSelectedBySuperAdminException;

import javax.persistence.EntityManager;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class InstitutionService {

    private static final String BELGIUM_COUNTRY_CODE = "BE";
    private static final String[] FLEMISH_INSTITUTIONS_ERASMUS_CODES = {
            "B  ANTWERP62",
            "B  GENT39",
            "B  BRUSSEL46",
            "B  HEVERLE05",
            "B  DIEPENB01",
            "B  GENT25",
            "B  KORTRIJ03",
            "B  ANTWERP59",
            "B  BRUGGE11",
            "B  KORTRIJ01",
            "B  BRUSSEL43",
            "B  BRUSSEL48",
            "B  HASSELT22",
            "B  GEEL07",
            "B  MECHELE14",
            "B  LEUVEN18",
            "B  HASSELT20",
            "B  GENT01",
            "B  ANTWERP01",
            "B  BRUSSEL01",
            "B  LEUVEN01"
    };
    private static final Pattern INSTITUTION_NAME_WITHOUT_BRACKETS_PATTERN = Pattern.compile("(.*) \\(.*\\)");

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private InstitutionDatatablesJpaRepository institutionDatatablesJpaRepository;

    @Autowired
    private SAMLMetadataService samlMetadataService;

    @Autowired
    private MetadataManager metadataManager;

    @Autowired
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<Institution> findNotVirtual() {
        return institutionRepository.findByVirtualFalseOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Institution> findActive() {
        return institutionRepository.findWithAdmin();
    }

    @Transactional(readOnly = true)
    public List<Institution> findActive(boolean virtual) {
        return institutionRepository.findWithAdmin(virtual);
    }

    @Transactional(readOnly = true)
    public List<Institution> findActiveUsingFederatedIdP() {
        return institutionRepository.findByIdpEntityIdIsNotNullAndWithAdmin();
    }

    @Transactional(readOnly = true)
    public Institution findById(Long institutionId) throws InstitutionNotFoundException {
        Institution institution = institutionRepository.findById(institutionId);

        if (institution == null) {
            throw InstitutionNotFoundException.byId(institutionId);
        }

        return institution;
    }

    @Transactional(readOnly = true)
    public Institution findByIdNotWithFederatedIdP(Long institutionId) throws InstitutionNotFoundException {
        Institution institution = institutionRepository.findById(institutionId);

        if (institution == null || institution.isWithFederatedIdP()) {
            throw InstitutionNotFoundException.byIdNotWithFederatedIdP(institutionId);
        }

        return institution;
    }

    @Transactional(readOnly = true)
    public Institution findByIdpEntityId(String remoteEntityID) throws InstitutionNotFoundException {
        Institution institution = institutionRepository.findByIdpEntityId(remoteEntityID);

        if (institution == null) {
            throw InstitutionNotFoundException.byIdpEntityId(remoteEntityID);
        }

        return institution;
    }

    @Transactional
    public void save(Institution... institutions) throws MetadataProviderException {
        institutionRepository.save(Arrays.asList(institutions));

        for (Institution institution : institutions) {
            if (institution.isWithFederatedIdP()) {
                metadataManager.addMetadataProvider(samlMetadataService.getMetadataProvider(institution));
            }
        }
    }

    @Transactional
    public void deleteAll() {
        institutionRepository.deleteAll();
    }

    @Transactional(readOnly = true)
    public Institution findByPic(String pic) throws InstitutionNotFoundException {
        Institution institution = findByPicOrNull(pic);

        if (institution == null) {
            throw InstitutionNotFoundException.byPic(pic);
        }

        return institution;
    }

    @Transactional(readOnly = true)
    public Institution findByPicOrNull(String pic) {
        return institutionRepository.findByPic(pic);
    }

    @Transactional(readOnly = true)
    public Institution findByErasmusCodeOrNull(String erasmusCode) {
        return institutionRepository.findByErasmusCodeIn(erasmusCode);
    }

    @Transactional(readOnly = true)
    public Institution findByNameAndCountryOrNull(String name, Country country) {
        return institutionRepository.findByNameAndCountry(name, country);
    }

    @Transactional(readOnly = true)
    public Institution findByNameAndCountryOrNullIgnoreAccentsBrackets(String name, Country country) {
        Session session = getSession();
        Query query = session.createSQLQuery("SELECT * FROM INSTITUTIONS " +
                "WHERE (CONVERT(NAME, 'US7ASCII') = CONVERT(:nameWithoutBrackets, 'US7ASCII') " +
                "    OR CONVERT(NAME, 'US7ASCII') LIKE CONVERT(:nameWithBrackets, 'US7ASCII')) " +
                "AND COUNTRY_CODE = :countryCode")
                .addEntity(Institution.class)
                .setParameter("nameWithoutBrackets", institutionNameWithoutBrackets(name))
                .setParameter("nameWithBrackets", institutionNameWithoutBrackets(name) + " (%)")
                .setParameter("countryCode", country.getCode());

        return (Institution) query.uniqueResult();
    }

    private String institutionNameWithoutBrackets(String name) {
        Matcher matcher = INSTITUTION_NAME_WITHOUT_BRACKETS_PATTERN.matcher(name);

        if (!matcher.find()) {
            return name;
        } else {
            return matcher.group(1);
        }
    }

    @Transactional(readOnly = true)
    public Institution findByNameEnAndCountryOrNull(String nameEn, Country country) {
        return institutionRepository.findByNameEnAndCountry(nameEn, country);
    }

    @Transactional(readOnly = true)
    public List<Institution> findWithoutAdmins() {
        return institutionRepository.findWithoutAdmins();
    }

    @Transactional
    public void delete(Institution institution) {
        institutionRepository.delete(institution);
    }

    @Transactional(readOnly = true)
    public DataSet<Institution> findInstitutionsNotVirtualWithDatatablesCriterias(DatatablesCriterias criterias) {
        List<Institution> institutions =
                institutionDatatablesJpaRepository.findInstitutionsNotVirtualWithDatatablesCriterias(criterias);
        Long count = institutionDatatablesJpaRepository.getTotalNotVirtualCount();
        Long countFiltered = institutionDatatablesJpaRepository.getFilteredNotVirtualCount(criterias);

        return new DataSet<>(institutions, count, countFiltered);
    }

    public long count() {
        return institutionRepository.count();
    }

    /**
     * @param instId  requested institution's id
     * @param admin   admin that is requesting
     * @param virtual is requested institution virtual
     * @return requested institution
     * @throws InstitutionNotSelectedBySuperAdminException if superadmin is requesting but didn't specify institution
     */
    public Institution getRequestedInstitution(Long instId, User admin, boolean virtual)
            throws InstitutionNotSelectedBySuperAdminException {
        Institution institution;

        if (admin.isSuperAdmin()) {
            if (instId == null) {
                throw new InstitutionNotSelectedBySuperAdminException(virtual);
            } else {
                institution = findById(instId);
            }
        } else {
            institution = admin.getInstitution();
        }

        if (!findActive(virtual).contains(institution)) {
            throw new RuntimeException("Wrong institution: " + institution);
        }

        return institution;
    }

    /**
     * @param country a country for which to get the institutions
     * @return list of institutions that should be visible to the national agency of country
     */
    public List<Institution> getInstitutionsForCountry(Country country) {
        if (country.getCode().equals(BELGIUM_COUNTRY_CODE)) {
            // Belgium should be limited to Flemish institutions (see EQUAT-173)
            return institutionRepository.findByErasmusCodeIn(FLEMISH_INSTITUTIONS_ERASMUS_CODES);
        } else {
            return institutionRepository.findByCountryAndVirtualFalse(country);
        }
    }

    public List<Institution> findByUrlSimilar(URL url) {
        String host = url.getHost();
        String domain = host.startsWith("www.") ? host.substring(4) : host;

        Session session = getSession();
        Query query = session.createSQLQuery("SELECT * FROM INSTITUTIONS " +
                "WHERE URL LIKE :httpUrlPattern OR URL LIKE :httpsUrlPattern " +
                "OR URL LIKE :httpWwwUrlPattern OR URL LIKE :httpsWwwUrlPattern")
                .addEntity(Institution.class)
                .setParameter("httpUrlPattern", "http://" + domain + "%")
                .setParameter("httpsUrlPattern", "https://" + domain + "%")
                .setParameter("httpWwwUrlPattern", "http://www." + domain + "%")
                .setParameter("httpsWwwUrlPattern", "https://www." + domain + "%");

        return query.list();
    }

    private Session getSession() {
        Session session = entityManager.unwrap(Session.class);
        if (session == null) {
            throw new RuntimeException("Could not obtain Session");
        }
        return session;
    }
}

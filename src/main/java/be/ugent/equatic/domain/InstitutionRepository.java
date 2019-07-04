package be.ugent.equatic.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    @SuppressWarnings("JpaQlInspection")
    String WITH_ADMIN_FROM_WHERE =
            "from Institution i join i.users u join u.adminRoles r where r.role LIKE 'ROLE_ADMIN_%'";
    /**
     * Get institutions that have an administrator.
     */
    String WITH_ADMIN = "select distinct i " + WITH_ADMIN_FROM_WHERE;

    List<Institution> findByVirtualFalseOrderByNameAsc();

    @Query(WITH_ADMIN)
    List<Institution> findWithAdmin();

    @Query(WITH_ADMIN + " and i.virtual = ?1")
    List<Institution> findWithAdmin(boolean virtual);

    @Query(WITH_ADMIN + " and i.idpEntityId is not null")
    List<Institution> findByIdpEntityIdIsNotNullAndWithAdmin();

    @Query("select i from Institution i where i.id not in "
            + "(select distinct iwl.id from Institution iwl join iwl.users u join u.adminRoles r "
            + "where r.role = 'ROLE_ADMIN_INSTITUTIONAL' or r.role = 'ROLE_ADMIN_NATIONAL')")
    List<Institution> findWithoutAdmins();

    Institution findByIdpEntityId(String idpEntityId);

    Institution findById(Long institutionId);

    Institution findByPic(String pic);

    Institution findByErasmusCodeIn(String erasmusCode);

    List<Institution> findByErasmusCodeIn(String[] erasmusCodes);

    Institution findByNameAndCountry(String name, Country country);

    Institution findByNameEnAndCountry(String nameEn, Country country);

    List<Institution> findByCountryAndVirtualFalse(Country country);
}

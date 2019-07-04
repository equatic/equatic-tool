package be.ugent.equatic.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findById(long id);

    User findByToken(String token);

    User findByUsernameAndInstitution(String username, Institution institution);

    User findByUsernameIgnoreCaseAndInstitution(String username, Institution institution);

    User findByEmailIgnoreCaseAndInstitution(String email, Institution institution);

    @Query("select u from User u join u.institution i join u.adminRoles r where i.id = ?1 and r.role = ?2")
    List<User> findByInstitutionIdAndAuthority(long id, Authority authority);

    @Query("select u from User u join u.adminRoles r where r.role = ?1")
    List<User> findByAuthority(Authority adminRole);

    @Query("select u from User u join u.adminRoles r where r.role IN ?1")
    List<User> findByAuthorityIn(List<Authority> adminRoles);

    List<User> findUsersByInstitution(Institution institution);

    void deleteByInstitution(Institution institution);
}
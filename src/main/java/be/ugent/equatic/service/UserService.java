package be.ugent.equatic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import be.ugent.equatic.domain.Authority;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.User;
import be.ugent.equatic.domain.UserRepository;
import be.ugent.equatic.exception.UserNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public User findById(long id) throws UserNotFoundException {
        User user = userRepository.findById(id);

        if (user == null) {
            throw new UserNotFoundException(id);
        }

        return user;
    }

    @Transactional(readOnly = true)
    public User findByToken(String token) throws UserNotFoundException {
        User user = userRepository.findByToken(token);

        if (user == null) {
            throw new UserNotFoundException(token);
        }

        return user;
    }

    @Transactional(readOnly = true)
    public User findByUsernameAndInstitution(String username, Institution institution) {
        User found = userRepository.findByUsernameAndInstitution(username, institution);

        if (found == null) {
            throw new UserNotFoundException(username, institution);
        }

        return found;
    }

    @Transactional(readOnly = true)
    public User findByUsernameIgnoreCaseAndInstitution(String username, Institution institution) throws UserNotFoundException {
        User found = userRepository.findByUsernameIgnoreCaseAndInstitution(username, institution);

        if (found == null) {
            throw new UserNotFoundException(username, institution);
        }

        return found;
    }

    @Transactional(readOnly = true)
    public User findByEmailIgnoreCaseAndInstitution(String email, Institution institution) {
        User found = userRepository.findByEmailIgnoreCaseAndInstitution(email, institution);

        if (found == null) {
            throw UserNotFoundException.byEmailAndInstitution(email, institution);
        }

        return found;
    }

    /**
     * Saves users and encodes their passwords.
     *
     * @param users the array of users to save
     */
    @Transactional
    public void save(User... users) {
        userRepository.save(Arrays.asList(users));
    }

    @Transactional
    public void deleteAll() {
        userRepository.deleteAll();
    }

    @Transactional(readOnly = true)
    public List<User> findInstitutionalAdmins(Institution institution) {
        return userRepository.findByInstitutionIdAndAuthority(institution.getId(), Authority.ROLE_ADMIN_INSTITUTIONAL);
    }

    public List<String> getInstitutionalAdminNames(Institution institution) {
        return findInstitutionalAdmins(institution).stream()
                .map(User::getDisplayName).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<User> findAdmins() {
        return userRepository.findByAuthorityIn(
                Arrays.asList(Authority.ROLE_ADMIN_INSTITUTIONAL, Authority.ROLE_ADMIN_NATIONAL));
    }

    @Transactional(readOnly = true)
    public List<User> findSuperAdmins() {
        return userRepository.findByAuthority(Authority.ROLE_ADMIN_SUPER);
    }

    @Transactional(readOnly = true)
    public List<User> findUsersByInstitution(Institution institution) {
        return userRepository.findUsersByInstitution(institution);
    }

    @Transactional
    public void deleteByInstitution(Institution institution) {
        userRepository.deleteByInstitution(institution);
    }

    @Transactional
    public void delete(User user) {
        userRepository.delete(user);
    }

    public boolean hasBeenCreatedBySuperAdmin(User user) {
        List<Authority> authorities = user.getAuthorities();

        if (authorities.contains(Authority.ROLE_ADMIN_INSTITUTIONAL)) {
            return findInstitutionalAdmins(user.getInstitution()).size() == 1;
        } else {
            return authorities.contains(Authority.ROLE_ADMIN_NATIONAL);
        }
    }

    @Transactional
    public void edit(User user) {
        if (user.isEmailChanged()) {
            user.generateToken();
            user.setEmailConfirmed(false);
            user.setActivated(false);
        }

        save(user);
    }
}

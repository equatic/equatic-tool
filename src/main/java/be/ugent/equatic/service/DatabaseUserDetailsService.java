package be.ugent.equatic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.exception.UserNotFoundException;
import be.ugent.equatic.security.DatabaseUserDetails;

/**
 * Loads user details from database by username and institution ID.
 */
@Service
public class DatabaseUserDetailsService {

    @Autowired
    private UserService userService;

    public UserDetails loadUserByUsernameAndInstitution(String username, Institution institution)
            throws UserNotFoundException {
        return new DatabaseUserDetails(userService.findByUsernameIgnoreCaseAndInstitution(username, institution));
    }
}
package be.ugent.equatic.util;

import be.ugent.equatic.domain.Authority;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.Role;
import be.ugent.equatic.domain.User;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UserUtil {

    public static User getUser(String username, Institution institution, Authority... adminAuthorities) {
        User user = new User(username, institution, "firstname", "lastname",
                username.replace("@", "") + "@institution.com");
        user.generateToken();
        return addAdminAuthorities(user, Arrays.asList(adminAuthorities));
    }

    private static User addAdminAuthorities(User user, List<Authority> authorities) {
        List<Role> roles = authorities.stream().map(authority -> new Role(authority, user))
                .collect(Collectors.toList());
        user.setAdminRoles(roles);
        return user;
    }
}

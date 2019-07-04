package be.ugent.equatic.web;

import org.junit.Test;
import be.ugent.equatic.core.MockMvcTest;
import be.ugent.equatic.security.DatabaseUserDetails;
import be.ugent.equatic.web.admin.UserManagementController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthorizationTest extends MockMvcTest {

    @Test
    public void userCannotAccessAdminPages() throws Exception {
        mockMvc.perform(get(UserManagementController.VIEW_USERS_LIST, UserManagementController.AdminType.institutional)
                .with(user(new DatabaseUserDetails(ghentUser))))
                .andExpect(status().isForbidden());
    }
}

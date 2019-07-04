package be.ugent.equatic.web;

import org.junit.Ignore;
import org.junit.Test;
import be.ugent.equatic.core.MockMvcTest;
import be.ugent.equatic.web.pages.account.SignInPage;
import be.ugent.equatic.web.pages.institutionaladmin.UsersPage;
import be.ugent.equatic.web.pages.institutionaluser.InstitutionReport;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HomeControllerTest extends MockMvcTest {

    @Test
    @Ignore // HtmlUnit can't parse new JS constructs used in NumberGauge
    public void userIsRedirectedToUserPage() throws Exception {
        InstitutionReport institutionReport = SignInPage.signInUser(ghentUser, driver, InstitutionReport.class);

        assertThat(institutionReport.getNavBarUsername().getText(), is(ghentUser.getUsername()));
    }

    @Test
    public void institutionalAdminIsRedirectedToUsersPage() throws Exception {
        SignInPage.signInUser(ghentAdmin, driver, UsersPage.class);
    }
}

package be.ugent.equatic.core;

import org.junit.Before;
import be.ugent.equatic.domain.*;
import be.ugent.equatic.util.UserUtil;

public class FixtureTest {

    Country greatBritain;
    protected Country belgium;
    protected Country poland;
    Country austria;

    protected Institution equatic;
    protected Institution okta;
    protected Institution ghentUniversity;
    protected Institution warsawUniversity;
    protected Institution grazUniversity;
    protected Institution polandNationalAgency;

    protected User superAdmin;

    protected User ghentAdmin;
    protected User ghentAdmin2;
    protected User warsawAdmin;
    protected User oktaAdmin;

    protected User ghentUser;
    protected User warsawUser;

    protected AcademicYear currentAcademicYear;

    @Before
    public void setUpCountries() {
        greatBritain = new Country("GB", "United Kingdom of Great Britain and Northern Ireland", "UK");
        belgium = new Country("BE", "Belgium");
        poland = new Country("PL", "Poland");
        austria = new Country("AT", "Austria");
    }

    @Before
    public void setUpInstitutions() {
        equatic = new Institution(null, null, "eQuATIC", "eQuATIC", null,
                belgium);
        equatic.setVirtual(true);

        okta = new Institution(null, null, "Okta", null, "http://www.okta.com/", greatBritain);
        okta.setIdpEntityId("https://ideq.ugent.be/simplesaml/saml2/idp/metadata.php");
        okta.setIdpMetadataUrl("https://ideq.ugent.be/simplesaml/saml2/idp/metadata.php");

        ghentUniversity = new Institution("999986096", "B  GENT01", "Universiteit Gent", "Ghent University", null,
                belgium);

        warsawUniversity = new Institution("999572294", "PL WARSZAW01", "Uniwersytet Warszawski",
                "University of Warsaw", null, poland);

        grazUniversity = new Institution("999873188", "A  GRAZ01", "Karl-Franzens-Universität Graz",
                "University of Graz", null, austria);

        polandNationalAgency = new Institution(null, null, poland.getName(), null, null, poland);
        polandNationalAgency.setVirtual(true);
    }

    @Before
    public void setUpUsers() {
        superAdmin = UserUtil.getUser("superadmin", equatic, Authority.ROLE_ADMIN_SUPER);
        superAdmin.setEmailConfirmed(true);
        superAdmin.setActivated(true);

        ghentAdmin = UserUtil.getUser("ghentAdmin", ghentUniversity, Authority.ROLE_ADMIN_INSTITUTIONAL);
        ghentAdmin.setEmailConfirmed(true);
        ghentAdmin.setActivated(true);

        ghentAdmin2 = UserUtil.getUser("ghentAdmin2", ghentUniversity, Authority.ROLE_ADMIN_INSTITUTIONAL);
        ghentAdmin2.setEmailConfirmed(true);

        warsawAdmin = UserUtil.getUser("warsawAdmin", warsawUniversity, Authority.ROLE_ADMIN_INSTITUTIONAL);

        oktaAdmin = UserUtil.getUser("oktaAdmin", okta, Authority.ROLE_ADMIN_INSTITUTIONAL);
        oktaAdmin.setEmailConfirmed(true);

        ghentUser = UserUtil.getUser("ghentUser", ghentUniversity);
        ghentUser.setEmailConfirmed(true);
        ghentUser.setActivated(true);

        warsawUser = UserUtil.getUser("warsawUser", warsawUniversity);
        warsawUser.setEmailConfirmed(true);
        warsawUser.setActivated(true);
    }

    @Before
    public void setUpAcademicYears() {
        currentAcademicYear = new AcademicYear();
        /*
         This was not set in the constructor for a purpose! If I add a constructor that takes a String then Spring
         converts the AcademicYear class to a String and then to null when resolving @ModelAttribute AcademicYear
          */
        currentAcademicYear.setAcademicYear("2015-2016");
    }
}

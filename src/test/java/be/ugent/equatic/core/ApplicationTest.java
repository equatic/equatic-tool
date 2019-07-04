package be.ugent.equatic.core;

import be.ugent.equatic.service.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import be.ugent.equatic.Application;
import be.ugent.equatic.config.MailProperties;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
abstract public class ApplicationTest extends FixtureTest {

    protected static final String DEFAULT_PASSWORD = "password";
    protected static final int MAIL_TEST_PORT = 3025;
    protected static final String MAIL_TEST_HOST = "localhost";

    @Autowired
    protected CountryService countryService;

    @Autowired
    protected InstitutionService institutionService;

    @Autowired
    protected UserService userService;

    @Autowired
    protected RoleService roleService;

    @Autowired
    protected DataSheetUploadService dataSheetUploadService;

    @Autowired
    protected DataSheetRowService dataSheetRowService;

    @Autowired
    protected AcademicYearService academicYearService;

    @Autowired
    protected MessageSource messageSource;

    @Autowired
    protected MailProperties mailProperties;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Before
    public void setUpData() throws MetadataProviderException {
        dataSheetRowService.deleteAll();
        dataSheetUploadService.deleteAll();
        roleService.deleteAll();
        userService.deleteAll();
        institutionService.deleteAll();
        countryService.deleteAll();
        academicYearService.deleteAll();

        countryService.save(greatBritain, belgium, poland, austria);

        institutionService.save(equatic, okta, ghentUniversity, warsawUniversity, grazUniversity, polandNationalAgency);

        // Set default passwords for: superAdmin, ghentAdmin and ghentUser
        superAdmin.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        ghentAdmin.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        ghentUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));

        userService.save(ghentAdmin, ghentAdmin2, ghentUser, warsawUser, superAdmin, oktaAdmin, warsawAdmin);

        academicYearService.save(currentAcademicYear);
    }

    protected String getMessage(String messageCode) {
        return messageSource.getMessage(messageCode, null, LocaleContextHolder.getLocale());
    }

    protected String getMessage(String messageCode, Object[] params) {
        return messageSource.getMessage(messageCode, params, LocaleContextHolder.getLocale());
    }

    protected List<String> getMessages(String... messageCodes) {
        return Arrays.stream(messageCodes).map(this::getMessage).collect(Collectors.toList());
    }
}

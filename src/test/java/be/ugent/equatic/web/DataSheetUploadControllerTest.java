package be.ugent.equatic.web;

import be.ugent.equatic.domain.*;
import com.google.common.base.Joiner;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import be.ugent.equatic.core.MockMvcTest;
import be.ugent.equatic.datasheet.DataSheetUtils;
import be.ugent.equatic.security.DatabaseUserDetails;
import be.ugent.equatic.service.DataSheetService;
import be.ugent.equatic.service.DataSheetValueService;
import be.ugent.equatic.service.IscedService;
import be.ugent.equatic.web.admin.institutional.DataSheetUploadController;
import be.ugent.equatic.web.util.FoundInstitutionBy;
import be.ugent.equatic.web.util.Message;
import be.ugent.equatic.web.util.SearchedInstitution;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

public class DataSheetUploadControllerTest extends MockMvcTest {

    private static final int ROW_VALUES_START_INDEX = 6;
    private static final int ROW_VALUES_COUNT = 7;
    private static final String ICT_ISCED = "0610";

    @Autowired
    private DataSheetService dataSheetService;

    @Autowired
    private DataSheetValueService dataSheetValueService;

    @Autowired
    private IscedService iscedService;

    @Autowired
    private JavaMailSenderImpl mailSender;

    private GreenMail testSmtp;

    private DataSheet smpDataSheet;

    private List<Object> smpColumns;

    private Isced ictIsced;

    @Before
    public void setUpMail() {
        testSmtp = new GreenMail(ServerSetupTest.SMTP);
        testSmtp.start();

        mailSender.setPort(MAIL_TEST_PORT);
        mailSender.setHost(MAIL_TEST_HOST);
    }

    @After
    public void cleanup() {
        testSmtp.stop();
    }

    @Before
    public void setUpDataSheetUploadControllerTest() throws Exception {
        smpDataSheet = dataSheetService.findByCode(DataSheetCode.SMP);
        smpColumns = new ArrayList<>(Arrays.asList(
                DataSheetColumnCode.PIC,
                DataSheetColumnCode.ERASMUS_CODE,
                DataSheetColumnCode.LEGAL_NAME,
                DataSheetColumnCode.COUNTRY_CODE,
                DataSheetColumnCode.URL,
                DataSheetColumnCode.ISCED_CODE,
                DataSheetColumnCode.SMP_DEAL_QUESTIONS,
                DataSheetColumnCode.SMP_FIND_ACCOMMODATION,
                DataSheetColumnCode.SMP_QUALITY,
                DataSheetColumnCode.SMP_LA_SIGNED,
                DataSheetColumnCode.SMP_LA_NOT_SIGNED,
                DataSheetColumnCode.SMP_CERTIFICATE,
                DataSheetColumnCode.SMP_SUPPORT
        ));

        ictIsced = iscedService.findByCode(ICT_ISCED);
    }

    @Test
    public void columnNotPresentInDataSheet() throws Exception {
        List<Object[]> dataSheetRows = new ArrayList<>();

        DataSheetColumnCode removedColumnCode = DataSheetColumnCode.SMP_DEAL_QUESTIONS;
        smpColumns.remove(removedColumnCode);
        smpColumns.remove(DataSheetColumnCode.URL); // URL is optional and can be removed

        dataSheetRows.add(smpColumns.toArray());

        String errorMessage = getMessage("equatic.dataSheet.invalidFormat.columnNotExists",
                new String[]{smpDataSheet.getColumnByCode(removedColumnCode).getTitle()});

        performUpload(
                DataSheetUtils.getMockMultipartFile(dataSheetRows, smpColumns, smpDataSheet, dataSheetValueService))
                .andExpect(flash().attribute("message", is(Message.danger(errorMessage))));
    }

    @Test
    public void smpUpload() throws Exception {
        List<Object[]> dataSheetRows = new ArrayList<>();

        smpColumns.add("Additional column");
        Object[] warsawStudentRow = {
                warsawUniversity.getPic(),
                warsawUniversity.getErasmusCode(),
                warsawUniversity.getNameEn(),
                warsawUniversity.getCountryCode(),
                warsawUniversity.getUrl(),
                ICT_ISCED,
                DataSheetValueCode.SATISFIED_RATHER_DISSATISFIED,
                DataSheetValueCode.SATISFIED_VERY_DISSATISFIED,
                DataSheetValueCode.SATISFIED_NEITHER,
                DataSheetValueCode.LA_SIGNED_NO,
                DataSheetValueCode.LA_NOT_SIGNED_SMP_RECEIVING,
                DataSheetValueCode.YES,
                DataSheetValueCode.SATISFIED_NOT_APPLICABLE,
                "Additional answer"
        };
        Object[] grazStudentRow = {
                grazUniversity.getPic(),
                grazUniversity.getErasmusCode(),
                grazUniversity.getName(),
                grazUniversity.getCountryCode(),
                grazUniversity.getUrl(),
                ICT_ISCED,
                DataSheetValueCode.SATISFIED_NOT_APPLICABLE
        };
        Object[] institutionsNotMatching = {
                warsawUniversity.getPic(),
                grazUniversity.getErasmusCode(),
                null,
                null,
                null,
                ICT_ISCED,
        };
        String picNotFound = "123";
        String erasmusCodeNotFound = "NOT FOUND";
        String legalNameNotFound = "Not found";
        Object[] institutionNotFound = {
                picNotFound,
                erasmusCodeNotFound,
                legalNameNotFound,
                poland.getCode(),
                null,
                ICT_ISCED
        };
        SearchedInstitution searchedInstitutionNotFound = new SearchedInstitution(picNotFound, erasmusCodeNotFound,
                legalNameNotFound, poland.getCode());
        String noSuchValue = "No such value";
        Object[] valueNotFound = {
                warsawUniversity.getPic(),
                warsawUniversity.getErasmusCode(),
                warsawUniversity.getNameEn(),
                warsawUniversity.getCountryCode(),
                warsawUniversity.getUrl(),
                ICT_ISCED,
                noSuchValue
        };
        Object[] onlyLegalNameAndCountrySet = {
                null,
                null,
                warsawUniversity.getName(),
                warsawUniversity.getCountryCode(),
                null,
                ICT_ISCED,
                DataSheetValueCode.SATISFIED_NEITHER
        };
        Object[] onlyUrlSet = {
                null,
                null,
                null,
                null,
                okta.getUrl(),
                ICT_ISCED,
                DataSheetValueCode.SATISFIED_NEITHER
        };
        String wrongCountryCode = "WRONG";
        Object[] countryCodeNotFound = {
                null,
                null,
                warsawUniversity.getName(),
                wrongCountryCode,
                null,
                ICT_ISCED,
                DataSheetValueCode.SATISFIED_NEITHER
        };
        SearchedInstitution searchedInstitutionCountryNotFound = new SearchedInstitution(null, null,
                warsawUniversity.getName(), wrongCountryCode);
        String wrongIsced = "WRONG";
        Object[] iscedNotFound = {
                null,
                null,
                warsawUniversity.getName(),
                warsawUniversity.getCountryCode(),
                null,
                wrongIsced,
                DataSheetValueCode.SATISFIED_NEITHER
        };
        String malformedUrl = "htt://url.malformed.com";
        Object[] urlMalformed = {
                null,
                null,
                null,
                null,
                malformedUrl,
                ICT_ISCED,
                DataSheetValueCode.SATISFIED_NEITHER
        };

        dataSheetRows.addAll(Arrays.asList(
                smpColumns.toArray(), warsawStudentRow, grazStudentRow, institutionsNotMatching,
                institutionNotFound, institutionNotFound, valueNotFound, onlyLegalNameAndCountrySet,
                countryCodeNotFound, iscedNotFound, onlyUrlSet, urlMalformed));

        String validDataSheetRowsCount = "4"; // Warsaw student + Graz student + onlyLegalNameAndCountrySet + onlyUrlSet
        String foundInstitutionsCount = "3"; // Warsaw University + Graz University + Okta
        String invalidDataSheetRowsCount =
                "7"; // institutionsNotMatching + valueNotFound + notFoundInstitutions (x3) + iscedNotFound + urlMalformed
        String notFoundInstitutionsCount = "2"; // institutionNotFound + countryCodeNotFound

        String successMessage = getMessage("DataSheetUploadController.upload.success",
                new String[]{validDataSheetRowsCount, foundInstitutionsCount});
        String errorMessage = getMessage("DataSheetUploadController.upload.errors",
                new String[]{invalidDataSheetRowsCount, notFoundInstitutionsCount});

        DataSheetColumn smpColumn = smpDataSheet.getColumnByCode(
                (DataSheetColumnCode) smpColumns.get(ROW_VALUES_START_INDEX));
        List<String> validValues = dataSheetValueService.findByColumn(smpColumn).stream()
                .map(DataSheetValue::getValue).collect(Collectors.toList());

        DataSheetColumn countryColumn = smpDataSheet.getColumnByCode(DataSheetColumnCode.COUNTRY_CODE);
        DataSheetColumn iscedColumn = smpDataSheet.getColumnByCode(DataSheetColumnCode.ISCED_CODE);
        DataSheetColumn urlColumn = smpDataSheet.getColumnByCode(DataSheetColumnCode.URL);

        DataSheetUpload upload = new DataSheetUpload(ghentAdmin, ghentUniversity, smpDataSheet);

        String institutionNotFoundDetails = searchedInstitutionNotFound.getDetails(messageSource);
        String institutionNotFoundMessage = getMessage("DataSheetUploadController.upload.institutionNotFound",
                new String[]{institutionNotFoundDetails});

        DataSheetUploadError[] uploadErrors = new DataSheetUploadError[]{
                new DataSheetUploadError(upload, null, 4, null,
                        getMessage("equatic.upload.foundInstitutionsNotMatch",
                                new String[]{FoundInstitutionBy.PIC.toString(),
                                        FoundInstitutionBy.ERASMUS_CODE.toString()})),
                new DataSheetUploadError(upload, null, 5, null, institutionNotFoundMessage),
                new DataSheetUploadError(upload, null, 6, null, institutionNotFoundMessage),
                new DataSheetUploadError(upload, smpColumn, 7, noSuchValue,
                        getMessage("DataSheetUploadController.upload.valueNotFound",
                                new String[]{noSuchValue, smpColumn.getTitle(), Joiner.on(", ").join(validValues)})),
                new DataSheetUploadError(upload, countryColumn, 9, wrongCountryCode,
                        getMessage("equatic.upload.countryCodeNotFound", new String[]{wrongCountryCode})),
                new DataSheetUploadError(upload, iscedColumn, 10, wrongIsced,
                        getMessage("equatic.upload.iscedNotFound", new String[]{wrongIsced})),
                new DataSheetUploadError(upload, urlColumn, 12, malformedUrl,
                        getMessage("equatic.upload.urlMalformed", new String[]{malformedUrl}))
        };

        String superAdminNotified = getMessage("DataSheetUploadController.upload.superAdminNotified");
        performUpload(
                DataSheetUtils.getMockMultipartFile(dataSheetRows, smpColumns, smpDataSheet, dataSheetValueService))
                .andExpect(flash().attribute("upload", hasProperty("errors", containsInAnyOrder(uploadErrors))))
                .andExpect(flash().attribute("message",
                        is(Message.warning(successMessage + "\n" + errorMessage + "\n" + superAdminNotified))));

        DataSheetRow warsawStudent = new DataSheetRow(currentAcademicYear, ghentUniversity, smpDataSheet,
                warsawUniversity, ictIsced);
        addRowValues(warsawStudent, smpColumns, warsawStudentRow);
        DataSheetRow grazStudent = new DataSheetRow(currentAcademicYear, ghentUniversity, smpDataSheet,
                grazUniversity, ictIsced);
        addRowValues(grazStudent, smpColumns, grazStudentRow);
        DataSheetRow onlyLegalNameAndCountrySetRow = new DataSheetRow(currentAcademicYear, ghentUniversity,
                smpDataSheet, warsawUniversity, ictIsced);
        addRowValues(onlyLegalNameAndCountrySetRow, smpColumns, onlyLegalNameAndCountrySet);
        DataSheetRow onlyUrlSetRow = new DataSheetRow(currentAcademicYear, ghentUniversity, smpDataSheet, okta,
                ictIsced);
        addRowValues(onlyUrlSetRow, smpColumns, onlyUrlSet);

        List<DataSheetRow> rowsFromDatabase = dataSheetRowService.findAll();
        assertThat(rowsFromDatabase,
                containsInAnyOrder(warsawStudent, grazStudent, onlyLegalNameAndCountrySetRow, onlyUrlSetRow));

        javax.mail.Message[] messages = testSmtp.getReceivedMessages();
        assertThat(messages.length, is(1));
        javax.mail.Message message = messages[0];

        assertThat(message.getRecipients(javax.mail.Message.RecipientType.TO),
                is(new InternetAddress[]{new InternetAddress(superAdmin.getEmail())}));
        assertThat(message.getSubject(), is(getMessage("NotificationService.institutionNotFound.subject",
                new String[]{mailProperties.getSubjectPrefix()})));

        Multipart multipart = (Multipart) message.getContent();
        assertThat(multipart.getCount(), is(2));

        MimeMultipart messageBodyPart = (MimeMultipart) multipart.getBodyPart(0).getContent();
        StringWriter stringWriter = new StringWriter();
        InputStream inputStream = messageBodyPart.getBodyPart(0).getInputStream();
        IOUtils.copy(inputStream, stringWriter);
        String messageBody = stringWriter.toString().replaceAll("\r", "");

        String dataSheetName = getMessage("equatic.admin.uploadData." + DataSheetCode.SMP.name());
        assertThat(messageBody, is(getMessage("NotificationService.institutionNotFound.body",
                new String[]{dataSheetName, ghentUniversity.getDisplayName(), ghentAdmin.getDisplayName(),
                        ghentAdmin.getEmail()})));

        BodyPart institutionsListPart = multipart.getBodyPart(1);
        assertThat(institutionsListPart.getDisposition(), is(Part.ATTACHMENT));
    }

    @Test
    public void casesInColumnTitlesAndValuesShouldBeIgnored() throws Exception {
        DataSheetColumnCode columnCode = (DataSheetColumnCode) smpColumns.get(ROW_VALUES_START_INDEX);
        DataSheetColumn column = smpDataSheet.getColumnByCode(columnCode);
        smpColumns.remove(ROW_VALUES_START_INDEX);
        smpColumns.add(ROW_VALUES_START_INDEX, column.getTitle().toLowerCase());

        DataSheetValueCode valueCode = DataSheetValueCode.SATISFIED_NEITHER;
        DataSheetValue satisfiedNeither = dataSheetValueService.findByCode(valueCode);

        Object[] warsawStudentRow = {
                warsawUniversity.getPic(),
                warsawUniversity.getErasmusCode(),
                warsawUniversity.getNameEn(),
                warsawUniversity.getCountryCode(),
                warsawUniversity.getUrl(),
                ICT_ISCED,
                satisfiedNeither.getValue().toLowerCase()
        };

        List<Object[]> dataSheetRows = new ArrayList<>();
        dataSheetRows.addAll(Arrays.asList(smpColumns.toArray(), warsawStudentRow));

        String successMessage = getMessage("DataSheetUploadController.upload.success", new String[]{"1", "1"});

        performUpload(
                DataSheetUtils.getMockMultipartFile(dataSheetRows, smpColumns, smpDataSheet, dataSheetValueService))
                .andExpect(flash().attribute("message", is(Message.success(successMessage))));

        DataSheetRow warsawStudent = new DataSheetRow(currentAcademicYear, ghentUniversity, smpDataSheet,
                warsawUniversity, ictIsced);
        warsawStudent.addRowValue(new DataSheetRowValue(warsawStudent, columnCode, valueCode));
        DataSheetUtils.addRowValues(warsawStudent, smpColumns, warsawStudentRow, ROW_VALUES_START_INDEX + 1,
                ROW_VALUES_COUNT - 1);

        List<DataSheetRow> rowsFromDatabase = dataSheetRowService.findAll();
        assertThat(rowsFromDatabase, containsInAnyOrder(warsawStudent));
    }

    @Test
    public void specialCharactersInInstitutionNamesShouldBeIgnored() throws Exception {
        checkIfInstitutionUploadSuccessful("Karl-Franzens-Universitat Graz");
    }

    @Test
    public void bracketsEndingInstitutionNamesShouldBeIgnored() throws Exception {
        checkIfInstitutionUploadSuccessful("Karl-Franzens-Universität Graz (KFUG)");
    }

    private void checkIfInstitutionUploadSuccessful(String institutionName) throws Exception {
        Object[] grazStudentRow = {
                null,
                null,
                institutionName,
                grazUniversity.getCountryCode(),
                null,
                ICT_ISCED,
                DataSheetValueCode.SATISFIED_NEITHER
        };

        List<Object[]> dataSheetRows = new ArrayList<>();
        dataSheetRows.addAll(Arrays.asList(smpColumns.toArray(), grazStudentRow));

        String successMessage = getMessage("DataSheetUploadController.upload.success", new String[]{"1", "1"});

        performUpload(
                DataSheetUtils.getMockMultipartFile(dataSheetRows, smpColumns, smpDataSheet, dataSheetValueService))
                .andExpect(flash().attribute("message", is(Message.success(successMessage))));

        DataSheetRow grazStudent = new DataSheetRow(currentAcademicYear, ghentUniversity, smpDataSheet,
                grazUniversity, ictIsced);
        DataSheetUtils.addRowValues(grazStudent, smpColumns, grazStudentRow, ROW_VALUES_START_INDEX,
                ROW_VALUES_COUNT);

        List<DataSheetRow> rowsFromDatabase = dataSheetRowService.findAll();
        assertThat(rowsFromDatabase, containsInAnyOrder(grazStudent));
    }

    private ResultActions performUpload(MockMultipartFile uploadFile) throws Exception {
        return mockMvc.perform(fileUpload(DataSheetUploadController.VIEW_DATA_SHEET_UPLOAD, DataSheetCode.SMP)
                .file(uploadFile).with(user(new DatabaseUserDetails(ghentAdmin))).with(csrf())
                .param("institution", String.valueOf(ghentUniversity.getId()))
                .param("year", currentAcademicYear.getAcademicYear())
                .contentType(MediaType.MULTIPART_FORM_DATA));
    }

    private void addRowValues(DataSheetRow row, List<Object> columns, Object[] dataSheetArrayRow) {
        DataSheetUtils.addRowValues(row, columns, dataSheetArrayRow, ROW_VALUES_START_INDEX, ROW_VALUES_COUNT);
    }
}

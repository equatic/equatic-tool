package be.ugent.equatic.web;

import be.ugent.equatic.domain.*;
import org.apache.poi.ss.usermodel.Sheet;
import org.dbunit.Assertion;
import org.dbunit.dataset.excel.XlsDataSet;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import be.ugent.equatic.core.MockMvcTest;
import be.ugent.equatic.datasheet.DataSheetParsedRow;
import be.ugent.equatic.datasheet.DataSheetParser;
import be.ugent.equatic.datasheet.DataSheetUtils;
import be.ugent.equatic.exception.CountryNotFoundException;
import be.ugent.equatic.security.DatabaseUserDetails;
import be.ugent.equatic.service.DataSheetService;
import be.ugent.equatic.web.admin.superadmin.InstitutionManagementController;
import be.ugent.equatic.web.pages.account.SignInPage;
import be.ugent.equatic.web.pages.superadmin.AdminsPage;
import be.ugent.equatic.web.pages.superadmin.EditInstitutionPage;
import be.ugent.equatic.web.pages.superadmin.InstitutionsPage;
import be.ugent.equatic.web.util.FoundInstitutionBy;
import be.ugent.equatic.web.util.Message;
import be.ugent.equatic.web.util.SearchedInstitution;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

public class InstitutionManagementControllerTest extends MockMvcTest {

    private static final String INSTITUTIONS_NEW_NAME = "new name";

    @Autowired
    private DataSheetService dataSheetService;

    @Test
    public void institutionsListShouldBeComplete() throws Exception {
        AdminsPage adminsPage = SignInPage.signInUser(superAdmin, driver, AdminsPage.class);
        InstitutionsPage institutionsPage = adminsPage.selectActionFromMenu(adminsPage.getSuperAdminMenu(),
                InstitutionManagementController.VIEW_INSTITUTIONS_LIST, InstitutionsPage.class);

        assertThat(institutionsPage.getNameList(),
                containsInAnyOrder(grazUniversity.getName(), okta.getName(), ghentUniversity.getName(),
                        warsawUniversity.getName()));
    }

    @Test
    public void editInstitution() throws Exception {
        AdminsPage adminsPage = SignInPage.signInUser(superAdmin, driver, AdminsPage.class);
        InstitutionsPage institutionsPage = adminsPage.selectActionFromMenu(adminsPage.getSuperAdminMenu(),
                InstitutionManagementController.VIEW_INSTITUTIONS_LIST, InstitutionsPage.class);

        EditInstitutionPage editInstitutionPage = institutionsPage.clickEdit(ghentUniversity);

        editInstitutionPage.getPic().clear();
        editInstitutionPage.getPic().sendKeys("11111111");
        editInstitutionPage = editInstitutionPage.editExpectError();
        assertThat(editInstitutionPage.getErrors(), containsInAnyOrder(getMessage("Institution.pic.Format")));

        editInstitutionPage.getPic().sendKeys("a");
        editInstitutionPage = editInstitutionPage.editExpectError();
        assertThat(editInstitutionPage.getErrors(), containsInAnyOrder(getMessage("Institution.pic.Format")));
        editInstitutionPage.getPic().clear();
        editInstitutionPage.getPic().sendKeys(ghentUniversity.getPic());

        editInstitutionPage.getName().clear();
        editInstitutionPage = editInstitutionPage.editExpectError();
        assertThat(editInstitutionPage.getErrors(), containsInAnyOrder(getMessage("Institution.name.NotEmpty")));
        editInstitutionPage.getName().sendKeys(ghentUniversity.getName());
        editInstitutionPage.getPic().clear();
        editInstitutionPage.getErasmusCode().clear();
        editInstitutionPage.getNameEn().clear();
        editInstitutionPage.getNameEn().sendKeys(INSTITUTIONS_NEW_NAME);

        institutionsPage = editInstitutionPage.editExpectSuccess();
        String editSuccessfulMessage = getMessage("InstitutionManagementController.edit.confirmation",
                new String[]{INSTITUTIONS_NEW_NAME});
        assertThat(institutionsPage.getSuccessMessage().getText(), is(editSuccessfulMessage));

        ghentUniversity = institutionService.findById(ghentUniversity.getId());
        assertThat(ghentUniversity.getPic(), isEmptyOrNullString());
        assertThat(ghentUniversity.getErasmusCode(), isEmptyOrNullString());
        assertThat(ghentUniversity.getNameEn(), is(INSTITUTIONS_NEW_NAME));
    }

    @Test
    public void tryEditIncorrectInstitution() throws Exception {
        AdminsPage adminsPage = SignInPage.signInUser(superAdmin, driver, AdminsPage.class);
        InstitutionsPage institutionsPage = adminsPage.selectActionFromMenu(adminsPage.getSuperAdminMenu(),
                InstitutionManagementController.VIEW_INSTITUTIONS_LIST, InstitutionsPage.class);

        userService.deleteByInstitution(ghentUniversity);
        institutionService.delete(ghentUniversity);
        EditInstitutionPage editInstitutionPage = institutionsPage.clickEdit(ghentUniversity);

        assertThat(editInstitutionPage.getWarningMessage().getText(),
                is(getMessage("equatic.InstitutionNotFoundException.byId",
                        new String[]{ghentUniversity.getId().toString()})));
    }

    @Test
    public void setInstitutionsIdp() throws Exception {
        AdminsPage adminsPage = SignInPage.signInUser(superAdmin, driver, AdminsPage.class);
        InstitutionsPage institutionsPage = adminsPage.selectActionFromMenu(adminsPage.getSuperAdminMenu(),
                InstitutionManagementController.VIEW_INSTITUTIONS_LIST, InstitutionsPage.class);

        EditInstitutionPage editInstitutionPage = institutionsPage.clickEdit(ghentUniversity);

        editInstitutionPage.getIdpEntityId().sendKeys(okta.getIdpEntityId());
        editInstitutionPage = editInstitutionPage.editExpectError();
        assertThat(editInstitutionPage.getErrors(),
                containsInAnyOrder(getMessage("Institution.idpMetadataUrl.NotEmptyIfIdpEntityIdSet")));

        editInstitutionPage.getIdpMetadataUrl().sendKeys("http://www.google.com");
        editInstitutionPage.getIdpEntityId().clear();
        editInstitutionPage = editInstitutionPage.editExpectError();
        List<String> errorMessages = getMessages("Institution.idpMetadataUrl.Correct",
                "Institution.idpEntityId.NotEmptyIfIdpMetadataSet");
        assertThat(editInstitutionPage.getErrors(), containsInAnyOrder(errorMessages.toArray()));

        editInstitutionPage.getIdpEntityId().sendKeys(okta.getIdpEntityId());
        editInstitutionPage.getIdpMetadataUrl().clear();
        editInstitutionPage.getIdpMetadataUrl().sendKeys(okta.getIdpMetadataUrl());
        editInstitutionPage.editExpectSuccess();
    }

    @Test
    public void institutionsXlsExportIsComplete() throws Exception {
        MvcResult result = mockMvc.perform(get(InstitutionManagementController.VIEW_INSTITUTIONS_LIST_EXPORT +
                "?dtt=c&dti=institutionsTable&dtf=xls&dtp=y&dandelionAssetFilterState=false", "xls")
                .with(user(new DatabaseUserDetails(superAdmin))))
                .andReturn();

        ClassLoader classLoader = getClass().getClassLoader();
        XlsDataSet actualXls = new XlsDataSet(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
        XlsDataSet expectedXls = new XlsDataSet(new File(classLoader.getResource("institutions.xls").getFile()));

        Assertion.assertEquals(expectedXls, actualXls);
    }

    @Test
    public void institutionsCorrectlyParsed() throws Exception {
        DataSheet institutionsDataSheet = dataSheetService.findByCode(DataSheetCode.INSTITUTIONS);

        List<String[]> dataSheetRows = new ArrayList<>();
        dataSheetRows.add(institutionsDataSheet.getCurrentColumnTitles());
        List<Object> institutions = Arrays.asList(okta, ghentUniversity, warsawUniversity);
        dataSheetRows.addAll(DataSheetUtils.getRowsForObject(institutions, institutionsDataSheet));
        String[][] dataSheetArray = dataSheetRows.toArray(
                new String[institutions.size()][institutionsDataSheet.getColumnsCount()]);

        List<DataSheetParsedRow> parsedRows = DataSheetParser.parseDataSheet(
                DataSheetUtils.arrayToDataSheet(dataSheetArray), institutionsDataSheet);
        List<Institution> parsedInstitutions = parsedRows.stream().map(row -> {
            try {
                return InstitutionManagementController.getInstitutionFromParsedRowValues(row.getRowValueMap(),
                        countryService);
            } catch (CountryNotFoundException e) {
                return null;
            }
        }).collect(Collectors.toList());

        assertThat(parsedInstitutions, equalTo(institutions));
    }

    @Test
    public void institutionsUpload() throws Exception {
        DataSheet institutionsDataSheet = dataSheetService.findByCode(DataSheetCode.INSTITUTIONS);

        List<String[]> dataSheetRows = new ArrayList<>();

        dataSheetRows.add(institutionsDataSheet.getCurrentColumnTitles());

        String originalPic = grazUniversity.getPic();
        String wrongPic = "123";
        grazUniversity.setPic(wrongPic);
        String originalName = ghentUniversity.getName();
        ghentUniversity.setName(null);
        warsawUniversity.setNameEn("Changed name");
        okta.setNameEn("Changed name");
        Institution conflictingCodes = new Institution(ghentUniversity.getPic(), warsawUniversity.getErasmusCode(),
                "Some name", "Some nameEn", null, belgium);
        Institution conflictingName = new Institution(ghentUniversity.getPic(), ghentUniversity.getErasmusCode(),
                warsawUniversity.getName(), "Some nameEn", null, warsawUniversity.getCountry());
        Institution conflictingNameEn = new Institution(ghentUniversity.getPic(), ghentUniversity.getErasmusCode(),
                "Some name", warsawUniversity.getNameEn(), null, warsawUniversity.getCountry());
        Institution newInstitution = new Institution("222222222", "NEW INST01", "Some name01", "Some nameEn01", null,
                poland);
        Country notFound = new Country("XY", "Not found");
        Institution countryNotFound = new Institution("333333333", "NEW INST02", "Some name02", "Some nameEn02", null,
                notFound);
        Institution duplicatedInstitution = warsawUniversity;
        SearchedInstitution searchedDuplicatedInstitution = new SearchedInstitution(warsawUniversity.getPic(),
                warsawUniversity.getErasmusCode(), warsawUniversity.getName(), warsawUniversity.getCountryCode());

        Institution notUniqueUrl = new Institution("444444444", "NEW INST03", "Some name03", "Some nameEn03", null,
                poland);
        String urlSimilar = "https://okta.com/en";
        notUniqueUrl.setUrl(urlSimilar);

        List<Object> institutions = Arrays.asList(grazUniversity, ghentUniversity, warsawUniversity, okta,
                conflictingCodes, conflictingName, conflictingNameEn, newInstitution, countryNotFound,
                duplicatedInstitution, notUniqueUrl);
        dataSheetRows.addAll(DataSheetUtils.getRowsForObject(institutions, institutionsDataSheet));
        String[][] dataSheetArray = dataSheetRows.toArray(
                new String[institutions.size()][institutionsDataSheet.getColumnsCount()]);

        Sheet sheet = DataSheetUtils.arrayToDataSheet(dataSheetArray);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        sheet.getWorkbook().write(outputStream);
        MockMultipartFile uploadFile = new MockMultipartFile("file", outputStream.toByteArray());

        String updatedItems = "2"; // Warsaw University changed nameEn and URL, Okta changed nameEn
        String addedItems = "1"; // newInstitution
        /*
         * Graz (wrong PIC), Ghent (empty name), conflictingCodes, conflictingName, conflictingNameEn, countryNotFound,
         * duplicatedInstitution, notUniqueUrl
         */
        String invalidItems = "8";
        String successfulUploadMessage = getMessage("equatic.upload.success",
                new String[]{updatedItems, addedItems, invalidItems});

        DataSheetUpload upload = new DataSheetUpload(superAdmin, null, institutionsDataSheet);
        DataSheetUploadError[] uploadErrors = new DataSheetUploadError[]{
                new DataSheetUploadError(upload, institutionsDataSheet.getColumnByCode(DataSheetColumnCode.PIC), 2,
                        wrongPic, getMessage("Institution.pic.Format")),
                new DataSheetUploadError(upload, institutionsDataSheet.getColumnByCode(DataSheetColumnCode.NAME), 3,
                        null, getMessage("Institution.name.NotEmpty")),
                new DataSheetUploadError(upload, null, 6, null,
                        getMessage("equatic.upload.foundInstitutionsNotMatch",
                                new String[]{FoundInstitutionBy.PIC.toString(),
                                        FoundInstitutionBy.ERASMUS_CODE.toString()})),
                new DataSheetUploadError(upload, null, 7, null,
                        getMessage("equatic.upload.foundInstitutionsNotMatch",
                                new String[]{FoundInstitutionBy.PIC.toString(),
                                        FoundInstitutionBy.NAME_AND_COUNTRY_CODE.toString()})),
                new DataSheetUploadError(upload, null, 8, null,
                        getMessage("equatic.upload.foundInstitutionsNotMatch",
                                new String[]{FoundInstitutionBy.PIC.toString(),
                                        FoundInstitutionBy.NAME_EN_AND_COUNTRY_CODE.toString()})),
                new DataSheetUploadError(upload,
                        institutionsDataSheet.getColumnByCode(DataSheetColumnCode.COUNTRY_CODE), 10, notFound.getCode(),
                        getMessage("equatic.upload.countryCodeNotFound", new String[]{notFound.getCode()})),
                new DataSheetUploadError(upload, null, 11, null,
                        getMessage("equatic.upload.institutionDuplicated",
                                new String[]{searchedDuplicatedInstitution.getDetails(messageSource)})),
                new DataSheetUploadError(upload, institutionsDataSheet.getColumnByCode(DataSheetColumnCode.URL), 12,
                        urlSimilar, getMessage("Institution.url.Unique", new String[]{urlSimilar}))
        };

        mockMvc.perform(fileUpload(InstitutionManagementController.VIEW_INSTITUTIONS_LIST)
                .file(uploadFile).with(user(new DatabaseUserDetails(superAdmin))).with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(flash().attribute("message", is(Message.warning(successfulUploadMessage))))
                .andExpect(flash().attribute("upload", hasProperty("errors", containsInAnyOrder(uploadErrors))));

        List<Institution> institutionsFromDatabase = institutionService.findNotVirtual();
        Institution newInstitutionFromDatabase = institutionService.findByPic(newInstitution.getPic());

        grazUniversity.setPic(originalPic);
        ghentUniversity.setName(originalName);
        newInstitution.setId(newInstitutionFromDatabase.getId());
        assertThat(institutionsFromDatabase,
                containsInAnyOrder(okta, ghentUniversity, warsawUniversity, grazUniversity, newInstitution));
    }
}

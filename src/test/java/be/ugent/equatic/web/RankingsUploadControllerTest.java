package be.ugent.equatic.web;

import be.ugent.equatic.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import be.ugent.equatic.core.MockMvcTest;
import be.ugent.equatic.datasheet.DataSheetUtils;
import be.ugent.equatic.security.DatabaseUserDetails;
import be.ugent.equatic.service.DataSheetService;
import be.ugent.equatic.service.DataSheetValueService;
import be.ugent.equatic.web.admin.superadmin.SuperAdminDataSheetUploadController;
import be.ugent.equatic.web.util.Message;
import be.ugent.equatic.web.util.SearchedInstitution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

public class RankingsUploadControllerTest extends MockMvcTest {

    private static final int ROW_VALUES_START_INDEX = 4;
    private static final int ROW_VALUES_COUNT = 3;

    @Autowired
    private DataSheetService dataSheetService;

    @Autowired
    private DataSheetValueService dataSheetValueService;

    private DataSheet rankingsDataSheet;

    private List<Object> rankingsColumns;

    @Before
    public void setUpRankingsUploadControllerTest() throws Exception {
        rankingsDataSheet = dataSheetService.findByCode(DataSheetCode.RANKINGS);
        rankingsColumns = new ArrayList<>(Arrays.asList(
                DataSheetColumnCode.PIC,
                DataSheetColumnCode.ERASMUS_CODE,
                DataSheetColumnCode.LEGAL_NAME,
                DataSheetColumnCode.COUNTRY_CODE,
                DataSheetColumnCode.RANKING_AWRU_POSITION,
                DataSheetColumnCode.RANKING_THE_POSITION,
                DataSheetColumnCode.RANKING_QS_POSITION
        ));
    }

    @Test
    public void rankingsUpload() throws Exception {
        List<Object[]> dataSheetRows = new ArrayList<>();

        Object[] warsawPositionsRow = {
                warsawUniversity.getPic(),
                warsawUniversity.getErasmusCode(),
                warsawUniversity.getNameEn(),
                warsawUniversity.getCountryCode(),
                "101-150",
                "6",
                "4",
        };
        Object[] grazPositionsRow = {
                grazUniversity.getPic(),
                grazUniversity.getErasmusCode(),
                grazUniversity.getName(),
                grazUniversity.getCountryCode(),
                "101-150"
        };
        String picNotFound = "123";
        String erasmusCodeNotFound = "NOT FOUND";
        String legalNameNotFound = "Not found";
        Object[] institutionNotFound = {
                picNotFound,
                erasmusCodeNotFound,
                legalNameNotFound,
                poland.getCode(),
        };
        SearchedInstitution searchedInstitutionNotFound = new SearchedInstitution(picNotFound, erasmusCodeNotFound,
                legalNameNotFound, poland.getCode());

        dataSheetRows.addAll(
                Arrays.asList(rankingsColumns.toArray(), warsawPositionsRow, grazPositionsRow, institutionNotFound,
                        warsawPositionsRow));

        String validDataSheetRowsCount = "2"; // Warsaw position + Graz position
        String foundInstitutionsCount = "2"; // Warsaw University + Graz University
        String invalidDataSheetRowsCount = "2"; // institutionNotFound + duplicate Warsaw University
        String notFoundInstitutionsCount = "1"; // institutionNotFound

        String successMessage = getMessage("DataSheetUploadController.upload.success",
                new String[]{validDataSheetRowsCount, foundInstitutionsCount});
        String errorMessage = getMessage("DataSheetUploadController.upload.errors",
                new String[]{invalidDataSheetRowsCount, notFoundInstitutionsCount});

        DataSheetUpload upload = new DataSheetUpload(superAdmin, null, rankingsDataSheet);

        String institutionNotFoundMessage = getMessage("DataSheetUploadController.upload.institutionNotFound",
                new String[]{searchedInstitutionNotFound.getDetails(messageSource)});

        SearchedInstitution warsawSearch = new SearchedInstitution(warsawUniversity.getPic(),
                warsawUniversity.getErasmusCode(), warsawUniversity.getNameEn(), warsawUniversity.getCountryCode());
        String institutionAlreadyUploadedMessage = getMessage(
                "DataSheetUploadController.upload.dataForInstitutionAlreadyUploaded",
                new String[]{warsawSearch.getDetails(messageSource)});

        DataSheetUploadError[] uploadErrors = new DataSheetUploadError[]{
                new DataSheetUploadError(upload, null, 4, null, institutionNotFoundMessage),
                new DataSheetUploadError(upload, null, 5, null, institutionAlreadyUploadedMessage)
        };

        String superAdminNotified = getMessage("DataSheetUploadController.upload.superAdminNotified");
        performUpload(DataSheetUtils.getMockMultipartFile(dataSheetRows, rankingsColumns, rankingsDataSheet,
                dataSheetValueService))
                .andExpect(flash().attribute("upload", hasProperty("errors", containsInAnyOrder(uploadErrors))))
                .andExpect(flash().attribute("message",
                        is(Message.warning(successMessage + "\n" + errorMessage + "\n" + superAdminNotified))));

        DataSheetRow warsawPositions = new DataSheetRow(currentAcademicYear, null, rankingsDataSheet,
                warsawUniversity, null);
        addRowValues(warsawPositions, rankingsColumns, warsawPositionsRow);
        DataSheetRow grazPositions = new DataSheetRow(currentAcademicYear, null, rankingsDataSheet,
                grazUniversity, null);
        addRowValues(grazPositions, rankingsColumns, grazPositionsRow);

        List<DataSheetRow> rowsFromDatabase = dataSheetRowService.findAll();
        assertThat(rowsFromDatabase, containsInAnyOrder(warsawPositions, grazPositions));
    }

    private ResultActions performUpload(MockMultipartFile uploadFile) throws Exception {
        return mockMvc.perform(
                fileUpload(SuperAdminDataSheetUploadController.VIEW_DATA_SHEET_UPLOAD, DataSheetCode.RANKINGS)
                        .file(uploadFile).with(user(new DatabaseUserDetails(superAdmin))).with(csrf())
                        .param("year", currentAcademicYear.getAcademicYear())
                        .contentType(MediaType.MULTIPART_FORM_DATA));
    }

    private void addRowValues(DataSheetRow row, List<Object> columns, Object[] dataSheetArrayRow) {
        DataSheetUtils.addRowValues(row, columns, dataSheetArrayRow, ROW_VALUES_START_INDEX, ROW_VALUES_COUNT);
    }
}

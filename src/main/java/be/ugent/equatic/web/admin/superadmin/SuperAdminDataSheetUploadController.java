package be.ugent.equatic.web.admin.superadmin;

import be.ugent.equatic.domain.*;
import be.ugent.equatic.exception.DataSheetColumnNotFoundException;
import be.ugent.equatic.exception.DataSheetUploadNotFoundException;
import be.ugent.equatic.exception.DataSheetValueNotFoundException;
import be.ugent.equatic.service.AcademicYearService;
import be.ugent.equatic.service.DataSheetRowService;
import be.ugent.equatic.service.DataSheetService;
import be.ugent.equatic.service.DataSheetUploadService;
import be.ugent.equatic.web.util.Message;
import be.ugent.equatic.web.util.PrincipalUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class SuperAdminDataSheetUploadController extends SuperAdminController {

    private static final String VIEW_DATA_SHEET_UPLOAD_PAGE = VIEW_PATH + "/dataSheetUpload";
    public static final String VIEW_DATA_SHEET_UPLOAD = VIEW_DATA_SHEET_UPLOAD_PAGE + "/{dataSheetCode}";
    private static final String VIEW_DATA_SHEET_EXPORT = VIEW_DATA_SHEET_UPLOAD + "/export";
    private static final String VIEW_UPLOADS_LIST = VIEW_DATA_SHEET_UPLOAD_PAGE + "/uploads";
    private static final String VIEW_UPLOAD_ERRORS = VIEW_DATA_SHEET_UPLOAD_PAGE + "/errors/{dataSheetUpload}";

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private DataSheetService dataSheetService;

    @Autowired
    private DataSheetRowService dataSheetRowService;

    @Autowired
    private DataSheetUploadService dataSheetUploadService;

    @ModelAttribute("dataSheetCodes")
    private DataSheetCode[] getDataSheetCodes() {
        return new DataSheetCode[]{DataSheetCode.RANKINGS, DataSheetCode.GRADING_TABLES};
    }

    @ModelAttribute("academicYear")
    public AcademicYear getAcademicYear(@RequestParam(value = "year", required = false) AcademicYear academicYear) {
        if (academicYear == null) {
            return academicYearService.findCurrent();
        } else {
            return academicYear;
        }
    }

    @ModelAttribute("uploadStats")
    public Map<DataSheetCode, UploadStat> getUploadStats(@ModelAttribute AcademicYear academicYear) {
        Map<DataSheetCode, UploadStat> result = new HashMap<>();

        for (UploadStat uploadStat : dataSheetRowService.getSuperAdminUploadStatsForAcademicYear(academicYear)) {
            result.put(uploadStat.getDataSheet().getCode(), uploadStat);
        }

        return result;
    }

    @RequestMapping(value = VIEW_DATA_SHEET_UPLOAD_PAGE, method = RequestMethod.GET)
    public String dataSheetUpload(Model model) {
        model.addAttribute("academicYears", academicYearService.findAll());

        return "admin/dataSheetUpload";
    }

    @RequestMapping(value = VIEW_DATA_SHEET_UPLOAD, method = RequestMethod.POST)
    public String dataSheetUpload(@ModelAttribute AcademicYear academicYear, @RequestParam MultipartFile file,
                                  @PathVariable DataSheetCode dataSheetCode, RedirectAttributes redirect,
                                  Principal principal, Locale locale)
            throws IOException, InvalidFormatException {
        redirect.addAttribute("year", academicYear.getAcademicYear());

        if (file.isEmpty()) {
            String uploadFileRequired = messageSource.getMessage("equatic.upload.required", null, locale);
            redirect.addFlashAttribute("message", Message.danger(uploadFileRequired));
            return "redirect:" + VIEW_DATA_SHEET_UPLOAD_PAGE;
        }

        DataSheet dataSheet = dataSheetService.findByCode(dataSheetCode);
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        User superAdmin = PrincipalUtil.getUser(principal);

        dataSheetUploadService.uploadDataSheet(dataSheet, sheet, null, superAdmin, academicYear, false,
                redirect, locale);

        return "redirect:" + VIEW_DATA_SHEET_UPLOAD_PAGE;
    }

    @RequestMapping(value = VIEW_DATA_SHEET_EXPORT, method = RequestMethod.GET)
    public void dataSheetExport(@ModelAttribute AcademicYear academicYear, @PathVariable DataSheetCode dataSheetCode,
                                HttpServletResponse response)
            throws IOException, DataSheetValueNotFoundException, DataSheetColumnNotFoundException {
        DataSheet dataSheet = dataSheetService.findByCode(dataSheetCode);
        List<DataSheetRow> dataSheetRows = dataSheetRowService.findByDataSheetAndAcademicYear(dataSheet, academicYear);

        dataSheetService.respondWithDataSheet(response, dataSheet, dataSheetRows);
    }

    @RequestMapping(value = VIEW_UPLOADS_LIST, method = RequestMethod.GET)
    public String uploadsList(Model model) {
        model.addAttribute("uploads", getUploads());

        return "uploads";
    }

    private List<DataSheetUpload> getUploads() {
        return dataSheetUploadService.findByDataSheetCodeIn(getDataSheetCodes());
    }

    @RequestMapping(value = VIEW_UPLOAD_ERRORS, method = RequestMethod.GET)
    public String uploadErrors(@ModelAttribute DataSheetUpload dataSheetUpload) {
        if (getUploads().contains(dataSheetUpload)) {
            return "uploadErrors";
        } else {
            throw new DataSheetUploadNotFoundException(dataSheetUpload.getId());
        }
    }
}

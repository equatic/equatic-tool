package be.ugent.equatic.web.admin.superadmin;

import com.github.dandelion.datatables.core.ajax.DataSet;
import com.github.dandelion.datatables.core.ajax.DatatablesCriterias;
import com.github.dandelion.datatables.core.ajax.DatatablesResponse;
import com.github.dandelion.datatables.core.export.DatatablesExport;
import com.github.dandelion.datatables.core.export.ExportConf;
import com.github.dandelion.datatables.core.export.ExportUtils;
import com.github.dandelion.datatables.core.export.HtmlTableBuilder;
import com.github.dandelion.datatables.extras.export.poi.XlsExport;
import com.github.dandelion.datatables.extras.spring3.ajax.DatatablesParams;
import com.google.common.base.CaseFormat;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import be.ugent.equatic.datasheet.DataSheetParsedRow;
import be.ugent.equatic.datasheet.DataSheetParsedRowValue;
import be.ugent.equatic.datasheet.DataSheetParser;
import be.ugent.equatic.domain.*;
import be.ugent.equatic.exception.*;
import be.ugent.equatic.service.CountryService;
import be.ugent.equatic.service.DataSheetService;
import be.ugent.equatic.service.DataSheetUploadService;
import be.ugent.equatic.service.InstitutionService;
import be.ugent.equatic.validation.InstitutionAdditionalValidator;
import be.ugent.equatic.web.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.*;

@Controller
public class InstitutionManagementController extends SuperAdminController {

    public static final String VIEW_INSTITUTIONS_LIST = VIEW_PATH + "/institutions";
    public static final String VIEW_INSTITUTIONS_LIST_EXPORT = VIEW_INSTITUTIONS_LIST + "/export/{type}";
    public static final String VIEW_EDIT_INSTITUTION = VIEW_PATH + "/institution/{institution}/edit";
    public static final String VIEW_DELETE_INSTITUTION = VIEW_PATH + "/institution/{institution}/delete";
    private static final String VIEW_UPLOADS_LIST = VIEW_INSTITUTIONS_LIST + "/uploads";
    private static final String VIEW_UPLOAD_ERRORS = VIEW_INSTITUTIONS_LIST + "/errors/{dataSheetUpload}";

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private DataSheetService dataSheetService;

    @Autowired
    private DataSheetUploadService dataSheetUploadService;

    @Autowired
    private InstitutionAdditionalValidator institutionAdditionalValidator;

    @Autowired
    private Validator validator;

    @RequestMapping(value = VIEW_INSTITUTIONS_LIST, method = RequestMethod.GET)
    public String institutions() {
        return VIEW_INSTITUTIONS_LIST;
    }

    @RequestMapping(value = VIEW_INSTITUTIONS_LIST + "/ajax")
    public @ResponseBody DatatablesResponse<Institution> findAll(@DatatablesParams DatatablesCriterias criterias) {
        DataSet<Institution> dataSet = institutionService.findInstitutionsNotVirtualWithDatatablesCriterias(criterias);
        return DatatablesResponse.build(dataSet, criterias);
    }

    @RequestMapping(value = VIEW_INSTITUTIONS_LIST_EXPORT, method = RequestMethod.GET)
    public void institutionsExport(@PathVariable String type, HttpServletRequest request,
                                   HttpServletResponse response) {
        List<Institution> institutions = institutionService.findNotVirtual();

        DatatablesExport export;
        switch (type) {
            case "xls":
                export = new XlsExport();
                break;
            default:
                return; // TODO: add exception for wrong export type
        }

        ExportConf exportConf = new ExportConf.Builder(type)
                .header(true).exportClass(export)
                .fileName("institutions")
                .build();

        HtmlTableBuilder.BuildStep buildStep = (HtmlTableBuilder.BuildStep) new HtmlTableBuilder<Institution>()
                .newBuilder("institutionsTable", institutions, request, exportConf);
        for (DataSheetColumn column : dataSheetService.findByCode(DataSheetCode.INSTITUTIONS).getCurrentColumns()) {
            buildStep = buildStep.column()
                    .fillWithProperty(column.getPropertyName() + "Blank")
                    .title(column.getTitle());
        }

        ExportUtils.renderExport(buildStep.build(), exportConf, response);
    }

    @RequestMapping(value = VIEW_INSTITUTIONS_LIST, method = RequestMethod.POST)
    public String institutionsUpload(@RequestParam MultipartFile file, RedirectAttributes redirect, Principal principal,
                                     Locale locale)
            throws IOException, InvalidFormatException, MetadataProviderException {
        if (file.isEmpty()) {
            String uploadFileRequired = messageSource.getMessage("equatic.upload.required", null, locale);
            redirect.addFlashAttribute("message", Message.danger(uploadFileRequired));
            return "redirect:" + VIEW_INSTITUTIONS_LIST;
        }

        DataSheet institutionsDataSheet = dataSheetService.findByCode(DataSheetCode.INSTITUTIONS);
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        try {
            List<DataSheetParsedRow> rows = DataSheetParser.parseDataSheet(sheet, institutionsDataSheet);

            DataSheetUpload upload = new DataSheetUpload(PrincipalUtil.getUser(principal), null, institutionsDataSheet);
            List<Institution> updatedInstitutions = new ArrayList<>();
            List<Institution> addedInstitutions = new ArrayList<>();

            for (DataSheetParsedRow row : rows) {
                processDataSheetRow(row, upload, updatedInstitutions, addedInstitutions, locale);
            }

            dataSheetUploadService.save(upload);

            int invalidInstitutionsCount = rows.size() - updatedInstitutions.size() - addedInstitutions.size();
            String uploadSuccess = messageSource.getMessage("equatic.upload.success",
                    new String[]{String.valueOf(updatedInstitutions.size()), String.valueOf(addedInstitutions.size()),
                            String.valueOf(invalidInstitutionsCount)}, locale);
            redirect.addFlashAttribute("message", new Message(uploadSuccess,
                    invalidInstitutionsCount > 0 ? MessageType.warning : MessageType.success));

            if (invalidInstitutionsCount > 0) {
                redirect.addFlashAttribute("upload", upload);
            }
        } catch (DataSheetProcessingException exception) {
            redirect.addFlashAttribute("message", exception.getMessage(messageSource, locale));
        }

        return "redirect:" + VIEW_INSTITUTIONS_LIST;
    }

    private void processDataSheetRow(DataSheetParsedRow row, DataSheetUpload upload,
                                     List<Institution> updatedInstitutions, List<Institution> addedInstitutions,
                                     Locale locale) throws MetadataProviderException {
        Institution institution;
        DataSheet dataSheet = upload.getDataSheet();
        int rowNumber = row.getRowNum();
        try {
            institution = getInstitutionFromParsedRowValues(row.getRowValueMap(), countryService);
        } catch (CountryNotFoundException e) {
            String countryNotFoundMessage = messageSource.getMessage("equatic.upload.countryCodeNotFound",
                    new String[]{e.getCountryCode()}, locale);
            DataSheetColumn countryColumn;
            try {
                countryColumn = dataSheet.getColumnByCode(DataSheetColumnCode.COUNTRY_CODE);
            } catch (DataSheetColumnNotFoundException ex) {
                throw new RuntimeException("Country column should be present");
            }
            upload.addError(countryNotFoundMessage, countryColumn, rowNumber, e.getCountryCode());
            return;
        }

        Errors errors = new BindException(institution, "institution");
        institutionAdditionalValidator.validate(institution, errors);
        Set<ConstraintViolation<Institution>> violations = validator.validate(institution);

        if (errors.getErrorCount() != 0 || violations.size() != 0) {
            List<FieldError> fieldErrors = errors.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                DataSheetColumn column = getColumnFromPropertyName(fieldError.getField(), dataSheet);
                Object rejectedValue = fieldError.getRejectedValue();
                upload.addError(messageSource.getMessage(fieldError.getCode(), null, locale), column, rowNumber,
                        rejectedValue != null ? rejectedValue.toString() : null);
            }
            for (ConstraintViolation<Institution> violation : violations) {
                DataSheetColumn column = getColumnFromPropertyName(violation.getPropertyPath().toString(), dataSheet);
                Object invalidValue = violation.getInvalidValue();
                upload.addError(violation.getMessage(), column, rowNumber,
                        invalidValue != null ? invalidValue.toString() : null);
            }
        } else {
            try {
                FoundInstitution foundInstitution = new FoundInstitution(null, null);
                if (institution.getPic() != null) {
                    foundInstitution = foundInstitution.compareWithNextFoundInstitution(
                            institutionService.findByPicOrNull(institution.getPic()), FoundInstitutionBy.PIC);
                }
                if (institution.getErasmusCode() != null) {
                    foundInstitution = foundInstitution.compareWithNextFoundInstitution(
                            institutionService.findByErasmusCodeOrNull(institution.getErasmusCode()),
                            FoundInstitutionBy.ERASMUS_CODE);
                }
                foundInstitution = foundInstitution.compareWithNextFoundInstitution(
                        institutionService.findByNameAndCountryOrNull(institution.getName(), institution.getCountry()),
                        FoundInstitutionBy.NAME_AND_COUNTRY_CODE);
                if (institution.getNameEn() != null) {
                    foundInstitution = foundInstitution.compareWithNextFoundInstitution(
                            institutionService.findByNameEnAndCountryOrNull(
                                    institution.getNameEn(), institution.getCountry()),
                            FoundInstitutionBy.NAME_EN_AND_COUNTRY_CODE);
                }

                if (foundInstitution.getInstitution() != null) {
                    if (updatedInstitutions.contains(institution)) {
                        throw new InstitutionDuplicatedException(institution);
                    }

                    updateInstitution(institution, foundInstitution.getInstitution());
                    updatedInstitutions.add(institution);
                } else {
                    if (addedInstitutions.contains(institution)) {
                        throw new InstitutionDuplicatedException(institution);
                    }

                    institutionService.save(institution);
                    addedInstitutions.add(institution);
                }
            } catch (InstitutionsNotMatchException exception) {
                String foundInstitutionsNotMatchMessage = messageSource.getMessage(
                        "equatic.upload.foundInstitutionsNotMatch",
                        new String[]{exception.getFoundInstitution().getBy().toString(),
                                exception.getNextFoundInstitution().getBy().toString()}, locale);
                upload.addError(foundInstitutionsNotMatchMessage, null, rowNumber, null);
            } catch (InstitutionDuplicatedException exception) {
                String duplicatedInstitutionMessage = messageSource.getMessage(
                        "equatic.upload.institutionDuplicated",
                        new String[]{exception.getSearchedInstitution().getDetails(messageSource)}, locale);
                upload.addError(duplicatedInstitutionMessage, null, rowNumber, null);
            }
        }
    }

    private DataSheetColumn getColumnFromPropertyName(String propertyName, DataSheet dataSheet)
            throws DataSheetInternalErrorException {
        String columnCode = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, propertyName);
        DataSheetColumn column;
        try {
            column = dataSheet.getColumnByCode(DataSheetColumnCode.valueOf(columnCode));
        } catch (DataSheetColumnNotFoundException exception) {
            throw new DataSheetInternalErrorException(exception);
        }
        return column;
    }

    public static Institution getInstitutionFromParsedRowValues(
            Map<DataSheetColumnCode, DataSheetParsedRowValue> rowValuesMap, CountryService countryService)
            throws DataSheetProcessingException, CountryNotFoundException {
        try {
            // Institution(String pic, String erasmusCode, String name, String nameEn, String url, Country country)
            Constructor<Institution> constructor = Institution.class.getConstructor(String.class, String.class,
                    String.class, String.class, String.class, Country.class);
            String countryCode = (String) rowValuesMap.get(DataSheetColumnCode.COUNTRY_CODE).getValue();
            DataSheetParsedRowValue urlRowValue = rowValuesMap.get(DataSheetColumnCode.URL);

            return constructor.newInstance(
                    rowValuesMap.get(DataSheetColumnCode.PIC).getValue(),
                    rowValuesMap.get(DataSheetColumnCode.ERASMUS_CODE).getValue(),
                    rowValuesMap.get(DataSheetColumnCode.NAME).getValue(),
                    rowValuesMap.get(DataSheetColumnCode.NAME_EN).getValue(),
                    urlRowValue != null ? urlRowValue.getValue() : null,
                    countryService.findByCode(countryCode));
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new DataSheetInternalErrorException(e);
        }
    }

    private void updateInstitution(Institution institutionUploaded, Institution institutionInDatabase)
            throws MetadataProviderException {
        institutionUploaded.setId(institutionInDatabase.getId());
        institutionUploaded.setIdpEntityId(institutionInDatabase.getIdpEntityId());
        institutionUploaded.setIdpMetadataUrl(institutionInDatabase.getIdpMetadataUrl());

        institutionService.save(institutionUploaded);
    }

    @RequestMapping(value = VIEW_EDIT_INSTITUTION, method = RequestMethod.GET)
    private String editInstitutionForm(@ModelAttribute Institution institution,
                                       @PathVariable(value = "institution") long institutionId, Model model) {
        if (institution.getId() == null) {
            throw InstitutionNotFoundException.byId(institutionId);
        }
        model.addAttribute("countries", countryService.findAll());

        return VIEW_PATH + "/institutionEdit";
    }

    @RequestMapping(value = VIEW_EDIT_INSTITUTION, method = RequestMethod.POST)
    public String editInstitution(@Valid Institution institution, BindingResult result, RedirectAttributes redirect,
                                  Model model, Locale locale) throws MetadataProviderException {
        institutionAdditionalValidator.validate(institution, result);

        if (result.hasErrors()) {
            return editInstitutionForm(institution, institution.getId(), model);
        }

        institutionService.save(institution);

        String editSuccessfulMessage = messageSource.getMessage("InstitutionManagementController.edit.confirmation",
                new String[]{institution.getDisplayName()}, locale);
        redirect.addFlashAttribute("message", Message.success(editSuccessfulMessage));
        return "redirect:" + VIEW_INSTITUTIONS_LIST;
    }

    @RequestMapping(value = VIEW_DELETE_INSTITUTION, method = RequestMethod.GET)
    public String deleteInstitution(@PathVariable("institution") long institutionId, RedirectAttributes redirect,
                                    Locale locale) {
        Institution institution = institutionService.findById(institutionId);

        try {
            institutionService.delete(institution);
        } catch (DataIntegrityViolationException exception) {
            String cannotDeleteMessage = messageSource.getMessage("InstitutionManagementController.delete.notPossible",
                    new String[]{institution.getDisplayName()}, locale);
            redirect.addFlashAttribute("message", Message.danger(cannotDeleteMessage));
            return "redirect:" + VIEW_INSTITUTIONS_LIST;
        }

        String editSuccessfulMessage = messageSource.getMessage("InstitutionManagementController.delete.confirmation",
                new String[]{institution.getDisplayName()}, locale);
        redirect.addFlashAttribute("message", Message.success(editSuccessfulMessage));
        return "redirect:" + VIEW_INSTITUTIONS_LIST;
    }

    @RequestMapping(value = VIEW_UPLOADS_LIST, method = RequestMethod.GET)
    public String uploadsList(Model model) {
        model.addAttribute("uploads", dataSheetUploadService.findByDataSheetCode(DataSheetCode.INSTITUTIONS));

        return "uploads";
    }

    @RequestMapping(value = VIEW_UPLOAD_ERRORS, method = RequestMethod.GET)
    public String uploadErrors(@ModelAttribute DataSheetUpload dataSheetUpload) {
        return "uploadErrors";
    }
}

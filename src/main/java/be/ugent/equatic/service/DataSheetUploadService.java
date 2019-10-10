package be.ugent.equatic.service;

import be.ugent.equatic.datasheet.DataSheetParsedRow;
import be.ugent.equatic.datasheet.DataSheetParsedRowValue;
import be.ugent.equatic.datasheet.DataSheetParser;
import be.ugent.equatic.domain.*;
import be.ugent.equatic.exception.*;
import be.ugent.equatic.web.util.*;
import com.google.common.base.Joiner;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataSheetUploadService {

    @Autowired
    private DataSheetUploadRepository dataSheetUploadRepository;

    @Autowired
    private DataSheetRowService dataSheetRowService;

    @Autowired
    private DataSheetValueService dataSheetValueService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private IscedService iscedService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private MessageSource messageSource;

    @Transactional
    public DataSheetUpload save(DataSheetUpload dataSheetUpload) {
        return dataSheetUploadRepository.save(dataSheetUpload);
    }

    @Transactional
    public void deleteAll() {
        dataSheetUploadRepository.deleteAll();
    }

    public DataSheetUpload findById(Long uploadId) throws DataSheetUploadNotFoundException {
        DataSheetUpload dataSheetUpload = dataSheetUploadRepository.findById(uploadId);

        if (dataSheetUpload == null) {
            throw new DataSheetUploadNotFoundException(uploadId);
        }

        return dataSheetUpload;
    }

    public List<DataSheetUpload> findByDataSheetCode(DataSheetCode dataSheetCode) {
        return dataSheetUploadRepository.findByDataSheetCode(dataSheetCode);
    }

    public List<DataSheetUpload> findByInstitutionAndDataSheetCodeIn(Institution institution,
                                                                     DataSheetCode[] dataSheetCodes) {
        return dataSheetUploadRepository.findByInstitutionAndDataSheetCodeIn(institution, dataSheetCodes);
    }

    public List<DataSheetUpload> findByDataSheetCodeIn(DataSheetCode[] dataSheetCodes) {
        return dataSheetUploadRepository.findByDataSheetCodeIn(dataSheetCodes);
    }

    public void uploadDataSheet(DataSheet dataSheet, Sheet sheet, Institution institution, User admin,
                                AcademicYear academicYear, boolean selfAssessment, RedirectAttributes redirect,
                                Locale locale) {
        try {
            List<DataSheetParsedRow> rows = DataSheetParser.parseDataSheet(sheet, dataSheet);

            DataSheetUpload upload = new DataSheetUpload(admin, institution, dataSheet);
            Set<Institution> foundInstitutions = new HashSet<>();
            Set<SearchedInstitution> notFoundInstitutions = new HashSet<>();
            List<DataSheetRow> validDataSheetRows = new ArrayList<>();

            for (DataSheetParsedRow row : rows) {
                processDataSheetRow(row, upload, institution, academicYear, foundInstitutions, notFoundInstitutions,
                        validDataSheetRows, selfAssessment, locale);
            }

            dataSheetRowService.deleteByInstitutionAndDataSheetAndAcademicYear(institution, dataSheet, academicYear);
            dataSheetRowService.save(validDataSheetRows);
            save(upload);

            int invalidDataSheetRowsCount = rows.size() - validDataSheetRows.size();
            String uploadSuccess = messageSource.getMessage("DataSheetUploadController.upload.success",
                    new String[]{String.valueOf(validDataSheetRows.size()),
                            String.valueOf(foundInstitutions.size())}, locale);
            if (invalidDataSheetRowsCount > 0) {
                int notFoundInstitutionsCount = notFoundInstitutions.size();
                String uploadErrors = messageSource.getMessage("DataSheetUploadController.upload.errors",
                        new String[]{String.valueOf(String.valueOf(invalidDataSheetRowsCount)),
                                String.valueOf(notFoundInstitutionsCount)}, locale);
                String warningMessage = uploadSuccess + "\n" + uploadErrors;
                if (notFoundInstitutionsCount > 0) {
                    String superAdminNotifiedMessage =
                            messageSource.getMessage("DataSheetUploadController.upload.superAdminNotified", null,
                                    locale);
                    warningMessage += "\n" + superAdminNotifiedMessage;
                }
                redirect.addFlashAttribute("message", new Message(warningMessage, MessageType.warning));
                redirect.addFlashAttribute("upload", upload);

                if (notFoundInstitutionsCount > 0 && !admin.isSuperAdmin()) {
                    notificationService.sendNotFoundInstitutionsNotification(admin, dataSheet, institution,
                            notFoundInstitutions, locale);
                }
            } else {
                redirect.addFlashAttribute("message", new Message(uploadSuccess, MessageType.success));
            }
        } catch (DataSheetProcessingException exception) {
            redirect.addFlashAttribute("message", exception.getMessage(messageSource, locale));
        }
    }

    private void processDataSheetRow(DataSheetParsedRow parsedRow, DataSheetUpload upload,
                                     Institution uploaderInstitution, AcademicYear academicYear,
                                     Set<Institution> matchedInstitutions,
                                     Set<SearchedInstitution> notFoundInstitutions,
                                     List<DataSheetRow> validDataSheetRows, boolean selfAssessment, Locale locale) {
        int rowNumber = parsedRow.getRowNum();
        DataSheet dataSheet = upload.getDataSheet();

        String pic = (String) parsedRow.removeRowValueForColumnCode(DataSheetColumnCode.PIC);
        String erasmusCode = (String) parsedRow.removeRowValueForColumnCode(DataSheetColumnCode.ERASMUS_CODE);
        String legalName = (String) parsedRow.removeRowValueForColumnCode(DataSheetColumnCode.LEGAL_NAME);
        String countryCode = (String) parsedRow.removeRowValueForColumnCode(DataSheetColumnCode.COUNTRY_CODE);
        String url = (String) parsedRow.removeRowValueForColumnCode(DataSheetColumnCode.URL);
        String iscedCode = (String) parsedRow.removeRowValueForColumnCode(DataSheetColumnCode.ISCED_CODE);

        try {
            Institution partnerInstitution = getPartnerInstitution(pic, erasmusCode, legalName, countryCode, url);

            if (dataSheet.isOnlyOneRowForInstitution() && matchedInstitutions.contains(partnerInstitution)) {
                SearchedInstitution searchedInstitution = new SearchedInstitution(pic, erasmusCode, legalName,
                        countryCode);
                throw new DataForInstitutionAlreadyUploadedException(searchedInstitution);
            }

            Isced isced = null;
            if (iscedCode != null) {
                isced = iscedService.findByCode(iscedCode);
            } else if (dataSheet.isIscedRequired()) {
                throw new IscedNotPresentException();
            }

            DataSheetRow dataSheetRow = new DataSheetRow(academicYear, uploaderInstitution, dataSheet,
                    partnerInstitution, isced, selfAssessment);

            addValuesToRow(parsedRow, dataSheetRow);

            matchedInstitutions.add(partnerInstitution);

            validDataSheetRows.add(dataSheetRow);
        } catch (InstitutionsNotMatchException exception) {
            String foundInstitutionsNotMatchMessage = messageSource.getMessage(
                    "equatic.upload.foundInstitutionsNotMatch",
                    new String[]{exception.getFoundInstitution().getBy().toString(),
                            exception.getNextFoundInstitution().getBy().toString()}, locale);
            upload.addError(foundInstitutionsNotMatchMessage, null, rowNumber, null);
        } catch (PartnerInstitutionNotFoundException exception) {
            SearchedInstitution searchedInstitution = exception.getSearchedInstitution();
            String institutionNotFoundMessage = messageSource.getMessage(
                    "DataSheetUploadController.upload.institutionNotFound",
                    new String[]{searchedInstitution.getDetails(messageSource, locale)}, locale);
            upload.addError(institutionNotFoundMessage, null, rowNumber, null);

            notFoundInstitutions.add(searchedInstitution);
        } catch (DataForInstitutionAlreadyUploadedException exception) {
            SearchedInstitution searchedInstitution = exception.getSearchedInstitution();
            String dataForInstitutionAlreadyUploadedMessage = messageSource.getMessage(
                    "DataSheetUploadController.upload.dataForInstitutionAlreadyUploaded",
                    new String[]{searchedInstitution.getDetails(messageSource, locale)}, locale);
            upload.addError(dataForInstitutionAlreadyUploadedMessage, null, rowNumber, null);
        } catch (DataSheetValueNotFoundException exception) {
            DataSheetColumn column = exception.getColumn();
            String value = exception.getValue();
            List<String> validValues = dataSheetValueService.findByColumn(column).stream().map(
                    DataSheetValue::getValue).collect(Collectors.toList());
            String valueNotFoundMessage = messageSource.getMessage(
                    "DataSheetUploadController.upload.valueNotFound",
                    new String[]{value, column.getTitle(), Joiner.on(", ").join(validValues)}, locale);
            upload.addError(valueNotFoundMessage, column, rowNumber, value);
        } catch (IscedNotFoundException e) {
            String iscedNotFoundMessage = messageSource.getMessage(
                    "equatic.upload.iscedNotFound", new String[]{iscedCode}, locale);
            try {
                DataSheetColumn iscedColumn = dataSheet.getColumnByCode(DataSheetColumnCode.ISCED_CODE);
                upload.addError(iscedNotFoundMessage, iscedColumn, rowNumber, iscedCode);
            } catch (DataSheetColumnNotFoundException ex) {
                throw new RuntimeException("ISCED column should be present", e);
            }
        } catch (CountryNotFoundException e) {
            SearchedInstitution searchedInstitution = new SearchedInstitution(pic, erasmusCode, legalName, countryCode);
            String countryCodeNotFoundMessage = messageSource.getMessage(
                    "equatic.upload.countryCodeNotFound", new String[]{countryCode}, locale);
            try {
                DataSheetColumn countryColumn = dataSheet.getColumnByCode(DataSheetColumnCode.COUNTRY_CODE);
                upload.addError(countryCodeNotFoundMessage, countryColumn, rowNumber, countryCode);
            } catch (DataSheetColumnNotFoundException ex) {
                throw new RuntimeException("Country column should be present", e);
            }

            notFoundInstitutions.add(searchedInstitution);
        } catch (MalformedURLException e) {
            String urlMalformedMessage = messageSource.getMessage(
                    "equatic.upload.urlMalformed", new String[]{url}, locale);
            try {
                DataSheetColumn urlColumn = dataSheet.getColumnByCode(DataSheetColumnCode.URL);
                upload.addError(urlMalformedMessage, urlColumn, rowNumber, url);
            } catch (DataSheetColumnNotFoundException ex) {
                throw new RuntimeException("URL not found", e);
            }
        } catch (IscedNotPresentException exception) {
            String iscedNotPresentMessage = messageSource.getMessage(
                    "equatic.upload.iscedNotPresent", null, locale);
            try {
                DataSheetColumn iscedColumn = dataSheet.getColumnByCode(DataSheetColumnCode.ISCED_CODE);
                upload.addError(iscedNotPresentMessage, iscedColumn, rowNumber, null);
            } catch (DataSheetColumnNotFoundException e) {
                throw new RuntimeException("ISCED column should be present", e);
            }
        }
    }

    private Institution getPartnerInstitution(String pic, String erasmusCode, String legalName, String countryCode,
                                              String url)
            throws InstitutionsNotMatchException, PartnerInstitutionNotFoundException, CountryNotFoundException,
            MalformedURLException {
        Country country;
        FoundInstitution foundInstitution = new FoundInstitution(null, null);

        if (pic != null) {
            foundInstitution = foundInstitution.compareWithNextFoundInstitution(
                    institutionService.findByPicOrNull(pic), FoundInstitutionBy.PIC);
        }
        if (erasmusCode != null) {
            foundInstitution = foundInstitution.compareWithNextFoundInstitution(
                    institutionService.findByErasmusCodeOrNull(erasmusCode),
                    FoundInstitutionBy.ERASMUS_CODE);
        }
        if (legalName != null && countryCode != null) {
            country = countryService.findByCode(countryCode);

            foundInstitution = foundInstitution.compareWithNextFoundInstitution(
                    institutionService.findByNameAndCountryOrNullIgnoreAccentsBrackets(legalName, country),
                    FoundInstitutionBy.NAME_AND_COUNTRY_CODE);
            foundInstitution = foundInstitution.compareWithNextFoundInstitution(
                    institutionService.findByNameEnAndCountryOrNull(legalName, country),
                    FoundInstitutionBy.NAME_EN_AND_COUNTRY_CODE);
        }
        if (url != null) {
            List<Institution> foundInstitutions = institutionService.findByUrlSimilar(new URL(url));
            if (!foundInstitutions.isEmpty()) {
                foundInstitution = foundInstitution.compareWithNextFoundInstitution(
                        foundInstitutions.get(0), FoundInstitutionBy.URL);
            }
        }

        if (foundInstitution.getInstitution() == null) {
            SearchedInstitution searchedInstitution = new SearchedInstitution(pic, erasmusCode, legalName, countryCode);
            throw new PartnerInstitutionNotFoundException(searchedInstitution);
        }

        return foundInstitution.getInstitution();
    }

    private void addValuesToRow(DataSheetParsedRow parsedRow, DataSheetRow dataSheetRow)
            throws DataSheetValueNotFoundException {
        for (DataSheetParsedRowValue parsedRowValue : parsedRow.getRowValueMap().values()) {
            DataSheetColumn column = parsedRowValue.getColumn();
            DataSheetColumnType columnType = column.getType();

            Object parsedValue = parsedRowValue.getValue();

            Object[] values;
            if (columnType.isMultipleChoice() && parsedValue != null) {
                String stringValue = (String) parsedValue;
                values = stringValue.split(DataSheetService.MULTIPLE_CHOICE_SEPARATOR);
            } else {
                values = new Object[]{parsedValue};
            }

            for (Object value : values) {
                DataSheetRowValue rowValue;
                DataSheetColumnCode columnCode = column.getCode();

                if (!columnType.isFixedValue()) {
                    if (value instanceof String) {
                        rowValue = new DataSheetRowValue(dataSheetRow, columnCode, (String) value);
                    } else if (value instanceof Double) {
                        rowValue = new DataSheetRowValue(dataSheetRow, columnCode, (Double) value);
                    } else {
                        rowValue = new DataSheetRowValue(dataSheetRow, columnCode, (Date) value);
                    }
                } else {
                    DataSheetValueCode dataSheetValueCode = null;
                    if (value != null) {
                        dataSheetValueCode =
                                dataSheetValueService.findByColumnAndValueIgnoreCase(column, (String) value).getCode();
                    }
                    rowValue = new DataSheetRowValue(dataSheetRow, columnCode, dataSheetValueCode);
                }
                dataSheetRow.addRowValue(rowValue);
            }
        }
    }
}

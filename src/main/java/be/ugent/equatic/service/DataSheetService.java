package be.ugent.equatic.service;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import be.ugent.equatic.datasheet.DataSheetParser;
import be.ugent.equatic.domain.*;
import be.ugent.equatic.exception.DataSheetColumnNotFoundException;
import be.ugent.equatic.exception.DataSheetValueNotFoundException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataSheetService {

    static final String MULTIPLE_CHOICE_SEPARATOR = ";";

    @Autowired
    private DataSheetRepository dataSheetRepository;

    @Autowired
    private DataSheetValueService dataSheetValueService;

    @Transactional
    public DataSheet save(DataSheet dataSheet) {
        return dataSheetRepository.save(dataSheet);
    }

    @Transactional(readOnly = true)
    public DataSheet findByCode(DataSheetCode dataSheetCode) {
        return dataSheetRepository.findByCode(dataSheetCode);
    }

    private Sheet getSheet(DataSheet dataSheet, List<DataSheetRow> dataSheetRows)
            throws DataSheetValueNotFoundException, DataSheetColumnNotFoundException {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        CellStyle cellDateStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        cellDateStyle.setDataFormat(createHelper.createDataFormat().getFormat(DataSheetParser.DATA_SHEET_DATE_FORMAT));

        Map<DataSheetColumnCode, Integer> columnMap = new HashMap<>();
        Row columnsRow = sheet.createRow(0);
        int celln = 0;
        for (DataSheetColumn column : dataSheet.getCurrentColumns()) {
            columnsRow.createCell(celln).setCellValue(column.getTitle());
            columnMap.put(column.getCode(), celln);
            celln++;
        }

        int rown = 1;
        for (DataSheetRow dataSheetRow : dataSheetRows) {
            Row row = sheet.createRow(rown);
            Institution partnerInstitution = dataSheetRow.getPartnerInstitution();

            String pic = partnerInstitution.getPic();
            if (pic != null) {
                row.createCell(columnMap.get(DataSheetColumnCode.PIC)).setCellValue(pic);
            }

            String erasmusCode = partnerInstitution.getErasmusCode();
            if (erasmusCode != null) {
                row.createCell(columnMap.get(DataSheetColumnCode.ERASMUS_CODE)).setCellValue(erasmusCode);
            }

            String displayName = partnerInstitution.getDisplayName();
            if (displayName != null) {
                row.createCell(columnMap.get(DataSheetColumnCode.LEGAL_NAME)).setCellValue(displayName);
            }

            String countryCode = partnerInstitution.getCountryCode();
            if (countryCode != null) {
                row.createCell(columnMap.get(DataSheetColumnCode.COUNTRY_CODE)).setCellValue(countryCode);
            }

            Isced isced = dataSheetRow.getIsced();
            if (isced != null) {
                row.createCell(columnMap.get(DataSheetColumnCode.ISCED_CODE)).setCellValue(isced.getCode());
            }

            for (DataSheetRowValue rowValue : dataSheetRow.getValues()) {
                DataSheetColumnCode columnCode = rowValue.getColumnCode();
                Integer columnNumber = columnMap.get(columnCode);
                if (rowValue.getValue() != null) {
                    row.createCell(columnNumber).setCellValue(rowValue.getValue());
                } else if (rowValue.getDateValue() != null) {
                    Cell cell = row.createCell(columnNumber);
                    cell.setCellStyle(cellDateStyle);
                    cell.setCellValue(rowValue.getDateValue());
                } else if (rowValue.getNumericValue() != null) {
                    row.createCell(columnNumber).setCellValue(rowValue.getNumericValue());
                } else if (rowValue.getValueCode() != null) {
                    String value = dataSheetValueService.findByCode(rowValue.getValueCode()).getValue();
                    DataSheetColumn column = dataSheet.getColumnByCode(columnCode);
                    Cell cell = row.getCell(columnNumber);
                    if (column.getType().isMultipleChoice() && cell != null) {
                        value = cell.getStringCellValue() + MULTIPLE_CHOICE_SEPARATOR + value;
                    }
                    row.createCell(columnNumber).setCellValue(value);
                }
            }

            rown++;
        }

        return sheet;
    }

    public void respondWithDataSheet(HttpServletResponse response, DataSheet dataSheet,
                                     List<DataSheetRow> dataSheetRows)
            throws DataSheetValueNotFoundException, IOException, DataSheetColumnNotFoundException {
        Sheet sheet = getSheet(dataSheet, dataSheetRows);

        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"export_" + dataSheet.getCode() + ".xls\"");

        ServletOutputStream out = response.getOutputStream();
        sheet.getWorkbook().write(out);
        out.flush();
    }
}

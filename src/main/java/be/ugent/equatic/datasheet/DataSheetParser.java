package be.ugent.equatic.datasheet;

import be.ugent.equatic.domain.DataSheet;
import be.ugent.equatic.domain.DataSheetColumn;
import be.ugent.equatic.domain.DataSheetColumnType;
import be.ugent.equatic.domain.DataSheetValueType;
import be.ugent.equatic.exception.DataSheetInternalErrorException;
import be.ugent.equatic.exception.DataSheetProcessingException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.*;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL;

public class DataSheetParser {

    public static final String DATA_SHEET_DATE_FORMAT = "dd/MM/yy";

    public static List<DataSheetParsedRow> parseDataSheet(Sheet sheet, DataSheet dataSheet)
            throws DataSheetProcessingException {
        List<DataSheetParsedRow> rows = new ArrayList<>();
        Iterator<Row> dataSheetRowIterator = sheet.rowIterator();

        if (!dataSheetRowIterator.hasNext()) {
            throw new DataSheetProcessingException("equatic.dataSheet.invalidFormat.noRows");
        }

        Map<DataSheetColumn, Integer> columnsMap = getColumnsMap(dataSheet, dataSheetRowIterator);

        while (dataSheetRowIterator.hasNext()) {
            Row dataSheetRow = dataSheetRowIterator.next();
            if (isRowEmpty(dataSheetRow)) {
                continue;
            }

            DataSheetParsedRow row = new DataSheetParsedRow(dataSheetRow.getRowNum() + 1);
            for (Map.Entry<DataSheetColumn, Integer> entry : columnsMap.entrySet()) {
                DataSheetColumn column = entry.getKey();
                DataSheetColumnType columnType = column.getType();
                Cell cell = dataSheetRow.getCell(entry.getValue(), RETURN_BLANK_AS_NULL);
                Object value = null;
                if (cell != null) {
                    DataSheetValueType valueType = columnType.getValueType();

                    switch (valueType) {
                        case NUMERIC:
                            try {
                                cell.setCellType(valueType.getCellType());
                                value = cell.getNumericCellValue();
                            } catch (IllegalStateException exception) {
                                throw new DataSheetProcessingException(
                                        "equatic.dataSheet.invalidFormat.cellTypeShouldBeNumeric", column.getTitle());
                            }
                            break;
                        case STRING:
                            cell.setCellType(valueType.getCellType());
                            value = cell.getStringCellValue();
                            if ("".equals(value)) {
                                value = null;
                            }
                            break;
                        case DATE:
                            try {
                                value = cell.getDateCellValue();
                            } catch (Exception exception) {
                                throw new DataSheetProcessingException(
                                        "equatic.dataSheet.invalidFormat.invalidDateFormat", column.getTitle());
                            }
                            break;
                        default:
                            throw new DataSheetInternalErrorException();
                    }
                }
                row.addRowValue(new DataSheetParsedRowValue(column, value));
            }
            rows.add(row);
        }

        return rows;
    }

    private static Map<DataSheetColumn, Integer> getColumnsMap(DataSheet dataSheet, Iterator<Row> rowIterator)
            throws DataSheetProcessingException {
        Row columnTitlesRow = rowIterator.next();
        Map<DataSheetColumn, Integer> columnMap = new HashMap<>();

        int lastCellNum = columnTitlesRow.getLastCellNum();
        Map<String, Integer> actualColumnTitlesMap = new HashMap<>();
        for (int cellNum = columnTitlesRow.getFirstCellNum(); cellNum <= lastCellNum; cellNum++) {
            Cell column = columnTitlesRow.getCell(cellNum, RETURN_BLANK_AS_NULL);
            if (column == null || column.getCellTypeEnum() != CellType.STRING) {
                continue;
            }
            actualColumnTitlesMap.put(column.getStringCellValue().toLowerCase(), cellNum);
        }

        for (List<DataSheetColumn> expectedColumnsForCode : dataSheet.getColumnsMapByCodes().values()) {
            Integer cellNum = null;
            for (DataSheetColumn expectedColumn : expectedColumnsForCode) {
                cellNum = actualColumnTitlesMap.get(expectedColumn.getTitle().toLowerCase());
                if (cellNum != null) {
                    columnMap.put(expectedColumn, cellNum);
                    break;
                }
            }

            DataSheetColumn currentExpectedColumn = DataSheet.getCurrentColumn(expectedColumnsForCode);
            if (cellNum == null) {
                if (!currentExpectedColumn.isOptional()) {
                    throw new DataSheetProcessingException("equatic.dataSheet.invalidFormat.columnNotExists",
                            currentExpectedColumn.getTitle());
                }
            }
        }

        return columnMap;
    }

    private static boolean isRowEmpty(Row row) {
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            if (cell.getCellTypeEnum() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
}

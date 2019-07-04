package be.ugent.equatic.datasheet;

import be.ugent.equatic.domain.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.mock.web.MockMultipartFile;
import be.ugent.equatic.exception.DataSheetColumnNotFoundException;
import be.ugent.equatic.exception.DataSheetValueNotFoundException;
import be.ugent.equatic.service.DataSheetValueService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DataSheetUtils {

    public static List<String[]> getRowsForObject(List<Object> objects, DataSheet dataSheet)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<String[]> rows = new ArrayList<>();

        for (Object object : objects) {
            List<String> row = new ArrayList<>();
            for (DataSheetColumn column : dataSheet.getCurrentColumns()) {
                Method method = object.getClass().getMethod(column.getMethod());
                row.add((String) method.invoke(object));
            }
            rows.add(row.toArray(new String[dataSheet.getColumnsCount()]));
        }

        return rows;
    }

    public static Sheet arrayToDataSheet(String[][] dataSheetArray) {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        for (int rown = 0; rown < dataSheetArray.length; rown++) {
            Row row = sheet.createRow(rown);
            for (int celln = 0; celln < dataSheetArray[rown].length; celln++) {
                row.createCell(celln).setCellValue(dataSheetArray[rown][celln]);
            }
        }

        return sheet;
    }

    public static MockMultipartFile getMockMultipartFile(List<Object[]> dataSheetRows, List<Object> columns,
                                                         DataSheet dataSheet,
                                                         DataSheetValueService dataSheetValueService)
            throws DataSheetColumnNotFoundException, DataSheetValueNotFoundException, IOException {
        String[][] dataSheetArray = new String[dataSheetRows.size()][columns.size()];

        for (int rowNo = 0; rowNo < dataSheetRows.size(); rowNo++) {
            for (int colNo = 0; colNo < columns.size(); colNo++) {
                Object cell = (colNo < dataSheetRows.get(rowNo).length) ? dataSheetRows.get(rowNo)[colNo] : null;
                dataSheetArray[rowNo][colNo] = convertObjectToString(cell, dataSheet, dataSheetValueService);
            }
        }

        Sheet sheet = DataSheetUtils.arrayToDataSheet(dataSheetArray);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        sheet.getWorkbook().write(outputStream);
        return new MockMultipartFile("file", outputStream.toByteArray());
    }

    public static void addRowValues(DataSheetRow row, List<Object> columns, Object[] dataSheetArrayRow,
                                    int rowValuesStartIndex, int rowValuesCount) {
        for (int rowValueNo = 0; rowValueNo < rowValuesCount; rowValueNo++) {
            int rowValueIndex = rowValuesStartIndex + rowValueNo;
            DataSheetColumnCode columnCode = (DataSheetColumnCode) columns.get(rowValueIndex);

            DataSheetRowValue dataSheetRowValue;
            Object rowValueObject = rowValueIndex < dataSheetArrayRow.length ? dataSheetArrayRow[rowValueIndex] : null;
            if (rowValueObject instanceof DataSheetValueCode) {
                dataSheetRowValue = new DataSheetRowValue(row, columnCode, (DataSheetValueCode) rowValueObject);
            } else {
                dataSheetRowValue = new DataSheetRowValue(row, columnCode, (String) rowValueObject);
            }
            row.addRowValue(dataSheetRowValue);
        }
    }

    private static String convertObjectToString(Object cell, DataSheet dataSheet,
                                                DataSheetValueService dataSheetValueService)
            throws DataSheetColumnNotFoundException, DataSheetValueNotFoundException {
        if (cell instanceof DataSheetColumnCode) {
            return dataSheet.getColumnByCode((DataSheetColumnCode) cell).getTitle();
        } else if (cell instanceof DataSheetValueCode) {
            return dataSheetValueService.findByCode((DataSheetValueCode) cell).getValue();
        } else {
            return (String) cell;
        }
    }
}

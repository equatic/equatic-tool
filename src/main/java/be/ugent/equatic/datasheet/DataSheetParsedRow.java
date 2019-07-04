package be.ugent.equatic.datasheet;

import be.ugent.equatic.domain.DataSheetColumnCode;

import java.util.HashMap;
import java.util.Map;

public class DataSheetParsedRow {

    private int rowNum;
    private Map<DataSheetColumnCode, DataSheetParsedRowValue> rowValueMap = new HashMap<>();

    DataSheetParsedRow(int rowNum) {
        this.rowNum = rowNum;
    }

    public Object removeRowValueForColumnCode(DataSheetColumnCode dataSheetColumnCode) {
        DataSheetParsedRowValue rowValue = getRowValueMap().remove(dataSheetColumnCode);
        return rowValue != null ? rowValue.getValue() : null;
    }

    void addRowValue(DataSheetParsedRowValue rowValue) {
        rowValueMap.put(rowValue.getColumn().getCode(), rowValue);
    }

    public int getRowNum() {
        return rowNum;
    }

    public Map<DataSheetColumnCode, DataSheetParsedRowValue> getRowValueMap() {
        return rowValueMap;
    }
}

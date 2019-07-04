package be.ugent.equatic.datasheet;

import be.ugent.equatic.domain.DataSheetColumn;

public class DataSheetParsedRowValue {

    private DataSheetColumn column;
    private Object value;

    DataSheetParsedRowValue(DataSheetColumn column, Object value) {
        this.column = column;
        this.value = value;
    }

    public DataSheetColumn getColumn() {
        return column;
    }

    public Object getValue() {
        return value;
    }
}

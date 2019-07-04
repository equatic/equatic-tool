package be.ugent.equatic.exception;

import be.ugent.equatic.domain.DataSheetColumn;
import be.ugent.equatic.domain.DataSheetValueCode;

public class DataSheetValueNotFoundException extends Exception {

    private DataSheetColumn column;
    private String value;
    private DataSheetValueCode code;

    public DataSheetValueNotFoundException(DataSheetColumn column, String value) {
        this.column = column;
        this.value = value;
    }

    public DataSheetValueNotFoundException(DataSheetValueCode code) {
        this.code = code;
    }

    public DataSheetColumn getColumn() {
        return column;
    }

    public String getValue() {
        return value;
    }

    public DataSheetValueCode getCode() {
        return code;
    }
}

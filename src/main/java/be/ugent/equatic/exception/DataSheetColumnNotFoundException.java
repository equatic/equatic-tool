package be.ugent.equatic.exception;

import be.ugent.equatic.domain.DataSheetColumnCode;

public class DataSheetColumnNotFoundException extends Exception {

    public DataSheetColumnNotFoundException(DataSheetColumnCode code) {
        super("Could not find column with code: " + code.name());
    }
}

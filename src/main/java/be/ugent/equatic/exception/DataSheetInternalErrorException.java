package be.ugent.equatic.exception;

public class DataSheetInternalErrorException extends DataSheetProcessingException {

    public DataSheetInternalErrorException() {
        super("equatic.dataSheet.internalError");
    }

    public DataSheetInternalErrorException(Throwable cause) {
        super("equatic.dataSheet.internalError", cause);
    }
}

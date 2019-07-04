package be.ugent.equatic.exception;

public class DataSheetProcessingException extends UserMessageException {

    public DataSheetProcessingException(String messageCode, String... params) {
        super(messageCode, params);
    }

    public DataSheetProcessingException(String messageCode, Throwable cause) {
        super(messageCode, cause);
    }
}

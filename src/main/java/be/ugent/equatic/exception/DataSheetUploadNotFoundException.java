package be.ugent.equatic.exception;

public class DataSheetUploadNotFoundException extends ResourceNotFoundException {

    public DataSheetUploadNotFoundException(Long uploadId) {
        super("equatic.DataSheetUploadNotFoundException.byUploadId", String.valueOf(uploadId));
    }
}

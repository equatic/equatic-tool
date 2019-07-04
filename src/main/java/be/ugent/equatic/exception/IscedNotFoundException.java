package be.ugent.equatic.exception;

public class IscedNotFoundException extends Exception {

    private String iscedCode;

    public IscedNotFoundException(String iscedCode) {
        this.iscedCode = iscedCode;
    }

    public String getIscedCode() {
        return iscedCode;
    }
}

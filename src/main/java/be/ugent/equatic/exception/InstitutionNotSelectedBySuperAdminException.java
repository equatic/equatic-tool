package be.ugent.equatic.exception;

public class InstitutionNotSelectedBySuperAdminException extends Exception {

    private boolean virtual = false;

    public InstitutionNotSelectedBySuperAdminException(boolean virtual) {
        this.virtual = virtual;
    }

    public boolean isVirtual() {
        return virtual;
    }
}

package be.ugent.equatic.web.util;

public class InstitutionsNotMatchException extends Throwable {

    private FoundInstitution foundInstitution;
    private FoundInstitution nextFoundInstitution;

    InstitutionsNotMatchException(FoundInstitution foundInstitution, FoundInstitution nextFoundInstitution) {
        this.foundInstitution = foundInstitution;
        this.nextFoundInstitution = nextFoundInstitution;
    }

    public FoundInstitution getFoundInstitution() {
        return foundInstitution;
    }

    public FoundInstitution getNextFoundInstitution() {
        return nextFoundInstitution;
    }
}

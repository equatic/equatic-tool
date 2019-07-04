package be.ugent.equatic.web.util;

import be.ugent.equatic.domain.Institution;

public class FoundInstitution {

    private Institution institution;
    private FoundInstitutionBy by;

    public FoundInstitution(Institution institution, FoundInstitutionBy by) {
        this.institution = institution;
        this.by = by;
    }

    public FoundInstitution compareWithNextFoundInstitution(Institution nextFoundInstitution, FoundInstitutionBy by)
            throws InstitutionsNotMatchException {
        if (institution == null) {
            return new FoundInstitution(nextFoundInstitution, by);
        } else if (nextFoundInstitution == null || institution.equals(nextFoundInstitution)) {
            return this;
        } else {
            throw new InstitutionsNotMatchException(this, new FoundInstitution(nextFoundInstitution, by));
        }
    }

    public Institution getInstitution() {
        return institution;
    }

    public FoundInstitutionBy getBy() {
        return by;
    }
}

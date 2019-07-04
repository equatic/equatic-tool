package be.ugent.equatic.web.util;

import be.ugent.equatic.domain.Institution;

public class InstitutionDuplicatedException extends Throwable {

    private Institution institution;

    public InstitutionDuplicatedException(Institution institution) {
        this.institution = institution;
    }

    public SearchedInstitution getSearchedInstitution() {
        return new SearchedInstitution(institution.getPic(), institution.getErasmusCode(), institution.getName(),
                institution.getCountryCode());
    }
}

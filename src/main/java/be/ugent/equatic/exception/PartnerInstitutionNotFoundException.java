package be.ugent.equatic.exception;

import be.ugent.equatic.web.util.SearchedInstitution;

public class PartnerInstitutionNotFoundException extends Exception {

    private SearchedInstitution searchedInstitution;

    public PartnerInstitutionNotFoundException(SearchedInstitution searchedInstitution) {
        this.searchedInstitution = searchedInstitution;
    }

    public SearchedInstitution getSearchedInstitution() {
        return searchedInstitution;
    }
}

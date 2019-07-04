package be.ugent.equatic.exception;

import be.ugent.equatic.web.util.SearchedInstitution;

public class DataForInstitutionAlreadyUploadedException extends Exception {

    private SearchedInstitution searchedInstitution;

    public DataForInstitutionAlreadyUploadedException(SearchedInstitution searchedInstitution) {
        this.searchedInstitution = searchedInstitution;
    }

    public SearchedInstitution getSearchedInstitution() {
        return searchedInstitution;
    }
}

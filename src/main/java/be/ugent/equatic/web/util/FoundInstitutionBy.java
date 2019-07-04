package be.ugent.equatic.web.util;

public enum FoundInstitutionBy {
    PIC("PIC"),
    ERASMUS_CODE("Erasmus code"),
    NAME_AND_COUNTRY_CODE("name and country code"),
    NAME_EN_AND_COUNTRY_CODE("English name and country code"),
    URL("URL");

    private final String description;

    FoundInstitutionBy(String description) {
        this.description = description;
    }

    public String toString() {
        return description;
    }
}

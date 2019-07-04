package be.ugent.equatic.exception;

public class CountryNotFoundException extends Exception {

    private String countryCode;

    public CountryNotFoundException(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }
}

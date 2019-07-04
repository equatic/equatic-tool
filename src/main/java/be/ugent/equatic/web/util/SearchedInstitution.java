package be.ugent.equatic.web.util;

import org.springframework.context.MessageSource;

import java.util.Locale;

public class SearchedInstitution {

    private String pic;
    private String erasmusCode;
    private String legalName;
    private String countryCode;

    public SearchedInstitution(String pic, String erasmusCode, String legalName, String countryCode) {
        this.pic = pic;
        this.erasmusCode = erasmusCode;
        this.legalName = legalName;
        this.countryCode = countryCode;
    }

    public String getDetails(MessageSource messageSource, Locale locale) {
        return messageSource.getMessage("equatic.pic", null, locale) + ": " + pic + ", " +
                messageSource.getMessage("equatic.erasmusCode", null, locale) + ": " + erasmusCode + ", " +
                messageSource.getMessage("equatic.legalName", null, locale) + ": " + legalName + ", " +
                messageSource.getMessage("equatic.countryCode", null, locale) + ": " + countryCode;
    }

    public String getDetails(MessageSource messageSource) {
        return getDetails(messageSource, Locale.getDefault());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchedInstitution that = (SearchedInstitution) o;

        if (pic != null ? !pic.equals(that.pic) : that.pic != null) return false;
        if (erasmusCode != null ? !erasmusCode.equals(that.erasmusCode) : that.erasmusCode != null) return false;
        if (legalName != null ? !legalName.equals(that.legalName) : that.legalName != null) return false;
        return !(countryCode != null ? !countryCode.equals(that.countryCode) : that.countryCode != null);

    }

    @Override
    public int hashCode() {
        int result = pic != null ? pic.hashCode() : 0;
        result = 31 * result + (erasmusCode != null ? erasmusCode.hashCode() : 0);
        result = 31 * result + (legalName != null ? legalName.hashCode() : 0);
        result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0);
        return result;
    }

    public String getPic() {
        return pic;
    }

    public String getErasmusCode() {
        return erasmusCode;
    }

    public String getLegalName() {
        return legalName;
    }

    public String getCountryCode() {
        return countryCode;
    }
}

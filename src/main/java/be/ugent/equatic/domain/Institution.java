package be.ugent.equatic.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "institutions")
public class Institution implements Serializable, Comparable {

    @Id
    @SequenceGenerator(name = "institutions_seq_gen", sequenceName = "institutions_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "institutions_seq_gen")
    private Long id;

    private String pic;

    @Size(max = 20)
    private String erasmusCode;

    /**
     * Institution name in local language.
     */
    @NotEmpty(message = "{Institution.name.NotEmpty}")
    @Size(max = 200)
    private String name;

    /**
     * Institution name in English.
     */
    @Size(max = 200)
    private String nameEn;

    @URL
    @Size(max = 200)
    private String url;

    @ManyToOne
    @JoinColumn(name = "country_code")
    private Country country;

    /**
     * Entity ID used for SAML federated sign in (e.g. http://idp.ssocircle.com).
     */
    private String idpEntityId;

    /**
     * URL for the SAML entity metadata (e.g. https://idp.ssocircle.com/idp-meta.xml).
     */
    private String idpMetadataUrl;

    @OneToMany(mappedBy = "institution")
    private List<User> users;

    private String displayName;

    private boolean virtual = false;

    /**
     * Is institution using federated IdP for sign in?
     */
    @Transient
    private boolean withFederatedIdP;

    public Institution() {
    }

    public Institution(String pic, String erasmusCode, String name, String nameEn, String url, Country country) {
        this.pic = pic;
        this.erasmusCode = erasmusCode;
        this.name = name;
        this.nameEn = nameEn;
        this.url = url;
        this.country = country;

        updateDisplayName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Institution that = (Institution) o;

        if (pic != null && pic.equals(that.pic)) {
            return true;
        }
        if (erasmusCode != null && erasmusCode.equals(that.erasmusCode)) {
            return true;
        }
        if (name != null && country != null && name.equals(that.name) && country.equals(that.country)) {
            return true;
        }
        if (nameEn != null && country != null && nameEn.equals(that.nameEn) && country.equals(that.country)) {
            return true;
        }
        if (url != null && url.equals(that.url)) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = pic != null ? pic.hashCode() : 0;
        result = 31 * result + (erasmusCode != null ? erasmusCode.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (nameEn != null ? nameEn.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Sets the name of the institution that should be displayed on the page.
     * <p>
     * We should display English name first and original name only if it is not present.
     * </p>
     */
    private void updateDisplayName() {
        displayName = (nameEn == null || nameEn.equals("")) ? name : nameEn;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPic() {
        return pic;
    }

    @JsonIgnore
    public String getPicBlank() {
        return pic == null ? "" : pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getErasmusCode() {
        return erasmusCode;
    }

    @JsonIgnore
    public String getErasmusCodeBlank() {
        return erasmusCode == null ? "" : erasmusCode;
    }

    public void setErasmusCode(String erasmusCode) {
        this.erasmusCode = erasmusCode;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public String getNameBlank() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;

        updateDisplayName();
    }

    public String getNameEn() {
        return nameEn;
    }

    @JsonIgnore
    public String getNameEnBlank() {
        return nameEn == null ? "" : nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;

        updateDisplayName();
    }

    public String getUrl() {
        return url;
    }

    @JsonIgnore
    public String getUrlBlank() {
        return url == null ? "" : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Country getCountry() {
        return country;
    }

    public String getCountryCode() {
        return country.getCode();
    }

    @JsonIgnore
    public String getCountryCodeBlank() {
        return country == null ? "" : country.getCode();
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    @JsonIgnore
    public String getIdpEntityId() {
        return idpEntityId;
    }

    public void setIdpEntityId(String idpEntityId) {
        this.idpEntityId = idpEntityId;
    }

    @JsonIgnore
    public String getIdpMetadataUrl() {
        return idpMetadataUrl;
    }

    public void setIdpMetadataUrl(String idpMetadataUrl) {
        this.idpMetadataUrl = idpMetadataUrl;
    }

    public boolean isWithFederatedIdP() {
        String idpEntityId = getIdpEntityId();
        String idpMetadataUrl = getIdpMetadataUrl();

        return idpEntityId != null && !idpEntityId.isEmpty() && idpMetadataUrl != null && !idpMetadataUrl.isEmpty();
    }

    public boolean isVirtual() {
        return virtual;
    }

    public void setVirtual(boolean virtual) {
        this.virtual = virtual;
    }

    @Override
    public int compareTo(Object o) {
        Institution institution = (Institution) o;

        int countryNameCompare = country.getName().compareTo(institution.getCountry().getName());
        if (countryNameCompare == 0) {
            return displayName.compareTo(institution.getDisplayName());
        } else {
            return countryNameCompare;
        }
    }
}

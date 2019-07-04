package be.ugent.equatic.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "countries")
public class Country implements Serializable, Comparable {

    @Id
    private String code;

    private String alternativeCode;

    private String name;

    public Country() {
    }

    public Country(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Country(String code, String name, String alternativeCode) {
        this.code = code;
        this.name = name;
        this.alternativeCode = alternativeCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Country country = (Country) o;

        if (!code.equals(country.code))
            return false;
        return name.equals(country.name);

    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Object o) {
        Country country = (Country) o;

        return name.compareTo(country.getName());
    }
}

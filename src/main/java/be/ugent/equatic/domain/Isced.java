package be.ugent.equatic.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.regex.Pattern;

@Entity
@Table(name = "isceds")
public class Isced {

    @Id
    private String code;

    private String fieldName;

    public String getCode() {
        return code;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isBroadCode() {
        return Pattern.matches("^\\d\\d00$", code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Isced isced = (Isced) o;

        return code.equals(isced.code);

    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return "Isced{" +
                "code='" + code + '\'' +
                '}';
    }

    public String getBroadKey() {
        return code.substring(0, 2);
    }
}

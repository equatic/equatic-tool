package be.ugent.equatic.domain;

import javax.persistence.*;

@Entity
@Table(name = "data_sheet_column_types")
public class DataSheetColumnType {

    @Id
    private String code;

    @Enumerated(EnumType.STRING)
    private DataSheetValueType valueType;

    private boolean isFixedValue;

    private boolean isMultipleChoice;

    public DataSheetColumnType() {
    }

    public DataSheetColumnType(String code, DataSheetValueType valueType, boolean isFixedValue,
                               boolean isMultipleChoice) {
        this.code = code;
        this.valueType = valueType;
        this.isFixedValue = isFixedValue;
        this.isMultipleChoice = isMultipleChoice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSheetColumnType that = (DataSheetColumnType) o;

        if (isFixedValue != that.isFixedValue) return false;
        if (isMultipleChoice != that.isMultipleChoice) return false;
        if (!code.equals(that.code)) return false;
        return valueType == that.valueType;

    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + valueType.hashCode();
        result = 31 * result + (isFixedValue ? 1 : 0);
        result = 31 * result + (isMultipleChoice ? 1 : 0);
        return result;
    }

    public String getCode() {
        return code;
    }

    public DataSheetValueType getValueType() {
        return valueType;
    }

    public boolean isFixedValue() {
        return isFixedValue;
    }

    public boolean isMultipleChoice() {
        return isMultipleChoice;
    }
}

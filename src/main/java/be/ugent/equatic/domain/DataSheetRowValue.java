package be.ugent.equatic.domain;

import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "data_sheet_row_values")
public class DataSheetRowValue {

    private static final int STRING_VALUE_LIMIT = 200;

    @Id
    @SequenceGenerator(name = "data_sheet_row_values_seq_gen", sequenceName = "data_sheet_row_values_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_sheet_row_values_seq_gen")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "row_id")
    private DataSheetRow row;

    @Enumerated(EnumType.STRING)
    private DataSheetColumnCode columnCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_code")
    private DataSheetValueCode valueCode;

    @Column(name = "value")
    private String value;

    private Date dateValue;

    private Double numericValue;

    @ManyToOne
    @JoinColumn(name = "value_code", insertable = false, updatable = false)
    private DataSheetValueParams valueParams;

    public DataSheetRowValue() {
    }

    public DataSheetRowValue(DataSheetRow row, DataSheetColumnCode columnCode, DataSheetValueCode valueCode) {
        this.row = row;
        this.columnCode = columnCode;
        this.valueCode = valueCode;
    }

    public DataSheetRowValue(DataSheetRow row, DataSheetColumnCode columnCode, String value) {
        this.row = row;
        this.columnCode = columnCode;
        this.value = StringUtils.substring(value, 0, STRING_VALUE_LIMIT);
    }

    public DataSheetRowValue(DataSheetRow row, DataSheetColumnCode columnCode, Date dateValue) {
        this.row = row;
        this.columnCode = columnCode;
        this.dateValue = dateValue;
    }

    public DataSheetRowValue(DataSheetRow row, DataSheetColumnCode columnCode, Double numericValue) {
        this.row = row;
        this.columnCode = columnCode;
        this.numericValue = numericValue;
    }

    public DataSheetRow getRow() {
        return row;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DataSheetRowValue rowValue = (DataSheetRowValue) o;

        if (columnCode != rowValue.columnCode)
            return false;
        if (valueCode != rowValue.valueCode)
            return false;
        if (value != null ? !value.equals(rowValue.value) : rowValue.value != null)
            return false;
        if (dateValue != null ? !dateValue.equals(rowValue.dateValue) : rowValue.dateValue != null)
            return false;
        return numericValue != null ? numericValue.equals(rowValue.numericValue) : rowValue.numericValue == null;
    }

    @Override
    public int hashCode() {
        int result = columnCode.hashCode();
        result = 31 * result + (valueCode != null ? valueCode.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (dateValue != null ? dateValue.hashCode() : 0);
        result = 31 * result + (numericValue != null ? numericValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DataSheetRowValue{" +
                "columnCode=" + columnCode +
                ", valueCode=" + valueCode +
                '}';
    }

    public DataSheetColumnCode getColumnCode() {
        return columnCode;
    }

    public DataSheetValueCode getValueCode() {
        return valueCode;
    }

    public String getValue() {
        return value;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public Double getNumericValue() {
        return numericValue;
    }
}

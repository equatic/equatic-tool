package be.ugent.equatic.domain;

import com.google.common.base.CaseFormat;

import javax.persistence.*;

@Entity
@Table(name = "data_sheet_columns")
public class DataSheetColumn {

    @Id
    @SequenceGenerator(name = "data_sheet_columns_seq_gen", sequenceName = "data_sheet_columns_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_sheet_columns_seq_gen")
    private Long id;

    @Enumerated(EnumType.STRING)
    private DataSheetColumnCode code;

    @ManyToOne
    @JoinColumn(name = "data_sheet_code")
    private DataSheet dataSheet;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "type")
    private DataSheetColumnType type;

    private String title;

    public DataSheetColumn() {
    }

    public DataSheetColumn(DataSheetColumnCode code, DataSheet dataSheet, DataSheetColumnType type, String title) {
        this.code = code;
        this.dataSheet = dataSheet;
        this.type = type;
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSheetColumn that = (DataSheetColumn) o;

        if (!code.equals(that.code)) return false;
        if (!dataSheet.equals(that.dataSheet)) return false;
        if (!type.equals(that.type)) return false;
        return title.equals(that.title);

    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + dataSheet.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + title.hashCode();
        return result;
    }

    public Long getId() {
        return id;
    }

    public DataSheetColumnCode getCode() {
        return code;
    }

    public DataSheet getDataSheet() {
        return dataSheet;
    }

    public DataSheetColumnType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getPropertyName() {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, code.name());
    }

    public String getMethod() {
        return "get" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, code.name());
    }

    public boolean isOptional() {
        return code.isOptional();
    }
}

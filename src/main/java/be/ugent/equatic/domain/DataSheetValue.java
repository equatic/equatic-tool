package be.ugent.equatic.domain;

import javax.persistence.*;

@Entity
@Table(name = "data_sheet_values")
public class DataSheetValue {

    @Id
    @SequenceGenerator(name = "data_sheet_values_seq_gen", sequenceName = "data_sheet_values_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_sheet_values_seq_gen")
    private Long id;

    @Enumerated(EnumType.STRING)
    private DataSheetValueCode code;

    @ManyToOne
    @JoinColumn(name = "column_type")
    private DataSheetColumnType columnType;

    private String value;

    public Long getId() {
        return id;
    }

    public DataSheetValueCode getCode() {
        return code;
    }

    public DataSheetColumnType getColumnType() {
        return columnType;
    }

    public String getValue() {
        return value;
    }
}

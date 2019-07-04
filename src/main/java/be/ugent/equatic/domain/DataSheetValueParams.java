package be.ugent.equatic.domain;

import javax.persistence.*;

@Entity
@Table(name = "data_sheet_value_codes")
public class DataSheetValueParams {

    @Id
    @Enumerated(EnumType.STRING)
    private DataSheetValueCode code;

    private Integer ord;

    private Integer defaultValue;
}

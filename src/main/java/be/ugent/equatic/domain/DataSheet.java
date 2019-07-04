package be.ugent.equatic.domain;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import be.ugent.equatic.exception.DataSheetColumnNotFoundException;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "data_sheets")
public class DataSheet {

    @Id
    @Enumerated(EnumType.STRING)
    private DataSheetCode code;

    @OneToMany(mappedBy = "dataSheet", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    @Fetch(value = FetchMode.SUBSELECT)
    private List<DataSheetColumn> columns = new ArrayList<>();

    public DataSheet() {
    }

    public DataSheet(DataSheetCode code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DataSheet dataSheet = (DataSheet) o;

        return code == dataSheet.code;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return code.name();
    }

    public DataSheetCode getCode() {
        return code;
    }

    /**
     * @return list of *ALL* columns for data sheet (it may also contain previous columns with old titles!)
     */
    private List<DataSheetColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<DataSheetColumn> columns) {
        this.columns = columns;
    }

    public DataSheetColumn getColumnByCode(DataSheetColumnCode code) throws DataSheetColumnNotFoundException {
        for (DataSheetColumn column : columns) {
            if (column.getCode().equals(code)) {
                return column;
            }
        }
        throw new DataSheetColumnNotFoundException(code);
    }

    public String[] getCurrentColumnTitles() {
        return getCurrentColumns().stream().map(DataSheetColumn::getTitle).toArray(String[]::new);
    }

    public int getColumnsCount() {
        return getCurrentColumns().size();
    }

    public boolean isOnlyOneRowForInstitution() {
        return Collections.singletonList(DataSheetCode.RANKINGS).contains(code);
    }

    public Map<DataSheetColumnCode, List<DataSheetColumn>> getColumnsMapByCodes() {
        Map<DataSheetColumnCode, List<DataSheetColumn>> columnsMapByCodes = new HashMap<>();
        for (DataSheetColumn column : getColumns()) {
            DataSheetColumnCode columnCode = column.getCode();

            if (!columnsMapByCodes.containsKey(columnCode)) {
                columnsMapByCodes.put(columnCode, new ArrayList<>());
            }

            columnsMapByCodes.get(columnCode).add(column);
        }

        return columnsMapByCodes;
    }

    /**
     * @return list of columns that are currently in use (with the most recent title)
     */
    public List<DataSheetColumn> getCurrentColumns() {
        return getColumnsMapByCodes().values().stream().map(DataSheet::getCurrentColumn).collect(Collectors.toList());
    }

    public static DataSheetColumn getCurrentColumn(List<DataSheetColumn> dataSheetColumns) {
        return dataSheetColumns.get(dataSheetColumns.size() - 1);
    }

    /**
     * See EQUAT-404.
     */
    public boolean isIscedRequired() {
        return getCode() == DataSheetCode.INSTITUTIONAL_AGREEMENTS;
    }
}

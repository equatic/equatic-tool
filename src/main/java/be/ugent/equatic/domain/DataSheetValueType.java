package be.ugent.equatic.domain;

import org.apache.poi.ss.usermodel.CellType;

public enum DataSheetValueType {
    NUMERIC(CellType.NUMERIC),
    STRING(CellType.STRING),
    DATE(CellType.STRING);

    private final CellType cellType;

    DataSheetValueType(final CellType cellType) {
        this.cellType = cellType;
    }

    public CellType getCellType() {
        return cellType;
    }
}

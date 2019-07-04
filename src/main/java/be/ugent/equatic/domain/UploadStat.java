package be.ugent.equatic.domain;

public class UploadStat {

    private DataSheet dataSheet;
    private Long rowsCount;
    private Long institutionsCount;

    public UploadStat(DataSheet dataSheet, Long rows, Long institutions) {
        this.dataSheet = dataSheet;
        this.rowsCount = rows;
        this.institutionsCount = institutions;
    }

    public DataSheet getDataSheet() {
        return dataSheet;
    }

    public Long getRowsCount() {
        return rowsCount;
    }

    public Long getInstitutionsCount() {
        return institutionsCount;
    }
}

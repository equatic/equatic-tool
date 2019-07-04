package be.ugent.equatic.domain;

import javax.persistence.*;

@Entity
@Table(name = "data_sheet_upload_errors")
public class DataSheetUploadError implements Comparable {

    @Id
    @SequenceGenerator(name = "data_sheet_upload_errors_seq_gen", sequenceName = "data_sheet_upload_errors_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_sheet_upload_errors_seq_gen")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "upload_id")
    private DataSheetUpload upload;

    @ManyToOne
    @JoinColumn(name = "column_id")
    private DataSheetColumn column;

    private Integer rowNum;

    private String cellValue;

    private String errorDesc;

    public DataSheetUploadError() {
    }

    public DataSheetUploadError(DataSheetUpload upload, DataSheetColumn column, Integer rowNum, String cellValue,
                                String errorDesc) {
        this.upload = upload;
        this.column = column;
        this.rowNum = rowNum;
        this.cellValue = cellValue;
        this.errorDesc = errorDesc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSheetUploadError that = (DataSheetUploadError) o;

        if (column != null ? !column.equals(that.column) : that.column != null) return false;
        if (rowNum != null ? !rowNum.equals(that.rowNum) : that.rowNum != null) return false;
        if (cellValue != null ? !cellValue.equals(that.cellValue) : that.cellValue != null) return false;
        return errorDesc.equals(that.errorDesc);

    }

    @Override
    public int hashCode() {
        int result = column != null ? column.hashCode() : 0;
        result = 31 * result + (rowNum != null ? rowNum.hashCode() : 0);
        result = 31 * result + (cellValue != null ? cellValue.hashCode() : 0);
        result = 31 * result + errorDesc.hashCode();
        return result;
    }

    @Override
    public int compareTo(Object o) {
        DataSheetUploadError that = (DataSheetUploadError) o;

        return rowNum - that.rowNum;
    }

    @Override
    public String toString() {
        return "DataSheetUploadError{" +
                "column=" + column +
                ", rowNum=" + rowNum +
                ", cellValue='" + cellValue + '\'' +
                ", errorDesc='" + errorDesc + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public DataSheetUpload getUpload() {
        return upload;
    }

    public DataSheetColumn getColumn() {
        return column;
    }

    public Integer getRowNum() {
        return rowNum;
    }

    public String getCellValue() {
        return cellValue;
    }

    public String getErrorDesc() {
        return errorDesc;
    }
}

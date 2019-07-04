package be.ugent.equatic.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "data_sheet_rows")
public class DataSheetRow {

    @Id
    @SequenceGenerator(name = "data_sheet_rows_seq_gen", sequenceName = "data_sheet_rows_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_sheet_rows_seq_gen")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "academic_year")
    private AcademicYear academicYear;

    @ManyToOne
    @JoinColumn(name = "inst_id")
    private Institution institution;

    @ManyToOne
    @JoinColumn(name = "data_sheet_code")
    private DataSheet dataSheet;

    @ManyToOne
    @JoinColumn(name = "partner_inst_id")
    private Institution partnerInstitution;

    @ManyToOne
    @JoinColumn(name = "isced_code")
    private Isced isced;

    @OneToMany(mappedBy = "row", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<DataSheetRowValue> values = new HashSet<>();

    public DataSheetRow() {
    }

    public DataSheetRow(AcademicYear academicYear, Institution institution, DataSheet dataSheet,
                        Institution partnerInstitution, Isced isced) {
        this.academicYear = academicYear;
        this.institution = institution;
        this.dataSheet = dataSheet;
        this.partnerInstitution = partnerInstitution;
        this.isced = isced;
    }

    public Long getId() {
        return id;
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public Institution getInstitution() {
        return institution;
    }

    public DataSheet getDataSheet() {
        return dataSheet;
    }

    public Institution getPartnerInstitution() {
        return partnerInstitution;
    }

    public Isced getIsced() {
        return isced;
    }

    public Set<DataSheetRowValue> getValues() {
        return values;
    }

    public void addRowValue(DataSheetRowValue rowValue) {
        values.add(rowValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSheetRow that = (DataSheetRow) o;

        if (!academicYear.equals(that.academicYear)) return false;
        if (institution != null ? !institution.equals(that.institution) : that.institution != null) return false;
        if (!dataSheet.equals(that.dataSheet)) return false;
        if (partnerInstitution != null ? !partnerInstitution.equals(
                that.partnerInstitution) : that.partnerInstitution != null) return false;
        if (isced != null ? !isced.equals(that.isced) : that.isced != null) return false;
        return values.equals(that.values);

    }

    @Override
    public int hashCode() {
        int result = academicYear.hashCode();
        result = 31 * result + (institution != null ? institution.hashCode() : 0);
        result = 31 * result + dataSheet.hashCode();
        result = 31 * result + (partnerInstitution != null ? partnerInstitution.hashCode() : 0);
        result = 31 * result + (isced != null ? isced.hashCode() : 0);
        result = 31 * result + values.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DataSheetRow{" +
                "academicYear=" + academicYear +
                ", institution=" + institution +
                ", dataSheet=" + dataSheet +
                ", partnerInstitution=" + partnerInstitution +
                ", isced=" + isced +
                ", values=" + values +
                '}';
    }
}

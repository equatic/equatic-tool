package be.ugent.equatic.domain;

import javax.persistence.*;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@Table(name = "data_sheet_uploads")
public class DataSheetUpload {

    @Id
    @SequenceGenerator(name = "data_sheet_uploads_seq_gen", sequenceName = "data_sheet_uploads_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_sheet_uploads_seq_gen")
    private Long id;

    private Date uploadDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "inst_id")
    private Institution institution;

    @ManyToOne
    @JoinColumn(name = "data_sheet_code")
    private DataSheet dataSheet;

    @OneToMany(mappedBy = "upload", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("row_num ASC")
    private SortedSet<DataSheetUploadError> errors = new TreeSet<>();

    public DataSheetUpload() {
    }

    public DataSheetUpload(User user, Institution institution, DataSheet dataSheet) {
        this.user = user;
        this.institution = institution;
        this.dataSheet = dataSheet;
    }

    @PrePersist
    protected void onCreate() {
        if (uploadDate == null) {
            uploadDate = new Date();
        }
    }

    public Long getId() {
        return id;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public User getUser() {
        return user;
    }

    public DataSheet getDataSheet() {
        return dataSheet;
    }

    public SortedSet<DataSheetUploadError> getErrors() {
        return errors;
    }

    public void addError(String errorDesc, DataSheetColumn column, Integer rowNum, String cellValue) {
        errors.add(new DataSheetUploadError(this, column, rowNum, cellValue, errorDesc));
    }
}

package be.ugent.equatic.web.user.national;

import be.ugent.equatic.domain.AcademicYear;

public class NationalUserReportRow {

    private String institutionToDisplayName;
    private String institutionFromDisplayName;
    private Integer outgoingStudents;
    private Integer incomingStudents;
    private Integer outgoingStaff;
    private Integer incomingStaff;
    private String broadIsced;
    private String narrowIsced;
    private String uploadMinAcademicYear;
    private String uploadMaxAcademicYear;

    public NationalUserReportRow() {
    }

    public String getInstitutionToDisplayName() {
        return institutionToDisplayName;
    }

    public void setInstitutionToDisplayName(String institutionToDisplayName) {
        this.institutionToDisplayName = institutionToDisplayName;
    }

    public String getInstitutionFromDisplayName() {
        return institutionFromDisplayName;
    }

    public void setInstitutionFromDisplayName(String institutionFromDisplayName) {
        this.institutionFromDisplayName = institutionFromDisplayName;
    }

    public Integer getOutgoingStudents() {
        return outgoingStudents;
    }

    public void setOutgoingStudents(Integer outgoingStudents) {
        this.outgoingStudents = outgoingStudents;
    }

    public Integer getIncomingStudents() {
        return incomingStudents;
    }

    public void setIncomingStudents(Integer incomingStudents) {
        this.incomingStudents = incomingStudents;
    }

    public Integer getOutgoingStaff() {
        return outgoingStaff;
    }

    public void setOutgoingStaff(Integer outgoingStaff) {
        this.outgoingStaff = outgoingStaff;
    }

    public Integer getIncomingStaff() {
        return incomingStaff;
    }

    public void setIncomingStaff(Integer incomingStaff) {
        this.incomingStaff = incomingStaff;
    }

    public String getBroadIsced() {
        return broadIsced;
    }

    public void setBroadIsced(String broadIsced) {
        this.broadIsced = broadIsced;
    }

    public String getNarrowIsced() {
        return narrowIsced;
    }

    public void setNarrowIsced(String narrowIsced) {
        this.narrowIsced = narrowIsced;
    }

    public String getUploadMinAcademicYear() {
        return uploadMinAcademicYear;
    }

    public void setUploadMinAcademicYear(String uploadMinAcademicYear) {
        this.uploadMinAcademicYear = uploadMinAcademicYear;
    }

    private String getUploadMaxAcademicYear() {
        return uploadMaxAcademicYear;
    }

    public void setUploadMaxAcademicYear(String uploadMaxAcademicYear) {
        this.uploadMaxAcademicYear = uploadMaxAcademicYear;
    }

    public String getDataRange() {
        AcademicYear minAcademicYear = new AcademicYear(getUploadMinAcademicYear());
        AcademicYear maxAcademicYear = new AcademicYear(getUploadMaxAcademicYear());

        return String.format("%d-%d", minAcademicYear.getStartYear(), maxAcademicYear.getStartYear() + 1);
    }
}

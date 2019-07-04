package be.ugent.equatic.web.util;

import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.domain.Isced;
import be.ugent.equatic.indicator.IndicatorCode;

import java.util.List;

public class InstitutionsScoresOptions implements AcademicYearsOption {

    private AcademicYear academicYearFrom;
    private AcademicYear academicYearTo;
    private List<IndicatorCode> indicatorCodes;
    private List<Isced> isceds;
    private List<Institution> institutions;
    private InstitutionsScoresMode mode;

    public InstitutionsScoresOptions() {
    }

    public InstitutionsScoresOptions(AcademicYear academicYearFrom, AcademicYear academicYearTo,
                                     List<IndicatorCode> indicatorCodes,
                                     List<Isced> isceds,
                                     List<Institution> institutions,
                                     InstitutionsScoresMode mode) {
        this.academicYearFrom = academicYearFrom;
        this.academicYearTo = academicYearTo;
        this.indicatorCodes = indicatorCodes;
        this.isceds = isceds;
        this.institutions = institutions;
        this.mode = mode;
    }

    public AcademicYear getAcademicYearFrom() {
        return academicYearFrom;
    }

    public void setAcademicYearFrom(AcademicYear academicYearFrom) {
        this.academicYearFrom = academicYearFrom;
    }

    public AcademicYear getAcademicYearTo() {
        return academicYearTo;
    }

    public void setAcademicYearTo(AcademicYear academicYearTo) {
        this.academicYearTo = academicYearTo;
    }

    public List<IndicatorCode> getIndicatorCodes() {
        return indicatorCodes;
    }

    public void setIndicatorCodes(List<IndicatorCode> indicatorCodes) {
        this.indicatorCodes = indicatorCodes;
    }

    /**
     * Returns null rather then empty array as an empty array would cause problems as an SQL parameter (see EQUAT-320).
     */
    public List<Isced> getIsceds() {
        return isceds == null || isceds.isEmpty() ? null : isceds;
    }

    public void setIsceds(List<Isced> isceds) {
        this.isceds = isceds;
    }

    /**
     * Returns null rather then empty array as an empty array would cause problems as an SQL parameter (see EQUAT-320).
     */
    public List<Institution> getInstitutions() {
        return institutions == null || institutions.isEmpty() ? null : institutions;
    }

    public void setInstitutions(List<Institution> institutions) {
        this.institutions = institutions;
    }

    public InstitutionsScoresMode getMode() {
        return mode;
    }

    public void setMode(InstitutionsScoresMode mode) {
        this.mode = mode;
    }
}

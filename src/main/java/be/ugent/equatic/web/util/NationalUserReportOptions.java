package be.ugent.equatic.web.util;

import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.Country;

public class NationalUserReportOptions implements AcademicYearsOption {

    private AcademicYear academicYearFrom;
    private AcademicYear academicYearTo;
    private Country countryChosen;

    public NationalUserReportOptions() {
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

    public Country getCountryChosen() {
        return countryChosen;
    }

    public void setCountryChosen(Country countryChosen) {
        this.countryChosen = countryChosen;
    }
}

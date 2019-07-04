package be.ugent.equatic.web.util;

import be.ugent.equatic.domain.AcademicYear;

public interface AcademicYearsOption {

    AcademicYear getAcademicYearFrom();

    AcademicYear getAcademicYearTo();

    void setAcademicYearFrom(AcademicYear academicYear);

    void setAcademicYearTo(AcademicYear academicYear);
}

package be.ugent.equatic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.web.util.AcademicYearsOption;

@Service
public class AcademicYearsOptionService {

    @Autowired
    private AcademicYearService academicYearService;

    public void initializeAcademicYearsOption(AcademicYearsOption options) {
        AcademicYear academicYearFrom = options.getAcademicYearFrom();
        AcademicYear academicYearTo = options.getAcademicYearTo();
        if (academicYearFrom == null) {
            academicYearFrom = academicYearService.findNearest5YearsRange();
            options.setAcademicYearFrom(academicYearFrom);
        }
        if (academicYearTo == null) {
            academicYearTo = academicYearService.findCurrent();
            options.setAcademicYearTo(academicYearTo);
        }
    }
}

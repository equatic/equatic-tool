package be.ugent.equatic.web.util;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import be.ugent.equatic.domain.AcademicYear;

@Service
public class AcademicYearsOptionValidator implements Validator {

    private static final int MAXIMUM_RANGE = 5;

    @Override
    public boolean supports(Class<?> clazz) {
        return AcademicYearsOption.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AcademicYearsOption academicYearsOption = (AcademicYearsOption) target;

        AcademicYear academicYearFrom = academicYearsOption.getAcademicYearFrom();
        AcademicYear academicYearTo = academicYearsOption.getAcademicYearTo();

        int difference = academicYearTo.subtract(academicYearFrom);
        if (difference < 0) {
            errors.rejectValue("academicYearFrom", "InstitutionsScoresOptions.fromBeforeTo");
        } else if (difference >= MAXIMUM_RANGE) {
            errors.rejectValue("academicYearTo", "InstitutionsScoresOptions.fromToDifference", new Integer[]{MAXIMUM_RANGE},
                    "Year range exceeded");
        }
    }
}

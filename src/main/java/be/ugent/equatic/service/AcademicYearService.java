package be.ugent.equatic.service;

import com.google.common.collect.Iterables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import be.ugent.equatic.domain.AcademicYear;
import be.ugent.equatic.domain.AcademicYearRepository;

import java.util.Arrays;
import java.util.List;

@Service
public class AcademicYearService {

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Transactional(readOnly = true)
    public List<AcademicYear> findAll() {
        return academicYearRepository.findAllByOrderByAcademicYearDesc();
    }

    @Transactional(readOnly = true)
    public AcademicYear findCurrent() {
        return academicYearRepository.findTop1ByOrderByAcademicYearDesc();
    }

    @Transactional(readOnly = true)
    public AcademicYear findNearest5YearsRange() {
        List<AcademicYear> top5academicYears = academicYearRepository.findTop5ByOrderByAcademicYearDesc();
        return Iterables.getLast(top5academicYears);
    }

    @Transactional
    public AcademicYear createNextAcademicYear() {
        AcademicYear currentAcademicYear = this.findCurrent();

        return new AcademicYear(currentAcademicYear.getStartYear() + 1);
    }

    @Transactional
    public void save(AcademicYear... academicYears) {
        academicYearRepository.save(Arrays.asList(academicYears));
    }

    @Transactional
    public void deleteAll() {
        academicYearRepository.deleteAll();
    }

    public List<AcademicYear> findYearBetween(AcademicYear academicYearFrom, AcademicYear academicYearTo) {
        return academicYearRepository.findByAcademicYearBetween(academicYearFrom.getAcademicYear(),
                academicYearTo.getAcademicYear());
    }
}

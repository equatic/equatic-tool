package be.ugent.equatic.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, String> {

    List<AcademicYear> findAllByOrderByAcademicYearDesc();

    AcademicYear findTop1ByOrderByAcademicYearDesc();

    List<AcademicYear> findTop5ByOrderByAcademicYearDesc();

    List<AcademicYear> findByAcademicYearBetween(String academicYearFrom, String academicYearTo);
}

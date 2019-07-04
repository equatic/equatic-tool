package be.ugent.equatic.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "academic_years")
public class AcademicYear {

    @Id
    private String academicYear;

    public AcademicYear() {
    }

    public AcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public AcademicYear(int academicYearStart) {
        this.academicYear = String.format("%s-%s", academicYearStart, academicYearStart + 1);
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    /**
     * Returns the starting year of the academic year.
     *
     * @return the specified year
     */
    public int getStartYear() {
        return Integer.parseInt(academicYear.substring(0, 4));
    }

    /**
     * Returns the result of subtracting starting years of the academic years.
     *
     * @param subtrahend the subtrahend
     * @return the specified result
     */
    public int subtract(AcademicYear subtrahend) {
        return getStartYear() - subtrahend.getStartYear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AcademicYear that = (AcademicYear) o;

        return academicYear.equals(that.academicYear);

    }

    @Override
    public int hashCode() {
        return academicYear.hashCode();
    }

    @Override
    public String toString() {
        return academicYear;
    }
}

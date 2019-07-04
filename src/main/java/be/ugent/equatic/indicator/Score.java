package be.ugent.equatic.indicator;

public class Score {

    private Double value;
    private Long studentsCount;
    private Double standardError;

    public Score(Double value, Long studentsCount, Double standardError) {
        this.value = value;
        this.studentsCount = studentsCount;
        this.standardError = standardError;
    }

    public Score(Double value) {
        this.value = value;
    }

    public Double getValue() {
        return value;
    }

    public Long getStudentsCount() {
        return studentsCount;
    }

    public Double getStandardError() {
        return standardError;
    }
}

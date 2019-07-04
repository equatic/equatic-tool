package be.ugent.equatic.domain;

import be.ugent.equatic.indicator.IndicatorCode;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "score_interpretations")
public class ScoreInterpretation implements Serializable {

    private static final int DEFAULT_FAIR_SCORE_MIN = 50;
    private static final int DEFAULT_GOOD_SCORE_MIN = 65;
    private static final int MIN_SCORE_THRESHOLD = 1;
    private static final int MAX_SCORE_THRESHOLD = 99;

    @Id
    @SequenceGenerator(name = "score_interp_seq_gen", sequenceName = "score_interp_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "score_interp_seq_gen")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inst_id")
    @NotNull(message = "{User.institution.NotNull}")
    private Institution institution;

    @Min(message = "{ScoreInterpretation.scoreThreshold.tooLow}", value = MIN_SCORE_THRESHOLD)
    @Max(message = "{ScoreInterpretation.scoreThreshold.tooHigh}", value = MAX_SCORE_THRESHOLD)
    private Integer fairScoreMin;

    @Min(message = "{ScoreInterpretation.scoreThreshold.tooLow}", value = MIN_SCORE_THRESHOLD)
    @Max(message = "{ScoreInterpretation.scoreThreshold.tooHigh}", value = MAX_SCORE_THRESHOLD)
    private Integer goodScoreMin;

    @NotNull
    private IndicatorCode indicatorCode;

    public ScoreInterpretation(Institution institution, IndicatorCode indicatorCode) {
        this.institution = institution;
        this.fairScoreMin = DEFAULT_FAIR_SCORE_MIN;
        this.goodScoreMin = DEFAULT_GOOD_SCORE_MIN;
        this.indicatorCode = indicatorCode;
    }

    public ScoreInterpretation() {

    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public void setFairScoreMin(Integer fairScoreMin) {
        this.fairScoreMin = fairScoreMin;
    }

    public void setGoodScoreMin(Integer goodScoreMin) {
        this.goodScoreMin = goodScoreMin;
    }

    public void setIndicatorCode(IndicatorCode indicatorCode) {
        this.indicatorCode = indicatorCode;
    }

    public Long getId() {
        return id;
    }

    public Institution getInstitution() {
        return institution;
    }

    public Integer getFairScoreMin() {
        return fairScoreMin;
    }

    public Integer getGoodScoreMin() {
        return goodScoreMin;
    }

    public IndicatorCode getIndicatorCode() {
        return indicatorCode;
    }
}

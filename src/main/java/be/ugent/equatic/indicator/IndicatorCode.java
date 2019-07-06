package be.ugent.equatic.indicator;

public enum IndicatorCode {
    PERFORMANCE_OF_INCOMING_STUDENTS(new PerformanceOfIncomingStudentsIndicator()),
    PERFORMANCE_OF_OUTGOING_STUDENTS(new PerformanceOfOutgoingStudentsIndicator()),
    SUPPORT_AND_FACILITIES(new SupportAndFacilitiesIndicator()),
    ACADEMIC_QUALITY(new AcademicQualityIndicator()),
    RANKINGS(new RankingsIndicator()),
    COURSE_CATALOGUE_INFORMATION(new CourseCatalogueInformationIndicator()),
    EXCHANGE_OF_ECTS_DOCUMENTS(new ExchangeOfEctsDocumentsIndicator()),
    MOBILITY_RATE(new MobilityRateIndicator()),
    INVOLVEMENT(new InvolvementIndicator()),
    EDUCATIONAL_COOPERATION(new EducationalCooperationIndicator());

    private final Indicator indicator;

    IndicatorCode(Indicator indicator) {
        this.indicator = indicator;
    }

    public Indicator getIndicator() {
        return indicator;
    }

    /**
     * Needed by Thymeleaf.
     */
    public String getName() {
        return name();
    }
}

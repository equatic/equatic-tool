package be.ugent.equatic.domain;

public enum DataSheetColumnCode {
    PIC,
    NAME,
    NAME_EN,
    ERASMUS_CODE,
    COUNTRY_CODE,
    LEGAL_NAME,
    URL(true),
    ISCED_CODE(true),
    SMS_SUPPORT_ADMINISTRATIVE,
    SMS_SUPPORT_MENTORING,
    SMS_SUPPORT_INITIATIVES,
    SMS_DEAL_QUESTIONS,
    SMS_FIND_ACCOMMODATION,
    SMS_FACILITIES_CLASSROOM,
    SMS_FACILITIES_STUDY_ROOMS,
    SMS_FACILITIES_LIBRARIES,
    SMS_FACILITIES_ACCESS_PC,
    SMS_FACILITIES_ACCESS_INTERNET,
    SMS_FACILITIES_CAFETERIA,
    SMS_FACILITIES_ACCESS_PUBLICATIONS,
    SMS_QUALITY_CONTENT,
    SMS_QUALITY_TEACHING,
    SMS_QUALITY_LEARNING,
    SMS_LA_SIGNED,
    SMS_LA_NOT_SIGNED,
    SMS_TOR,
    SMS_COURSE_CATALOGUE,
    SMP_DEAL_QUESTIONS,
    SMP_FIND_ACCOMMODATION,
    SMP_QUALITY,
    SMP_LA_SIGNED,
    SMP_LA_NOT_SIGNED,
    SMP_CERTIFICATE,
    SMP_SUPPORT,
    RANKING_AWRU_POSITION,
    RANKING_THE_POSITION,
    RANKING_QS_POSITION,
    START_DATE,
    END_DATE,
    CREDITS_TAKEN,
    CREDITS_COMPLETED,
    GRADUATES_NUMBER,
    PROJECT_NAME;

    private final boolean optional;

    DataSheetColumnCode() {
        optional = false;
    }

    DataSheetColumnCode(boolean optional) {
        this.optional = optional;
    }

    public boolean isOptional() {
        return optional;
    }
}

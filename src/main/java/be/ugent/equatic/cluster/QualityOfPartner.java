package be.ugent.equatic.cluster;

import be.ugent.equatic.indicator.IndicatorCode;

public class QualityOfPartner extends ClusterFromIndicators {

    @Override
    public String getName() {
        return "Quality of partner";
    }

    @Override
    protected IndicatorCode[] indicatorCodes() {
        return new IndicatorCode[]{IndicatorCode.PERFORMANCE_OF_INCOMING_STUDENTS,
                IndicatorCode.PERFORMANCE_OF_OUTGOING_STUDENTS, IndicatorCode.SUPPORT_AND_FACILITIES,
                IndicatorCode.ACADEMIC_QUALITY, IndicatorCode.RANKINGS};
    }
}

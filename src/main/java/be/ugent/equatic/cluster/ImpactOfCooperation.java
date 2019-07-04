package be.ugent.equatic.cluster;

import be.ugent.equatic.indicator.IndicatorCode;

public class ImpactOfCooperation extends ClusterFromIndicators {

    @Override
    public String getName() {
        return "Impact of the cooperation";
    }

    @Override
    protected IndicatorCode[] indicatorCodes() {
        return new IndicatorCode[]{IndicatorCode.MOBILITY_RATE, IndicatorCode.INVOLVEMENT}; // TODO: add missing indicators (see EQUAT-208)
    }
}

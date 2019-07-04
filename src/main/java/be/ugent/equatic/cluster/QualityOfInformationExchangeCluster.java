package be.ugent.equatic.cluster;

import be.ugent.equatic.indicator.IndicatorCode;

public class QualityOfInformationExchangeCluster extends ClusterFromIndicators {

    @Override
    public String getName() {
        return "Quality of the information exchange";
    }

    @Override
    protected IndicatorCode[] indicatorCodes() {
        return new IndicatorCode[]{IndicatorCode.COURSE_CATALOGUE_INFORMATION, IndicatorCode.EXCHANGE_OF_ECTS_DOCUMENTS};
    }
}

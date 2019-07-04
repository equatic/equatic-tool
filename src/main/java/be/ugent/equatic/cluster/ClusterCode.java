package be.ugent.equatic.cluster;

public enum ClusterCode {
    QUALITY_OF_PARTNER(new QualityOfPartner()),
    QUALITY_OF_INFORMATION_EXCHANGE_CLUSTER(new QualityOfInformationExchangeCluster()),
    IMPACT_OF_COOPERATION(new ImpactOfCooperation());

    private final Cluster cluster;

    ClusterCode(Cluster cluster) {
        this.cluster = cluster;
    }

    public Cluster getCluster() {
        return cluster;
    }
}

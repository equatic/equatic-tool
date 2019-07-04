package be.ugent.equatic.web.util;

public enum InstitutionsScoresMode {
    DETAILED("Detailed scores"),
    CLUSTERS("Cluster scores");

    private final String name;

    InstitutionsScoresMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

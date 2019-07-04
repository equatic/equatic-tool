package be.ugent.equatic.util;

public class IscedStat {

    private int broadIscedCount;
    private int narrowIscedCount;

    public IscedStat(int broadIscedCount, int narrowIscedCount) {
        this.broadIscedCount = broadIscedCount;
        this.narrowIscedCount = narrowIscedCount;
    }

    public int getBroadIscedCount() {
        return broadIscedCount;
    }

    public int getNarrowIscedCount() {
        return narrowIscedCount;
    }
}

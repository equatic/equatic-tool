package be.ugent.equatic.util;

public class BroadIscedStat {

    private int minNarrowIscedCount;
    private int maxNarrowIscedCount;

    public BroadIscedStat(Integer minNarrowIscedCount, Integer maxNarrowIscedCount) {
        this.minNarrowIscedCount = minNarrowIscedCount;
        this.maxNarrowIscedCount = maxNarrowIscedCount;
    }

    public int getMinNarrowIscedCount() {
        return minNarrowIscedCount;
    }

    public int getMaxNarrowIscedCount() {
        return maxNarrowIscedCount;
    }
}

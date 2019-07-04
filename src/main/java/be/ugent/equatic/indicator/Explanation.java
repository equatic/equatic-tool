package be.ugent.equatic.indicator;

public class Explanation {

    private String[] paragraphs;
    private String[] shortInfos;
    private boolean showReferenceGroup = true;

    public Explanation(String... paragraphs) {
        this.paragraphs = paragraphs;
    }

    public Explanation(String[] paragraphs, String[] shortInfos) {
        this.paragraphs = paragraphs;
        this.shortInfos = shortInfos;
    }

    void hideReferenceGroup() {
        this.showReferenceGroup = false;
    }

    public String[] getParagraphs() {
        return paragraphs;
    }

    public String[] getShortInfos() {
        return shortInfos;
    }

    public boolean isShowReferenceGroup() {
        return showReferenceGroup;
    }
}

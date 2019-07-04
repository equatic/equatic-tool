package be.ugent.equatic.util;

import be.ugent.equatic.domain.Isced;

import java.util.ArrayList;
import java.util.List;

public class BroadIsced {

    private Isced isced;
    private List<Isced> narrowIsceds = new ArrayList<>();

    public BroadIsced() {
    }

    public void setIsced(Isced isced) {
        this.isced = isced;
    }

    public void addNarrowIsced(Isced narrowIsced) {
        narrowIsceds.add(narrowIsced);
    }

    public Isced getIsced() {
        return isced;
    }

    public List<Isced> getNarrowIsceds() {
        return narrowIsceds;
    }
}

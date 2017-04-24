package com.spentapp.spentvenues.model.interfaces;

/**
 * Created by anton on 4/24/17.
 */

public class ModelEntry {
    private String mName;
    private long mDistance;

    public ModelEntry(String mName, long mDistance) {
        this.mName = mName;
        this.mDistance = mDistance;
    }

    public String getName() {
        return mName;
    }

    public long getDistance() {
        return mDistance;
    }
}

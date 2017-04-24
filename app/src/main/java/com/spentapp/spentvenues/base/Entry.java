package com.spentapp.spentvenues.base;

import io.realm.RealmObject;

/**
 * Created by anton on 4/24/17.
 */

public class Entry {
    private String mName;
    private long mDistance;

    public Entry(String mName, long mDistance) {
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

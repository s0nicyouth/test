package com.spentapp.spentvenues.model;

import io.realm.RealmObject;

/**
 * Created by antonivanov on 24.04.17.
 */

public class DbEntry extends RealmObject {
    private String mName;
    private long mDistance;

    public DbEntry() {}

    DbEntry(String name, long distance) {
        this.mName = name;
        this.mDistance = distance;
    }

    public String getName() {
        return mName;
    }

    public long getDistance() {
        return mDistance;
    }
}

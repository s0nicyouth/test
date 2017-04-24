package com.spentapp.spentvenues.model.interfaces;

import com.spentapp.spentvenues.base.Entry;

import java.util.List;

/**
 * Created by anton on 4/24/17.
 */

public interface ModelInterface {
    interface onDataReady {
        void ready(List<Entry> entries);
    }

    interface onDataWriten {
        void written();
    }

    // Not specified what actually to show, so I only store name and distance to it
    void getEntries(onDataReady callback);
    void updateVenues(double lat, double lon);
    void setDataWrittenCallback(onDataWriten callback);
    boolean isProcessing();
}

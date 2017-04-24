package com.spentapp.spentvenues.views.interfaces;

import com.spentapp.spentvenues.base.Entry;

import java.util.List;

/**
 * Created by anton on 4/24/17.
 */

public interface MainInterface {
    boolean askPermissions();
    void displayList(List<Entry> entries);
    void displayAwait();
    void dismissAwait();
}

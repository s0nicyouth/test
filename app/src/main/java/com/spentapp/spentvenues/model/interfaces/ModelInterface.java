package com.spentapp.spentvenues.model.interfaces;

import java.util.List;

/**
 * Created by anton on 4/24/17.
 */

public interface ModelInterface {
    // Not specified what actually to show, so I only store name and distance to it
    void storeEntries(List<ModelEntry> entries);
}

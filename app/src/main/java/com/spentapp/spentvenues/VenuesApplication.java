package com.spentapp.spentvenues;

import android.app.Application;

import com.spentapp.spentvenues.model.Model;

/**
 * Created by anton on 4/24/17.
 */

public class VenuesApplication extends Application {
    public Model mModel;

    public VenuesApplication() {
        mModel = new Model(this);
    }

    public Model getModel() {
        return mModel;
    }
}

package com.spentapp.spentvenues.presenters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.spentapp.spentvenues.base.Entry;
import com.spentapp.spentvenues.model.interfaces.ModelInterface;
import com.spentapp.spentvenues.presenters.interfaces.MainPresenterInterface;
import com.spentapp.spentvenues.views.interfaces.MainInterface;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by anton on 4/24/17.
 */

public class MainPresenter implements MainPresenterInterface {
    private static final int MIN_UPDATE_TIME_MS = 1000;
    private static final int MIN_UPDATE_DISTANCE_MT = 0;

    private ModelInterface mModel;
    private LocationManager mLocationManager;
    private WeakReference<MainInterface> mView;
    private boolean mLocating = false;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            mLocating = false;
            mLocationManager.removeUpdates(this);
            mModel.updateVenues(location.getLatitude(), location.getLongitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    public MainPresenter(ModelInterface model, Context ctx, MainInterface view) {
        mView = new WeakReference<>(view);
        mModel = model;
        mLocationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        mModel.setDataWrittenCallback(new ModelInterface.onDataWriten() {
            @Override
            public void written() {
                mModel.getEntries(new ModelInterface.onDataReady() {
                    @Override
                    public void ready(List<Entry> entries) {
                        MainInterface view = getView();
                        view.displayList(entries);
                        view.dismissAwait();
                    }
                });
            }
        });
        if (mModel.isProcessing()) {
            getView().displayAwait();
        }
    }

    private MainInterface getView() {
        MainInterface view = mView.get();
        if (view == null) {
            throw new IllegalStateException("view is null");
        }
        return view;
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onUpdateNearest() {
        if (mModel.isProcessing() || mLocating) {
            return;
        }
        MainInterface view = getView();
        if (!view.askPermissions()) {
            return;
        }
        view.displayAwait();
        mLocating = true;
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                MIN_UPDATE_TIME_MS,
                MIN_UPDATE_DISTANCE_MT,
                mLocationListener);
    }

    @Override
    public void onStop() {
        getView().dismissAwait();
    }

    @Override
    public void onStart() {
        if (mModel.isProcessing() || mLocating) {
            getView().displayAwait();
        } else {
            getView().displayAwait();
            mModel.getEntries(new ModelInterface.onDataReady() {
                @Override
                public void ready(List<Entry> entries) {
                    getView().dismissAwait();
                    getView().displayList(entries);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        mModel.setDataWrittenCallback(null);
    }
}

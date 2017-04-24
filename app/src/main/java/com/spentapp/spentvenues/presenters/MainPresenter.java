package com.spentapp.spentvenues.presenters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.DateFormat;

import com.spentapp.spentvenues.model.interfaces.ModelEntry;
import com.spentapp.spentvenues.model.interfaces.ModelInterface;
import com.spentapp.spentvenues.presenters.interfaces.MainPresenterInterface;
import com.spentapp.spentvenues.views.interfaces.MainInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by anton on 4/24/17.
 */

public class MainPresenter implements MainPresenterInterface {
    private static final int MIN_UPDATE_TIME_MS = 1000;
    private static final int MIN_UPDATE_DISTANCE_MT = 0;
    private static final String REQUEST = "https://api.foursquare.com/v2/venues/" +
            "search?ll=%s&client_id=EPV4H2F0GMSHF1FTXOPL0JDHMII5Y4EVZR1X35MK5SSIAGTI" +
            "&client_secret=KMVHU321VO12J5OF4EWZAMLRGEMOOKB0JSV04OA3YW2LTKON&v=%s";

    private ModelInterface mModel;
    private LocationManager mLocationManager;
    private final OkHttpClient mClient = new OkHttpClient();
    private WeakReference<MainInterface> mView;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            mLocationManager.removeUpdates(this);
            getVenues(location.getLatitude(), location.getLongitude());
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
    }

    private void getVenues(double lat, double lon) {
        String date = DateFormat.format("yyyymmdd", new Date()).toString();
        StringBuilder ll = new StringBuilder();
        ll.append(lat).append(", ").append(lon);
        Request request = new Request.Builder().
                url(String.format(REQUEST,
                        ll.toString(),
                        date)).
                get().
                build();

        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String answer = response.body().string();
                mExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        processJson(answer);
                    }
                });
            }
        });
    }

    private void processJson(String json) {
        try {
            JSONObject object = new JSONObject(json);
            JSONObject meta = object.getJSONObject("meta");
            if (meta.getInt("code") != 200) {
                return;
            }
            JSONObject response = object.getJSONObject("response");
            JSONArray venues = response.getJSONArray("venues");
            ArrayList<ModelEntry> entriesTOAdd = new ArrayList<>();
            for (int i = 0; i < venues.length(); i++) {
                JSONObject curVenue = venues.getJSONObject(i);
                String name = curVenue.getString("name");
                JSONObject location = curVenue.getJSONObject("location");
                long distance = location.getLong("distance");

                entriesTOAdd.add(new ModelEntry(name, distance));
            }

            mModel.storeEntries(entriesTOAdd);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onUpdateNearable() {
        MainInterface view = mView.get();
        if (view == null) {
            throw new IllegalStateException("View is null, should not be so");
        }
        if (!view.askPermissions()) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                MIN_UPDATE_TIME_MS,
                MIN_UPDATE_DISTANCE_MT,
                mLocationListener);
    }
}

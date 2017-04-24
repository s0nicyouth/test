package com.spentapp.spentvenues.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Looper;
import android.provider.BaseColumns;
import android.text.format.DateFormat;
import android.util.Log;

import com.spentapp.spentvenues.base.Entry;
import com.spentapp.spentvenues.model.interfaces.ModelInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by anton on 4/24/17.
 */

public class Model implements ModelInterface {

    private static final String REQUEST = "https://api.foursquare.com/v2/venues/" +
            "search?ll=%s&client_id=EPV4H2F0GMSHF1FTXOPL0JDHMII5Y4EVZR1X35MK5SSIAGTI" +
            "&client_secret=KMVHU321VO12J5OF4EWZAMLRGEMOOKB0JSV04OA3YW2LTKON&v=%s";
    private static final String META = "meta";
    private static final String OK_CODE = "code";
    private static final String RESPONSE = "response";
    private static final String VENUES = "venues";
    private static final String NAME = "name";
    private static final String LOCATION = "location";
    private static final String DISTANCE = "distance";

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final OkHttpClient mClient = new OkHttpClient();
    private final android.os.Handler mHandler = new android.os.Handler(Looper.getMainLooper());
    private boolean mDataProcessing = false;

    private onDataWriten mDataWrittenCallback = null;

    public Model(Context ctx) {
        Realm.init(ctx);
    }

    private void storeEntries(List<Entry> entries) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.deleteAll();
        for (Entry e : entries) {
            realm.copyToRealm(new DbEntry(e.getName(), e.getDistance()));
        }
        realm.commitTransaction();
        mDataProcessing = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataWrittenCallback.written();
            }
        });

    }

    private void runOnUiThread(Runnable r) {
        mHandler.post(r);
    }

    @Override
    public void getEntries(final onDataReady callback) {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getDefaultInstance();
                RealmResults<DbEntry> realmEntries = realm.where(DbEntry.class).findAll();
                final ArrayList<Entry> results = new ArrayList<>();
                for (DbEntry e : realmEntries) {
                    results.add(new Entry(e.getName(), e.getDistance()));
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.ready(results);
                    }
                });
            }
        });
    }

    private void processJson(String json) {
        try {
            JSONObject object = new JSONObject(json);
            JSONObject meta = object.getJSONObject(META);
            if (meta.getInt(OK_CODE) != 200) {
                return;
            }
            JSONObject response = object.getJSONObject(RESPONSE);
            JSONArray venues = response.getJSONArray(VENUES);
            ArrayList<Entry> entriesTOAdd = new ArrayList<>();
            for (int i = 0; i < venues.length(); i++) {
                JSONObject curVenue = venues.getJSONObject(i);
                String name = curVenue.getString(NAME);
                JSONObject location = curVenue.getJSONObject(LOCATION);
                long distance = location.getLong(DISTANCE);

                entriesTOAdd.add(new Entry(name, distance));
            }

            storeEntries(entriesTOAdd);
        } catch (JSONException e) {
            mDataProcessing = false;
            e.printStackTrace();
        }
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
                mExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        mDataProcessing = false;
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) {
                if (!response.isSuccessful()) {
                    return;
                }
                // Callback is done on OkHttp's thread
                mExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            processJson(response.body().string());
                        } catch (IOException e) {
                            mDataProcessing = false;
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void updateVenues(final double lat, final double lon) {
        mDataProcessing = true;
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                getVenues(lat, lon);
            }
        });
    }

    @Override
    public void setDataWrittenCallback(onDataWriten callback) {
        mDataWrittenCallback = callback;
    }

    @Override
    public boolean isProcessing() {
        return mDataProcessing;
    }
}

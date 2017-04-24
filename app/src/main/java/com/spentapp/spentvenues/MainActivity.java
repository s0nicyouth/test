package com.spentapp.spentvenues;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.spentapp.spentvenues.base.Entry;
import com.spentapp.spentvenues.presenters.MainPresenter;
import com.spentapp.spentvenues.presenters.interfaces.MainPresenterInterface;
import com.spentapp.spentvenues.views.interfaces.MainInterface;

import java.util.List;

public class MainActivity extends AppCompatActivity implements MainInterface {

    private static final int GET_PERMISSIONS_CODE = 1;

    private MainPresenterInterface mPresenter;

    private ListView mListView;
    private ProgressDialog mProgressDialog;

    private static class ViewHolder {
        private TextView name;
        private TextView distance;

        private ViewHolder(TextView name, TextView distance) {
            this.name = name;
            this.distance = distance;
        }
    }

    private class VenuesAdapter extends BaseAdapter {

        private List<Entry> mEntries;

        VenuesAdapter(List<Entry> entries) {
            mEntries = entries;
        }

        @Override
        public int getCount() {
            return mEntries.size();
        }

        @Override
        public Object getItem(int position) {
            return mEntries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).
                        inflate(
                                R.layout.venues_view,
                                parent,
                                false);
                TextView name = (TextView) convertView.findViewById(R.id.name);
                TextView distance = (TextView) convertView.findViewById(R.id.distance);
                holder = new ViewHolder(name, distance);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.name.setText(mEntries.get(position).getName());
            holder.distance.setText(String.valueOf(mEntries.get(position).getDistance()));

            return convertView;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.venues);

        mPresenter = new MainPresenter(((VenuesApplication)getApplication()).getModel(),
                this,
                this);
        mPresenter.onUpdateNearest();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }

    @Override
    public boolean askPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.INTERNET)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.INTERNET },
                    GET_PERMISSIONS_CODE);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void displayList(List<Entry> entries) {
        mListView.setAdapter(new VenuesAdapter(entries));
    }

    @Override
    public void displayAwait() {
        if (mProgressDialog != null) {
            return;
        }
        mProgressDialog = ProgressDialog.show(this, "Loading", "Wait while loading...");
    }

    @Override
    public void dismissAwait() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case GET_PERMISSIONS_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPresenter.onUpdateNearest();
                }
                break;
        }
    }
}

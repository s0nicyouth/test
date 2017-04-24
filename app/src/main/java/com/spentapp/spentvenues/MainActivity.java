package com.spentapp.spentvenues;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.spentapp.spentvenues.model.interfaces.ModelEntry;
import com.spentapp.spentvenues.presenters.MainPresenter;
import com.spentapp.spentvenues.presenters.interfaces.MainPresenterInterface;
import com.spentapp.spentvenues.views.interfaces.MainInterface;

import org.w3c.dom.Text;

import java.util.List;

public class MainActivity extends AppCompatActivity implements MainInterface {

    private static final int GET_PERMISSIONS_CODE = 1;

    private MainPresenterInterface mPresenter;

    private ListView mListView;

    private class VenuesAdapter extends BaseAdapter {

        private static class ViewHolder {
            private TextView name;
            private TextView distance;

            public ViewHolder(TextView name, TextView distance) {
                this.name = name;
                this.distance = distance;
            }
        }

        private List<ModelEntry> mEntries;

        public VenuesAdapter(List<ModelEntry> entries) {
            mEntries = entries;
        }

        @Override
        public int getCount() {
            return mEntries.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LinearLayout container = (LinearLayout) LayoutInflater.from(MainActivity.this).
                        inflate(
                                R.layout.venues_view,
                                parent,
                                false);
                TextView name = (TextView) container.findViewById(R.id.name);
                TextView distance = (TextView) container.findViewById(R.id.distance);
                ViewHolder holder = new ViewHolder(name, distance)
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.venues);

        mPresenter = new MainPresenter(((VenuesApplication)getApplication()).getModel(),
                this,
                this);
        mPresenter.onUpdateNearable();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case GET_PERMISSIONS_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPresenter.onUpdateNearable();
                }
                break;
        }
    }
}

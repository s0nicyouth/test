package com.spentapp.spentvenues.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.Entity;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import com.spentapp.spentvenues.model.interfaces.ModelEntry;
import com.spentapp.spentvenues.model.interfaces.ModelInterface;

import java.util.List;

/**
 * Created by anton on 4/24/17.
 */

public class Model implements ModelInterface {
    private static class VenueEntry implements BaseColumns {
        private static final String TABLE_NAME = "venues";
        private static final String VENUE_NAME = "name";
        private static final String VENUE_DISTANCE = "distance";
    }

    private static final String CREATE_TABLE_SQL = "CREATE TABLE " +
            VenueEntry.TABLE_NAME + " ( " +
            VenueEntry._ID + " INTEGER PRIMARY KEY," +
            VenueEntry.VENUE_NAME + " TEXT NUT NULL," +
            VenueEntry.VENUE_DISTANCE + " INTEGER NUT NULL" +
            " ) ";

    private static final String DELETA_TABLE_SQL =
            "DROP TABLE IF EXISTS " + VenueEntry.TABLE_NAME;

    private static String INSERT_SQL = "INSERT INTO " +
            VenueEntry.TABLE_NAME + " (" +
            VenueEntry.VENUE_NAME + ", " +
            VenueEntry.VENUE_DISTANCE +
            ") VALUES (?, ?)";


    private class VenuesDbHelper extends SQLiteOpenHelper {
        private static final int DB_VERSION = 1;
        private static final String DB_NAME = "venues.db";

        private VenuesDbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DELETA_TABLE_SQL);
            onCreate(db);
        }
    }

    private VenuesDbHelper mDbHelper;

    public Model(Context ctx) {
        mDbHelper = new VenuesDbHelper(ctx);
    }

    @Override
    public void storeEntries(List<ModelEntry> entries) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            SQLiteStatement statement = db.compileStatement(INSERT_SQL);
            db.beginTransaction();
            for (ModelEntry e : entries) {
                statement.bindString(1, e.getName());
                statement.bindLong(2, e.getDistance());

                statement.executeInsert();
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
        }
    }
}

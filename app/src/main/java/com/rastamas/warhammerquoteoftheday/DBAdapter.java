package com.rastamas.warhammerquoteoftheday;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Rasta on 05/01/2017.
 */

public class DBAdapter {

    private final Context mContext;
    private static final String TAG = "QuoteDbAdapter";
    private static final String DATABASE_NAME = "quotedb";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE = "create table quotes (date string primary key , "
            + "quote TEXT" + ");";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    public DBAdapter(Context ctx){
        this.mContext = ctx;
    }

    public DBAdapter open() throws SQLException {
        this.mDbHelper = new DatabaseHelper(this.mContext);
        this.mDb = this.mDbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        mDbHelper.close();
    }

    public String getQuote(String key){
        return mDb.query("quotes", new String[] {"quote"}, "date = " + key, null, null, null, null).getString(0);
    }

    public void putQuote(String date, String quote){
        mDb.execSQL("INSERT or REPLACE INTO quotes (date, quote) VALUES(" + date + "," + quote + ")");
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            onCreate(db);
        }

    }
}


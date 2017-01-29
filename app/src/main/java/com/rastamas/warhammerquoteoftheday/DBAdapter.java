package com.rastamas.warhammerquoteoftheday;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by Rasta on 05/01/2017.
 */

class DBAdapter {

    private final Context mContext;
    private static final String TAG = "QuoteDbAdapter";
    private static final String DATABASE_NAME = "quotedb";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE = "create table if not exists quotes (" +
            "date string primary key," +
            "quoteid TEXT," +
            "quote TEXT" +
            ");";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    DBAdapter(Context ctx) {
        this.mContext = ctx;
    }

    public DBAdapter open() throws SQLException {
        this.mDbHelper = new DatabaseHelper(this.mContext);
        this.mDb = this.mDbHelper.getWritableDatabase();
        return this;
    }

    void close() {
        mDbHelper.close();
    }

    boolean quoteExists(String dateKey) {
        Cursor cursor = mDb.query("quotes", new String[]{"quote"}, "date = ?", new String[]{dateKey}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            return true;
        }
        return false;
    }

    String getQuote(String dateKey) {
        Cursor cursor = mDb.query("quotes", new String[]{"quote"}, "date = ?", new String[]{dateKey}, null, null, null);
        String quote = "";
        if (cursor != null && cursor.moveToFirst()) {
            quote = cursor.getString(0);
            cursor.close();
        }
        return quote;
    }

    void putQuote(String dateKey, String quoteId, String quote) {
        try {
            mDb.execSQL("INSERT or REPLACE INTO quotes (date, quoteid, quote) VALUES(?,?,?)", new String[]{dateKey, quoteId, quote});
        } catch (Exception e) {
            mDb.execSQL("DROP TABLE quotes;");
            mDb.execSQL(DATABASE_CREATE);
            mDb.execSQL("INSERT or REPLACE INTO quotes (date, quoteid, quote) VALUES(?,?,?)", new String[]{dateKey, quoteId, quote});
        }
    }

    void deleteQuote(String dateKey) {
        mDb.execSQL("DELETE FROM quotes WHERE date=?", new String[]{dateKey});
    }

    String getQuoteId(String dateKey) {
        Cursor cursor = mDb.query("quotes", new String[]{"quoteid"}, "date = ?", new String[]{dateKey}, null, null, null);
        String quoteid = "";
        if (cursor != null && cursor.moveToFirst()) {
            quoteid = cursor.getString(0);
            cursor.close();
        }
        return quoteid;
    }

    ArrayList<String> getLastThirtyQuoteIds() {
        ArrayList<String> quoteIds = new ArrayList<>();
        Cursor cursor;
        try {
            cursor = mDb.rawQuery("SELECT quoteid FROM quotes ORDER BY _ROWID_ DESC;", null);
        } catch (Exception e) {
            mDb.execSQL("DROP TABLE quotes;");
            mDb.execSQL(DATABASE_CREATE);
            return new ArrayList<>();
        }
        int i = 30;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    quoteIds.add(cursor.getString(cursor.getColumnIndex("quoteid")));
                    i--;
                } while (cursor.moveToNext() && i > 0);
            }
            cursor.close();
        }
        return quoteIds;
    }

    TreeMap<String, String> getAllQuotes() {
        Cursor cursor = mDb.rawQuery("SELECT date, quote FROM quotes;", null);

        TreeMap<String, String> quotes = new TreeMap<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int cursorDateIndex = cursor.getColumnIndex("date");
                int cursorQuoteIndex = cursor.getColumnIndex("quote");
                do {
                    quotes.put(cursor.getString(cursorDateIndex), cursor.getString(cursorQuoteIndex));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return quotes;
    }

    void clearQuotes() {
        mDb.execSQL("DELETE FROM quotes;");
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


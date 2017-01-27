package com.rastamas.warhammerquoteoftheday;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.TreeMap;

/**
 * Created by Rasta on 05/01/2017.
 */

public class DBAdapter {

    private final Context mContext;
    private static final String TAG = "QuoteDbAdapter";
    private static final String DATABASE_NAME = "quotedb";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE = "create table if not exists quotes (" +
            "date string primary key," +
            "quote TEXT" +
            ");";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    DBAdapter(Context ctx){
        this.mContext = ctx;
    }

    public DBAdapter open() throws SQLException {
        this.mDbHelper = new DatabaseHelper(this.mContext);
        this.mDb = this.mDbHelper.getWritableDatabase();
        return this;
    }

    void close(){
        mDbHelper.close();
    }

    boolean quoteExists(String key){
        Cursor cursor = mDb.query("quotes", new String[] {"quote"}, "date = '" + key + "'", null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            return true;
        }
        return false;
    }

    String getQuote(String key){
        Cursor cursor = mDb.query("quotes", new String[] {"quote"}, "date = '" + key + "'", null, null, null, null);
        String quote = "";
        if (cursor != null && cursor.moveToFirst()) {
            quote = cursor.getString(0);
            cursor.close();
        }
        return quote;
    }

    void putQuote(String date, String quote){
        mDb.execSQL("INSERT or REPLACE INTO quotes (date, quote) VALUES(?,?)", new String[]{date, quote});
    }

    void deleteQuote(String key){
        mDb.execSQL("DELETE FROM quotes WHERE date=?", new String[] {key});
    }

    TreeMap<String,String> getAllQuotes(){
        Cursor cursor = mDb.rawQuery("SELECT date, quote FROM quotes;", null);

        TreeMap<String, String> quotes = new TreeMap<>();
        if(cursor != null){
            if(cursor.moveToFirst()){
                int cursorDateIndex =cursor.getColumnIndex("date");
                int cursorQuoteIndex = cursor.getColumnIndex("quote");
                do{
                quotes.put(cursor.getString(cursorDateIndex), cursor.getString(cursorQuoteIndex));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return quotes;
    }

    void clearQuotes(){
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


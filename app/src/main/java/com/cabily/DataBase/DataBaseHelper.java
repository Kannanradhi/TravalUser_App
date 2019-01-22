package com.cabily.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CAS64 on 9/14/2018.
 */

public class DataBaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "json_db";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DataBasePojo.CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + DataBasePojo.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public long InsertJson(String fromJson, String json) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        //ContentValues() is used to define the column name and its data to be stored
        ContentValues values = new ContentValues();
        values.put(DataBasePojo.COLUMN_JSON_FROM, fromJson);
        values.put(DataBasePojo.COLUMN_JSON, json);

        // insert row
        long id = db.insert(DataBasePojo.TABLE_NAME, null, values);

        // close db connection
        db.close();

        return id;
    }

    public List<DataBasePojo> getJson() {
        List<DataBasePojo> jsonList = new ArrayList<DataBasePojo>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + DataBasePojo.TABLE_NAME + " ORDER BY " +
                DataBasePojo.COLUMN_TIMESTAMP + " DESC";

        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                DataBasePojo dataBasePojo = new DataBasePojo();
                dataBasePojo.setId(cursor.getInt(cursor.getColumnIndex(DataBasePojo.COLUMN_ID)));
                dataBasePojo.setFromJson(cursor.getString(cursor.getColumnIndex(DataBasePojo.COLUMN_JSON_FROM)));
                dataBasePojo.setJson(cursor.getString(cursor.getColumnIndex(DataBasePojo.COLUMN_JSON)));
                dataBasePojo.setTimeStamp(cursor.getString(cursor.getColumnIndex(DataBasePojo.COLUMN_TIMESTAMP)));
                jsonList.add(dataBasePojo);
            } while (cursor.moveToNext());

        }
        // close db connection
        db.close();

        return jsonList;
    }

    public int getJsonCount(String fromJson) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DataBasePojo.TABLE_NAME,
                new String[]{DataBasePojo.COLUMN_JSON_FROM},
                DataBasePojo.COLUMN_JSON_FROM + "=?",
                new String[]{fromJson}, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }

    public void clearTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DataBasePojo.TABLE_NAME, null);

        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getCount();
            if (count > 0) {
                db.delete(DataBasePojo.CREATE_TABLE, null, null);
                System.out.println("*****************************Chat Table Cleared*****************************");
                db.close();
            } else {
                System.out.println("*****************************Chat Table empty*****************************");
            }
            cursor.close();
        }
        System.out.println("*****************************Chat Table cursor empty*****************************");

    }
}

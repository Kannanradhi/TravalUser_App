package com.cabily.DataBase;

/**
 * Created by CAS64 on 9/14/2018.
 */

public class DataBasePojo {
    public DataBasePojo() {
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getFromJson() {
        return fromJson;
    }

    public void setFromJson(String fromJson) {
        this.fromJson = fromJson;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String json, fromJson, timeStamp;
    private int id;

    public static final String TABLE_NAME = "jsontabile";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_JSON_FROM = "jsonfrom";
    public static final String COLUMN_JSON = "json";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_JSON_FROM + " TEXT,"
                    + COLUMN_JSON + " TEXT,"
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ")";


}

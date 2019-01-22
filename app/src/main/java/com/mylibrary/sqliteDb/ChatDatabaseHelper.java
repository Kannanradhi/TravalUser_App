package com.mylibrary.sqliteDb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cabily.pojo.ChatPojo;

import java.util.ArrayList;


/**
 * Created by CAS64 on 7/21/2017.
 */

public class ChatDatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "chatDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_CHAT = "chats";

    // User Table Columns
    private static final String KEY_CHAT_ID = "id";
    private static final String KEY_CHAT_MESSAGE = "chatMessage";
    private static final String KEY_CHAT_TYPE = "chatType";
    private static final String KEY_CHAT_TIME = "chatTime";
    private static final String KEY_CHAT_STATUS = "chatStatus";


    public ChatDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_CHAT_TABLE = "CREATE TABLE " + TABLE_CHAT +
                "(" +
                KEY_CHAT_ID + " INTEGER PRIMARY KEY," +
                KEY_CHAT_MESSAGE + " TEXT," +
                KEY_CHAT_TYPE + " TEXT," +
                KEY_CHAT_TIME + " TEXT," +
                KEY_CHAT_STATUS + " TEXT" +
                ")";

        db.execSQL(CREATE_CHAT_TABLE);
        System.out.println("----------------------------------chat table created--------------------------------");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT);
            onCreate(db);
        }
    }

    // Adding new contact
    public void addChat(ChatPojo chat) {
        SQLiteDatabase db = this.getWritableDatabase();

        System.out.println("----------------------------------------------db helper addchat----------------" + chat);

        ContentValues values = new ContentValues();
        values.put(KEY_CHAT_MESSAGE, chat.getMessage());
        values.put(KEY_CHAT_TYPE, chat.getType());
        values.put(KEY_CHAT_TIME, chat.getTime());
        values.put(KEY_CHAT_STATUS, chat.getStatus());

        System.out.println("******************add chat message***************" + chat.getMessage());
        System.out.println("******************add chat type***************" + chat.getType());
        System.out.println("******************add chat time***************" + chat.getTime());

        // Inserting Row
        db.insert(TABLE_CHAT, null, values);
        System.out.println("******************add chat***************" + values);
        db.close();
    }


    public ArrayList<ChatPojo> getAllChat() {
        ArrayList<ChatPojo> chats = new ArrayList<ChatPojo>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_CHAT;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        if (cursor.moveToFirst()) {
            do {
                ChatPojo Chat = new ChatPojo();
                Chat.setID(Integer.parseInt(cursor.getString(0)));
                Chat.setMessage(cursor.getString(1));
                Chat.setType(cursor.getString(2));
                Chat.setTime(cursor.getString(3));
                // Adding contact to list
                chats.add(Chat);

                // Add book to books
            } while (cursor.moveToNext());
        }

        Log.d("getAllBooks()", chats.toString());

        // return books
        return chats;
    }

    public void updateStatus() {
        SQLiteDatabase db = this.getWritableDatabase();


        String strSQL = "UPDATE " + TABLE_CHAT + " SET " + KEY_CHAT_STATUS + " =1 " + "WHERE " + KEY_CHAT_STATUS + "=0";
        System.out.println("***************************************Chat Status*****************" + strSQL);
        db.execSQL(strSQL);
        db.close();

    }

    public int ChatUnviewedCount() {
        SQLiteDatabase db = this.getReadableDatabase();


        String strSQL = "SELECT " + "* " + "FROM " + TABLE_CHAT + " WHERE " + KEY_CHAT_STATUS + "=0";
        System.out.println("***************************Count Query**************8"+strSQL);
        Cursor cursor = db.rawQuery(strSQL, null);
        int count = cursor.getCount();
        System.out.println("***************************Count**************8"+count);
        cursor.close();
        db.close();
        return count;
    }

    public void clearTable()   {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " +TABLE_CHAT, null);



        if(cursor != null){

            cursor.moveToFirst();

            int count = cursor.getInt(0);

            if(count > 0){
                db.delete(TABLE_CHAT, null, null);
                System.out.println("*****************************Chat Table Cleared*****************************");
                db.close();
            }
            System.out.println("*****************************Chat Table empty*****************************");

            cursor.close();
        }
        System.out.println("*****************************Chat Table cursor empty*****************************");

    }
}

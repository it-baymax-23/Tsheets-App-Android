package com.example.tsheetapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EmployeerDatabasehelper extends SQLiteOpenHelper {
    public static String KEY_ID = "id";
    public static String KEY_USERNAME = "username";
    public static String KEY_EMAIL = "email";
    public static String TABLE_NAME = "empployeer";
    public static  String KEY_PROFILE = "profile";
    public EmployeerDatabasehelper(@Nullable Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
        SQLiteDatabase db = this.getWritableDatabase();
        this.onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String table_create = "Create Table  IF NOT EXISTS " + TABLE_NAME + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_USERNAME + " TEXT," + KEY_EMAIL + " TEXT," + KEY_PROFILE + " TEXT)";
        db.execSQL(table_create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }

    public Cursor get_json()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from " + TABLE_NAME,new String[]{});
        return cursor;
    }

    public void database_update(JSONArray array)
    {
        int size = array.length();
        SQLiteDatabase db = this.getWritableDatabase();
        this.onUpgrade(db,0,1);
        Log.d("array_length",String.valueOf(array.length()));
        for(int i = 0;i<size;i++)
        {
            ContentValues value = new ContentValues();
            try {
                JSONObject object = array.getJSONObject(i);
                value.put(KEY_USERNAME,object.getString(KEY_USERNAME));
                value.put(KEY_ID,object.getInt(KEY_ID));
                value.put(KEY_EMAIL,object.getString(KEY_EMAIL));
                value.put(KEY_PROFILE,object.getString(KEY_PROFILE));
                db.insert(TABLE_NAME,null,value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        db.close();
    }

    public Cursor get_users(String userid)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from " + TABLE_NAME + " where id in (" + userid + ")",new String[]{});
        return cursor;
    }

    public Cursor get_user_by_name(String name)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from " + TABLE_NAME + " where " + KEY_USERNAME + " like '%" + name + "%'",new String[]{});
        return cursor;
    }

}

package com.example.tsheetapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "job";
    public static final String KEY_ID = "id";
    public static final String KEY_JOBNAME = "jobname";
    public static final String KEY_SHORTCODE = "shortcode";
    public static final String KEY_PARENTID = "parent_id";
    public static final String KEY_USERID = "user_id";
    public static final String KEY_ASSIGNED = "assigns";
    Context c;
    public DatabaseHelper(Context context) {
         super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
        SQLiteDatabase db = this.getWritableDatabase();
        this.onCreate(db);
         c = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_JOBNAME + " TEXT," + KEY_SHORTCODE + " TEXT," + KEY_PARENTID + " INTEGER," + KEY_USERID + " INTEGER," + KEY_ASSIGNED + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void job_replace(JSONArray array)
    {

        SQLiteDatabase db = this.getWritableDatabase();
        onUpgrade(db,0,1);
        int size = array.length();
        Log.d("datasize",String.valueOf(size));
        for(int i = 0; i< size;i++ )
        {
            try {
                JSONObject object = array.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(KEY_JOBNAME,object.getString(KEY_JOBNAME));
                values.put(KEY_ASSIGNED,object.getString(KEY_ASSIGNED));
                values.put(KEY_PARENTID,object.getInt(KEY_PARENTID));
                values.put(KEY_SHORTCODE,object.getString(KEY_SHORTCODE));
                values.put(KEY_USERID,object.getInt(KEY_USERID));
                values.put(KEY_ID,object.getInt(KEY_ID));
                db.insert(TABLE_NAME,null,values);
            } catch (JSONException e) {
                Log.d("jsonerror",e.getMessage());
                e.printStackTrace();
            }
        }
        db.close();
    }

    public Cursor get_jobs()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("Select * from " + TABLE_NAME,new String[]{});
    }

    public Cursor get_jobs_by_user(String user)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("Select * from " + TABLE_NAME + " where " + KEY_USERID + " = " + user,new String[]{});
    }

    public Cursor get_jobs(String jobname)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("Select * from " + TABLE_NAME + " where " + KEY_JOBNAME + " like '%" + jobname + "%'",new String[]{});
    }

    public Cursor get_job_byid(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("Select * from " + TABLE_NAME + " where " + KEY_ID + "=" +  id,new String[]{});
    }
}

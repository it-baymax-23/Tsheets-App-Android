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

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ShiftDatabaseHelper extends SQLiteOpenHelper {
    Context c;

    public  static final String KEY_ID = "id";
    public  static final String KEY_DATE = "date";
    public  static final String KEY_STARTTIME = "starttime";
    public  static final String KEY_ENDTIME = "endtime";
    public  static final String KEY_JOBID = "jobid";
    public  static final String KEY_ASSIGNS = "assigns";
    public  static final String KEY_LOCATION = "location";
    public  static final String KEY_COLOR = "color";
    public  static final String KEY_NOTES = "notes";
    public  static final String KEY_USERID = "user_id";
    public  static final String KEY_SHIFTTYPE = "shift_type";
    public  static final String KEY_STATUS = "status";
    public static final String TABLE_NAME = "shift";
    public static final String KEY_TITLE = "title";

    public ShiftDatabaseHelper(@Nullable Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
        SQLiteDatabase db = this.getWritableDatabase();
        this.onCreate(db);
        c = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String table = "Create Table  IF NOT EXISTS " + TABLE_NAME + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_ASSIGNS + " TEXT," + KEY_COLOR + " TEXT," + KEY_DATE + " DATE," + KEY_STARTTIME + " TEXT," + KEY_ENDTIME + " TEXT," + KEY_JOBID + " INTEGER," + KEY_LOCATION + " TEXT," + KEY_NOTES + " TEXT," + KEY_SHIFTTYPE + " BOOLEAN," + KEY_STATUS + " TEXT," + KEY_USERID + " INTEGER," + KEY_TITLE + " TEXT)";
        db.execSQL(table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("Drop TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }

    public void replacedatabase(JSONArray array)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        this.onUpgrade(db,0,1);

        int size = array.length();
        for(int i = 0;i<size;i++)
        {
            try {
                JSONObject object = array.getJSONObject(i);
                ContentValues content = new ContentValues();
                content.put(KEY_ID,object.getInt(KEY_ID));
                content.put(KEY_ASSIGNS,object.getString(KEY_ASSIGNS));
                content.put(KEY_COLOR,object.getString(KEY_COLOR));
                content.put(KEY_DATE,object.getString(KEY_DATE));
                String shift_type = object.getString(KEY_SHIFTTYPE);
                boolean type = false;
                if(shift_type.contentEquals("1"))
                {
                    type = true;
                }
                content.put(KEY_SHIFTTYPE,type);
                content.put(KEY_STARTTIME,object.getString(KEY_STARTTIME));
                content.put(KEY_ENDTIME,object.getString(KEY_ENDTIME));
                content.put(KEY_JOBID,object.getInt(KEY_JOBID));
                content.put(KEY_USERID,object.getInt(KEY_USERID));
                content.put(KEY_LOCATION,object.getString(KEY_LOCATION));
                content.put(KEY_NOTES,object.getString(KEY_NOTES));

                content.put(KEY_STATUS,object.getString(KEY_STATUS));
                content.put(KEY_TITLE,object.getString(KEY_TITLE));
                db.insert(TABLE_NAME,null,content);
            } catch (JSONException e) {
                Log.d("jsonexception",e.getMessage());
                e.printStackTrace();
            }
        }
        db.close();
    }

    public Cursor get_json(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("Select * from " + TABLE_NAME,new String[]{});
    }

    public Cursor get_schedule_for_user(int userid)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("Select " + TABLE_NAME + ".*,job.jobname as jobtitle from " + TABLE_NAME + ",job where (','||" + TABLE_NAME + "." + KEY_ASSIGNS + "||',') LIKE '%," + String.valueOf(userid) + ",%' and job.id = " + TABLE_NAME + ".jobid" +  " order by " + KEY_DATE,new String[]{});
    }

    public Cursor get_schedule_for_job(String s) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("Select " + TABLE_NAME + ".*,job.jobname as jobtitle from " + TABLE_NAME + ",job where " + TABLE_NAME + ".jobid in (" + s + ") and job.id = " + TABLE_NAME + ".jobid order by " + TABLE_NAME + "." + KEY_DATE,new String[]{});

    }

    public Cursor getschedulebyid(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("Select " + TABLE_NAME + ".*,job.jobname from " + TABLE_NAME + ",job where " + TABLE_NAME + ".jobid = job.id and " + TABLE_NAME + ".id = " + String.valueOf(id),new String[]{});
    }

    public void delete(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("Delete from " + TABLE_NAME + " where id = " + String.valueOf(id));
    }

    public Cursor get_schedule_for_date(int userid, Calendar date)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return db.rawQuery("Select " + TABLE_NAME + ".*,job.jobname as jobtitle from " + TABLE_NAME + ",job where (','||" + TABLE_NAME + "." + KEY_ASSIGNS + "||',') LIKE '%," + String.valueOf(userid) + ",%' and job.id = " + TABLE_NAME + ".jobid and " + TABLE_NAME + "." + KEY_DATE + " = '" + format.format(new Date(date.getTimeInMillis())) + "'",new String[]{});
    }
}

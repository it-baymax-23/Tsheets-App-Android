package com.example.tsheetapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeSheetDatabaseHelper extends SQLiteOpenHelper {

    String user_id;
    public static final String TABLE_NAME = "timesheet";
    public static final String KEY_ID = "id";
    public static final String KEY_JOBID = "jobid";
    public static final String KEY_NOTES = "notes";
    public static final String KEY_ATTACHMENT = "attachment";
    public static final String KEY_STARTTIME = "starttime";
    public static final String KEY_ENDTIME = "endtime";
    public static final String KEY_USERID = "userid";
    public static final String KEY_REFID = "ref_id";
    public static final String KEY_STATE = "state";
    Context c;
    public TimeSheetDatabaseHelper(Context context) {
        super(context,Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
        user_id = context.getSharedPreferences("credential",Context.MODE_PRIVATE).getString("user_id","0");
        SQLiteDatabase db = this.getWritableDatabase();
        this.onCreate(db);
        c = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NOTES + " TEXT," + KEY_ATTACHMENT + " TEXT," + KEY_JOBID + " INTEGER," + KEY_USERID + " INTEGER," + KEY_STARTTIME + " DATETIME," + KEY_ENDTIME + " DATETIME," + KEY_REFID + " INTEGER," + KEY_STATE + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    public void savetimesheet(Calendar start, Calendar end, String notes, int user_id, int jobid, String attachment, int timesheetid, int ref_id, String state)
    {
        ContentValues content = new ContentValues();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.getDefault());
        content.put(KEY_STARTTIME,format.format(start.getTimeInMillis()));
        content.put(KEY_ENDTIME,format.format(end.getTimeInMillis()));
        content.put(KEY_NOTES,notes);
        content.put(KEY_USERID,user_id);
        content.put(KEY_JOBID,jobid);
        content.put(KEY_ATTACHMENT,attachment);
        content.put(KEY_STATE,state);
        if(ref_id > 0)
        {
            content.put(KEY_REFID,ref_id);
        }
        SQLiteDatabase db = this.getWritableDatabase();
        if(timesheetid == 0)
        {
            db.insert(TABLE_NAME,null,content);
        }
        else
        {
            db.update(TABLE_NAME,content,KEY_ID + " = ?",new String[]{String.valueOf(timesheetid)});
        }

        db.close();
    }

    public Cursor get_json()
    {
        SQLiteDatabase helper = this.getReadableDatabase();
        Cursor cursor = helper.rawQuery("Select " + TABLE_NAME + ".*," + DatabaseHelper.TABLE_NAME +  ".jobname as jobname from " + TABLE_NAME + "," + DatabaseHelper.TABLE_NAME + " Where " + TABLE_NAME + ".jobid = " + DatabaseHelper.TABLE_NAME + ".id and " + TABLE_NAME + "." + KEY_USERID + " = " + user_id + " order by " + TABLE_NAME + ".starttime desc",new String[]{});

        return cursor;
    }

    public void savetimesheetwithobject(JSONObject save_object) throws JSONException {
        SQLiteDatabase helper = this.getWritableDatabase();
        ContentValues content = new ContentValues();
        int id = save_object.getInt(KEY_ID);
        content.put(KEY_JOBID,save_object.getInt(KEY_JOBID));
        content.put(KEY_STARTTIME,save_object.getString(KEY_STARTTIME));
        content.put(KEY_ENDTIME,save_object.getString(KEY_ENDTIME));
        content.put(KEY_NOTES,save_object.getString(KEY_NOTES));
        content.put(KEY_USERID,save_object.getInt(KEY_USERID));
        content.put(KEY_JOBID,save_object.getInt(KEY_JOBID));
        content.put(KEY_ATTACHMENT,save_object.getString(KEY_ATTACHMENT));
        content.put(KEY_REFID,save_object.getInt("ref_id"));
        content.put(KEY_STATE,save_object.getString("state"));

        Log.d("object_string",content.getAsString(KEY_ATTACHMENT));
        if(id == 0)
        {
            helper.insert(TABLE_NAME,null,content);
        }
        else
        {
            helper.update(TABLE_NAME,content,KEY_ID + " = ?",new String[]{String.valueOf(id)});
        }

        helper.close();
    }

    public Cursor gettimesheetbydate(Calendar starttime,String user_id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
        Log.d("query","Select " + TABLE_NAME + ".*,job.jobname as jobname from " + TABLE_NAME + ",job where job.id = " + TABLE_NAME + ".jobid and DATE(" + TABLE_NAME + "." + KEY_STARTTIME + ") = DATE('" + sdf.format(starttime.getTimeInMillis()) + "') and " + TABLE_NAME + "." + KEY_USERID + " = " + user_id + " order by " + TABLE_NAME + "." + KEY_STARTTIME);
        Cursor cursor = db.rawQuery("Select " + TABLE_NAME + ".*,job.jobname as jobname from " + TABLE_NAME + ",job where job.id = " + TABLE_NAME + ".jobid and DATE(" + TABLE_NAME + "." + KEY_STARTTIME + ") = DATE('" + sdf.format(starttime.getTimeInMillis()) + "') and " + TABLE_NAME + "." + KEY_USERID + " = " + user_id + " order by " + TABLE_NAME + "." + KEY_STARTTIME ,new String[]{});
        return cursor;
    }

    public Cursor gettimesheetbyjobid(Date starttime,String jobid,String state)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        return db.rawQuery("Select * from " + TABLE_NAME + " where " + KEY_STARTTIME + "='" + sdf.format(starttime) + "' and " + KEY_JOBID + "=" + jobid + " and " + KEY_STATE + "='" + state + "'",new String[]{});
    }

    public Cursor gettimesheetbyid(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select " + TABLE_NAME + ".*," + DatabaseHelper.TABLE_NAME + ".jobname as jobname from " + TABLE_NAME + "," + DatabaseHelper.TABLE_NAME + " where " + TABLE_NAME + ".id = " + String.valueOf(id) + " and " + TABLE_NAME + ".jobid = " + DatabaseHelper.TABLE_NAME + ".id",new String[]{});
        return cursor;

    }

    public void deletebystate(String state)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("Delete from " + TABLE_NAME + " where " + KEY_STATE + " = '" + state + "'");
        db.close();
    }
    public void delete(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("Delete from " + TABLE_NAME + " where " + KEY_ID + " = " + String.valueOf(id));
        db.close();
    }

    public void deletebyrefid(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("Delete from " + TABLE_NAME + " where " + KEY_REFID + " = " + id);
        db.close();
    }

    public Cursor gettimesheetbyweek()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Calendar now = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
        now.setFirstDayOfWeek(Calendar.SUNDAY);
        now.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);

        Calendar to_day = (Calendar) now.clone();
        to_day.add(Calendar.DATE,7);
        return db.rawQuery("Select " + TABLE_NAME + ".*,job.jobname from " + TABLE_NAME + ",job where job.id = " + TABLE_NAME + ".jobid and DATE(starttime) <  DATE('" + format.format(to_day.getTimeInMillis()) + "') and DATE(starttime) >= DATE('" + format.format(now.getTimeInMillis()) + "') and " + TABLE_NAME + "." + KEY_USERID + "=" + user_id,new String[]{});
    }

    public void updatetimesheet(String attachment,int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues content = new ContentValues();
        content.put("attachment",attachment);
        db.update(TABLE_NAME,content,KEY_ID + " = ?",new String[]{String.valueOf(id)});
    }

    public int addinitial(Date starttime,int jobid,String state)
    {
        ContentValues value = new ContentValues();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss",Locale.getDefault());
        value.put("starttime",format.format(starttime));
        value.put("jobid",jobid);
        value.put("state",state);
        value.put(KEY_USERID,user_id);
        SQLiteDatabase db = this.getWritableDatabase();
        return (int) db.insert(TimeSheetDatabaseHelper.TABLE_NAME,null,value);
    }
}

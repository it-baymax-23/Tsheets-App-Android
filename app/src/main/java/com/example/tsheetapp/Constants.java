package com.example.tsheetapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.LogRecord;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Constants {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "tsheetapp";
    public static final String[] colors = {"#777777","#FF0000","#00FF00","#0000FF","#FFA500","#008080","#4B0082","#A52A2A","#FFC0CB","#808000","#DC143C","#800080","#000000","#90EE90","#ADD8E6"};
    public static final String[] colorname = {"Gray","Red","Green","Blue","Orange","Teal","Indigo","Brown","Pink","Olive","Crimson","Purple","Black","Light Green","Light Blue"};

    public static void display_profile(Context context, TextView usernametxt,CircleImageView userimage)
    {
        SharedPreferences preferences = context.getSharedPreferences("credential",Context.MODE_PRIVATE);

        String username = preferences.getString("user","");
        String userprofile = preferences.getString("profile","");
        Log.d("userprofiletext",userprofile);
        usernametxt.setText(username.toUpperCase().substring(0,1));
        usernametxt.setBackgroundResource(R.drawable.circle);
        userimage.setVisibility(View.GONE);
        if(!userprofile.isEmpty())
        {
            new DownloadImageEmployee(userimage,usernametxt).execute(API.url + userprofile);
        }

    }


    public static void initdatabase(JSONObject object,Context context)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        ShiftDatabaseHelper shift = new ShiftDatabaseHelper(context);
        EmployeerDatabasehelper employee = new EmployeerDatabasehelper(context);
        TimeSheetDatabaseHelper timesheet = new TimeSheetDatabaseHelper(context);
        try {
            helper.job_replace(object.getJSONArray("job"));
            shift.replacedatabase(object.getJSONArray("shift"));

            employee.database_update(object.getJSONArray("employee"));
            Cursor cursor = timesheet.get_json();
            JSONArray object_array = new JSONArray();
            ArrayList<Integer> id_array = new ArrayList<Integer>();
            if(cursor.moveToFirst())
            {
                do {
                    if(cursor.getInt(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_REFID)) == 0)
                    {
                        JSONObject initial = new JSONObject();
                        initial.put(TimeSheetDatabaseHelper.KEY_ID,cursor.getInt(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_ID)));
                        initial.put(TimeSheetDatabaseHelper.KEY_JOBID,cursor.getInt(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_JOBID)));
                        initial.put(TimeSheetDatabaseHelper.KEY_ATTACHMENT,cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_ATTACHMENT)));
                        initial.put(TimeSheetDatabaseHelper.KEY_STARTTIME,cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_STARTTIME)));
                        initial.put(TimeSheetDatabaseHelper.KEY_ENDTIME,cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_ENDTIME)));
                        initial.put(TimeSheetDatabaseHelper.KEY_USERID,cursor.getInt(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_USERID)));
                        initial.put(TimeSheetDatabaseHelper.KEY_NOTES,cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_NOTES)));
                        update_timesheet(initial,context);
                    }
                    else
                    {
                        id_array.add(cursor.getInt(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_REFID)));
                    }
                }while(cursor.moveToNext());
            }

            JSONArray array = object.getJSONArray("timesheet");
            Log.d("array_length","" + array.length());
            for(int i = 0; i < array.length();i++)
            {
                Log.d("id_array",String.valueOf(id_array.indexOf(array.getJSONObject(i).getInt("id"))));
                if(id_array.indexOf(array.getJSONObject(i).getInt("id")) == -1)
                {
                    JSONObject object_date = array.getJSONObject(i);
                    SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.getDefault());
                    Calendar starttime = Calendar.getInstance();
                    starttime.setTime(format.parse(object_date.getString("starttime")));
                    Calendar endtime = Calendar.getInstance();
                    String attachement = object_date.getString("attachment");
                    endtime.setTime(format.parse(object_date.getString("endtime")));
                    timesheet.savetimesheet(starttime,endtime,object_date.getString("notes"),object_date.getInt("userid"),object_date.getInt("jobid"),"",0,object_date.getInt("id"),object_date.getString("state"));
                    SQLiteDatabase db = timesheet.getReadableDatabase();
                    Cursor cursor_value = db.rawQuery("Select max(id) as id from " + TimeSheetDatabaseHelper.TABLE_NAME,new String[]{});
                    if(cursor_value.moveToFirst())
                    {
                        int id = cursor_value.getInt(cursor_value.getColumnIndex("id"));
                        Log.d("attachment",attachement);
                        String[] attachment_array = TextUtils.split(",",attachement);
                        for(int index = 0;index < attachment_array.length;index++)
                        {
                            if(!attachment_array[index].isEmpty() && !attachment_array[index].contentEquals(","))
                            {
                                Log.d("attachment_index",attachment_array[index]);
                                new AttachmentDownloadtask(id,context).execute(attachment_array[index]);
                            }
                        }
                    }
                }
                else
                {
                    int index = id_array.indexOf(array.getJSONObject(i).getInt("id"));
                    id_array.remove(index);
                }
            }

            for(int i = 0;i<id_array.size();i++)
            {
                timesheet.deletebyrefid(id_array.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    public static void Logout(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences("credential",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_id","");
        editor.putString("role","");
        editor.putString("user","");
        editor.putString("profile","");
        editor.commit();
        Intent intent = new Intent(context,MainActivity.class);
        context.startActivity(intent);
    }
    public static void update_timesheet(JSONObject object,Context context)
    {

    }

    public static boolean isBrightColor(int color) {
        if (android.R.color.transparent == color)
            return true;

        boolean rtnValue = false;

        int[] rgb = { Color.red(color), Color.green(color), Color.blue(color) };

        int brightness = (int) Math.sqrt(rgb[0] * rgb[0] * .241 + rgb[1]
                * rgb[1] * .691 + rgb[2] * rgb[2] * .068);

        // color is light
        if (brightness >= 200) {
            rtnValue = true;
        }

        return rtnValue;
    }
    public static void initdata(final Context context)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        ShiftDatabaseHelper shift = new ShiftDatabaseHelper(context);
        EmployeerDatabasehelper employee = new EmployeerDatabasehelper(context);
        TimeSheetDatabaseHelper timesheet = new TimeSheetDatabaseHelper(context);
        final Handler init_handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                String jsonbody = bundle.getString("data");
                Log.d("jsonbody",jsonbody);
                try {
                    JSONObject object = new JSONObject(jsonbody);
                    if(object.getBoolean("success"))
                    {
                        initdatabase(object,context);
                    }
                    else
                    {
                        Logout(context);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        SharedPreferences preferences = context.getSharedPreferences("credential",Context.MODE_PRIVATE);
        String user_id = preferences.getString("user_id","0");
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url(API.url + "/data/get/" + user_id).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                Log.d("responsebody",body);
                if(response.isSuccessful())
                {
                    Message msg = init_handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("data",body);
                    msg.setData(bundle);
                    init_handler.sendMessage(msg);
                }
            }
        });
    }
}

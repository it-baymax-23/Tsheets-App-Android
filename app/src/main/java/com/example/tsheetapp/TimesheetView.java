package com.example.tsheetapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.lang.Math.abs;

public class TimesheetView extends AppCompatActivity implements View.OnClickListener{
    JSONArray array;
    SimpleDateFormat format;
    List<JSONObject> listarray;
    SharedPreferences preferences;
    String user_id;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timesheet);
        format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.getDefault());
        TextView user = (TextView)findViewById(R.id.username);
        CircleImageView imageView = (CircleImageView)findViewById(R.id.image_user);
        Constants.display_profile(this,user,imageView);
        init();
    }

    @Override
    protected void onStart() {
        init();
        super.onStart();
    }

    @Override
    protected void onResume() {
        init();
        super.onResume();
    }

    public void init()
    {
        TimeSheetDatabaseHelper helper = new TimeSheetDatabaseHelper(this);
        Cursor cursor = helper.get_json();
        array = new JSONArray();
        TextView user = (TextView)findViewById(R.id.username);
        preferences = getSharedPreferences("credential",MODE_PRIVATE);
        user_id = preferences.getString("user_id","");
        String user_name = preferences.getString("user","");
        user.setText(user_name.substring(0,1).toUpperCase());
        if(cursor.moveToFirst())
        {
            do {
                JSONObject object = new JSONObject();
                String starttime = cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_STARTTIME));
                String endtime = "";
                String state = cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_STATE));
                if(!cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_STATE)).contentEquals("clockin"))
                {
                    endtime = cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_ENDTIME));
                }

                String jobname = cursor.getString(cursor.getColumnIndex("jobname"));
                int jobid = cursor.getInt(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_JOBID));
                int id = cursor.getInt(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_ID));
                Calendar starttimeentry = Calendar.getInstance();
                Calendar endtimeentry = Calendar.getInstance();
                try {
                    starttimeentry.setTime(format.parse(starttime));
                    if(!state.contentEquals("clockin"))
                    {
                        endtimeentry.setTime(format.parse(endtime));
                        object.put(TimeSheetDatabaseHelper.KEY_ENDTIME,endtimeentry);
                    }

                    object.put("state",state);

                    object.put(TimeSheetDatabaseHelper.KEY_STARTTIME,starttimeentry);
                    object.put("jobname",jobname);
                    object.put(TimeSheetDatabaseHelper.KEY_JOBID,jobid);
                    object.put(TimeSheetDatabaseHelper.KEY_ID,id);
                    object.put(TimeSheetDatabaseHelper.KEY_REFID,cursor.getInt(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_REFID)));
                    array.put(object);
                    Log.d("jsonarray",object.toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }while(cursor.moveToNext());
        }

        listarray = new ArrayList<>();

        Calendar today = Calendar.getInstance();
        int selected = -1;
        JSONArray array_default = new JSONArray();
        int j = 0;

        Log.d("array_length",String.valueOf(array.length()));
        for(int i = 0;i<array.length();i++)
        {
            try {
                JSONObject object = array.getJSONObject(i);
                JSONObject newobject = new JSONObject();
                String state = object.getString("state");
                Calendar startime = (Calendar) object.get(TimeSheetDatabaseHelper.KEY_STARTTIME);
                Calendar endtime = null;
                newobject.put("state",state);
                if(!state.contentEquals("clockin"))
                {
                    endtime = (Calendar)object.get(TimeSheetDatabaseHelper.KEY_ENDTIME);
                    newobject.put("endtime",endtime);
                }
                newobject.put("jobname",object.getString("jobname"));
                newobject.put("starttime",startime);

                newobject.put("type","shift");
                newobject.put("id",object.getInt("id"));
                newobject.put("ref_id",object.getInt("ref_id"));
                if(Check_Same(startime,today) && selected != -1)
                {
                    long delay = array_default.getJSONObject(selected).getLong("duration");
                    if(!state.contentEquals("clockin"))
                    {
                        delay += endtime.getTimeInMillis() - startime.getTimeInMillis();
                    }
                    array_default.getJSONObject(selected).put("duration",delay);
                }
                else
                {
                    selected = j;
                    JSONObject parent = new JSONObject();
                    parent.put("date",startime);
                    if(!state.contentEquals("clockin"))
                    {
                        parent.put("duration",endtime.getTimeInMillis() - startime.getTimeInMillis());
                    }
                    else
                    {
                        parent.put("duration",(long)0);
                    }
                    parent.put("type","total");
                    array_default.put(parent);
                    today = (Calendar) startime.clone();
                    j++;
                }
                array_default.put(newobject);
                j++;
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("exception",e.getMessage());
                Log.d("select_index",String.valueOf(selected));

            }
        }

        Log.d("array_default_length",String.valueOf(array_default.length()));
        Log.d("array_j",String.valueOf(j));
        for(int i = 0;i<array_default.length();i++)
        {
            try {
                JSONObject newobject = array_default.getJSONObject(i);

                String type = (String) newobject.get("type");
                JSONObject timesheetentry = new JSONObject();
                if(type.contentEquals("total"))
                {
                    timesheetentry.put("date",getdate((Calendar) newobject.get("date")));
                    timesheetentry.put("duration",getduration((Long) newobject.get("duration")));
                    timesheetentry.put("type","total");
                }
                else
                {
                    Calendar starttime = (Calendar) newobject.get("starttime");


                    timesheetentry.put("jobname",(String)newobject.get("jobname"));
                    timesheetentry.put("id",(int)newobject.get("id"));

                    if(!newobject.getString("state").contentEquals("clockin"))
                    {
                        Calendar endtime = (Calendar) newobject.get("endtime");
                        timesheetentry.put("duration",getduration(endtime.getTimeInMillis() - starttime.getTimeInMillis()));
                        timesheetentry.put("period",gettime(starttime,endtime));
                    }
                    else
                    {
                        SimpleDateFormat format = new SimpleDateFormat("hh:mm a",Locale.getDefault());
                        timesheetentry.put("period",format.format(new Date(starttime.getTimeInMillis())) + " - ");
                    }
                    timesheetentry.put("state",newobject.getString("state"));
                    timesheetentry.put("type","shift");
                    timesheetentry.put("ref_id",newobject.getInt("ref_id"));
                }
                Log.d("jsonobject",timesheetentry.toString());
                listarray.add(timesheetentry);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.d("listitemarray",String.valueOf(listarray.size()));
        TimeSheetList list = new TimeSheetList(this,R.layout.list_item,listarray);

        ListView listview = (ListView)findViewById(R.id.timesheetcontainer);
        listview.setAdapter(list);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject object = listarray.get(position);
                Log.d("timesheetarray","array");
                try {
                    Log.d("timesheetlistitem",object.getString("type"));
                    if(object.getString("type").contentEquals("shift")) {
                        int id_tsheet = object.getInt("id");
                        Intent intent = new Intent(getApplicationContext(), TimeSheetEdit.class);
                        intent.putExtra("sheet_id", id_tsheet);
                        intent.putExtra("ref_id",object.getInt("ref_id"));
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    public boolean Check_Same(Calendar startime,Calendar endtime)
    {
        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
        if(dft.format(startime.getTimeInMillis()).contentEquals(dft.format(endtime.getTimeInMillis())))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public String getdate(Calendar date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("E MMM, dd", Locale.getDefault());
        Log.d("get_date",sdf.format(date.getTimeInMillis()));
        return sdf.format(date.getTimeInMillis());
    }

    public String getduration(long millis)
    {

        Log.d("millisecond",String.valueOf(millis));
        int hours = (int) (abs(millis) / (1000 * 60 * 60));
        int mins = (int) ((abs(millis)/(1000*60)) % 60);
        int secs = (int) ((abs(millis) / 1000) % 60);

        NumberFormat f = new DecimalFormat("00");
        String str = "";

        if(millis < 0)
        {
            str = "-";
        }
        if(hours > 0)
        {
            str += f.format(hours) + "h ";
        }

        str += f.format(mins) + "m " + f.format(secs) + "s";

        return str;

    }

    public String gettime(Calendar starttime,Calendar endtime)
    {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a",Locale.getDefault());
        return format.format(starttime.getTimeInMillis()) + "-" + format.format(endtime.getTimeInMillis());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.overview:
                Intent intent = new Intent(this,DashboardActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.left_out,R.anim.right_out);
                break;
            case R.id.timesheet:
                intent = new Intent(this,TimesheetView.class);
                overridePendingTransition(R.anim.left_out,R.anim.right_out);
                startActivity(intent);
                break;
            case R.id.schedule:
                intent = new Intent(this,ShiftView.class);
                overridePendingTransition(R.anim.right_in,R.anim.right_out);
                startActivity(intent);
                break;
            case R.id.timecard:
                intent = new Intent(this,TimeCardActivity.class);
                overridePendingTransition(R.anim.left_out,R.anim.right_out);
                startActivity(intent);
                break;
            case R.id.more:
                intent = new Intent(this,MoreActivity.class);
                overridePendingTransition(R.anim.right_in,R.anim.right_out);
                startActivity(intent);
                break;
            case R.id.username:
            case R.id.image_user:
                intent = new Intent(this,Profile.class);
                overridePendingTransition(R.anim.top_in,R.anim.right_out);
                startActivity(intent);
                break;
            case R.id.addtimesheet:
                intent = new Intent(this,AddTimeSheet.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                break;
            case R.id.prev:
                finish();
                break;
        }

    }
}

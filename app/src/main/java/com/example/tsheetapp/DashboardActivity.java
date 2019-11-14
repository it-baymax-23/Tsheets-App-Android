package com.example.tsheetapp;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener{
    PieChartView pieChart;
    ArrayList<Integer> jobid;
    ArrayList<Long> delay;
    ArrayList<String> jobname;
    LinearLayout daytotal;
    LinearLayout weektotal;
    ProgressBar progress;
    String user_id;
    SharedPreferences preference;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        pieChart = (PieChartView)findViewById(R.id.piechart);
        init();
        initdisplay();
        initschedule();
    }

    public void init(){
        TimeSheetDatabaseHelper helper = new TimeSheetDatabaseHelper(this);
        Cursor cursor = helper.gettimesheetbyweek();
        jobid = new ArrayList<Integer>();
        delay = new ArrayList<Long>();
        jobname = new ArrayList<String>();
        preference = getSharedPreferences("credential",MODE_PRIVATE);
        TextView user = (TextView)findViewById(R.id.username);
        CircleImageView imageView = (CircleImageView)findViewById(R.id.image_user);
        Constants.display_profile(this,user,imageView);
        user_id = preference.getString("user_id","0");
        if(cursor.moveToFirst())
        {
            do{
                if(!cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_STATE)).contentEquals("clockin"))
                {
                    int id = cursor.getInt(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_JOBID));
                    if(jobid.indexOf(id) == -1)
                    {
                        jobid.add(id);
                        delay.add((long) 0);
                        jobname.add(cursor.getString(cursor.getColumnIndex("jobname")));
                    }

                    int index = jobid.indexOf(id);
                    long delaytime = delay.get(index);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.getDefault());
                    String starttime = cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_STARTTIME));
                    String endtime = cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_ENDTIME));

                    try {
                        delaytime+= format.parse(endtime).getTime() - format.parse(starttime).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    delay.set(index,delaytime);
                }

            }
            while(cursor.moveToNext());
        }

        List<SliceValue> piedata = new ArrayList<>();
        Random random = new Random();
        for(int i = 0; i <jobid.size();i++)
        {
            piedata.add(new SliceValue(delay.get(i),Color.argb(255,random.nextInt(256),random.nextInt(256),random.nextInt(256))).setLabel(calculate_time(delay.get(i))));

        }

        PieChartData pieChartData = new PieChartData(piedata);
        pieChartData.setHasLabels(true);
        pieChartData.setHasCenterCircle(true);
        pieChartData.setValueLabelsTextColor(Color.argb(255,0,0,0));
        pieChartData.setValueLabelTextSize(10);
        pieChartData.setCenterCircleScale(0.2f);

        pieChart.setPieChartData(pieChartData);
        pieChart.startDataAnimation();

    }

    public void initdisplay(){
        daytotal = (LinearLayout)findViewById(R.id.daytotal);
        weektotal = (LinearLayout)findViewById(R.id.weektotal);
        progress = (ProgressBar)findViewById(R.id.progressBar);
        displaytype("daytotal");

    }

    public long get_daily_hour(Cursor cursor,String type)
    {
        long time = 0;
        String starttime = "";
        String endtime;
        String jobname = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss",Locale.getDefault());
        if(cursor.moveToFirst())
        {
            do {
                if(!cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_STATE)).contentEquals("clockin"))
                {
                    starttime = cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_STARTTIME));
                    endtime = cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_ENDTIME));
                    jobname = cursor.getString(cursor.getColumnIndex("jobname"));
                    try {
                        time += sdf.parse(endtime).getTime() - sdf.parse(starttime).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

            }while(cursor.moveToNext());
        }
        Log.d("starttimelong",String.valueOf(time));
        TextView clockedin = (TextView)findViewById(R.id.clockedin_time);
        TextView jobtext = (TextView)findViewById(R.id.clockedin_job);
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a",Locale.getDefault());
        try {
            if(type.contentEquals("daytotal"))
            {
                TimeSheetDatabaseHelper helper = new TimeSheetDatabaseHelper(this);
                SQLiteDatabase db = helper.getReadableDatabase();
                Cursor cursor_value = db.rawQuery("Select " + TimeSheetDatabaseHelper.TABLE_NAME + ".*,job.jobname as jobname from " + TimeSheetDatabaseHelper.TABLE_NAME + ",job where " + TimeSheetDatabaseHelper.TABLE_NAME + ".state ='clockin' and " + TimeSheetDatabaseHelper.TABLE_NAME + "." + TimeSheetDatabaseHelper.KEY_JOBID + " = job.id" ,new String[]{});
                Log.d("tag",String.valueOf(cursor_value.getCount()));
                if(cursor_value.moveToFirst())
                {
                    clockedin.setText(format.format(sdf.parse(cursor_value.getString(cursor_value.getColumnIndex(TimeSheetDatabaseHelper.KEY_STARTTIME)))));
                }
                else
                {
                    clockedin.setText("_ _ _ _");
                }

                if(cursor_value.moveToFirst())
                {
                    jobtext.setText(cursor_value.getString(cursor_value.getColumnIndex("jobname")));
                }
                else
                {
                    jobtext.setText("_ _ _ _");
                }
            }
            else
            {
                Calendar today = Calendar.getInstance();
                today.setFirstDayOfWeek(Calendar.SUNDAY);
                today.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);

                Calendar next = (Calendar) today.clone();
                next.add(Calendar.DATE,6);
                SimpleDateFormat dateformat = new SimpleDateFormat("MM/dd",Locale.getDefault());
                clockedin.setText(dateformat.format(today.getTimeInMillis()));
                jobtext.setText(dateformat.format(next.getTimeInMillis()));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }


        return time;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void displaytype(String type)
    {
        TextView displaytotaltxt = (TextView)findViewById(R.id.daytotaltxt);
        TextView weektotaltxt = (TextView)findViewById(R.id.weektotaltxt);
        TextView clockedin_title = (TextView)findViewById(R.id.clockedin_title);
        TextView job_title = (TextView)findViewById(R.id.job_title);
        TimeSheetDatabaseHelper helper = new TimeSheetDatabaseHelper(this);

        TextView total_time = (TextView)findViewById(R.id.total_time);
        if(type.contentEquals("daytotal"))
        {
            Calendar today = Calendar.getInstance();
            Cursor cursor = helper.gettimesheetbydate(today,user_id);
            Log.d("useriud",user_id);
            Log.d("cursorcolumncount",String.valueOf(cursor.getCount()));
            long times = get_daily_hour(cursor,type);
            weektotal.setBackground(null);
            daytotal.setBackgroundResource(R.drawable.border);
            displaytotaltxt.setTextAppearance(this,R.style.active_string_total);
            weektotaltxt.setTextAppearance(this,R.style.deactive_string_total);
            TextView timevalue = (TextView)findViewById(R.id.timevalue);

            progress.setProgress(Math.min(100,(int) (times/(8 * 60 * 60 * 10))));
            Log.d("calctime",String.valueOf(times));
            if(times > 8 * 60 * 60 * 1000)
            {
                progress.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar_red));
            }
            timevalue.setText(calculate_time(times));
            progress.invalidate();
            job_title.setText(getResources().getString(R.string.job));
            clockedin_title.setText(getResources().getString(R.string.clocked_in));
            total_time.setText("of 8.00 hours");
        }
        else
        {
            Cursor cursor = helper.gettimesheetbyweek();
            long times = get_daily_hour(cursor,"weekday");
            weektotal.setBackgroundResource(R.drawable.border);
            daytotal.setBackground(null);
            weektotaltxt.setTextAppearance(this,R.style.active_string_total);
            displaytotaltxt.setTextAppearance(this,R.style.deactive_string_total);
            progress.setProgress(Math.min(100,(int) (times/(40 * 60 * 60 * 10))));
            if(times > 40 * 60 * 60 * 1000)
            {
                progress.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar_red));
            }
            TextView timevalue = (TextView)findViewById(R.id.timevalue);
            timevalue.setText(calculate_time(times));
            job_title.setText("Ended");
            clockedin_title.setText("Started");
            total_time.setText("of 40 hours");
        }
        Log.d("progressvalue",String.valueOf(progress.getProgress()));
    }

    public String calculate_time(long time)
    {
        int hours = (int)(time / (1000 * 60 *(60)));
        int mins = (int)(time / (1000 * 60) % 60);

        NumberFormat f = new DecimalFormat("00");

        String str = f.format(hours) + " h " + f.format(mins) + " m";
        return str;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.weektotal:
                displaytype("weektotal");
                break;
            case R.id.daytotal:
                displaytype("daytotal");
                break;
            case R.id.username:
            case R.id.image_user:
                Intent intent = new Intent(this,Profile.class);
                startActivity(intent);
                overridePendingTransition(R.anim.top_in,R.anim.right_out);
                break;
            case R.id.timesheet:
                intent = new Intent(this,TimesheetView.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in,R.anim.right_out);
                break;
            case R.id.schedule:
                intent = new Intent(this,ShiftView.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in,R.anim.right_out);
                break;
            case R.id.more:
                intent = new Intent(this,MoreActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in,R.anim.right_out);
                break;
            case R.id.timecard:
                intent = new Intent(this,TimeCardActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in,R.anim.right_out);
                break;
            case R.id.fullschedule:
                intent = new Intent(this,ShiftView.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in,R.anim.right_out);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void initschedule()
    {
        LinearLayout schedule = (LinearLayout)findViewById(R.id.view_schedule);
        LayoutInflater inflater = LayoutInflater.from(this);

        ShiftDatabaseHelper helper = new ShiftDatabaseHelper(this);
        Cursor cursor = helper.get_schedule_for_date(Integer.valueOf(user_id),Calendar.getInstance());

        if(cursor.moveToFirst())
        {
            schedule.removeAllViews();
            do {
                View v = inflater.inflate(R.layout.schedule_list_item,null);
                String title = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_TITLE));

                if(title.isEmpty())
                {
                    title = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_NOTES));
                }

                if(title.isEmpty())
                {
                    title = cursor.getString(cursor.getColumnIndex("jobtitle"));
                }

                int allday = cursor.getInt(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_SHIFTTYPE));
                String time = "";
                if(allday == 1)
                {
                    time = "All Day";
                }
                else
                {
                    time = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_STARTTIME)) + " - " + cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_ENDTIME));
                }

                RelativeLayout container = (RelativeLayout) v.findViewById(R.id.container);
                container.setBackgroundResource(R.drawable.background_resource);
                GradientDrawable drawable = (GradientDrawable) container.getBackground();
                drawable.setColor(Color.parseColor(cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_COLOR))));
                TextView titletext = (TextView) v.findViewById(R.id.title);
                titletext.setText(title);
                TextView timetext = (TextView)v.findViewById(R.id.datetime);
                timetext.setText(time);
                if(Constants.isBrightColor(Color.parseColor(cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_COLOR)))))
                {
                    titletext.setTextColor(getResources().getColor(android.R.color.black));
                    timetext.setTextColor(getResources().getColor(android.R.color.black));
                }
                else
                {
                    titletext.setTextColor(getResources().getColor(android.R.color.white));
                    timetext.setTextColor(getResources().getColor(android.R.color.white));
                }
                schedule.addView(v);
            }while(cursor.moveToNext());
        }
    }
}

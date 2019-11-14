package com.example.tsheetapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShiftAddActivity extends AppCompatActivity implements View.OnClickListener{
    public static int REQEUST_JOB = 0;
    public static int REQUEST_NOTES = 1;
    public static int REQUEST_LOCATION = 2;
    public static int REQUEST_ASSIGN = 3;
    public static int REQUEST_COLOR = 4;
    public static String user_get = API.url + "/user/get";
    public static String save_shift_url = API.url + "/shift/save";
    public int jobid = 0;
    public String userid = "";
    public String location = "";
    public String notes = "";
    String color;
    public boolean allday;
    public ErrorDialog error;
    int id = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_shift);
        allday = false;
        Switch all_day = (Switch)findViewById(R.id.switch_all_day);
        all_day.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                allday = isChecked;
                display(isChecked);
            }
        });
        initdatabase();
        Intent intent = getIntent();
        id = intent.getIntExtra("id",0);
        Log.d("schedule_id",String.valueOf(id));
        if(id == 0)
        {
            display_time();
        }
        else{
            init_display(id);
        }
        Button publish = (Button)findViewById(R.id.publish);
        Button draft = (Button)findViewById(R.id.draft);
        publish.setOnClickListener(this);
        draft.setOnClickListener(this);
    }

    private void init_display(int id) {
        ShiftDatabaseHelper helper = new ShiftDatabaseHelper(this);
        Log.d("schedule_id",String.valueOf(id));
        Cursor cursor = helper.getschedulebyid(id);
        if(cursor.moveToFirst())
        {
            EditText title = (EditText)findViewById(R.id.shifttitle);
            title.setText(cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_TITLE)));
            int shift_type = cursor.getInt(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_SHIFTTYPE));
            allday = shift_type == 1?true:false;
            TextView date = (TextView)findViewById(R.id.date_select);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
            SimpleDateFormat sdf = new SimpleDateFormat("E,MMM dd,yyyy",Locale.getDefault());
            try {
                date.setText(sdf.format((format.parse(cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_DATE))))));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            TextView startime = (TextView)findViewById(R.id.starttime);
            TextView endtime = (TextView)findViewById(R.id.endtime);
            if(allday)
            {
                startime.setVisibility(View.GONE);
                endtime.setVisibility(View.GONE);
                Switch all_day = (Switch)findViewById(R.id.switch_all_day);
                all_day.setChecked(true);
            }
            else
            {
                startime.setText(cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_STARTTIME)));
                endtime.setText(cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_ENDTIME)));
            }

            jobid = cursor.getInt(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_JOBID));
            String jobname = cursor.getString(cursor.getColumnIndex("jobname"));
            if(!jobname.isEmpty())
            {
                TextView jobnametext = (TextView)findViewById(R.id.addjobtext);
                jobnametext.setText(jobname);
            }

            String assigned = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_ASSIGNS));
            EmployeerDatabasehelper helper_user = new EmployeerDatabasehelper(this);
            Cursor users = helper_user.get_users(assigned);
            TextView assign_text = (TextView)findViewById(R.id.employees);
            if(users.getCount() > 0)
            {
                String[] userarray = new String[users.getCount()];
                userid = assigned;
                if(users.moveToFirst())
                {
                    int j = 0;
                    do {
                        userarray[j] = users.getString(users.getColumnIndex(EmployeerDatabasehelper.KEY_USERNAME));
                        j++;
                    }while(users.moveToNext());

                    assign_text.setText(TextUtils.join(",",userarray));
                }
            }
            else
            {
                assign_text.setText("unassigned");
            }

            location = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_LOCATION));
            TextView location_text = (TextView)findViewById(R.id.locationtext);
            if(!location.isEmpty())
            {
                location_text.setText(location);
            }

            color = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_COLOR));

            int index = -1;
            for(int i = 0;i<Constants.colors.length;i++)
            {
                if(Constants.colors[i].contentEquals(color))
                {
                    index = i;
                }
            }

            LinearLayout layout = (LinearLayout)findViewById(R.id.colorselect);
            layout.setBackgroundColor(Color.parseColor(color));
            if(index != -1)
            {
                TextView colortext = (TextView)findViewById(R.id.colorname);
                colortext.setText(Constants.colorname[index]);
            }

            notes = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_NOTES));
            if(!notes.isEmpty())
            {
                TextView note_text = (TextView)findViewById(R.id.notestext);
                note_text.setText(notes);
            }

            TextView actiontitle = (TextView)findViewById(R.id.action_title);
            actiontitle.setText("Edit Shift");
        }
    }

    Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String message = bundle.getString("message");
            String data = bundle.getString("data");

            try {
                JSONObject responseobject = new JSONObject(data);
                if(responseobject.getBoolean("success"))
                {
                    JSONArray dataarray = responseobject.getJSONArray("data");
                    ShiftDatabaseHelper helper = new ShiftDatabaseHelper(getApplicationContext());
                    helper.replacedatabase(dataarray);

                }
            } catch (JSONException e) {
                Log.d("jsonexceptiondata",e.getMessage());
                e.printStackTrace();
            }
        }
    };

    public void display_time()
    {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("E,MMM dd,yyyy",Locale.getDefault());
        TextView date = (TextView)findViewById(R.id.date_select);
        TextView starttime = (TextView)findViewById(R.id.starttime);
        TextView endtime = (TextView)findViewById(R.id.endtime);
        date.setText(format.format(c.getTimeInMillis()));
        SimpleDateFormat f = new SimpleDateFormat("hh:mm a",Locale.getDefault());
        starttime.setText(f.format(c.getTimeInMillis()));
        endtime.setText(f.format(c.getTimeInMillis()));
    }
    public void display(boolean checked)
    {
        TextView starttime = (TextView)findViewById(R.id.starttime);
        TextView endtime = (TextView)findViewById(R.id.endtime);
        if(checked)
        {
            starttime.setVisibility(View.GONE);
            endtime.setVisibility(View.GONE);
        }
        else
        {
            starttime.setVisibility(View.VISIBLE);
            endtime.setVisibility(View.VISIBLE);
        }
    }
    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.addjob:
                Intent intent = new Intent(ShiftAddActivity.this,SearchJob.class);
                startActivityForResult(intent,REQEUST_JOB);
                break;
            case R.id.date_select:
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int mday = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(this, AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar date = Calendar.getInstance();
                        date.set(Calendar.YEAR,year);
                        date.set(Calendar.MONTH,month);
                        date.set(Calendar.DAY_OF_MONTH,dayOfMonth);

                        SimpleDateFormat format = new SimpleDateFormat("E,MMM dd, yyyy",Locale.getDefault());

                        TextView text = (TextView)findViewById(R.id.date_select);
                        text.setText(format.format(date.getTimeInMillis()));
                    }
                },year,month,mday);

                dialog.show();
                break;
            case R.id.starttime:
                c = Calendar.getInstance();
                int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
                int minutes = c.get(Calendar.MINUTE);

                TimePickerDialog timedialog = new TimePickerDialog(this, AlertDialog.THEME_HOLO_LIGHT,new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar m = Calendar.getInstance();
                        m.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        m.set(Calendar.MINUTE,minute);

                        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
                        TextView text = (TextView)findViewById(R.id.starttime);
                        text.setText(format.format(m.getTimeInMillis()));
                    }
                },hourOfDay,minutes,false);

                timedialog.show();
                break;

            case R.id.endtime:
                c = Calendar.getInstance();
                hourOfDay = c.get(Calendar.HOUR_OF_DAY);
                minutes = c.get(Calendar.MINUTE);

                timedialog = new TimePickerDialog(this , AlertDialog.THEME_HOLO_LIGHT,new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar m = Calendar.getInstance();
                        m.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        m.set(Calendar.MINUTE,minute);

                        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
                        TextView text = (TextView)findViewById(R.id.endtime);
                        text.setText(format.format(m.getTimeInMillis()));
                    }
                },hourOfDay,minutes,false);

                timedialog.show();
                break;
            case R.id.add_notes:
                intent = new Intent(this,TextAreaActivity.class);
                intent.putExtra("title","Add Notes");
                startActivityForResult(intent,REQUEST_NOTES);
                break;
            case R.id.add_location:
                intent = new Intent(this,TextAreaActivity.class);
                intent.putExtra("title","Add Location");
                startActivityForResult(intent,REQUEST_LOCATION);
                break;
            case R.id.coloralert:
                intent = new Intent(this, ColorActivity.class);
                startActivityForResult(intent,REQUEST_COLOR);
                break;
            case R.id.unassigned:
                intent = new Intent(this,EmployeeActivity.class);
                startActivityForResult(intent,REQUEST_ASSIGN);
                break;
            case R.id.draft:
                String status = "draft";
                save_shift(status);
                break;
            case R.id.publish:
                status = "publish";
                save_shift(status);
                break;
            case R.id.close:
                finish();
        }
    }

    public void save_shift(String status)
    {
        SimpleDateFormat format = new SimpleDateFormat("E,MMM dd,yyyy",Locale.getDefault());
        SimpleDateFormat convert = new SimpleDateFormat("yyyy-MM-dd");
        TextView textView = (TextView)findViewById(R.id.date_select);

        try {
            String date = convert.format(format.parse(textView.getText().toString()));
            Log.d("dateparse",date);

            if(allday)
            {
                SimpleDateFormat formatdate = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
                if(!new Date().before(convert.parse(date)) && !formatdate.format(new Date()).contentEquals(formatdate.format(convert.parse(date))))
                {
                    display_error("you have selected wrong date");
                    return;
                }
            }
            String starttime = "";
            String endtime = "";
            if(!allday)
            {
                TextView starttimeentry = (TextView)findViewById(R.id.starttime);
                TextView endtimeentry = (TextView)findViewById(R.id.endtime);
                starttime = starttimeentry.getText().toString();
                endtime = starttimeentry.getText().toString();

                SimpleDateFormat formattime = new SimpleDateFormat("yyyy-MM-dd hh:mm a",Locale.getDefault());

                String comparestartdate = date + " " + starttime;
                String compareenddate = date + " " + endtime;
                Log.d("starttimestring",comparestartdate);
                Log.d("starttimelong",String.valueOf((formattime.parse(comparestartdate))));
                if(formattime.parse(comparestartdate).getTime() < Calendar.getInstance().getTimeInMillis() || formattime.parse(compareenddate).getTime() < formattime.parse(comparestartdate).getTime())
                {
                    display_error("you have selected wrong time, Please retry again");
                    return;
                }

            }

            if(jobid == 0)
            {
                display_error("Please select the job");
                return;
            }

            OkHttpClient client = new OkHttpClient();
            MultipartBody.Builder request_body = new MultipartBody.Builder().setType(MultipartBody.FORM);
            EditText title = (EditText)findViewById(R.id.shifttitle);
            request_body.addFormDataPart("title",title.getText().toString());
            request_body.addFormDataPart("date",date);
            request_body.addFormDataPart("starttime",starttime);
            request_body.addFormDataPart("endtime",endtime);
            request_body.addFormDataPart("jobid",String.valueOf(jobid));
            request_body.addFormDataPart("shift_type",String.valueOf(allday));
            request_body.addFormDataPart("assigns",userid);
            request_body.addFormDataPart("location",location);
            request_body.addFormDataPart("color",color);
            request_body.addFormDataPart("notes",notes);
            request_body.addFormDataPart("status",status);
            if(id > 0)
            {
                Log.d("id",String.valueOf(id));
                request_body.addFormDataPart("id",String.valueOf(id));
            }
            RequestBody body = request_body.build();
            Request save_shift = new Request.Builder().url(save_shift_url).post(body).build();

            client.newCall(save_shift).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String body = response.body().string();
                    Log.d("uploadprocess",body);
                    if(response.isSuccessful())
                    {
                        Message message = mhandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("message","you have successfully saved shift");
                        bundle.putString("data",body);
                        message.setData(bundle);
                        mhandler.sendMessage(message);
                    }
                }
            });

        } catch (ParseException e) {
            Log.d("parseerror",e.getMessage());
            e.printStackTrace();
        }
    }

    public void display_error(String str)
    {
        error = new ErrorDialog(this);
        error.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        error.show();
        TextView error_message = error.findViewById(R.id.error_message);
        error_message.setText(str);

        Button ok_btn = (Button)error.findViewById(R.id.ok);
        ok_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                error.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQEUST_JOB)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                String jobname = data.getStringExtra("jobname");
                TextView text = (TextView) findViewById(R.id.addjobtext);
                jobid = data.getIntExtra("jobid",0);
                text.setText(jobname);
            }
        }
        else if(requestCode == REQUEST_NOTES)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                String notes_str = data.getStringExtra("name");
                if(notes_str == null)
                {
                    notes_str = "Add a Notes";
                }
                else
                {
                    notes = notes_str;
                }
                TextView text = (TextView)findViewById(R.id.notestext);
                text.setText(notes_str);
            }
        }
        else if(requestCode == REQUEST_LOCATION)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                String locationstr = data.getStringExtra("name");
                if(locationstr == null)
                {
                    locationstr = "Add a Location";
                   location = "";
                }
                else
                {
                    location = locationstr;
                }
                TextView text = (TextView)findViewById(R.id.locationtext);
                text.setText(locationstr);
            }
        }
        else if(requestCode == REQUEST_COLOR)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                String colorname = data.getStringExtra("colorname");
                TextView text = (TextView)findViewById(R.id.colorname);
                text.setText(colorname);
                color = data.getStringExtra("color");
                LinearLayout layout = (LinearLayout)findViewById(R.id.colorselect);
                layout.setBackgroundColor(Color.parseColor(color));
            }
        }
        else if(requestCode == REQUEST_ASSIGN)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                String username = data.getStringExtra("username");
                TextView text = (TextView)findViewById(R.id.employees);
                if(username != null)
                {
                    userid = data.getStringExtra("userid");
                    text.setText(username);
                }
                else
                {
                    userid = "";
                    text.setText("unassigned");
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void initdatabase()
    {
        OkHttpClient client = new OkHttpClient();

        Request body_request = new Request.Builder().url(user_get).get().build();

        client.newCall(body_request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responsestr = response.body().string();
                Log.d("uploadprogress",responsestr);
                try {
                    JSONArray array = new JSONArray(responsestr);
                    EmployeerDatabasehelper helper = new EmployeerDatabasehelper(getApplicationContext());
                    helper.database_update(array);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

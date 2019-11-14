package com.example.tsheetapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ScheduleView extends AppCompatActivity implements View.OnClickListener{
    int id = 0;
    DeleteDialog dialog;
    DeleteDialog dialog_delete;
    String delete_url = API.url + "/shift/delete/";
    String title,date,starttime,endtime,jobname,color,colorname,notes,location,userid;
    int shift_type;
    int jobid;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_edit);
        Intent intent = getIntent();
        id = intent.getIntExtra("id",0);
        String role = getSharedPreferences("credential",MODE_PRIVATE).getString("role","");
        if(role.contentEquals("employee"))
        {
            TextView edit = (TextView)findViewById(R.id.edit);
            edit.setVisibility(View.GONE);
            TextView delete = (TextView)findViewById(R.id.more);
            delete.setVisibility(View.GONE);
        }
//        TextView user = (TextView)findViewById(R.id.username);
//        CircleImageView imageView = (CircleImageView)findViewById(R.id.image_user);
//        Constants.display_profile(this,user,imageView);

    }

    @Override
    protected void onStart() {
        if(id > 0)
        {
            init(id);
        }
        else
        {
            finish();
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        if(id > 0)
        {
            init(id);
        }
        else
        {
            finish();
        }
        super.onResume();
    }

    public void init(int id)
    {
        ShiftDatabaseHelper helper = new ShiftDatabaseHelper(this);
        Cursor cursor = helper.getschedulebyid(id);
        if(cursor.moveToFirst())
        {
            title = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_TITLE));
            if(title.isEmpty())
            {
                title = "No Title";
            }

            TextView titletext = (TextView)findViewById(R.id.title);
            titletext.setText(title);
            jobname = cursor.getString(cursor.getColumnIndex("jobname"));
            TextView title_job = (TextView)findViewById(R.id.jobname);
            title_job.setText(jobname);
            location = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_LOCATION));
            if(location.isEmpty())
            {
                TextView text_location = (TextView)findViewById(R.id.location);
                text_location.setText(location);
                LinearLayout layout_location = (LinearLayout)findViewById(R.id.location_container);
                layout_location.setVisibility(View.VISIBLE);
            }
            date = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_DATE));
            TextView date_text = (TextView)findViewById(R.id.date);
            date_text.setText(date);
            shift_type = cursor.getInt(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_SHIFTTYPE));
            TextView textView = (TextView)findViewById(R.id.time);
            if(shift_type == 1)
            {
                textView.setVisibility(View.GONE);
            }
            else
            {
                starttime = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_STARTTIME));
                endtime = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_ENDTIME));
                textView.setText(starttime + " - " + endtime);
            }

            String assigned = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_ASSIGNS));
            userid = assigned;
            if(!assigned.isEmpty())
            {
                EmployeerDatabasehelper userhelper = new EmployeerDatabasehelper(this);
                Cursor users = userhelper.get_users(assigned);
                if(users.moveToFirst())
                {
                    String[] userarray = new String[users.getCount()];
                    int i = 0;
                    do {
                        userarray[i] = users.getString(users.getColumnIndex(EmployeerDatabasehelper.KEY_USERNAME));
                        i++;
                    }while(users.moveToNext());
                    assigned = TextUtils.join(",",userarray);
                }
                else
                {
                    assigned = "unassigned";
                }
            }
            else
            {
                assigned = "unassigned";
            }

            TextView assigned_text = (TextView)findViewById(R.id.assigned);
            assigned_text.setText(assigned);
            String color = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_COLOR));
            int index = -1;

            LinearLayout color_select = (LinearLayout)findViewById(R.id.color_select);
            color_select.setBackgroundColor(Color.parseColor(color));
            for(int i = 0;i<Constants.colors.length;i++)
            {
                if(Constants.colors[i].contentEquals(color))
                {
                    index = i;
                    break;
                }
            }

            colorname = Constants.colorname[index];
            TextView color_text = (TextView)findViewById(R.id.color);
            color_text.setText(colorname);
            notes = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_NOTES));
            if(!notes.isEmpty())
            {
                LinearLayout layoutnotes = (LinearLayout)findViewById(R.id.note_container);
                layoutnotes.setVisibility(View.VISIBLE);
                TextView textnotes = (TextView)findViewById(R.id.notes);
                textnotes.setText(notes);
            }

            jobid = cursor.getInt(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_JOBID));
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.more:
                dialog = new DeleteDialog(this,R.layout.delete_dialog);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();

                dialog_delete = new DeleteDialog(this,R.layout.dialog_delete);
                dialog_delete.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                Button cancel = (Button)dialog.findViewById(R.id.cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                Button delete = (Button)dialog.findViewById(R.id.delete);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        dialog_delete.show();
                        Button delete_dialog = (Button)dialog_delete.findViewById(R.id.delete);
                        TextView error = (TextView) dialog_delete.findViewById(R.id.error_text);
                        error.setText("Delete this shift ?");
                        delete_dialog.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View v) {
                                delete();
                                dialog_delete.dismiss();
                            }
                        });

                        Button cancel = (Button)dialog_delete.findViewById(R.id.cancel);
                        cancel.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View v) {
                                dialog_delete.dismiss();
                            }
                        });
                    }
                });


                dialog.show();
                break;
            case R.id.edit:
                edit();
                break;
            case R.id.overview:
                Intent intent = new Intent(this,DashboardActivity.class);
                startActivity(intent);
                break;
            case R.id.timesheet:
                intent = new Intent(this,TimesheetView.class);
                startActivity(intent);
                break;
            case R.id.schedule:
                intent = new Intent(this,ShiftView.class);
                startActivity(intent);
                break;
            case R.id.timecard:
                intent = new Intent(this,TimeCardActivity.class);
                startActivity(intent);
                break;
            case R.id.more_dashboard:
                intent = new Intent(this,MoreActivity.class);
                startActivity(intent);
                break;
            case R.id.username:
            case R.id.image_user:
                intent = new Intent(this,Profile.class);
                startActivity(intent);
                break;
            case R.id.prev:
                finish();
        }
    }

    public void delete()
    {
        if(id > 0)
        {
            OkHttpClient client = new OkHttpClient();
            Request delete_shift = new Request.Builder().url(delete_url + String.valueOf(id)).get().build();
            client.newCall(delete_shift).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful())
                    {
                        try {
                            JSONObject object = new JSONObject(response.body().string());
                            ShiftDatabaseHelper helper = new ShiftDatabaseHelper(getApplicationContext());
                            helper.delete(id);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
        }
    }

    public void edit()
    {
        Intent intent = new Intent(this,ShiftAddActivity.class);
        Log.d("schedule_id",String.valueOf(id));
        
        intent.putExtra("id",id);
        startActivity(intent);
    }
}

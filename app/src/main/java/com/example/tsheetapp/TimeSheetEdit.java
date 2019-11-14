package com.example.tsheetapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TimeSheetEdit extends AppCompatActivity implements View.OnClickListener{
    int sheet_id;
    int ref_id;
    public  static String delete_uri = API.url + "/upload/delete";
    DeleteDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_timesheet);
        Intent intent = getIntent();
        sheet_id = intent.getIntExtra("sheet_id",0);
        ref_id = intent.getIntExtra("ref_id",0);
        if(sheet_id == 0)
        {
            finish();
        }
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
        Cursor cursor = helper.gettimesheetbyid(sheet_id);
        if(cursor.moveToFirst())
        {
            try {
                String starttime = cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_STARTTIME));
                String endtime = cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_ENDTIME));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.getDefault());
                String delaytime = gettimedelay(sdf.parse(starttime),sdf.parse(endtime));
                String attachments = cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_ATTACHMENT));
                SimpleDateFormat title_date = new SimpleDateFormat("E,MMM dd,yyyy",Locale.getDefault());
                String title = title_date.format(sdf.parse(starttime));
                SimpleDateFormat timeformat = new SimpleDateFormat("hh:mm a",Locale.getDefault());

                TextView starttimetxt = (TextView)findViewById(R.id.starttime);
                starttimetxt.setText(timeformat.format(sdf.parse(starttime)));
                TextView endtimetxt = (TextView)findViewById(R.id.endtime);
                endtimetxt.setText(timeformat.format((sdf.parse(endtime))));

                TextView totaltext = (TextView)findViewById(R.id.total);
                totaltext.setText(delaytime);

                TextView jobtxt = (TextView)findViewById(R.id.job);
                jobtxt.setText(cursor.getString(cursor.getColumnIndex("jobname")));
                TextView note = (TextView)findViewById(R.id.notes);
                note.setText(cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_NOTES)));

                String[] attachments_array = TextUtils.split(attachments,",");
                Log.d("fileattachment","files:" + attachments);

                GridView layout = (GridView) findViewById(R.id.attachment);

                GridAdapter adapter = new GridAdapter(this,attachments_array);
                layout.setAdapter(adapter);
                TextView titletxt = (TextView)findViewById(R.id.title);
                titletxt.setText(title);

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        else
        {
            finish();
        }
    }

    public String gettimedelay(Date starttime, Date endtime)
    {
        long time = endtime.getTime() - starttime.getTime();
        int hours = (int) (time / (1000 * 60 * 60));
        int mins = (int) ((time / (1000 * 60)) % 60);

        NumberFormat f = new DecimalFormat("00");
        String str = "";
        if (hours > 0) {
            str += f.format(hours) + "h ";
        }

        str += f.format(mins) + "m ";
        return str;
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
                startActivity(intent);
                overridePendingTransition(R.anim.left_out,R.anim.right_out);
                break;
            case R.id.schedule:
                intent = new Intent(this,ShiftView.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in,R.anim.right_out);
                break;
            case R.id.timecard:
                intent = new Intent(this,TimeCardActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.left_out,R.anim.right_out);
                break;
            case R.id.more:
                intent = new Intent(this,MoreActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in,R.anim.right_out);
                break;
            case R.id.delete:
                dialog = new DeleteDialog(this,R.layout.delete_dialog);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();
                Button delete = (Button)dialog.findViewById(R.id.delete);
                delete.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        delete_sheet();
                        dialog.dismiss();
                    }
                });

                Button cancel = (Button)dialog.findViewById(R.id.cancel);
                cancel.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;
            case R.id.edit:
                intent = new Intent(this,AddTimeSheet.class);
                intent.putExtra("timesheet_id",sheet_id);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                break;
            case R.id.prev:
                finish();
        }
    }

    Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
           Bundle bundle = msg.getData();
           if(bundle.getBoolean("success"))
           {
               String responsebody = bundle.getString("result");
               try {
                   JSONObject object = new JSONObject(responsebody);
                   if(object.getBoolean("success"))
                   {
                       TimeSheetDatabaseHelper helper = new TimeSheetDatabaseHelper(getApplicationContext());
                       helper.delete(sheet_id);
                       finish();
                   }
               } catch (JSONException e) {
                   e.printStackTrace();
               }
           }
           else
           {
               String error = bundle.getString("error");
               Toast.makeText(getApplicationContext(),"The server is not connected yet",Toast.LENGTH_LONG).show();
           }

        }
    };


    public void delete_sheet()
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(delete_uri + "/" + ref_id).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message msg = mhandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putBoolean("success",false);
                bundle.putString("result",e.getMessage());
                msg.setData(bundle);
                mhandler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responsebody = response.body().string();
                Log.d("responsebody",responsebody);
                if(response.isSuccessful())
                {
                    Message msg = mhandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("success",true);
                    bundle.putString("result",responsebody);
                    msg.setData(bundle);
                    mhandler.sendMessage(msg);
                }
            }
        });
    }
}

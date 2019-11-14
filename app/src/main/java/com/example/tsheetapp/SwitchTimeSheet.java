package com.example.tsheetapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SwitchTimeSheet extends AppCompatActivity implements View.OnClickListener{
    int jobid;
    int realjobid;
    String jobname;
    TextView current_time;
    private static int REQUEST_JOB = 1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.switch_timecard);
        Intent intent = getIntent();
        jobid = intent.getIntExtra("jobid",0);
        jobname = intent.getStringExtra("jobname");
        realjobid = jobid;
        EditText jobnametxt = (EditText)findViewById(R.id.job);
        current_time = (TextView)findViewById(R.id.text_current_time);
        jobnametxt.setText(jobname);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.cancel:
                Intent intent = new Intent();
                setResult(Activity.RESULT_CANCELED, intent);
                finish();
                break;
            case R.id.save:
                intent = new Intent();
                ErrorDialog dialog = new ErrorDialog(this);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                if(jobid == realjobid)
                {
                    dialog.show();
                    TextView error = (TextView)dialog.findViewById(R.id.error_message);
                    error.setText("You have to select another job, Please reselect again");
                    break;
                }
                EditText notestext = (EditText)findViewById(R.id.notes);
                String notes = notestext.getText().toString();
                if(notes.isEmpty())
                {
                    dialog.show();
                    TextView error = (TextView)findViewById(R.id.error_message);
                    error.setText("You have to type in notes");
                    break;
                }

                intent.putExtra("jobid",jobid);
                intent.putExtra("jobname",jobname);
                intent.putExtra("notes",notes);
                setResult(Activity.RESULT_OK,intent);
                finish();
                break;
            case R.id.job:
                intent = new Intent(this,SearchJob.class);
                startActivityForResult(intent,REQUEST_JOB);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_JOB)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                String jobname = data.getStringExtra("jobname");
                realjobid = data.getIntExtra("jobid",0);
                EditText jobtext = (EditText)findViewById(R.id.job);
                jobtext.setText(jobname);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Date setcurrenttime() {
        Date currenttime = Calendar.getInstance().getTime();
        SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm:ss a");
        current_time.setText("Today, " + sdfs.format(currenttime));
        return currenttime;
    }
}

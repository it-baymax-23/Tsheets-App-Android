package com.example.tsheetapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class MoreActivity extends AppCompatActivity implements View.OnClickListener{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more);
        TextView user = (TextView)findViewById(R.id.username);
        CircleImageView imageView = (CircleImageView)findViewById(R.id.image_user);
        Constants.display_profile(this,user,imageView);
        String role = getSharedPreferences("credential",MODE_PRIVATE).getString("role","");
        if(role.contentEquals("employee"))
        {
            RelativeLayout layout = (RelativeLayout)findViewById(R.id.add_employee);
            layout.setVisibility(View.GONE);
            RelativeLayout layout1 = (RelativeLayout)findViewById(R.id.employeer_list);
            layout1.setVisibility(View.GONE);
            RelativeLayout layout2 = (RelativeLayout)findViewById(R.id.manage_jobs);
            layout1.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.add_employee:
                Intent intent = new Intent(this,AddEmployeer.class);
                startActivity(intent);
                break;
            case R.id.employeer_list:
                intent = new Intent(this,WorkingEmployeer.class);
                startActivity(intent);
                break;
            case R.id.manage_jobs:
                intent = new Intent(this,SearchJob.class);
                intent.putExtra("selected",false);
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
            case R.id.more:
                intent = new Intent(this,MoreActivity.class);
                startActivity(intent);
                break;
            case R.id.username:
            case R.id.image_user:
                intent = new Intent(this,Profile.class);
                startActivity(intent);
                break;
            case R.id.timecard:
                intent = new Intent(this,TimeCardActivity.class);
                startActivity(intent);
                break;
        }
    }
}

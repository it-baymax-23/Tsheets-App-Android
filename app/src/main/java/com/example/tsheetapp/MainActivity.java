package com.example.tsheetapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("credential",MODE_PRIVATE);

        String userid = preferences.getString("user_id","");
        if(!userid.isEmpty())
        {
            Intent intent = new Intent(this,TimeCardActivity.class);
            startActivity(intent);
        }
        Button button_admin = (Button)findViewById(R.id.button_admin);
        Button Employeer = (Button)findViewById(R.id.button_employee);
        button_admin.setOnClickListener(this);
        Employeer.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String role = "";
        switch (view.getId())
        {
            case R.id.button_admin:
                role = "admin";
                break;
            case R.id.button_employee:
                role = "employee";
                break;
        }

        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        intent.putExtra("com.example.tsheetapp.role",role);
        startActivity(intent);
    }
}

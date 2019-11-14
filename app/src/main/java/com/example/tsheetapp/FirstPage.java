package com.example.tsheetapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FirstPage extends AppCompatActivity {
    SharedPreferences preferences;
    String userid;
    boolean display = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(0,0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstpage);
        preferences = getSharedPreferences("credential",MODE_PRIVATE);

        userid = preferences.getString("user_id","");

        Thread welcomeThread = new Thread() {

            @Override
            public void run() {
                try {
                    sleep(2000);
                } catch (Exception e) {

                } finally {
                    if(!userid.isEmpty())
                    {
                        Intent intent = new Intent(FirstPage.this,TimeCardActivity.class);
                        startActivity(intent);
                    }
                    else {
                        Intent i = new Intent(FirstPage.this,
                                MainActivity.class);
                        startActivity(i);
                    }
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    finish();
                }
            }
        };
        welcomeThread.start();
    }
}

package com.example.tsheetapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

public class LogoutDialog extends Dialog implements View.OnClickListener{
    Context c;
    public LogoutDialog(@NonNull Context context) {
        super(context);
        c = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logoutdialog);
        Button button_logout = (Button)findViewById(R.id.logout);
        Button buttoncancel = (Button)findViewById(R.id.cancel);
        button_logout.setOnClickListener(this);
        buttoncancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.logout:
                logout();
                break;
            case R.id.cancel:
                this.dismiss();
                break;
        }
    }

    public void logout()
    {
        SharedPreferences preferences = c.getSharedPreferences("credential",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_id","");
        editor.putString("role","");
        editor.putString("user","");
        editor.putString("profile","");
        editor.commit();
        Intent intent = new Intent(c,MainActivity.class);
        c.startActivity(intent);
    }
}

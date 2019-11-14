package com.example.tsheetapp;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

public class AddEmployeer extends AppCompatActivity implements View.OnClickListener{
    TextInputLayout emailaddress;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addemployeer);
        emailaddress = (TextInputLayout)findViewById(R.id.emailaddress);
        Button add_employeer = (Button)findViewById(R.id.invite_employeer);
        add_employeer.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.invite_employeer:
                if(isEmpty(emailaddress))
                {
                    invite(emailaddress.getEditText().getText().toString());
                }
        }
    }

    public void invite(String email)
    {

    }

    public boolean isEmpty(TextInputLayout text)
    {
        if(text.getEditText().getText().toString().isEmpty())
        {
            text.setError("this field is required");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(text.getEditText().getText().toString()).matches())
        {
            text.setError("please type in valid email address");
            return false;
        }
        else
        {
            text.setError(null);
            return true;
        }
    }
}

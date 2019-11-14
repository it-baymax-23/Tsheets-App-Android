package com.example.tsheetapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class TextAreaActivity extends AppCompatActivity implements View.OnClickListener{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addtextarea);
        EditText text= (EditText) findViewById(R.id.edit_textarea);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(text, InputMethodManager.SHOW_IMPLICIT);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        TextView titletext = (TextView)findViewById(R.id.title_textarea);
        titletext.setText(title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.save:
                EditText text = (EditText)findViewById(R.id.edit_textarea);
                String name = text.getText().toString();
                Intent result = new Intent();
                result.putExtra("name",name);
                setResult(Activity.RESULT_OK,result);
                finish();
                break;
            case R.id.cancel:
                result = new Intent();
                setResult(Activity.RESULT_CANCELED,result);
                finish();
        }
    }
}

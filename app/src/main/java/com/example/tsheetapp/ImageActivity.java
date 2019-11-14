package com.example.tsheetapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class ImageActivity extends AppCompatActivity implements View.OnClickListener{
    int index;
    String file = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageactivity);
        Intent intent = getIntent();
        index = intent.getIntExtra("index",0);
        file = intent.getStringExtra("file");

        ImageView imageView = (ImageView)findViewById(R.id.imagedetail);
        imageView.setImageURI(Uri.fromFile(new File(file)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.cancel:
                finish();
                break;
            case R.id.remove:
                Intent intent = new Intent();
                intent.putExtra("index",index);
                setResult(Activity.RESULT_OK,intent);
                finish();
                break;
        }
    }
}

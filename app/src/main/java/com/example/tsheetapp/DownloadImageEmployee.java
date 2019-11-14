package com.example.tsheetapp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class DownloadImageEmployee extends AsyncTask<String, Void, Bitmap> {
    CircleImageView imageview;
    TextView username;
    public DownloadImageEmployee(CircleImageView image,TextView username) {
        this.imageview = image;
        this.username = username;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    @SuppressLint("WrongThread")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void onPostExecute(Bitmap result) {
        if(result != null)
        {
            this.imageview.setImageBitmap(result);
            this.imageview.setVisibility(View.VISIBLE);
            if(this.username != null)
            {
                this.username.setVisibility(View.GONE);
            }
        }

    }
}
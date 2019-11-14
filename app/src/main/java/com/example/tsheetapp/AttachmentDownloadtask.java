package com.example.tsheetapp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class AttachmentDownloadtask  extends AsyncTask<String, Void, Bitmap> {
    int timesheetid;
    Context context;
    public AttachmentDownloadtask(int id, Context context) {
        this.timesheetid = id;this.context = context;
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
        TimeSheetDatabaseHelper helper = new TimeSheetDatabaseHelper(context);
        Cursor cursor = helper.gettimesheetbyid(timesheetid);
        if(cursor.moveToFirst() && result != null)
        {
            String attachment = cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_ATTACHMENT));
            String path = Environment.getExternalStorageDirectory().toString();
            OutputStream fout = null;
            File file = new File(path, Calendar.getInstance().getTimeInMillis() + ".jpg");
            try {
                fout = new FileOutputStream(file);
                result.compress(Bitmap.CompressFormat.PNG,100,fout);
                attachment += "," +  file.getAbsolutePath();
                helper.updatetimesheet(attachment,timesheetid);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

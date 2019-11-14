package com.example.tsheetapp;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class TsheetApplication extends Application {
    public static final String Channel_ID = "exampleServiceChannel";
    private RequestQueue mRequestQueue;
    public static final String TAG = TsheetApplication.class.getSimpleName();
    private static TsheetApplication mInstance;
    @Override
    public void onCreate() {

        super.onCreate();
    }

    public static synchronized TsheetApplication getInstance() {
        return mInstance;
    }

    public void createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
        {
            NotificationChannel servicechannel = new NotificationChannel(
                    Channel_ID,
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(servicechannel);
        }
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

}

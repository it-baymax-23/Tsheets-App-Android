package com.example.tsheetapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.sql.DriverManager.println;

public class TimeTractService extends Service {

    Handler mhandler;
    Thread thread;
    Date currentdate;
    Context context;
    public boolean exit = false;
    String channel_id = TsheetApplication.Channel_ID;
    GoogleApiClient mgoogleapi;
    LocationRequest mlocationrequest;
    boolean connected = false;
    LocationManager location;
    LocationListener locationListener;
    int jobid;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            thread.sleep(500);
            exit = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //thread.stop();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        try {
            location = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d("location", String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()));
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            };

            exit = false;
            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                Log.d("command","error connection");
                return super.onStartCommand(intent, flags, startid);
            }
            location.requestLocationUpdates("gps", 5000, 0, locationListener);
            Log.d("commanded","send request");
            currentdate = new Date();
            jobid = intent.getIntExtra("jobid",0);
            mhandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Bundle bundle = msg.getData();
                    String string = bundle.getString("data");
                    long delaytime = bundle.getLong("timedelay");
                    Intent intent = new Intent();
                    intent.setAction("service.to.activity.transfer");
                    intent.putExtra("delay", string);
                    intent.putExtra("delaytime",delaytime);
                    intent.putExtra("starttime",currentdate.getTime());
                    intent.putExtra("jobid",jobid);
                    sendBroadcast(intent);

                    Intent notificationintent = new Intent(context, TimeCardActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationintent, 0);
                    Notification notification = new NotificationCompat.Builder(context, channel_id)
                            .setContentTitle("Time Card")
                            .setContentText(string)
                            .setSmallIcon(R.drawable.logo)
                            .setContentIntent(pendingIntent)
                            .build();
                    startForeground(1, notification);
                    if (ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    Activity#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.
                        Log.d("command","error connection");

                    }
                    else
                    {
                        location.requestLocationUpdates("gps", 5000, 0, locationListener);
                    }


                }
            };


            thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    while (!exit) {
                        Date curdate = Calendar.getInstance().getTime();
                        long millis = curdate.getTime() - currentdate.getTime();

                        int hours = (int) (millis / (1000 * 60 * 60));
                        int mins = (int) ((millis / (1000 * 60)) % 60);
                        int secs = (int) ((millis / 1000) % 60);

                        NumberFormat f = new DecimalFormat("00");
                        String str = "";
                        if (hours > 0) {
                            str += f.format(hours) + "h ";
                        }

                        str += f.format(mins) + "m " + f.format(secs) + "s";
                        Message msg = mhandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("data", str);
                        bundle.putLong("timedelay",millis);
                        msg.setData(bundle);
                        mhandler.sendMessage(msg);
                        try {
                            thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }


                }
            });

            thread.start();
            Intent new_intent = new Intent();
            new_intent.setAction("service.to.activity.transfer");
            new_intent.putExtra("number",startid);
            sendBroadcast(new_intent);
        }
        catch (Exception e)
        {
            Log.d("Exception e",e.getMessage());
        }
        return super.onStartCommand(intent, flags, startid);
    }



}

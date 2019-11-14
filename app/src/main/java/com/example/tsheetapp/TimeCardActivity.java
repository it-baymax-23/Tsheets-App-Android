package com.example.tsheetapp;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import android.view.*;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.RequestException;

public class TimeCardActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_PICK_PICTURE = 2;
    public static int SEARCH_REQUEST = 3;
    public static int SWITCH_TIMECARD = 4;
    String user_id;
    TextView current_time;
    BroadcastReceiver trackreceiver;
    IntentFilter filter;
    TextView delay_text;
    int step = 1;
    CustomDialog dialog;
    File mPhotoFile;
    ArrayList<String> filearray = null;
    Uri photoURI;
    String upload_uri = API.url + "/upload/uploadtimesheet";
    Date starttime;
    SharedPreferences preferences;
    int jobid;
    JSONObject save_object;
    long startdelaytime = 0;
    int timesheetid = 0;
    int ref_id = 0;
    private ImageCompressTask imageCompressTask;

    boolean switched = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timecard);
        current_time = (TextView) findViewById(R.id.text_current_time);
        setcurrenttime();
        timer_handler.postDelayed(timer_runnable, 0);

        Constants.initdata(this);

        preferences = getSharedPreferences("credential",MODE_PRIVATE);
        user_id = preferences.getString("user_id","");
        TextView user = (TextView)findViewById(R.id.username);
        CircleImageView imageView = (CircleImageView)findViewById(R.id.image_user);
        Constants.display_profile(this,user,imageView);

        Button clockin = (Button) findViewById(R.id.clocked_in);
        Button clockout = (Button) findViewById(R.id.clock_out);
        Button switch_btn = (Button) findViewById(R.id.switch_btn);
        delay_text = (TextView) findViewById(R.id.delay_time);
        clockin.setOnClickListener(this);
        clockout.setOnClickListener(this);
        switch_btn.setOnClickListener(this);
        filter = new IntentFilter();
        filter.addAction("service.to.activity.transfer");
//        TimeSheetDatabaseHelper helper = new TimeSheetDatabaseHelper(this);
//        helper.onUpgrade(helper.getWritableDatabase(),0,1);
        display_step(1);
        delete_initialstate();
        trackreceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String str = intent.getStringExtra("delay");
                    jobid = intent.getIntExtra("jobid",0);
                    long millis = intent.getLongExtra("delaytime",0);
                    starttime = new Date();
                    starttime.setTime(intent.getLongExtra("starttime",0));
                    Log.d("millis",String.valueOf(millis));
                    display_total_time(startdelaytime + millis);
                    setDelayTime(str);
                    if(step != 2)
                    {
                        step = 2;
                        display_step(2);
                    }

                    if(timesheetid == 0)
                    {
                        saveinitialtimesheet();
                    }
                }
            }
        };

        registerReceiver(trackreceiver, filter);
    }

    public void delete_initialstate()
    {
        TimeSheetDatabaseHelper helper = new TimeSheetDatabaseHelper(this);
        helper.deletebystate("clockin");
        Request request = new Request.Builder().url(API.url + "/user/delete/" + user_id).get().build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("response",response.body().string());
            }
        });
    }
    public void saveinitialtimesheet()
    {
        TimeSheetDatabaseHelper helper = new TimeSheetDatabaseHelper(this);
        Cursor cursor = helper.gettimesheetbyjobid(starttime,String.valueOf(jobid),"clockin");
        if(!cursor.moveToFirst())
        {
            MultipartBody.Builder request = new MultipartBody.Builder().setType(MultipartBody.FORM);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.getDefault());
            request.addFormDataPart("starttime",format.format(starttime));
            request.addFormDataPart("endtime","");
            request.addFormDataPart("jobid",String.valueOf(jobid));
            request.addFormDataPart("state","clockin");
            request.addFormDataPart("userid",user_id);
            RequestBody body = request.build();
            Request request_upload = new Request.Builder().url(upload_uri).post(body).build();
            timesheetid = inserttimesheetinitial(starttime,jobid);
            OkHttpClient client = new OkHttpClient();
            client.newCall(request_upload).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    timesheetid = inserttimesheetinitial(starttime,jobid);
                    ref_id = 0;
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String body = response.body().string();
                    Log.d("body_string",body);
                    if(response.isSuccessful())
                    {
                        try {
                            JSONObject object = new JSONObject(body);
                            ref_id = object.getInt("ref_id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
            Log.d("timesheetid",String.valueOf(timesheetid));
        }
        else
        {
            timesheetid = cursor.getInt(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_ID));
            Log.d("timesheetid",timesheetid + "");
            ref_id = cursor.getInt(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_REFID));
        }
    };

    public int inserttimesheetinitial(Date starttime,int jobid)
    {
        TimeSheetDatabaseHelper helper = new TimeSheetDatabaseHelper(this);
        return helper.addinitial(starttime,jobid,"clockin");

    }

    @Override
    public void onPause() {
        unregisterReceiver(trackreceiver);
        super.onPause();
    }

    @Override
    protected void onStart() {
        startdelaytime = calctotaltime();
        super.onStart();
    }

    @Override
    public void onResume() {
        registerReceiver(trackreceiver, filter);
        //startdelaytime = calctotaltime();
        super.onResume();
    }

    Handler timer_handler = new Handler();
    Runnable timer_runnable = new Runnable() {
        @Override
        public void run() {
            setcurrenttime();
            timer_handler.postDelayed(this, 1000);
        }
    };

    public Date setcurrenttime() {
        Date currenttime = Calendar.getInstance().getTime();
        SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm:ss a");
        current_time.setText("Today, " + sdfs.format(currenttime));
        return currenttime;
    }

    public long calctotaltime()
    {
        TimeSheetDatabaseHelper helper = new TimeSheetDatabaseHelper(this);
        Cursor cursor = helper.gettimesheetbydate(Calendar.getInstance(),user_id);
        long millis = 0;

        if(cursor.moveToFirst())
        {
            do {
                if(!cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_STATE)).contentEquals("clockin"))
                {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                    String starttime = cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_STARTTIME));
                    Log.d("element_starttime",starttime);

                    String endtime = cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_ENDTIME));
                    Log.d("element_endtime",endtime);
                    Calendar today = Calendar.getInstance();
                    try {
                        Date starttimedate = sdf.parse(starttime);
                        Date endtimedate = sdf.parse(endtime);
                        Log.d("elementtimedate",String.valueOf(endtimedate.getDate()));
                        Log.d("elementtimemonth",String.valueOf(endtimedate.getYear()));
                        Log.d("elementtimeyear",String.valueOf(endtimedate.getMonth()));
                        Log.d("elementtodayyear",String.valueOf(today.get(Calendar.YEAR)));
                        Log.d("elementtodaymonth",String.valueOf(today.get(Calendar.MONTH)));
                        Log.d("elementtodaydate",String.valueOf(today.get(Calendar.DATE)));
                        if(endtimedate.getYear() == today.get(Calendar.YEAR) - 1900 && endtimedate.getMonth() == today.get(Calendar.MONTH) && endtimedate.getDate() == today.get(Calendar.DATE))
                        {
                            millis += endtimedate.getTime() - starttimedate.getTime();
                            Log.d("element_millis",String.valueOf(millis));
                        }
                        else
                        {
                            Calendar t = (Calendar) today.clone();

                            t.set(Calendar.HOUR,23);
                            t.set(Calendar.MINUTE,59);
                            t.set(Calendar.SECOND,59);
                            Log.d("element_today_endtime",sdf.format(t.getTimeInMillis()));
                            Log.d("element_starttime",sdf.format(starttimedate.getTime()));
                            Log.d("element_today_millis",String.valueOf(t.getTimeInMillis()));
                            Log.d("element_start_millis",String.valueOf(starttimedate.getTime()));
                            millis += t.getTimeInMillis() - starttimedate.getTime();
                            Log.d("element_millis_today",String.valueOf(millis));
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }


            }while(cursor.moveToNext());
        }

        return millis;
    }

    public void setDelayTime(String str) {
        delay_text.setText(str);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clocked_in:
                if(jobid == 0)
                {
                    ErrorDialog error = new ErrorDialog(this);
                    error.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    error.show();
                    TextView error_text = (TextView) error.findViewById(R.id.error_message);
                    error_text.setText("I think you forgot to select the job, Please select the job");

                }
                else
                {
                    try
                    {
                        Intent intent = new Intent(TimeCardActivity.this, TimeTractService.class);
                        intent.putExtra("jobid",jobid);
                        intent.putExtra("userid",user_id);
                        step = 2;
                        starttime = setcurrenttime();
                        startService(intent);
                        display_step(step);
                        saveinitialtimesheet();

                    }
                    catch (Exception e)
                    {
                        Log.d("Excepton E",e.getMessage());
                    }

                }
                break;
            case R.id.clock_out:
                save_time_sheet();

                break;
            case R.id.switch_btn:
                Intent intent = new Intent(this,SwitchTimeSheet.class);
                intent.putExtra("jobid",jobid);
                EditText jobname = (EditText) findViewById(R.id.job);
                intent.putExtra("jobname",jobname.getText().toString());
                startActivityForResult(intent,SWITCH_TIMECARD);
                overridePendingTransition(R.anim.left_out,R.anim.right_out);
                break;
            case R.id.attachment:
                dialog = new CustomDialog(this);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();
                Button button_take_photo = (Button) dialog.findViewById(R.id.take_photo);
                button_take_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dispatchTakePictureIntent();
                        dialog.dismiss();
                    }
                });

                Button select_photo = (Button) dialog.findViewById(R.id.select_photo);
                select_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        get_photo_from_gallery();
                        dialog.dismiss();
                    }
                });

                Button cancel = (Button) dialog.findViewById(R.id.cancel);
                cancel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;
            case R.id.overview:
                intent = new Intent(this,DashboardActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.left_out,R.anim.right_out);
                break;
            case R.id.timesheet:
                intent = new Intent(this,TimesheetView.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in,R.anim.right_out);
                break;
            case R.id.schedule:
                intent = new Intent(this,ShiftView.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in,R.anim.right_out);
                break;
            case R.id.more:
                intent = new Intent(this,MoreActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in,R.anim.right_out);
                break;
            case R.id.username:
            case R.id.image_user:
                intent = new Intent(this,Profile.class);
                startActivity(intent);
                overridePendingTransition(R.anim.bottom,R.anim.right_out);
                break;
            case R.id.job:
                intent = new Intent(this,SearchJob.class);
                startActivityForResult(intent,SEARCH_REQUEST);
                overridePendingTransition(R.anim.top_in,R.anim.right_out);
                break;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String mFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Toast.makeText(this, Environment.DIRECTORY_PICTURES, Toast.LENGTH_LONG).show();
        File mFile = File.createTempFile(mFileName, ".jpg", storageDir);
        return mFile;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Toast.makeText(this, BuildConfig.APPLICATION_ID + ".provider", Toast.LENGTH_LONG).show();
                photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);

                mPhotoFile = photoFile;

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }

    }

    Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            boolean success = bundle.getBoolean("success");
            if(success)
            {
                TimeSheetDatabaseHelper helper = new TimeSheetDatabaseHelper(getApplicationContext());
                try {
                    helper.savetimesheetwithobject(save_object);
                    if(switched)
                    {
                        Intent intent = new Intent(TimeCardActivity.this, TimeTractService.class);
                        startService(intent);
                        switched = false;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                step = 1;
            }
        }
    };


    public boolean save_time_sheet()
    {
        try {
            save_object = new JSONObject();

            int size = filearray.size();
            OkHttpClient client = new OkHttpClient();
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

            String[] filetext = new String[filearray.size()];
            for(int i = 0;i<size;i++)
            {
                filetext[i] = filearray.get(i);
                File f = new File(filearray.get(i));
                String content_type = getMimeType(f.getPath());
                RequestBody file_body = RequestBody.create(MediaType.parse(content_type),f);
                builder.addFormDataPart("image" + String.valueOf(i),filearray.get(i).substring(filearray.get(i).lastIndexOf("/")),file_body);
            }

            save_object.put("attachment", TextUtils.join(",",filetext));
            Log.d("attachmentfile",TextUtils.join(",",filetext));
            save_object.put("id",timesheetid);
            save_object.put("state","clockout");
            EditText editText = (EditText)findViewById(R.id.notes);

            if(editText.getText().toString().isEmpty())
            {
                editText.setError("Please Type in notes");
                return false;
            }
            else
            {
                editText.setError(null);
                builder.addFormDataPart("notes",editText.getText().toString());
                save_object.put("notes",editText.getText().toString());
            }

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            builder.addFormDataPart("starttime",df.format(starttime));

            save_object.put("starttime",df.format(starttime));

            builder.addFormDataPart("endtime",df.format(setcurrenttime()));
            save_object.put("endtime",df.format(setcurrenttime()));
            builder.addFormDataPart("jobid",String.valueOf(jobid));
            save_object.put("jobid",jobid);
            builder.addFormDataPart("userid",user_id);
            save_object.put("userid",Integer.valueOf(user_id));
            builder.addFormDataPart("ref_id",String.valueOf(ref_id));
            builder.addFormDataPart("state","clockout");
            RequestBody body = builder.build();
            Intent intent = new Intent(TimeCardActivity.this, TimeTractService.class);
            stopService(intent);

            if(!switched)
            {
                step = 1;
                display_step(step);
            }
            else
            {
                starttime = setcurrenttime();
            }
            Request request = new Request.Builder().url(upload_uri).post(body).build();

            client.newCall(request).enqueue(
                    new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("uploadresult",e.getMessage());
                        try {
                            save_object.put("ref_id",0);
                            Message msg =mhandler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("success",true);
                            msg.setData(bundle);
                            mhandler.sendMessage(msg);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        String body = response.body().string();
                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonObject = new JSONObject(body);
                                Message msg = mhandler.obtainMessage();
                                Bundle bundle = new Bundle();
                                bundle.putBoolean("success",jsonObject.getBoolean("success"));
                                if(jsonObject.getBoolean("success"))
                                {
                                    save_object.put("ref_id",jsonObject.getInt("ref_id"));
                                    msg.setData(bundle);
                                    mhandler.sendMessage(msg);
                                }
                                else
                                {
                                    save_object.put("ref_id","0");
                                    msg.setData(bundle);
                                    mhandler.sendMessage(msg);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Log.d("uploadresponse",response.toString());
                            //Toast.makeText(getApplicationContext(),response.toString(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
        } catch (Exception e) {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        return true;
    }

    public String getMimeType(String path)
    {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }



    private void get_photo_from_gallery()
    {
        Intent gallery = new Intent(Intent.ACTION_PICK);
        gallery.setType("image/*");
        startActivityForResult(gallery,REQUEST_PICK_PICTURE);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode < 3) {
            Uri photo = null;
            if (requestCode == REQUEST_TAKE_PHOTO) {
                try{
                    filearray.add(mPhotoFile.getAbsolutePath());
                    photo = photoURI;
                }catch (Exception e){
                    Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                }
            }
            else if(requestCode == REQUEST_PICK_PICTURE){
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final Uri imageUri = data.getData();
                photo = imageUri;

                FileOutputStream fileOutputStream = null;
                try {
                    InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                    fileOutputStream = new FileOutputStream(photoFile);
                    copyStream(inputStream, fileOutputStream);
                    fileOutputStream.close();
                    inputStream.close();
                    filearray.add(photoFile.getAbsolutePath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            int[] array_file = new int[]{R.id.image1,R.id.image2,R.id.image3,R.id.image4};
            if(filearray.size() > 3)
            {
                ImageView imageview = new ImageView(this);
                GridLayout layout = (GridLayout)findViewById(R.id.attachment_grid);
                LinearLayout linear = new LinearLayout(this);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.attachment_width),(int)getResources().getDimension(R.dimen.attachment_height));
                lp.setMargins(0,0,(int)getResources().getDimension(R.dimen.margin_10dp),(int)getResources().getDimension(R.dimen.margin_10dp));
                imageview.setLayoutParams(lp);
                imageview.setImageURI(photo);
                layout.addView(imageview);
            }
            else
            {
                Log.d("imageview",String.valueOf(filearray.size()));
                ImageView imageView = (ImageView)findViewById(array_file[filearray.size() - 1]);
                imageView.setImageURI(photo);
            }

        }
        else if(resultCode == RESULT_OK && requestCode == 3)
        {
            String jobname = data.getStringExtra("jobname");
            jobid = data.getIntExtra("jobid",0);
            EditText jobtext = (EditText)findViewById(R.id.job);
            jobtext.setText(jobname);
        }
        else if(resultCode == RESULT_OK && requestCode == SWITCH_TIMECARD)
        {
            String jobname = data.getStringExtra("jobname");
            EditText jobtext = (EditText)findViewById(R.id.job);
            jobtext.setText(jobname);

            jobid = data.getIntExtra("jobid",0);

            String notes = data.getStringExtra("notes");

            EditText notestxt  = (EditText)findViewById(R.id.notes);
            notestxt.setText(notes);
            switched = true;
            save_time_sheet();
        }
    }

    public void display_step(int step)
    {
        switch(step)
        {
            case 1:
                RelativeLayout layout = (RelativeLayout)findViewById(R.id.clockin_btn_container);
                layout.setVisibility(View.VISIBLE);
                RelativeLayout timetrack = (RelativeLayout)findViewById(R.id.timetrack);
                timetrack.setVisibility(View.GONE);

                LinearLayout jobcontainer = (LinearLayout)findViewById(R.id.jobcontainer);
                jobcontainer.setVisibility(View.VISIBLE);

                LinearLayout layout_for_note = (LinearLayout)findViewById(R.id.notecontainer);
                layout_for_note.setVisibility(View.GONE);

                RelativeLayout timenontrack = (RelativeLayout)findViewById(R.id.timenontrack);
                timenontrack.setVisibility(View.VISIBLE);

                LinearLayout attachment = (LinearLayout) findViewById(R.id.attachment_container);
                attachment.setVisibility(View.GONE);

                RelativeLayout layout_clockout = (RelativeLayout)findViewById(R.id.clockout_btn_container);
                layout_clockout.setVisibility(View.GONE);
                long totaltime = calctotaltime();
                display_total_time(totaltime);
                initimageview();
                break;
            case 2:
                filearray = new ArrayList<String>();
                jobcontainer = (LinearLayout)findViewById(R.id.jobcontainer);
                jobcontainer.setVisibility(View.GONE);
                layout = (RelativeLayout)findViewById(R.id.clockin_btn_container);
                layout.setVisibility(View.GONE);
                layout_clockout = (RelativeLayout)findViewById(R.id.clockout_btn_container);
                timetrack = (RelativeLayout)findViewById(R.id.timetrack);
                timetrack.setVisibility(View.VISIBLE);
                layout_for_note = (LinearLayout)findViewById(R.id.notecontainer);
                layout_for_note.setVisibility(View.VISIBLE);
                timenontrack = (RelativeLayout)findViewById(R.id.timenontrack);
                timenontrack.setVisibility(View.GONE);
                attachment = (LinearLayout)findViewById(R.id.attachment_container);
                attachment.setVisibility(View.VISIBLE);
                layout_clockout.setVisibility(View.VISIBLE);
                timer_handler.removeCallbacks(timer_runnable);

                break;
        }
    }


    public void initimageview()
    {
        GridLayout layout = (GridLayout)findViewById(R.id.attachment_grid);

        boolean enable = true;
        while(enable)
        {
            int count = layout.getChildCount();
            enable = false;
            for(int i = 0;i<count;i++)
            {
                View v = layout.getChildAt(i);
                if(v instanceof ImageView)
                {
                    int id = v.getId();
                    if(id > 0)
                    {
                        ((ImageView) v).setImageResource(R.drawable.backgroundwhite);
                        continue;
                    }
                    else
                    {
                        enable = true;
                        layout.removeView(v);
                        break;
                    }
                }
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void savebitmap(Bitmap bm)
    {
        try (FileOutputStream out = new FileOutputStream("image1")) {
            bm.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    public void display_total_time(long time)
    {
        int hours = (int) (time / (1000 * 60 * 60));
        int mins = (int) ((time / (1000 * 60)) % 60);
        int secs = (int) ((time / 1000) % 60);

        NumberFormat f = new DecimalFormat("00");
        String str = "";
        if (hours > 0) {
            str += f.format(hours) + "h ";
        }

        str += f.format(mins) + "m " + f.format(secs) + "s";

        TextView daytotal = (TextView)findViewById(R.id.totaltime);
        TextView daytotaltime = (TextView)findViewById(R.id.totalclocktime);
        daytotal.setText(str);
        daytotaltime.setText(str);
    }
}

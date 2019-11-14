package com.example.tsheetapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static java.lang.Math.abs;

public class AddTimeSheet extends AppCompatActivity implements View.OnClickListener{
    Calendar starttime; Calendar endtime;
    Datetimepickerapp dialog;
    CustomDialog dialog_custom;
    EditText jobname;
    String type;
    int jobid;
    int userid;
    int ref_id;
    public static int REQUEST_JOBSEARCH = 1;
    ErrorDialog error;
    SimpleDateFormat format;
    String save_uri = API.url + "/upload/uploadtimesheet";
    timepicker timedialog;
    TextView startimetext;
    TextView endtimetext;
    Button timein;
    Button duration;
    int timesheet_id = 0;
    ArrayList<String> filearray = new ArrayList<>();
    GridLayout grid_layout;
    File mPhotoFile;
    Uri photoURI;
    public static int TAKE_PHOTO = 0;
    public static int SELECT_PHOTO = 2;
    public static int VIEW_IMAGE = 3;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addtimesheet);

        Intent intent = getIntent();
        timesheet_id = intent.getIntExtra("timesheet_id",0);
        init();
        userid = Integer.valueOf(getSharedPreferences("credential",MODE_PRIVATE).getString("user_id",""));
//        TimeSheetDatabaseHelper helper = new TimeSheetDatabaseHelper(this);
//        helper.onUpgrade(helper.getWritableDatabase(),Constants.DATABASE_VERSION,0);
    }

    public void init()
    {
        Button timesheet = (Button)findViewById(R.id.submittimesheet);
        timesheet.setOnClickListener(this);
        timein = (Button)findViewById(R.id.timein);
        timein.setOnClickListener(this);
        duration = (Button)findViewById(R.id.duration);
        duration.setOnClickListener(this);

        dialog =  new Datetimepickerapp(this);
        startimetext = (TextView)findViewById(R.id.starttime);
        endtimetext = (TextView)findViewById(R.id.endtime);
        jobid = 0;
        starttime = Calendar.getInstance();
        endtime = Calendar.getInstance();
        display(Calendar.getInstance(),startimetext);
        display(Calendar.getInstance(),endtimetext);
        format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss",Locale.getDefault());
        jobname = (EditText)findViewById(R.id.jobname);
        EditText notes = (EditText)findViewById(R.id.notes);
        type = "timein/out";
        notes.requestFocus();
        grid_layout = (GridLayout)findViewById(R.id.attachment);
        Button button_submit = (Button)findViewById(R.id.submittimesheet);
        button_submit.setOnClickListener(this);

        jobname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                {
                    Intent searchintent = new Intent(AddTimeSheet.this,SearchJob.class);
                    startActivityForResult(searchintent,REQUEST_JOBSEARCH);
                    overridePendingTransition(R.anim.top_in,R.anim.right_out);
                }
            }
        });

        if(timesheet_id > 0)
        {
            TimeSheetDatabaseHelper helper = new TimeSheetDatabaseHelper(this);
            Cursor cursor = helper.gettimesheetbyid(timesheet_id);
            if(cursor.moveToFirst())
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss",Locale.getDefault());
                try {
                    starttime.setTime(sdf.parse(cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_STARTTIME))));
                    endtime.setTime(sdf.parse(cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_ENDTIME))));
                    display(starttime,startimetext);
                    display(endtime,endtimetext);
                    jobname.setText(cursor.getString(cursor.getColumnIndex("jobname")));
                    notes.setText(cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_NOTES)));
                    jobid = cursor.getInt(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_JOBID));
                    String attachment = cursor.getString(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_ATTACHMENT));
                    String[] attachment_array = TextUtils.split(attachment,",");
                    ref_id = cursor.getInt(cursor.getColumnIndex(TimeSheetDatabaseHelper.KEY_REFID));
                    TextView titletxt = (TextView)findViewById(R.id.title);
                    titletxt.setText("Edit Timesheet");
                    for(int i = 0;i<attachment_array.length;i++)
                    {
                        if(attachment_array[i].isEmpty())
                        {
                            continue;
                        }
                        addfiletolayout(attachment_array[i],i);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String error = bundle.getString("data");
            Toast.makeText(AddTimeSheet.this,error,Toast.LENGTH_LONG).show();

            boolean result = bundle.getBoolean("success");
            if(result)
            {
                TimeSheetDatabaseHelper helper = new TimeSheetDatabaseHelper(AddTimeSheet.this);
                EditText notename = (EditText)findViewById(R.id.notes);

                String[] attachments = new String[filearray.size()];
                for(int i = 0;i<filearray.size();i++)
                {
                    attachments[i] = filearray.get(i);
                }

                helper.savetimesheet(starttime,endtime,notename.getText().toString(),userid,jobid,TextUtils.join(",",attachments),timesheet_id,bundle.getInt("ref_id",0),"clockout");
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_JOBSEARCH)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                String result = data.getStringExtra("jobname");
                jobid = data.getIntExtra("jobid",0);
                jobname.setText(result);
            }
            else
            {
                EditText notes = (EditText)findViewById(R.id.notes);
                notes.requestFocus();
            }
        }
        else if(requestCode == VIEW_IMAGE)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                int index = data.getIntExtra("index",0);
                Log.d("childcount",String.valueOf(grid_layout.getChildCount()));
                View v = grid_layout.getChildAt(index + 1);
                filearray.remove(index);
                grid_layout.removeView(v);
            }
        }
        else if (resultCode == RESULT_OK) {
            Uri photo = null; File photoFile = null;
            if (requestCode == TAKE_PHOTO) {
                try{
                    photo = photoURI;
                    photoFile = mPhotoFile;
                }catch (Exception e){
                    Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                }
            }
            else if(requestCode == SELECT_PHOTO){
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
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(photoFile != null)
            {
                addfiletolayout(photoFile.getAbsolutePath(),filearray.size());
            }


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void addfiletolayout(String file,int i)
    {
        filearray.add(file);
        LinearLayout layout_file = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layout_file.setLayoutParams(params);
        layout_file.setPadding(0, 0,(int) getResources().getDimension(R.dimen.margin_10dp),(int) getResources().getDimension(R.dimen.margin_10dp));

        final ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams params_image = new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.attachment_width),(int)getResources().getDimension(R.dimen.attachment_width));
        imageView.setLayoutParams(params_image);
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds=true;
            imageView.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(new File(file)), null, o));
            imageView.setImageURI(Uri.fromFile(new File(file)));
            layout_file.setTag(i);
            layout_file.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    String file = ""; int index = 0;
                    Log.d("tag",file);

                    int index_file = 0;
                    for(int i = 0;i<grid_layout.getChildCount() - 1;i++)
                    {
                        View layout_view = grid_layout.getChildAt(i + 1);
                        if(v.getTag() == layout_view.getTag())
                        {
                            file = filearray.get(i);
                            index_file = i;
                            break;
                        }
                    }
                    if(!file.isEmpty())
                    {
                        Intent intent = new Intent(getApplicationContext(),ImageActivity.class);
                        intent.putExtra("index",index_file);
                        intent.putExtra("file",file);
                        startActivityForResult(intent,VIEW_IMAGE);
                    }
                }
            });
            layout_file.addView(imageView);
            grid_layout.addView(layout_file);
        } catch (FileNotFoundException e) {
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

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.starttime:

                dialog.show();
                dialog.setDate(starttime);
                Button ok_btn = (Button)dialog.findViewById(R.id.button_ok);

                ok_btn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        starttime = (Calendar) dialog.getDate().clone();
                        TextView starttimetext = (TextView)findViewById(R.id.starttime);
                        display(starttime,starttimetext);
                        dialog.dismiss();
                    }
                });
                break;
            case R.id.endtime:
                dialog.show();
                dialog.setDate(endtime);
                ok_btn = (Button)dialog.findViewById(R.id.button_ok);
                ok_btn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        endtime = (Calendar) dialog.getDate().clone();
                        TextView endtimetext = (TextView)findViewById(R.id.endtime);
                        display(endtime,endtimetext);
                        dialog.dismiss();
                    }
                });
                break;
            case R.id.submittimesheet:
                if(validate())
                {
                    OkHttpClient client = new OkHttpClient();
                    MultipartBody.Builder request = new MultipartBody.Builder().setType(MultipartBody.FORM);
                    request.addFormDataPart("starttime",format.format(starttime.getTimeInMillis()));
                    request.addFormDataPart("endtime",format.format(endtime.getTimeInMillis()));
                    request.addFormDataPart("jobid",String.valueOf(jobid));
                    request.addFormDataPart("userid",String.valueOf(userid));
                    if(timesheet_id > 0)
                    {
                        request.addFormDataPart("ref_id",String.valueOf(ref_id));
                    }
                    EditText notename = (EditText)findViewById(R.id.notes);
                    request.addFormDataPart("notes",notename.getText().toString());

                    for(int i = 0;i<filearray.size();i++)
                    {
                        File f = new File(filearray.get(i));
                        String content_type = getMimeType(f.getPath());
                        RequestBody file_body = RequestBody.create(MediaType.parse(content_type),f);
                        request.addFormDataPart("image" + String.valueOf(i),filearray.get(i).substring(filearray.get(i).lastIndexOf("/")),file_body);
                    }

                    RequestBody body = request.build();
                    Request save_timesheet = new Request.Builder().url(save_uri).post(body).build();

                    client.newCall(save_timesheet).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Message msg = mhandler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("success",true);
                            bundle.putInt("ref_id",0);
                            bundle.putString("data","you have successfully added the new timesheet");

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String result = response.body().string();
                            Log.d("result",result);
                           if(response.isSuccessful())
                            {
                                try {
                                    JSONObject object = new JSONObject(result);
                                    boolean enable = object.getBoolean("success");
                                    Message message = mhandler.obtainMessage();
                                    Bundle bundle = new Bundle();
                                    if(!enable)
                                    {
                                        String error = "You have selected wrong time period. this is already exist";
                                        bundle.putString("data",error);
                                        bundle.putBoolean("success",enable);
                                    }
                                    else
                                    {
                                        bundle.putBoolean("success",enable);
                                        bundle.putInt("ref_id",object.getInt("ref_id"));
                                        bundle.putString("data","you have successfully added the new timesheet");
                                    }
                                    message.setData(bundle);
                                    mhandler.sendMessage(message);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    });
                }
                break;
            case R.id.total:
                if(type.contentEquals("duration"))
                {
                    timedialog = new timepicker(this);
                    timedialog.show();

                    Button btn_ok = (Button)timedialog.findViewById(R.id.button_ok);
                    btn_ok.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            Calendar clone = (Calendar) starttime.clone();
                            clone.add(Calendar.HOUR,timedialog.gethour());
                            clone.add(Calendar.MINUTE,timedialog.getmin());
                            endtime = clone;
                            display(endtime,endtimetext);
                            timedialog.dismiss();
                        }
                    });
                };
                break;
            case R.id.timein:
                type = "timein/out";
                display_type(type);
                break;
            case R.id.duration:
                type = "duration";
                display_type(type);
                break;
            case R.id.close:
                finish();
                break;
            case R.id.add_attachment:
                dialog_custom = new CustomDialog(this);
                dialog_custom.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog_custom.show();
                Button button_take_photo = (Button) dialog_custom.findViewById(R.id.take_photo);
                button_take_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dispatchTakePictureIntent();
                        dialog_custom.dismiss();
                    }
                });

                Button select_photo = (Button) dialog_custom.findViewById(R.id.select_photo);
                select_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        get_photo_from_gallery();
                        dialog_custom.dismiss();
                    }
                });

                Button cancel = (Button) dialog_custom.findViewById(R.id.cancel);
                cancel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog_custom.dismiss();
                    }
                });
                break;
        }
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
        startActivityForResult(gallery,SELECT_PHOTO);
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
                startActivityForResult(takePictureIntent, TAKE_PHOTO);
            }
        }

    }
    public void display_error(String str)
    {
        error = new ErrorDialog(this);
        error.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        error.show();
        TextView error_message = error.findViewById(R.id.error_message);
        error_message.setText(str);

        Button ok_btn = (Button)error.findViewById(R.id.ok);
        ok_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                error.dismiss();
            }
        });
    }

    public boolean validate()
    {
        boolean enable = true;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());

        if(endtime.before(starttime) || Calendar.getInstance().before(endtime))
        {
            String error = "Please Reselect the date";
            display_error(error);
            return false;
        }

        EditText notename = (EditText)findViewById(R.id.notes);

        if(isEmpty(notename))
        {
            String error = "I think you forgot enter in Notename, Please Type in notename and try again";
            display_error(error);
            return false;
        }

        if(jobid == 0)
        {
            String error = "Please Select Job For TimeSheet";
            display_error(error);
            return false;
        }

        return enable;
    }

    public boolean isEmpty(EditText text)
    {
        String str = text.getText().toString();
        if(str.isEmpty())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void display(Calendar date, TextView text)
    {
        Calendar cur_date = Calendar.getInstance();
        String str = "";
        if(DateUtils.isToday(date.getTimeInMillis()))
        {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            str = "Today, " + sdf.format(date.getTimeInMillis());
        }
        else
        {
            SimpleDateFormat sdf = new SimpleDateFormat("E MMM, dd hh:mm a",Locale.getDefault());
            str = sdf.format(date.getTimeInMillis());
        }

        TextView edittext = (TextView)findViewById(R.id.total);

        String total = display_total(starttime,endtime);
        edittext.setText(total);
        text.setText(str);
    }

    public String display_total(Calendar start,Calendar endtime)
    {
        long millis = endtime.getTimeInMillis() - start.getTimeInMillis();

        Log.d("millisecond",String.valueOf(millis));
        int hours = (int) (abs(millis) / (1000 * 60 * 60));
        int mins = (int) ((abs(millis)/(1000*60)) % 60);
        int secs = (int) ((abs(millis) / 1000) % 60);

        NumberFormat f = new DecimalFormat("00");
        String str = "";

        if(millis < 0)
        {
            str = "-";
        }
        if(hours > 0)
        {
            str += f.format(hours) + "h ";
        }

        str += f.format(mins) + "m " + f.format(secs) + "s";

        return str;

    }

    @TargetApi(Build.VERSION_CODES.M)
    public void display_type(String type)
    {
        RelativeLayout layout = (RelativeLayout)findViewById(R.id.endtimecontainer);
        if(type.contentEquals("timein/out"))
        {
            layout.setVisibility(View.VISIBLE);
            timein.setTextAppearance(this,R.style.timeoff_btn);
            timein.setBackgroundColor(getResources().getColor(R.color.colorButton));
            duration.setTextAppearance(this,R.style.active_nav);
            duration.setBackgroundColor(getResources().getColor(R.color.white));
        }
        else if(type.contentEquals("duration"))
        {
            layout.setVisibility(View.GONE);
            duration.setTextAppearance(this,R.style.timeoff_btn);
            duration.setBackgroundColor(getResources().getColor(R.color.colorButton));
            timein.setTextAppearance(this,R.style.active_nav);
            timein.setBackgroundColor(getResources().getColor(R.color.white));
        }
    }
}

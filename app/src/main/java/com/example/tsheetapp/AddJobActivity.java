package com.example.tsheetapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddJobActivity extends AppCompatActivity implements View.OnClickListener{
    ErrorDialog error;
    Spinner parent;
    JSONArray jobs;
    ArrayAdapter<String> adapter;
    ArrayList<String> list;
    String user_id;
    int jobid = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_job);

        Button button_submit = (Button)findViewById(R.id.button_timesheet);
        button_submit.setOnClickListener(this);
        jobs = new JSONArray();
        list = new ArrayList<String>();
        Switch switch_btn = (Switch)findViewById(R.id.sub_item);
        parent = (Spinner)findViewById(R.id.parent_job);
        switch_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                parent.setEnabled(isChecked);
            }
        });
        jobid = getIntent().getIntExtra("jobid",0);
        SharedPreferences preferences = getSharedPreferences("credential", Context.MODE_PRIVATE);
        user_id = preferences.getString("user_id","0");
        init_adapter();
        init();
        parent.setAdapter(adapter);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void init_adapter()
    {
        DatabaseHelper helper = new DatabaseHelper(this);
        Cursor cursor = helper.get_jobs();
        int index = 0;

        Log.d("cursorlength",String.valueOf(cursor.getCount()));
        for(int i = 0;i<jobs.length();i++)
        {
            jobs.remove(i);
        }
        list.clear();
        list.add("Select A Parent Job");

        //Toast.makeText(this,cursor.getColumnCount(),Toast.LENGTH_LONG).show();
        if(cursor.moveToFirst())
        {
            do
            {
                JSONObject object = new JSONObject();
                try {
                    String jobname = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_JOBNAME));
                    object.put(DatabaseHelper.KEY_ID,cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_ID)));
                    object.put(DatabaseHelper.KEY_USERID,cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_USERID)));
                    object.put(DatabaseHelper.KEY_SHORTCODE,cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_SHORTCODE)));
                    object.put(DatabaseHelper.KEY_PARENTID,cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_PARENTID)));
                    object.put(DatabaseHelper.KEY_ASSIGNED,cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_ASSIGNED)));
                    object.put(DatabaseHelper.KEY_JOBNAME,cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_JOBNAME)));
                    jobs.put(object);
                    list.add(jobname);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }while (cursor.moveToNext());
            Log.d("listsize",String.valueOf(list.size()));
        }
    }


    public void init()
    {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,list) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView)v.findViewById(android.R.id.text1)).setText("");
                    ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
                }

                return v;
            }

            @Override
            public int getCount() {
                return super.getCount(); // you dont display last item. It is used as hint.
            }

        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        DatabaseHelper helper = new DatabaseHelper(this);
        if(jobid > 0)
        {
            Cursor cursor = helper.get_job_byid(jobid);
            if(cursor.moveToFirst())
            {
                EditText jobname = (EditText)findViewById(R.id.job_name);
                jobname.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_JOBNAME)));
                EditText shortcode = (EditText)findViewById(R.id.short_code);
                shortcode.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_SHORTCODE)));
                String assigns = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_ASSIGNED));
                CheckBox me = (CheckBox)findViewById(R.id.checkbox_me);
                CheckBox everyone = (CheckBox)findViewById(R.id.checkbox_everyone);
                if(TextUtils.indexOf(assigns,"me") > -1)
                {
                    me.setChecked(true);
                }

                if(TextUtils.indexOf(assigns,"everyone") > -1)
                {
                    everyone.setChecked(true);
                }
            }
        }


    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.button_timesheet:
                try{
                    String str = submit();
                    if(str != null)
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
                    else
                    {
                        Toast.makeText(this,"You have successfully save the job",Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                catch (Exception e){

                }
                break;
            case R.id.close:
                finish();
        }
    }

    public String submit()
    {
        EditText jobname = (EditText)findViewById(R.id.job_name);

        if(isEmpty(jobname))
        {
            return "Looks Like you forgot to enter a job name. Please enter a jobname and retry again";
        }

        EditText shortcode = (EditText)findViewById(R.id.short_code);

        if(isEmpty(shortcode))
        {
            return "Looks Like you forgot to enter a short code. Please enter a short code and retry again";
        }

        CheckBox me = (CheckBox)findViewById(R.id.checkbox_me);
        CheckBox everyone = (CheckBox)findViewById(R.id.checkbox_everyone);

        String assigns = "";
        boolean checked_me = false;
        if(me.isChecked())
        {
            assigns += "me";
            checked_me = true;
        }

        if(everyone.isChecked())
        {
            if(checked_me)
            {
                assigns += ",";
            }
            assigns += "everyone";
        }

        add_job(jobname.getText().toString(),shortcode.getText().toString(),assigns);
        return null;

    }

    public void add_job(String jobname,String shortcode,String assigns)
    {
        Log.d("add_job","add");
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder request = new MultipartBody.Builder().setType(MultipartBody.FORM);

        request.addFormDataPart("jobname",jobname);
        request.addFormDataPart("shortcode",shortcode);
        request.addFormDataPart("assigns",assigns);
        request.addFormDataPart("user_id",user_id);
        request.addFormDataPart("id","" + jobid);
        RequestBody body = request.build();

        String url = API.url + "/jobs/add";
        Request save_job = new Request.Builder().url(url).post(body).build();

        client.newCall(save_job).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("uploadprogress",e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                 if(response.isSuccessful())
                {
                    String jsondata = response.body().string();
                    Log.d("uploadresult",jsondata);
                    try {
                        JSONObject jobject = new JSONObject(jsondata);
                        JSONArray array = jobject.getJSONArray("job");

                        DatabaseHelper helper = new DatabaseHelper(AddJobActivity.this);
                        helper.job_replace(array);
                        init_adapter();
                        init();

                    } catch (JSONException e) {
                        Log.d("uploaderror",e.getMessage());
                        e.printStackTrace();
                    }
                }
                else
                {

                }
            }
        });

    };

    public boolean isEmpty(EditText text)
    {
        if(text.getText().toString().isEmpty())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}

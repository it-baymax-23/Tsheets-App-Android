package com.example.tsheetapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchJob extends AppCompatActivity implements  View.OnClickListener{

    ArrayAdapter<String> adapter;
    ArrayList<String> adapter_array;
    ArrayList<Integer> job_id;
    ListView joblist;
    EditText textView;
    boolean selected = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_jobs);
        String role = getSharedPreferences("credential",MODE_PRIVATE).getString("role","");

        if(role.contentEquals("employee"))
        {
            TextView text = (TextView)findViewById(R.id.addjob);
            text.setVisibility(View.GONE);
        }

        textView = (EditText)findViewById(R.id.search_job_container);
        adapter_array = new ArrayList<String>();
        job_id = new ArrayList<Integer>();
        joblist = (ListView)findViewById(R.id.joblist);

        Intent intent = getIntent();
        selected = intent.getBooleanExtra("selected",true);
        textView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
                Cursor cursor = helper.get_jobs(textView.getText().toString());
                init(cursor);
                return false;
            }
        });

        textView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
                Cursor cursor = helper.get_jobs(textView.getText().toString());
                init(cursor);
                return false;
            }
        });
        DatabaseHelper helper = new DatabaseHelper(this);
        Cursor cursor = helper.get_jobs();

        init(cursor);
    }

    public void init(Cursor cursor)
    {
        adapter_array.clear();
        job_id.clear();

        if(cursor.moveToFirst())
        {
            do {
                job_id.add(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_ID)));
                adapter_array.add(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_JOBNAME)));
            }while(cursor.moveToNext());
        }

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,adapter_array);
        joblist.setAdapter(adapter);

        joblist.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int jobid = job_id.get(position);
                Intent intent = new Intent();
                if(selected)
                {
                    intent.putExtra("jobid",jobid);
                    intent.putExtra("jobname",adapter_array.get(position));
                    setResult(Activity.RESULT_OK,intent);
                    finish();
                }
                else
                {
                    intent = new Intent(getApplicationContext(),AddJobActivity.class);
                    intent.putExtra("jobid",jobid);
                    startActivity(intent);
                }
            }
        });
        //textView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.cancel:
                Intent result = new Intent();
                setResult(Activity.RESULT_CANCELED,result);
                finish();
                break;
            case R.id.addjob:
                Intent intent = new Intent(this,AddJobActivity.class);
                startActivity(intent);
                break;
        }
    }
}

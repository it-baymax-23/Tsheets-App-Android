package com.example.tsheetapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.zip.Inflater;

public class EmployeeActivity extends AppCompatActivity implements View.OnClickListener{
    ArrayList<String> username;
    ArrayList<Integer> userid;
    ArrayList<Boolean> checked;
    ListView list;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employeer);

        init();
    }

    public void init(){
        EmployeerDatabasehelper helper = new EmployeerDatabasehelper(this);
        Cursor cursor = helper.get_json();
        username = new ArrayList<String>();
        userid = new ArrayList<Integer>();
        checked = new ArrayList<Boolean>();
        if(cursor.moveToFirst())
        {
            do{
                String user  = cursor.getString(cursor.getColumnIndex(EmployeerDatabasehelper.KEY_USERNAME));
                username.add(user);
                int useridentry = cursor.getInt(cursor.getColumnIndex(EmployeerDatabasehelper.KEY_ID));
                userid.add(useridentry);
                checked.add(false);

            }while(cursor.moveToNext());
        }

        list = (ListView)findViewById(R.id.employeelist);
        list.setAdapter(new ArrayAdapter<String>(this,R.layout.list_item_employeer,username){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                View v = convertView;
                if(v == null)
                {
                    v = inflater.inflate(R.layout.list_item_employeer,null);
                }
                CheckBox employeerselect = (CheckBox)v.findViewById(R.id.employeer_checkbox);

                employeerselect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int position = (int)buttonView.getTag();
                        checked.set(position,isChecked);
                    }
                });

                employeerselect.setTag(position);
                TextView text = (TextView)v.findViewById(R.id.employeer_name);
                text.setText(username.get(position));
                return v;
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.cancel:
                Intent intent = new Intent();
                setResult(Activity.RESULT_CANCELED,intent);
                finish();
            case R.id.save:
                intent = new Intent();
                int size = checked.size();
                ArrayList<String> usernamestring = new ArrayList<String>();
                ArrayList<String> useridstring = new ArrayList<String>();

                int j = 0;
                for(int i = 0; i<size;i++)
                {
                    if(checked.get(i))
                    {
                        usernamestring.add(username.get(i));
                        useridstring.add(String.valueOf(userid.get(i)));
                        j++;
                    }
                }

                intent.putExtra("username",TextUtils.join(",",usernamestring));
                intent.putExtra("userid",TextUtils.join(",",useridstring));
                setResult(Activity.RESULT_OK,intent);
                finish();
        }
    }
}

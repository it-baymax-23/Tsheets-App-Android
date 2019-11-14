package com.example.tsheetapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ColorActivity extends AppCompatActivity implements View.OnClickListener{
    ArrayList<JSONObject> color_object;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_color);
        init();
    }

    public void init()
    {
        String[] colors = Constants.colors;
        String[] colorname = Constants.colorname;

        color_object = new ArrayList<JSONObject>();
        for(int i = 0;i<colors.length;i++)
        {
            JSONObject object = new JSONObject();
            try {
                object.put("color",colors[i]);
                object.put("colorname",colorname[i]);
                color_object.add(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ArrayAdapter<JSONObject> adapter = new ArrayAdapter<JSONObject>(this,R.layout.list_item_color,color_object){
            int selected_position = 0;

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = convertView;
                if(v == null)
                {
                    LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                    v = inflater.inflate(R.layout.list_item_color,null);
                }

                RadioButton button = (RadioButton) v.findViewById(R.id.colorselect);
                TextView text = (TextView)v.findViewById(R.id.colorname);
                LinearLayout layout = (LinearLayout)v.findViewById(R.id.colordisplay);
                try {
                    text.setText(color_object.get(position).getString("colorname"));
                    String color = color_object.get(position).getString("color");
                    Log.d("color",color);
                    button.setTag(position);
                    layout.setBackgroundColor(Color.parseColor(color));

                    button.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            int position = (int) v.getTag();
                            Log.d("colorposition",String.valueOf(position));
                            Intent intent = new Intent();
                            try {
                                intent.putExtra("colorname",color_object.get(position).getString("colorname"));
                                intent.putExtra("color",color_object.get(position).getString("color"));
                                setResult(Activity.RESULT_OK,intent);
                                finish();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return v;
            }
        };

        ListView list = (ListView)findViewById(R.id.colorcontainer);
        list.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.cancel)
        {
            Intent intent = new Intent();
           setResult(Activity.RESULT_CANCELED,intent);
           finish();
        }
    }
}

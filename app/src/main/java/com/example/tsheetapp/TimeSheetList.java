package com.example.tsheetapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.List;

public class TimeSheetList extends ArrayAdapter<JSONObject>{

    Context mctx;
    int resource;
    List<JSONObject> arraylist;
    public TimeSheetList(@NonNull Context context, int resource, @NonNull List<JSONObject> objects) {
        super(context, resource, objects);
        this.mctx = context;
        this.resource = resource;
        this.arraylist = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mctx);
        View view = inflater.inflate(R.layout.list_item,null);
        JSONObject object = getItem(position);


        Log.d("objectentry",object.toString());
        Log.d("position",String.valueOf(position));
        try {
            if(object.getString("type").contentEquals("total"))
            {
                TextView listleft = (TextView)view.findViewById(R.id.list_left);
                listleft.setText(object.getString("date"));
                TextView listrighttop = (TextView)view.findViewById(R.id.list_righttop);
                listrighttop.setText(object.getString("duration"));
                TextView listrightbottom = (TextView)view.findViewById(R.id.list_rightbottom);
                listrightbottom.setText("");
                listrightbottom.setVisibility(View.GONE);
                view.setBackgroundColor(mctx.getResources().getColor(R.color.listtext_parent));
            }
            else
            {
                TextView listleft = (TextView)view.findViewById(R.id.list_left);
                listleft.setText(object.getString("jobname"));
                if(!object.getString("state").contentEquals("clockin"))
                {
                    TextView listrighttop = (TextView)view.findViewById(R.id.list_righttop);
                    listrighttop.setText(object.getString("duration"));

                }
                TextView listrightbottom = (TextView)view.findViewById(R.id.list_rightbottom);
                listrightbottom.setText(object.getString("period"));

            }

        } catch (JSONException e) {
            Log.d("exception",e.getMessage());
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public int getCount() {
        Log.d("arraylistsize",String.valueOf(arraylist.size()));
        return arraylist.size();
    }


}

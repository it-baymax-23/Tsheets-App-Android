package com.example.tsheetapp;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WorkingEmployeer extends AppCompatActivity implements View.OnClickListener{
    String user_id;
    ArrayList<JSONObject> employee_list;
    ListView employeer;
    EditText search_text;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workingemployeer);
        employeer = (ListView)findViewById(R.id.list_employeer);
        user_id = getSharedPreferences("credential",MODE_PRIVATE).getString("user_id","");
        search_text = (EditText)findViewById(R.id.search_edit);
        initdatabase();
        display_search(false);
        search_text.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                EmployeerDatabasehelper helper = new EmployeerDatabasehelper(getApplicationContext());
                Cursor cursor = helper.get_user_by_name(search_text.getText().toString());
                try {
                    init_list(cursor);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        //init();
    }

    public void init()
    {
        EmployeerDatabasehelper helper = new EmployeerDatabasehelper(this);
        Cursor cursor = helper.get_json();
        Log.d("cursorcount",String.valueOf(cursor.getCount()));
        try {
            init_list(cursor);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void init_list(Cursor cursor) throws JSONException {
        employee_list = new ArrayList<JSONObject>();
        if(cursor.moveToFirst())
        {
            do {
                JSONObject object = new JSONObject();
                object.put("username",cursor.getString(cursor.getColumnIndex(EmployeerDatabasehelper.KEY_USERNAME)));
                object.put("profile",cursor.getString(cursor.getColumnIndex(EmployeerDatabasehelper.KEY_PROFILE)));
                employee_list.add(object);
            }while(cursor.moveToNext());
        }

        ArrayAdapter<JSONObject> adapter= new ArrayAdapter<JSONObject>(this,R.layout.list_item_employeer,employee_list){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                View v = convertView;
                if(v == null)
                {
                    v = inflater.inflate(R.layout.employeer_list,null);
                }

                JSONObject object = employee_list.get(position);
                try {
                    TextView profiletext = (TextView)v.findViewById(R.id.profiletext);
                    profiletext.setText(object.getString("username").toUpperCase().substring(0,1));
                    if(!object.getString("profile").isEmpty())
                    {
                        CircleImageView imageview = (CircleImageView)v.findViewById(R.id.profile);
                        imageview.setVisibility(View.GONE);
                        new DownloadImageEmployee(imageview,profiletext).execute(API.url + object.getString("profile"));
                    }

                    TextView username = (TextView) v.findViewById(R.id.username);
                    username.setText(object.getString("username"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
               return v;
            }
        };
        employeer.setAdapter(adapter);
    }

    Handler mhandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String str = bundle.getString("data");
            try {
                JSONArray array = new JSONArray(str);
                EmployeerDatabasehelper helper = new EmployeerDatabasehelper(getApplicationContext());
                helper.database_update(array);
                init();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.handleMessage(msg);
        }
    };
    public void initdatabase()
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(API.url + "/employee/get/" + user_id).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bodystring = response.body().string();
                Log.d("employeer list",bodystring);
                if(response.isSuccessful())
                {
                    Message msg = mhandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("data",bodystring);
                    msg.setData(bundle);
                    mhandler.sendMessage(msg);
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.close:
                finish();
                break;
            case R.id.search:
                display_search(true);
                break;
            case R.id.cancel:
                display_search(false);
                break;
        }
    }

    public void display_search(boolean enable)
    {
        TextView title = (TextView)findViewById(R.id.action_title);
        ImageView search_btn = (ImageView)findViewById(R.id.search);
        TextView cancel_search = (TextView)findViewById(R.id.cancel_search);
        if(enable)
        {
            search_text.setVisibility(View.VISIBLE);
            title.setVisibility(View.GONE);
            search_btn.setVisibility(View.GONE);
            cancel_search.setVisibility(View.GONE);
        }
        else
        {
            search_text.setVisibility(View.GONE);
            title.setVisibility(View.VISIBLE);
            search_btn.setVisibility(View.VISIBLE);
            cancel_search.setVisibility(View.VISIBLE);
        }
    }
}

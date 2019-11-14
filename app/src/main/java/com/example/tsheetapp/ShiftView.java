package com.example.tsheetapp;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShiftView extends AppCompatActivity implements View.OnClickListener{
    ListView list;
    ArrayList<JSONObject> schedule;
    Button myschedule;
    Button fullschedule;
    SharedPreferences preferences;
    int user_id;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule);

        myschedule = (Button)findViewById(R.id.myschedule);
        fullschedule = (Button)findViewById(R.id.full_schedule);

        myschedule.setOnClickListener(this);
        fullschedule.setOnClickListener(this);

        preferences = getSharedPreferences("credential",MODE_PRIVATE);
        user_id = Integer.valueOf(preferences.getString("user_id","0"));
        String role = preferences.getString("role","");
        if(role.contentEquals("employee"))
        {
            TextView textView = (TextView)findViewById(R.id.addschedule);
            textView.setVisibility(View.GONE);
        }
        TextView user = (TextView)findViewById(R.id.username);
        CircleImageView imageView = (CircleImageView)findViewById(R.id.image_user);


        Constants.display_profile(this,user,imageView);
    }

    @Override
    protected void onStart() {
        init_arraylist();
        super.onStart();
    }

    @Override
    protected void onResume() {
        init_arraylist();
        super.onResume();
    }

    public void init_arraylist()
    {
        ShiftDatabaseHelper helper = new ShiftDatabaseHelper(this);
        Cursor cursor = helper.get_schedule_for_user(2);
        Log.d("cursorlength",String.valueOf(cursor.getCount()));
        init(cursor);

    }
    public void init(Cursor cursor)
    {

        schedule = new ArrayList<JSONObject>();
        if(cursor.moveToFirst())
        {
            String datestring = "";
            do{
                JSONObject jsonobject = new JSONObject();
                try {
                    String date = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_DATE));
                    Log.d("datesub",date);
                    if(!datestring.contentEquals(date))
                    {
                        datestring = date;
                        JSONObject total = new JSONObject();
                        SimpleDateFormat sdf = new SimpleDateFormat("E,MMM dd", Locale.getDefault());
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
                        total.put("title",sdf.format((format.parse(date))));
                        total.put("type","total");
                        schedule.add(total);
                    }
                    String title = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_TITLE));

                    if(title.isEmpty())
                    {
                        title = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_NOTES));
                    }

                    if(title.isEmpty())
                    {
                        title = cursor.getString(cursor.getColumnIndex("jobtitle"));
                    }

                    jsonobject.put("title",title);
                    jsonobject.put("id",cursor.getInt(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_ID)));

                    int allday = cursor.getInt(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_SHIFTTYPE));
                    String time = "";
                    if(allday == 1)
                    {
                        time = "All Day";
                    }
                    else
                    {
                        time = cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_STARTTIME)) + " - " + cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_ENDTIME));
                    }

                    jsonobject.put("time",time);
                    jsonobject.put("color",cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_COLOR)));
                    jsonobject.put("type","schedule");
                    jsonobject.put("assigned",cursor.getString(cursor.getColumnIndex(ShiftDatabaseHelper.KEY_ASSIGNS)));
                    schedule.add(jsonobject);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }while(cursor.moveToNext());
        }

        ArrayAdapter<JSONObject> adapter = new ArrayAdapter<JSONObject>(this,R.layout.schedule_list_item,schedule){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                View v = inflater.inflate(R.layout.schedule_list_item,null);


                JSONObject data = schedule.get(position);
                String type = null;
                try {
                    type = data.getString("type");
                    TextView title = (TextView) v.findViewById(R.id.title);
                    title.setText(data.getString("title"));
                    TextView time = (TextView)v.findViewById(R.id.datetime);
                    if(type.contentEquals("schedule"))
                    {
                        time.setText(data.getString("time"));
                        RelativeLayout layout = (RelativeLayout)v.findViewById(R.id.container);
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
                        params.setMargins(10,5,10,5);
                        layout.setPadding(10,10,10,10);
                        layout.setBackgroundResource(R.drawable.background_resource);
                        GradientDrawable drawable = (GradientDrawable) layout.getBackground();
                        drawable.setColor(Color.parseColor(data.getString("color")));

                        boolean whitecolor = true;
                        if(Constants.isBrightColor(Color.parseColor(data.getString("color"))))
                        {
                            title.setTextColor(getResources().getColor(R.color.solid_black));
                            whitecolor = true;
                            time.setTextColor(getResources().getColor(R.color.solid_black));
                        }
                        else
                        {
                            whitecolor = false;
                            title.setTextColor(getResources().getColor(R.color.white));
                            time.setTextColor(getResources().getColor(R.color.white));
                        }
                        LinearLayout assigned = (LinearLayout) v.findViewById(R.id.assigned);
                        Log.d("assigned_user",data.getString("assigned"));
                        if(!data.getString("assigned").isEmpty())
                        {
                            EmployeerDatabasehelper helper = new EmployeerDatabasehelper(getApplicationContext());
                            Cursor users = helper.get_users(data.getString("assigned"));
                            int j = 1;
                            Log.d("usercount",String.valueOf(users.getCount()));
                            if(users.moveToFirst())
                            {
                                do {
                                    String username = users.getString(users.getColumnIndex(EmployeerDatabasehelper.KEY_USERNAME));
                                    String profile = users.getString(users.getColumnIndex(EmployeerDatabasehelper.KEY_PROFILE));
                                    int scale = (int) Resources.getSystem().getDisplayMetrics().density;
                                    LinearLayout.LayoutParams paramstext = new LinearLayout.LayoutParams(36 * scale,36 * scale);

                                    TextView text = new TextView(getApplicationContext());
                                    text.setLayoutParams(paramstext);
                                    //((RelativeLayout.LayoutParams)text.getLayoutParams()).setMargins(0,0,10 * j,0);
                                    text.setBackgroundResource(R.drawable.circle);
                                    GradientDrawable gradient = (GradientDrawable) text.getBackground();
                                    text.setTextAppearance(getApplicationContext(),R.style.s);
                                    text.setGravity(Gravity.CENTER);
                                    if(whitecolor)
                                    {
                                        gradient.setColor(getResources().getColor(R.color.solid_black));
                                        text.setTextColor(getResources().getColor(R.color.white));
                                    }
                                    else
                                    {
                                        gradient.setColor(getResources().getColor(R.color.white));
                                        text.setTextColor(getResources().getColor(R.color.solid_black));
                                    }
                                    text.setText(username.toUpperCase().substring(0,1));

                                    assigned.addView(text);
                                    if(!profile.isEmpty())
                                    {
                                        CircleImageView imageView = new CircleImageView(getApplicationContext());
                                        imageView.setLayoutParams(paramstext);
                                        imageView.setVisibility(View.GONE);
                                        //((RelativeLayout.LayoutParams)imageView.getLayoutParams()).setMargins(0,0,10 * j,0);
                                        assigned.addView(imageView);
                                        //Log.d("margin_image",String.valueOf(((RelativeLayout.LayoutParams) imageView.getLayoutParams()).rightMargin));
                                        (new DownloadImageEmployee(imageView,text)).execute(API.url + profile);
                                    }

                                    j++;
                                }while(users.moveToNext());
                                assigned.invalidate();
                                Log.d("layoutchild","" + assigned.getChildCount());
                            }
                        }
                    }
                    else
                    {
                        time.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return v;

            }
        };
        list = (ListView)findViewById(R.id.list_schedule);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject object = schedule.get(position);
                try {
                    String type = object.getString("type");

                    if(type.contentEquals("schedule"))
                    {
                        Message msg = mhandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("id",String.valueOf(object.getInt("id")));
                        msg.setData(bundle);
                        mhandler.sendMessage(msg);
                        Log.d("schedule_type",type);
                    }
                } catch (JSONException e) {
                    Log.d("jsonexception",e.getMessage());
                    e.printStackTrace();
                }

            }
        });
    }
    Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String id = (String) bundle.get("id");
            Intent intent = new Intent(ShiftView.this,ScheduleView.class);
            intent.putExtra("id",Integer.valueOf(id));
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        }
    };




    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.myschedule:
                display("myschedule");
                break;
            case R.id.full_schedule:
                display("fullschedule");
                break;
            case R.id.addschedule:
                Intent intent = new Intent(this,ShiftAddActivity.class);
                startActivity(intent);
                break;
            case R.id.overview:
                intent = new Intent(this,DashboardActivity.class);
                startActivity(intent);
                break;
            case R.id.timesheet:
                intent = new Intent(this,TimesheetView.class);
                startActivity(intent);
                break;
            case R.id.timecard:
                intent = new Intent(this,TimeCardActivity.class);
                startActivity(intent);
                break;
            case R.id.more:
                intent = new Intent(this,MoreActivity.class);
                startActivity(intent);
                break;
            case R.id.username:
            case R.id.image_user:
                intent = new Intent(this,Profile.class);
                startActivity(intent);
                break;

        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void display(String type)
    {
        if(type.contentEquals("myschedule"))
        {
            ShiftDatabaseHelper helper = new ShiftDatabaseHelper(this);
            myschedule.setBackground(getResources().getDrawable(R.drawable.color_btn));
            fullschedule.setBackground(getResources().getDrawable(R.drawable.btn_schedule));
            Cursor cursor = helper.get_schedule_for_user(2);
            fullschedule.setTextColor(Color.parseColor("#0D65C6"));
            myschedule.setTextColor(Color.parseColor("#FFFFFF"));
            init(cursor);
        }
        else
        {
            ShiftDatabaseHelper helper = new ShiftDatabaseHelper(this);
            myschedule.setBackground(getResources().getDrawable(R.drawable.btn_schedule_left));
            fullschedule.setBackground(getResources().getDrawable(R.drawable.color_btn_right));
            Cursor cursor = helper.get_schedule_for_job("15,25");
            fullschedule.setTextColor(Color.parseColor("#FFFFFF"));
            myschedule.setTextColor(Color.parseColor("#0D65C6"));
            init(cursor);
        }
    }
}

package com.example.tsheetapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangePassword extends AppCompatActivity implements View.OnClickListener{
    String user_id;
    String user_name;
    TextInputLayout current_password,password,rpassword;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepassword);
        Button changepassword = (Button)findViewById(R.id.changepassword);
        changepassword.setOnClickListener(this);
        init();
    }

    public void init()
    {
        SharedPreferences preferences = getSharedPreferences("credential",MODE_PRIVATE);
        user_id = preferences.getString("user_id","0");
        user_name = preferences.getString("user","");
        TextView username = (TextView)findViewById(R.id.username);
        username.setText("Hi, " + user_name);
        current_password = findViewById(R.id.currentpassword);
        password = findViewById(R.id.newpassword);
        rpassword = findViewById(R.id.rpassword);

    }
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.prev:
                finish();
                break;
            case R.id.changepassword:
                verify();
                break;
            case R.id.username:

        }
    }

    public void verify()
    {
        boolean enable = true;
        if(is_empty(current_password))
        {
            enable = false;
        }

        if(is_empty(password))
        {
            enable = false;
        }

        if(is_empty(rpassword))
        {
            enable = false;
        }

        if(!password.getEditText().getText().toString().contentEquals(rpassword.getEditText().getText().toString()))
        {
            enable = false;
            rpassword.setError("Confirm password has to be equal to New Password");
        }
        if(enable)
        {
            changepassword();
        }
    }

    Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            try {
                if(bundle.getBoolean("success"))
                {
                    JSONObject object = new JSONObject(bundle.getString("message"));
                    if(object.getBoolean("success"))
                    {
                        Toast.makeText(getApplicationContext(),"You have successfully changed password",Toast.LENGTH_LONG).show();
                    }
                    else {
                        current_password.setError("Password is not matched");
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please check internet or contact the server",Toast.LENGTH_LONG).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public void changepassword()
    {
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder request_body = new MultipartBody.Builder().setType(MultipartBody.FORM);
        request_body.addFormDataPart("user_id",user_id);
        request_body.addFormDataPart("confirm_password",current_password.getEditText().getText().toString());
        request_body.addFormDataPart("password",password.getEditText().getText().toString());

        RequestBody body = request_body.build();
        Request request = new Request.Builder().url(API.url + "/auth/changepass").post(body).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("exception",e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Message msg = mhandler.obtainMessage();
                Bundle bundle = new Bundle();
                if(response.isSuccessful())
                {
                    bundle.putBoolean("success",true);
                    bundle.putString("message",response.body().string());
                }
                else
                {
                    bundle.putBoolean("success",false);
                }

                msg.setData(bundle);
                mhandler.sendMessage(msg);
            }
        });
    }


    public boolean is_empty(TextInputLayout layout)
    {
        if(layout.getEditText().getText().toString().isEmpty())
        {
            layout.setError("This field is required");
            return true;
        }
        else
        {
            layout.setError(null);
            return false;
        }
    }
}

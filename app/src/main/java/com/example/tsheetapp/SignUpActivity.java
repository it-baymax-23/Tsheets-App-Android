package com.example.tsheetapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{

    TextInputLayout username,company_name,email,phone_number,password;
    Map<String,String> params;
    String role;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        username = (TextInputLayout) findViewById(R.id.username);
        company_name = (TextInputLayout) findViewById(R.id.company_name);
        email = (TextInputLayout)findViewById(R.id.email);
        phone_number = (TextInputLayout)findViewById(R.id.phone_number);
        password = (TextInputLayout)findViewById(R.id.password);

        Button create_account = (Button)findViewById(R.id.create_account);
        create_account.setOnClickListener(this);
        params = new HashMap<String,String>();

        Intent intent = getIntent();
        role = intent.getStringExtra("role");
    }

    public boolean check_validate()
    {
        boolean enable = true;

        if(isempty(username))
        {
            username.setError("this field is required");
            enable = false;
        }
        else
        {
            username.setError(null);
            params.put("username",username.getEditText().getText().toString());
        }

        if(isempty(company_name))
        {
            company_name.setError("this field is required");
            enable = false;
        }
        else
        {
            company_name.setError(null);
            params.put("company_name",company_name.getEditText().getText().toString());
        }

        if(isempty(email))
        {
            email.setError("this field is required");
            enable = false;
        }
        else
        {
            if(!isEmail())
            {
                email.setError("please enter valid email");
                enable = false;
            }
            else
            {
                email.setError(null);
                params.put("email",email.getEditText().getText().toString());
            }
        }

        if(isempty(phone_number))
        {
            phone_number.setError("this field is required");
            enable = false;
        }
        else
        {
            if(PhoneNumberUtils.isGlobalPhoneNumber(phone_number.getEditText().getText().toString()))
            {
                phone_number.setError(null);
                params.put("phone_number",phone_number.getEditText().getText().toString());

            }
            else
            {
                enable = false;
                phone_number.setError("please enter a valid phone number");
            }
        }

        if(isempty(password))
        {
            password.setError("this field is required");
            enable = false;
        }
        else
        {
            password.setError(null);
            params.put("password",password.getEditText().getText().toString());
        }
        return enable;
    }

    public boolean isempty(TextInputLayout text)
    {
        if(text.getEditText().getText().toString().isEmpty())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isEmail()
    {
        if(Patterns.EMAIL_ADDRESS.matcher(email.getEditText().getText().toString()).matches())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.create_account:
                if(check_validate())
                {
                    params.put("role",role);
                    signup();
                }
        }
    }

    Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            boolean success = bundle.getBoolean("success");
            try {
                if(success)
                {
                    String body = bundle.getString("body");
                    JSONObject object = new JSONObject(body);
                    boolean registersuccess = object.getBoolean("success");
                    if(!registersuccess)
                    {
                        AlertDialog.Builder alert=new AlertDialog.Builder(SignUpActivity.this);
                        alert.setMessage(object.getString("message")).setNegativeButton("RETRY", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                    }
                    else
                    {
                        AlertDialog alert = new AlertDialog.Builder(SignUpActivity.this).create();
                        alert.setTitle("Success");
                        alert.setMessage("You have Successfully been Registered");
                        alert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(SignUpActivity.this,LoginActivity.class);
                                intent.putExtra("com.example.tsheetapp.role",role);
                                startActivity(intent);
                                overridePendingTransition(R.anim.bottom,R.anim.right_out);
                                dialog.dismiss();
                            }
                        });
                        alert.show();
                    }
                }
                else
                {
                    AlertDialog.Builder alert=new AlertDialog.Builder(SignUpActivity.this);
                    alert.setMessage("register failed").setNegativeButton("RETRY",null).create().show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public void signup()
    {
        String url = API.url + "/auth/signup";
        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder request_body = new MultipartBody.Builder().setType(MultipartBody.FORM);
        request_body.addFormDataPart("username",params.get("username"));
        request_body.addFormDataPart("company_name",params.get("company_name"));
        request_body.addFormDataPart("email",params.get("email"));
        request_body.addFormDataPart("password",params.get("password"));
        request_body.addFormDataPart("phone_number",params.get("phone_number"));
        request_body.addFormDataPart("role",role);
        RequestBody body = request_body.build();
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                Log.d("uploadresponse",body);
                Message msg = mhandler.obtainMessage();
                Bundle bundle = new Bundle();
                if(response.isSuccessful())
                {
                    bundle.putBoolean("success",true);
                    bundle.putString("body",body);
                    msg.setData(bundle);
                    mhandler.sendMessage(msg);
                }
                else
                {
                    bundle.putBoolean("success",false);
                    msg.setData(bundle);
                    mhandler.sendMessage(msg);
                }
            }
        });
    }
}

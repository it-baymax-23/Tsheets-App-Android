package com.example.tsheetapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    String role;
    int step = 1;
    TextInputLayout editText;
    TextInputLayout password;
    Button button_continue;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        Intent intent = getIntent();
        role = intent.getStringExtra("com.example.tsheetapp.role");

        TextView text = (TextView)findViewById(R.id.create_account);
        if(role.contentEquals("admin"))
        {
            text.setText(R.string.create_company);
        }
        else
        {
            text.setText(R.string.know_account);
        }

        preferences = getSharedPreferences("credential",MODE_PRIVATE);

        editText = (TextInputLayout) findViewById(R.id.userid);
        editText.getEditText().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(editText.getEditText().getCompoundDrawables()[2] != null)
                {
                    if(event.getRawX() >= (editText.getEditText().getRight() - editText.getEditText().getCompoundDrawables()[2].getBounds().width())) {
                        // your action here
                        step = 1;
                        password.setVisibility(View.GONE);
                        button_continue.setText(R.string.continue_str);
                        Drawable img_left = editText.getEditText().getCompoundDrawables()[0];
                        editText.getEditText().setCompoundDrawables(img_left,null,null,null);
                        return true;
                    }
                }
                return false;
            }
        });
        password = findViewById(R.id.password);
        button_continue = (Button)findViewById(R.id.button_continue);
        button_continue.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.create_account:
                if(role.contentEquals("admin"))
                {
                    Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                    intent.putExtra("role",role);
                    overridePendingTransition(R.anim.bottom,R.anim.right_out);
                    startActivity(intent);
                }
                break;
            case R.id.button_continue:
                boolean enable = true;

                if(editText.getEditText().getText().toString().isEmpty()) {
                    editText.setError("this field is required");
                    enable = false;
                }
                else
                {
                    editText.setError(null);
                }

                if(step == 2)
                {
                    if(password.getEditText().getText().toString().isEmpty())
                    {
                        password.setError("this field is required");
                        enable = false;
                    }
                    else
                    {
                        password.setError(null);
                    }
                }

                if(enable)
                {
                    signin();
                }
                break;
        }
    }

    public void signin()
    {
        String url = API.url + "/auth/signin_username";
        if(step != 1)
        {
            url = API.url + "/auth/signin";
        }

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("loginresponse",response);
               try {
                    JSONObject object = new JSONObject(response);
                    boolean success = object.getBoolean("success");
                    if(step == 1)
                    {
                        if(success)
                        {
                            editText.setError(null);
                            password.setError(null);
                            step = 2;
                            password.setVisibility(View.VISIBLE);
                            Drawable img = getApplicationContext().getResources().getDrawable(R.drawable.close_btn);

                            img.setBounds(0,0,32,32);
                            Drawable img_left = editText.getEditText().getCompoundDrawables()[0];
                            editText.getEditText().setCompoundDrawables(img_left,null,img,null);
                            button_continue.setText(R.string.signin);
                        }
                        else
                        {
                            editText.setError(getApplicationContext().getResources().getString(R.string.userid_email_incorrect));
                        }
                    }
                    else if(step == 2)
                    {
                        if(success)
                        {
                            editText.setError(null);
                            password.setError(null);
                            String user_id = object.getString("user_id");
                            String username = object.getString("username");
                            String useremail = object.getString("useremail");
                            String profile = object.getString("profile");
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("user_id",user_id);
                            editor.putString("role",role);
                            editor.putString("user",username);
                            editor.putString("useremail",useremail);
                            editor.putString("profile",profile);
                            editor.commit();
                            Intent intent = new Intent(getApplicationContext(),TimeCardActivity.class);
                            overridePendingTransition(R.anim.right_in,R.anim.right_out);
                            startActivity(intent);
                        }
                        else
                        {
                            String type = object.getString("type");
                            if(type.contentEquals("username"))
                            {
                                editText.setError(getApplicationContext().getResources().getString(R.string.userid_email_incorrect));
                                password.setError(null);
                            }
                            else if(type.contentEquals("password"))
                            {
                                editText.setError(null);
                                password.setError(getApplicationContext().getResources().getString(R.string.password_incorrect));
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener= new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
            }
        };

        Map<String,String> param = new HashMap<String,String>();
        param.put("user",editText.getEditText().getText().toString());
        param.put("role",role);
        if(step == 2)
        {
            param.put("password",password.getEditText().getText().toString());
        }

        PostRequest request = new PostRequest(Request.Method.POST,url,listener,errorListener,param);
        queue.add(request);
    }
}

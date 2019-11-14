package com.example.tsheetapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Profile extends AppCompatActivity implements View.OnClickListener{
    String username = "";
    String user_id;
    String userprofile = "";
    SharedPreferences preferences;
    String useremail = "";
    String role;
    CustomDialog dialog;
    CircleImageView userimage;
    public  static  int REQUEST_TAKE_PHOTO = 0;
    public  static  int REQUEST_PICK_PICTURE = 1;
    private Uri photoURI;
    File mPhotoFile;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        init();
    }

    public void init()
    {
        preferences = getSharedPreferences("credential",MODE_PRIVATE);
        user_id = preferences.getString("user_id","0");
        username = preferences.getString("user","");
        useremail = preferences.getString("useremail","");
        userprofile = preferences.getString("profile","");
        role = preferences.getString("role","");
        TextView account_username = (TextView)findViewById(R.id.account_username);
        account_username.setText(username);
        TextView account_useremail = (TextView)findViewById(R.id.account_email);
        account_useremail.setText(useremail);
        TextView usernametxt = (TextView)findViewById(R.id.username);
        TextView tsheetweb = (TextView)findViewById(R.id.tsheetsweb);
        tsheetweb.setText(API.url);
        TextView username_logo = (TextView)findViewById(R.id.username_logo);
        username_logo.setText(username.substring(0,1).toUpperCase() + username.substring(1));
        TextView role_txt = (TextView)findViewById(R.id.role);
        role_txt.setText(role.substring(0,1).toUpperCase() + role.substring(1));
        userimage = (CircleImageView)findViewById(R.id.image_user);
        usernametxt.setText(username.toUpperCase().substring(0,1));
        usernametxt.setBackgroundResource(R.drawable.circle);
        userimage.setVisibility(View.GONE);
        if(!userprofile.isEmpty())
        {
           new DownloadImageEmployee(userimage,usernametxt).execute(API.url + userprofile);
        }
    }

    private void get_photo_from_gallery()
    {
        Intent gallery = new Intent(Intent.ACTION_PICK);
        gallery.setType("image/*");
        startActivityForResult(gallery,REQUEST_PICK_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode < 3) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                try{

                }catch (Exception e){
                    Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                }
            }
            else if(requestCode == REQUEST_PICK_PICTURE){
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FileOutputStream fileOutputStream = null;
                try {
                    InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                    fileOutputStream = new FileOutputStream(photoFile);
                    copyStream(inputStream, fileOutputStream);
                    fileOutputStream.close();
                    inputStream.close();
                    mPhotoFile = photoFile;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(mPhotoFile.exists())
            {
                upload(mPhotoFile);
            }
        }
    }

    Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            if(bundle.getBoolean("success"))
            {
                SharedPreferences.Editor editor = preferences.edit();
                if(!bundle.getString("profile").isEmpty())
                {
                    editor.putString("profile",bundle.getString("profile"));
                    TextView usernametxt = (TextView)findViewById(R.id.username);
                    editor.commit();
                    new DownloadImageEmployee(userimage,usernametxt).execute(API.url + userprofile);
                }

            }
        }
    };

    public void upload(File file)
    {
        MultipartBody.Builder request_body = new MultipartBody.Builder().setType(MultipartBody.FORM);
        String content_type = getMimeType(file.getPath());
        RequestBody file_body = RequestBody.create(MediaType.parse(content_type),file);
        request_body.addFormDataPart("image",file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/")),file_body);
        request_body.addFormDataPart("user_id",user_id);

        RequestBody body = request_body.build();
        Request request= new Request.Builder().url(API.url + "/auth/uploadprofile").post(body).build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful())
                {
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        Message msg = mhandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("success",true);
                        bundle.putString("profile",object.getString("profile"));
                        msg.setData(bundle);
                        mhandler.sendMessage(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    public String getMimeType(String path)
    {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Toast.makeText(this, BuildConfig.APPLICATION_ID + ".provider", Toast.LENGTH_LONG).show();
                photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);

                mPhotoFile = photoFile;

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String mFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Toast.makeText(this, Environment.DIRECTORY_PICTURES, Toast.LENGTH_LONG).show();
        File mFile = File.createTempFile(mFileName, ".jpg", storageDir);
        return mFile;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.signout:
                LogoutDialog dialog_logout = new LogoutDialog(this);
                dialog_logout.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog_logout.show();
                break;
            case R.id.prev:
                finish();
                break;
            case R.id.changepassword:
                Intent intent = new Intent(this,ChangePassword.class);
                startActivity(intent);
                break;
            case R.id.username:
            case R.id.image_user:
                dialog = new CustomDialog(this);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();
                Button button_take_photo = (Button) dialog.findViewById(R.id.take_photo);
                button_take_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dispatchTakePictureIntent();
                        dialog.dismiss();
                    }
                });

                Button select_photo = (Button) dialog.findViewById(R.id.select_photo);
                select_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        get_photo_from_gallery();
                        dialog.dismiss();
                    }
                });

                Button cancel = (Button) dialog.findViewById(R.id.cancel);
                cancel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

        }
    }
}

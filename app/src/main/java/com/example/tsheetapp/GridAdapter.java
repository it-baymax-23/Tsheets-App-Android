package com.example.tsheetapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileInputStream;

public class GridAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    String[] images;
    public GridAdapter(Context applicationContext, String[] files) {
        this.context = applicationContext;
        this.images  = files;
        inflater = (LayoutInflater.from(applicationContext));
    }
    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.imageadapter,null);
        ImageView img = (ImageView)convertView.findViewById(R.id.image);
        try{
            String imagefile = images[position];
            File newfile = new File(imagefile);

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds=true;
            Bitmap b = BitmapFactory.decodeFile(imagefile);
            if(b.getWidth() > 320 || b.getHeight() > 320)
            {
                Bitmap out = Bitmap.createScaledBitmap(b,320,320,false);
                img.setImageBitmap(out);
            }
            else
            {
                img.setImageBitmap(b);
            }
        }
        catch(Exception e)
        {
            Log.i("newfileexception",e.getMessage());
            img.setImageResource(android.R.drawable.picture_frame);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) context.getResources().getDimension(R.dimen.attachment_width));
        convertView.setLayoutParams(params);
        return convertView;
    }
}

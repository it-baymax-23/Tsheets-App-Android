package com.example.tsheetapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

public class DeleteDialog extends Dialog implements View.OnClickListener {
    int resid;

    public DeleteDialog(@NonNull Context context,int layout) {
        super(context);
        resid = layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(resid);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.cancel:
                this.dismiss();
                break;
        }
    }
}

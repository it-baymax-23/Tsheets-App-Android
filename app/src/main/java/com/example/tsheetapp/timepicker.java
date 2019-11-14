package com.example.tsheetapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class timepicker extends Dialog {
    Context c;
    NumberPicker hour;
    NumberPicker min;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timepicker);

        init();
    }

    public timepicker(@NonNull Context context) {
        super(context);
    }

    public void init()
    {
        hour = (NumberPicker)findViewById(R.id.hours);
        min = (NumberPicker)findViewById(R.id.min);
        ArrayList<String> houradapter = new ArrayList<String>();
        ArrayList<String> minadapter = new ArrayList<String>();
        NumberFormat f = new DecimalFormat("00");

        for(int i = 0;i<24;i++)
        {
            houradapter.add(f.format(i));
        }

        for(int i = 0;i<60;i++)
        {
            minadapter.add(f.format(i));
        }

        hour.setMinValue(0);
        hour.setMaxValue(23);

        min.setMinValue(0);
        min.setMaxValue(59);
        hour.setDisplayedValues(houradapter.toArray(new String[houradapter.size()]));
        min.setDisplayedValues(minadapter.toArray(new String[minadapter.size()]));
    }

    public int gethour()
    {
        return hour.getValue();
    }

    public int getmin()
    {
        return min.getValue();
    }

    public void sethour(int hourvalue)
    {
        hour.setValue(hourvalue);
    }

    public void setmin(int minvalue)
    {
        min.setValue(minvalue);
    }
}

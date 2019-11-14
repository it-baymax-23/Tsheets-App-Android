package com.example.tsheetapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Jaydipsinh Zala on 1/6/16.
 */
public class Datetimepickerapp extends Dialog {

    private NumberPicker np;
    private android.widget.TimePicker tp;
    private TextView txtSelDate;
    private Calendar selDate;
    ArrayList<Calendar> arrActualDates = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat("E MMM, dd", Locale.getDefault());
    Context c;
    public Datetimepickerapp(@NonNull Context context) {
        super(context);
        c = context;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datetimepicker);
        selDate = Calendar.getInstance();
        initUI();
        updateUI(selDate);
    }

    private void initUI() {
        np = (NumberPicker) this.findViewById(R.id.np);
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Calendar date = arrActualDates.get(picker.getValue());
               Log.d("Actual Dates",sdf.format(date.getTimeInMillis()));
                selDate.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
                selDate.set(Calendar.MONTH, date.get(Calendar.MONTH));
                selDate.set(Calendar.YEAR, date.get(Calendar.YEAR));
                Log.d("Calendar",String.valueOf(picker.getValue()));
                String formatedDate = sdf.format(selDate.getTimeInMillis());
                //txtSelDate.setText(formatedDate);
                updateUI(selDate);
            }
        });
        np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        tp = (android.widget.TimePicker) this.findViewById(R.id.tp);
        tp.setIs24HourView(DateFormat.is24HourFormat(c));
        tp.setOnTimeChangedListener(new android.widget.TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(android.widget.TimePicker view, int hourOfDay, int minute) {
                selDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selDate.set(Calendar.MINUTE, minute);
                String formatedDate = sdf.format(selDate.getTimeInMillis());
                //txtSelDate.setText(formatedDate);
            }
        });
        tp.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

    }

    private void updateUI(Calendar date) {
        Calendar app = (Calendar) date.clone();
        ArrayList<String> arrDates = new ArrayList<>();
        arrActualDates.clear();
        app.set(Calendar.DATE,app.get(Calendar.DATE) - 3);
        for (int i = 0; i < 7; i++) {
            arrDates.add(sdf.format(app.getTimeInMillis()));
            Log.d("DATE",sdf.format(app.getTimeInMillis()));
            arrActualDates.add((Calendar) app.clone());
            app.set(Calendar.DATE, app.get(Calendar.DATE) + 1);
        }
        arrDates.add(sdf.format(app.getTimeInMillis()));
        arrActualDates.add((Calendar)app.clone());
        np.setMinValue(0);
        np.setMaxValue(arrDates.size() - 1);
        np.setWrapSelectorWheel(false);
        String[] stringArray = arrDates.toArray(new String[arrDates.size()]);
        np.setDisplayedValues(stringArray);
        np.setValue(3);
    }

    public Calendar getDate()
    {
        return selDate;
    }

    public void setDate(Calendar date)
    {
        selDate = (Calendar) date.clone();
        updateUI(selDate);
    }
}
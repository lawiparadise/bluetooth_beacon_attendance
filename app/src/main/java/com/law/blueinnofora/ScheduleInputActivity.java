package com.law.blueinnofora;


import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;

import com.law.blueinnofora.MY.FirstStartApp;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ScheduleInputActivity extends Activity {
    public static final String TAG = "ScheduleInputActivity";
    FirstStartApp app;

    //  EditText messageInput;
    Button subject;
    Button student;
    Button timeButton;

    ImageView weather01Button;
    ImageView weather02Button;
    ImageView weather03Button;
    ImageView weather04Button;

    int selectedWeather = 0;

    public static final int DIALOG_TIME = 1101;

    public static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    Date selectedDate;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_input);
        app=(FirstStartApp)getApplicationContext();
        setTitle("Attendance Check");

        //    messageInput = (EditText) findViewById(R.id.messageInput);
        subject=(Button)findViewById(R.id.subject);
        subject.setText("Circuit");
        student=(Button)findViewById(R.id.student);
        //     student.setText("HYUN HONG");
        student.setText(app.getSTUDENTNAME());


        timeButton = (Button) findViewById(R.id.timeButton);
        timeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_TIME);
            }
        });

        weather01Button = (ImageView) findViewById(R.id.weather01Button);
        weather01Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                selectWeatherButton(0);
            }
        });

        weather02Button = (ImageView) findViewById(R.id.weather02Button);
        weather02Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                selectWeatherButton(1);
            }
        });

        weather03Button = (ImageView) findViewById(R.id.weather03Button);
        weather03Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                selectWeatherButton(2);
            }
        });

        weather04Button = (ImageView) findViewById(R.id.weather04Button);
        weather04Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                selectWeatherButton(3);
            }
        });

        Button saveButton = (Button) findViewById(R.id.saveButton); //이 버튼으로 저장한다.
        saveButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // String messageStr = messageInput.getText().toString();
                String messageStr = subject.getText().toString();
                String timeStr = timeButton.getText().toString();

                Intent intent = new Intent();
                intent.putExtra("time", timeStr);
                intent.putExtra("message", messageStr);
                intent.putExtra("weather", selectedWeather);

                setResult(RESULT_OK, intent); //CalendarMonthViewActivity로 결과값을 보낸다.

                finish();
            }
        });

        Button closeButton = (Button) findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });


        // set selected date using current date
        Date curDate = new Date();
        setSelectedDate(curDate);

        // process the passed intent
        Intent intent = getIntent();
        String weatherIconUrl = intent.getStringExtra("weatherIconUrl");
        if (weatherIconUrl != null) {
            File iconFile = new File(weatherIconUrl);
            String iconFileName = iconFile.getName();
            Log.d(TAG, "weather icon file name : " + iconFileName);

            if (iconFileName != null) {
                if (iconFileName.equals("sunny.gif")) {
                    selectWeatherButton(0);
                } else if (iconFileName.equals("cloudy.gif")) {
                    selectWeatherButton(1);
                } else if (iconFileName.equals("rain.gif")) {
                    selectWeatherButton(2);
                } else if (iconFileName.equals("snow.gif")) {
                    selectWeatherButton(3);
                } else {
                    Log.d(TAG, "weather icon is not found.");
                    selectWeatherButton(0);
                }
            } else {
                selectWeatherButton(0);
            }
        } else {
            selectWeatherButton(0);
        }

    }

    private void selectWeatherButton(int index) {
        selectedWeather = index;

        weather01Button.setBackgroundColor(Color.WHITE);
        weather02Button.setBackgroundColor(Color.WHITE);
        weather03Button.setBackgroundColor(Color.WHITE);
        weather04Button.setBackgroundColor(Color.WHITE);

        if (selectedWeather == 0) {
            weather01Button.setBackgroundColor(Color.RED);
        } else if (selectedWeather == 1) {
            weather02Button.setBackgroundColor(Color.RED);
        } else if (selectedWeather == 2) {
            weather03Button.setBackgroundColor(Color.RED);
        } else if (selectedWeather == 3) {
            weather04Button.setBackgroundColor(Color.RED);
        }
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_TIME:
                String timeStr = timeButton.getText().toString();

                Calendar calendar = Calendar.getInstance();
                Date curDate = new Date();
                try {
                    curDate = timeFormat.parse(timeStr);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }

                calendar.setTime(curDate);

                int curHour = calendar.get(Calendar.HOUR_OF_DAY);
                int curMinute = calendar.get(Calendar.MINUTE);

                return new TimePickerDialog(this,  timeSetListener,  curHour, curMinute, false);
            default:
                break;

        }

        return null;
    }

    private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedCalendar.set(Calendar.MINUTE, minute);

            Date curDate = selectedCalendar.getTime();
            setSelectedDate(curDate);
        }
    };

    private void setSelectedDate(Date curDate) {
        selectedDate = curDate;

        String selectedTimeStr = timeFormat.format(curDate);
        timeButton.setText(selectedTimeStr);
    }

}
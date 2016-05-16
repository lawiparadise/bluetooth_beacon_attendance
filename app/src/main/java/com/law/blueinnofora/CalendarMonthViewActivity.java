package com.law.blueinnofora;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.law.blueinnofora.MY.FirstStartApp;
import com.law.blueinnofora.service.BeaconService;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
import com.google.android.gms.gcm.GoogleCloudMessaging;



public class CalendarMonthViewActivity extends Activity {
    public static final String TAG = "CalendarMonthViewActivity";
    FirstStartApp app;

    BeaconService beaconservice = new BeaconService();

    private Button RESCANBUTTON;

    public static boolean ONOFF=false;
    GridView monthView;
    CalendarMonthAdapter monthViewAdapter;

    TextView monthText;

    int curYear;
    int curMonth;

    int curPosition;
    EditText scheduleInput;
    Button saveButton;

    ListView scheduleList;
    ScheduleListAdapter scheduleAdapter;
    ArrayList outScheduleList;

    public static final int REQUEST_CODE_SCHEDULE_INPUT = 1001;
    public static final int WEATHER_PROGRESS_DIALOG = 1002;
    public static final int WEATHER_SAVED_DIALOG = 1003;

    public static final int REQUEST_CODE_STUDENT_ID = 3;

    private static final String BASE_URL = "http://www.google.com";
    private static String WEATHER_URL = "http://www.google.com/ig/api?weather=";

    private static boolean weatherCanceled;

    WeatherCurrentCondition weather = null;

    Handler handler = new Handler();

    ScheduleDatabase database;

    ////////////////////////////////////////////push
    Sender sender;
    ArrayList<String> idList = new ArrayList<String>();
//    EditText messageInput;
    private Random random ;
    private int TTLTime = 60;
    private	int RETRY = 3;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        app = (FirstStartApp)getApplicationContext();
        ((FirstStartApp)getApplicationContext()).calendarMonthViewActivity=this;

/*         RESCANBUTTON=(Button)findViewById(R.id.RESCANBUTTON);
        RESCANBUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                beaconservice.setRESCAN(true);
                finish();
            }
        });*/


        sender = new Sender(GCMInfo.GOOGLE_API_KEY);


        try {
            // 단말 등록하고 등록 ID 받기
            registerDevice();

        } catch(Exception ex) {
            ex.printStackTrace();
        }


        /////////////////////////////////////////////////////



        monthView = (GridView) findViewById(R.id.monthView);
        monthViewAdapter = new CalendarMonthAdapter(this);
        monthView.setAdapter(monthViewAdapter);

        // set listener

        monthView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MonthItem curItem = (MonthItem) monthViewAdapter.getItem(position); //내가 선택한 날짜에 관한 내용
                int day = curItem.getDay();

                //Toast.makeText(getApplicationContext(), day + "일이 선택되었습니다.", 1000).show();

                monthViewAdapter.setSelectedPosition(position); //내가 선택한 날짜를 세팅해줌
                monthViewAdapter.notifyDataSetChanged();

                outScheduleList = monthViewAdapter.getSchedule(position);
                if (outScheduleList == null) {
                    outScheduleList = new ArrayList<ScheduleListItem>();
                }
                scheduleAdapter.scheduleList = outScheduleList; //이거에서 문제발생

                scheduleAdapter.notifyDataSetChanged();

                // show ScheduleInputActivity if the position is already selected
                if (position == curPosition) { //두번 클릭했을 때 실행되는 코드
                    //showScheduleInput();
                }

                // set schedule to the TextView
                curPosition = position;

            }
        });

        monthText = (TextView) findViewById(R.id.monthText);
        setMonthText();

        Button monthPrevious = (Button) findViewById(R.id.monthPrevious);
        monthPrevious.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                monthViewAdapter.setPreviousMonth();
                monthViewAdapter.notifyDataSetChanged();

                setMonthText();
            }
        });

        Button monthNext = (Button) findViewById(R.id.monthNext);
        monthNext.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                monthViewAdapter.setNextMonth();
                monthViewAdapter.notifyDataSetChanged();

                setMonthText();
            }
        });


        curPosition = -1;

        scheduleList = (ListView)findViewById(R.id.scheduleList);
        scheduleAdapter = new ScheduleListAdapter(this);
        scheduleList.setAdapter(scheduleAdapter);


        // open database
        if (database != null) {
            database.close();
            database = null;
        }

        database = ScheduleDatabase.getInstance(this);
        boolean isOpen = database.open();
        if (isOpen) {
            Log.d(TAG, "Schedule database is open.");
        } else {
            Log.d(TAG, "Schedule database is not open.");
        }

        // load all items from the database
        loadAllItems();

    }

    private void registerDevice() {
     //   Toast.makeText(this,"hi", Toast.LENGTH_SHORT).show();
        RegisterThread registerObj = new RegisterThread();
        registerObj.start();

    }

    class RegisterThread extends Thread {
        public void run() {

            try {
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                String regId = gcm.register(GCMInfo.PROJECT_ID);
            /*    println("푸시 서비스를 위해 단말을 등록했습니다.");
                println("registeroO0l ID : " + regId);*/

                // 등록 ID 리스트에 추가 (현재는 1개만)
                idList.clear();
                idList.add(regId);
            //    idList.add("APA91bH9ol0-NGDeZv4OObcyFb9n4rk0nlbLQ_5WPF4qX_Yzc6Qng-RkU_6zEfCVnoCaQ6yQjx8OZxO-Tp_Y9T79hSRm9GpCOFzuzYdC27Pi2uJpXcb1zBlh3wz_uA-i8TmKrhMs_PoGLKA-eMVocoN6om-KNGuvGA");
             //   idList.add("APA91bESOI9fkEOnqLZr2sLGlxR3ooWOYtED7sq6hcKZ5_dqtU8X5inp9CkWs5Prdm6DH8Us9hTcizYOwOCpQr0bXnn6sBLMdixvRt5EcJChNh0hNEU6qPPEHvfTMPRiWiVodsUtDVfT");
                idList.add("APA91bE9rmF-3aCzbgPFiJGh746ganpc9H6OvPI1K2jB9u27A3VZhKKifHeGkh6oRaDTNBCtIKsilhvwWOAHetExLhonEJhhQohwIm1sEqHUSdmA9zOi7PZihxKPKGPzhSBL9WNSqy1YbO6BhKKMU1CwYIIV7vyLjw");
                Log.e(TAG,"MY : register thread check");
            } catch(Exception ex) {
                ex.printStackTrace();
            }

        }
    }
    private void sendToDevice(String data) {

        SendThread thread = new SendThread(data);
        thread.start();

    }

    class SendThread extends Thread {
        String data;

        public SendThread(String inData) {
            data = inData;
        }

        public void run() {

            try {
                sendText(data);
            } catch(Exception ex) {
                ex.printStackTrace();
            }

        }

        public void sendText(String msg)
                throws Exception
        {

            if( random == null){
                random = new Random(System.currentTimeMillis());
            }

            String messageCollapseKey = String.valueOf(Math.abs(random.nextInt()));

            try {
                // 푸시 메시지 전송을 위한 메시지 객체 생성 및 환경 설정
                Message.Builder gcmMessageBuilder = new Message.Builder();
                gcmMessageBuilder.collapseKey(messageCollapseKey).delayWhileIdle(true).timeToLive(TTLTime);
                gcmMessageBuilder.addData("type","text");
                gcmMessageBuilder.addData("command", "show");
                gcmMessageBuilder.addData("data", URLEncoder.encode(data, "UTF-8"));

                Message gcmMessage = gcmMessageBuilder.build();

                // 여러 단말에 메시지 전송 후 결과 확인
                MulticastResult resultMessage = sender.send(gcmMessage, idList, RETRY);
                String output = "GCM right전송 메시지 결과 => " + resultMessage.getMulticastId()
                        + "," + resultMessage.getRetryMulticastIds() + "," + resultMessage.getSuccess();

           //     println(output);

            } catch(Exception ex) {
                ex.printStackTrace();

                String output = "GCM 메시지 전송 과정에서 에러 발생 : " + ex.toString();
           //     println(output);

            }

        }
    }



    @Override
    public void onStart(){
        super.onStart();

            saveAuto();

    }

    public void onScanClicked(View v){
        Toast.makeText(this,"Scanning...",Toast.LENGTH_SHORT).show();
        beaconservice.setRESCAN(true);

    }

    @Override
    protected void onNewIntent(Intent intent){
        Log.e(TAG,"MY : onNewIntent!!");
        saveAuto();

        super.onNewIntent(intent);
    }




    public void saveAuto(){
        if(ONOFF) {
            ONOFF=false;

            MonthItem curItem = (MonthItem) monthViewAdapter.getItem(monthViewAdapter.getTodayPosition());
            int day = curItem.getDay();

            monthViewAdapter.setSelectedPosition(monthViewAdapter.getTodayPosition()); //내가 선택한 날짜를 세팅해줌
            monthViewAdapter.notifyDataSetChanged();
            outScheduleList = monthViewAdapter.getSchedule(monthViewAdapter.getTodayPosition());
            if (outScheduleList == null) {
                outScheduleList = new ArrayList<ScheduleListItem>();
            }
            scheduleAdapter.scheduleList = outScheduleList; //이게 문제다. 근데 또 잘되네
            scheduleAdapter.notifyDataSetChanged();

            showScheduleInput();
           // finish();

        }
    }


    private void loadAllItems() {
        // load all weather items
        String SQL = "select _id, WCONDITION, WICON, WYEAR, WMONTH, WPOSITION from " + ScheduleDatabase.TABLE_WEATHER_INFO;
        Cursor cursor = database.rawQuery(SQL);

        int weatherCount = cursor.getCount();
        Log.d(TAG, "count of all weather items : " + weatherCount);
        if (weatherCount < 1) {
            return;
        }

        int curIndex = 0;
        monthViewAdapter.clearWeatherHash();
        while(cursor.moveToNext()) {
            int _id = cursor.getInt(0);
            String wCondition = cursor.getString(1);
            String wIconUrl = cursor.getString(2);
            int wYear = cursor.getInt(3);
            int wMonth = cursor.getInt(4);
            int wPosition = cursor.getInt(5);

            Log.d(TAG, "_id : " + _id + ", " + wCondition + ", " + wIconUrl + ", " + wYear + ", " + wMonth + ", " + wPosition);

            // make a WeatherCurrentCondition instance
            WeatherCurrentCondition aWeather = new WeatherCurrentCondition();
            aWeather.setCondition(wCondition);
            aWeather.setIconURL(wIconUrl);

            monthViewAdapter.putWeather(wYear, wMonth, wPosition, aWeather);

            curIndex++;
        }

        cursor.close();

        // load all schedule items
        SQL = "select _id, STIME, SMESSAGE, SYEAR, SMONTH, SPOSITION from " + ScheduleDatabase.TABLE_SCHEDULE_INFO;
        cursor = database.rawQuery(SQL);

        int scheduleCount = cursor.getCount();
        Log.d(TAG, "count of all schedule items : " + scheduleCount);
        if (scheduleCount < 1) {
            return;
        }

        curIndex = 0;
        monthViewAdapter.clearScheduleHash();
        if (outScheduleList == null) {
            outScheduleList = new ArrayList();
        } else {
            outScheduleList.clear();
        }

        while(cursor.moveToNext()) {
            int _id = cursor.getInt(0);
            String sTime = cursor.getString(1);
            String sMessage = cursor.getString(2);
            int sYear = cursor.getInt(3);
            int sMonth = cursor.getInt(4);
            int sPosition = cursor.getInt(5);

            Log.d(TAG, "_id : " + _id + ", " + sTime + ", " + sMessage + ", " + sYear + ", " + sMonth + ", " + sPosition);

            // make a ScheduleListItem instance
            ScheduleListItem aSchedule = new ScheduleListItem();
            aSchedule.setTime(sTime);
            aSchedule.setMessage(sMessage);

            ArrayList<ScheduleListItem> curScheduleList = monthViewAdapter.getSchedule(sYear, sMonth, sPosition);
            if (curScheduleList == null) {
                curScheduleList = new ArrayList<ScheduleListItem>();
            }
            curScheduleList.add(aSchedule);
            monthViewAdapter.putSchedule(sYear, sMonth, sPosition, curScheduleList);


            curIndex++;
        }

        cursor.close();


    }


    private void setMonthText() {
        curYear = monthViewAdapter.getCurYear();
        curMonth = monthViewAdapter.getCurMonth();

        monthText.setText(curYear + "Y " + (curMonth+1) + "M");
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        addOptionMenuItems(menu);

        return true;
    }

    private void addOptionMenuItems(Menu menu) {
        int id = Menu.FIRST;
        menu.clear();

        menu.add(id, id, Menu.NONE, "ChangeID");

/*        id = Menu.FIRST + 1;
        menu.add(id, id, Menu.NONE, "none");*/
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST:
                //showScheduleInput();
                saveID();
                return true;
/*            case Menu.FIRST+1:
                getCurrentWeather();

                return true;*/
            default:
                break;
        }

        return false;
    }

    private void saveID(){
        Intent i = new Intent(this,StudentIDInputActivity.class);
        startActivityForResult(i,REQUEST_CODE_STUDENT_ID);

    }
    /**
     * get current weather
     */
    private void getCurrentWeather() {
        weatherCanceled = false;

        showDialog(WEATHER_PROGRESS_DIALOG);

        CurrentWeatherSaveThread thread = new CurrentWeatherSaveThread();
        thread.start();

    }


    class CurrentWeatherSaveThread extends Thread {
        public CurrentWeatherSaveThread() {

        }

        public void run() {
            try {
                SAXParserFactory parserFactory = SAXParserFactory.newInstance();
                SAXParser parser = parserFactory.newSAXParser();
                XMLReader reader = parser.getXMLReader();

                WeatherHandler whandler = new WeatherHandler();
                reader.setContentHandler(whandler);

                String queryStr = WEATHER_URL + "Seoul,Korea";
                URL urlForHttp = new URL(queryStr);

                InputStream instream = getInputStreamUsingHTTP(urlForHttp);

                if (instream != null) {
                    reader.parse(new InputSource(instream));

                    weather = whandler.getWeather();

                    handler.post(completedRunnable);
                } else {
                    removeDialog(WEATHER_PROGRESS_DIALOG);
                }

            } catch(Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    private InputStream getInputStreamUsingHTTP(URL url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);

        int resCode = conn.getResponseCode();
        Log.d(TAG, "Response Code : " + resCode);

        if (weatherCanceled) {
            return null;
        }

        InputStream instream = conn.getInputStream();
        return instream;
    }

    Runnable completedRunnable = new Runnable() {
        public void run() {
            removeDialog(WEATHER_PROGRESS_DIALOG);

            if (weatherCanceled) {
                weather = null;
            } else {
                Toast.makeText(getApplicationContext(), "Current Weather : " + weather.getCondition() + ", " + weather.getIconURL(), Toast.LENGTH_LONG).show();

                // set today weather
                int todayPosition = monthViewAdapter.getTodayPosition();
                Log.d(TAG, "today position : " + todayPosition);

                monthViewAdapter.putWeather(monthViewAdapter.todayYear, monthViewAdapter.todayMonth, todayPosition, weather);

                // save also to the database
                saveWeather(monthViewAdapter.todayYear, monthViewAdapter.todayMonth, todayPosition, weather);

                monthViewAdapter.notifyDataSetChanged();

                showDialog(WEATHER_SAVED_DIALOG);
            }
        }
    };


    private void saveWeather(int year, int month, int position, WeatherCurrentCondition weather) {
        try {
            String wCondition = weather.getCondition();
            String wIconUrl = weather.getIconURL();

            database.execSQL( "insert into " + ScheduleDatabase.TABLE_WEATHER_INFO + "(WCONDITION, WICON, WYEAR, WMONTH, WPOSITION) values ('" + wCondition + "', '" + wIconUrl + "', " + year + ", " + month + ", " + position + ");" );
        } catch(Exception ex) {
            Log.e(TAG, "Exception in executing insert SQL.", ex);
        }
    }



    private void saveSchedule(int year, int month, int position, ScheduleListItem aItem) {
        try {
            String sTime = aItem.getTime();
            String sMessage = aItem.getMessage();

            database.execSQL( "insert into " + ScheduleDatabase.TABLE_SCHEDULE_INFO + "(STIME, SMESSAGE, SYEAR, SMONTH, SPOSITION) values ('" + sTime + "', '" + sMessage + "', " + year + ", " + month + ", " + position + ");" );
        } catch(Exception ex) {
            Log.e(TAG, "Exception in executing insert SQL.", ex);
        }
    }


    protected void onDestroy() {
        // open database
        if (database != null) {
            database.close();
            database = null;
        }

        super.onDestroy();
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case WEATHER_PROGRESS_DIALOG:
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("날씨정보 가져오는 중...");
                progressDialog.setCancelable(true);
                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        weatherCanceled = true;
                    }
                });

                return progressDialog;
            case WEATHER_SAVED_DIALOG:
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setMessage("날씨정보를 저장하였습니다.");
                alertBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = alertBuilder.create();
                return alertDialog;
        }

        return null;
    }


    private void showScheduleInput() {
        Intent intent = new Intent(this, ScheduleInputActivity.class);

        int todayPosition = monthViewAdapter.getTodayPosition();
        WeatherCurrentCondition weather = monthViewAdapter.getWeather(monthViewAdapter.todayYear, monthViewAdapter.todayMonth, todayPosition);
        if (weather != null) {
            String weatherIconUrl = weather.getIconURL();
            intent.putExtra("weatherIconUrl", weatherIconUrl);
        }

        startActivityForResult(intent, REQUEST_CODE_SCHEDULE_INPUT);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE_SCHEDULE_INPUT) {
            if (intent == null) { //이거 없으면 에러뜸, 인텐트가 비어있는데 저장시키려했기때문.
                Log.e(TAG,"MY : onActivityResult");
                return;
            }

            String time = intent.getStringExtra("time");
            String message = intent.getStringExtra("message");
            int selectedWeather = intent.getIntExtra("weather", 0);
            String attend="";
            if(selectedWeather==0) attend="Attendance";
            else if(selectedWeather==1) attend="Past Due";
            else if(selectedWeather==2) attend="Absent";
            else attend=".";

            if (message != null) {
                Toast toast = Toast.makeText(getBaseContext(), "time : " + time + ", message : " + message + ", Attendance : " + attend, Toast.LENGTH_SHORT);
                toast.show();

                ScheduleListItem aItem = new ScheduleListItem(time, message);


                if (outScheduleList == null) {
                    outScheduleList = new ArrayList();
                }
                outScheduleList.add(aItem);

                monthViewAdapter.putSchedule(curPosition, outScheduleList);

                // save also to the database
                saveSchedule(monthViewAdapter.getCurYear(), monthViewAdapter.getCurMonth(), curPosition, aItem);

                scheduleAdapter.scheduleList = outScheduleList;
                scheduleAdapter.notifyDataSetChanged();

                // put weather
                WeatherCurrentCondition aWeather = new WeatherCurrentCondition();
                if (selectedWeather == 0) {
                    aWeather.setCondition("Sunny");
                    aWeather.setIconURL("/ig/images/weather/sunny.gif");
                } else if (selectedWeather == 1) {
                    aWeather.setCondition("Cloudy");
                    aWeather.setIconURL("/ig/images/weather/cloudy.gif");
                } else if (selectedWeather == 2) {
                    aWeather.setCondition("Rain");
                    aWeather.setIconURL("/ig/images/weather/rain.gif");
                } else if (selectedWeather == 3) {
                    aWeather.setCondition("Snow");
                    aWeather.setIconURL("/ig/images/weather/snow.gif");
                }

                monthViewAdapter.putWeather(curPosition, aWeather);

                // save also to the database
                saveWeather(monthViewAdapter.getCurYear(), monthViewAdapter.getCurMonth(), curPosition, aWeather);

                monthViewAdapter.notifyDataSetChanged();

                        sendToDevice(app.getID());

            }
        }
        else if(requestCode==REQUEST_CODE_STUDENT_ID){
            if (intent == null) { //이거 없으면 에러뜸, 인텐트가 비어있는데 저장시키려했기때문.
                Log.e(TAG,"MY : null intent");
                return;
            }
            String name = intent.getStringExtra("name");
            String id= intent.getStringExtra("id");
            String major=intent.getStringExtra("major");

            Toast.makeText(this,"name:"+name+" id:"+id+" major:"+major,Toast.LENGTH_SHORT).show();

            app.setSTUDENTNAME(name);
            app.setID(id);
            app.setMAJOR(major);

        }

    }





}
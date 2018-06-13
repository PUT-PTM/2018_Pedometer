package com.example.karol.pedometer;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //statistics
    final int MSG_START_TIMER = 0;
    final int MSG_STOP_TIMER = 1;
    final int MSG_UPDATE_TIMER = 2;
    final int MSG_RESUME_TIMER = 3;
    final int MSG_PAUSE_TIMER = 4;
    Statistics statistics = new Statistics();
    final int REFRESH_RATE = 10;

    TextView stepsText,textStatus,timerText,velocityText,metersText,caloriesText, velocityCurrentText, modeText;
    Button btnStart,btnStop,btnResume,btnPause,btnReconnect;
    LinearLayout controlLayout;
    GraphView graph;
    ImageView speedometer,mode;

    private boolean isBtConnected = false;
    private boolean isReconnecting = false;

    //Saving data between views
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    Intent myIntent;
    MyMainReceiver myMainReceiver;

    AppDatabase mDb;
    HistoryDao mHistoryDao;

    boolean doubleBackToExitPressedOnce = false;
    boolean exitingApp = true;


    //for testing
    Button minPlus,minMinus,maxMinus,maxPlus,precisionPlus,precisionMinus;
    TextView textMin,textMax,textPrecision,stepsText2;

    int min=15,max=80,precision = 1500,stepIndicator = 0;

    BluetoothService bluetoothService;
    boolean isBound = false;

    public MainActivity() {
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.MyBinder binder = (BluetoothService.MyBinder) service;
            bluetoothService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothService = null;
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        myMainReceiver = new MainActivity.MyMainReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.MODE_RUNNING);
        intentFilter.addAction(BluetoothService.MODE_STANDING);
        intentFilter.addAction(BluetoothService.MODE_WALKING);
        intentFilter.addAction(BluetoothService.CONNECTION_LOST);
        intentFilter.addAction(BluetoothService.CONNECTION_RECONNECTED);
        intentFilter.addAction(BluetoothService.CONNECTION_RECONNECTION_FAILED);
        registerReceiver(myMainReceiver, intentFilter);

        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        //placeholders for test
        Intent i = getIntent();

        // statistics.setStepWidth(intent.getIntExtra("HEIGHT,170"),'f');
        statistics.setWeight(55);

        findViews();

        Bundle extras = i.getExtras();
        if (extras != null) {
            if(extras.containsKey("RADIO")){
                statistics.setWeight(extras.getDouble("WEIGHT",0));
                statistics.setStepWidth(extras.getDouble("HEIGHT",0),extras.getChar("RADIO",'f'));
            }
        }

        mDb = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "PedometerHistory").allowMainThreadQueries().build();

        mHistoryDao = mDb.historyDao();// Get DAO object

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater(); //get menu
        inflater.inflate(R.menu.menu, menu); //put custom menu in it
        return true;
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START_TIMER:
                    statistics.start(); //start showing statistics
                  //  stepsText2.setText(String.valueOf("0"));
                    stepIndicator = 0;
                    mHandler.sendEmptyMessage(MSG_UPDATE_TIMER);
                    break;

                case MSG_UPDATE_TIMER:
                    timerText.setText(statistics.getTime());
                    stepsText.setText(statistics.getSteps());
                    velocityText.setText(statistics.getVelocity());
                    metersText.setText(statistics.getDistance());
                    caloriesText.setText(statistics.getCalories());
                    modeText.setText(statistics.getMode());
                    velocityCurrentText.setText(statistics.getCurrentVelocity());
                    statistics.checkForMaxVelocity();
                    Log.d("velocityCurrent", "velocity current" + statistics.getCurrentVelocity());
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER,REFRESH_RATE); //text view is updated every second,
             //       stepsText2.setText(String.valueOf(stepIndicator));
                    break;
                case MSG_STOP_TIMER:
                    velocityText.setText(statistics.getMaxVelocity());
                    mHandler.removeMessages(MSG_UPDATE_TIMER); // no more updates.
                    break;

                case MSG_RESUME_TIMER:
                    statistics.resume();
                    mHandler.sendEmptyMessage(MSG_UPDATE_TIMER);
                    break;

                case MSG_PAUSE_TIMER:
                    statistics.pause();
                    modeText.setText("Standing");
                    mHandler.removeMessages(MSG_UPDATE_TIMER); // no more updates.
                    break;

                default:
                    break;
            }
        }
    };

    void findViews(){
        speedometer = findViewById(R.id.velocityIcon);
        mode = findViewById(R.id.modeIcon);

        textStatus = findViewById(R.id.textStatus);
        stepsText = findViewById(R.id.stepsText);
        timerText = findViewById(R.id.timerText);
        velocityText = findViewById(R.id.velocityText);
        metersText = findViewById(R.id.metersText);
        caloriesText = findViewById(R.id.caloriesText);
        velocityCurrentText = findViewById(R.id.velocityCurrentText);
        modeText = findViewById(R.id.modeText);
        controlLayout =  findViewById(R.id.controlLayout);

        btnStart = findViewById(R.id.startButton);
        btnStop = findViewById(R.id.stopButton);
        btnResume = findViewById(R.id.resumeButton);
        btnPause = findViewById(R.id.pauseButton);
        btnReconnect = findViewById(R.id.reconnectButton);

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnResume.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnReconnect.setOnClickListener(this);

        //only for testing
/*
        minMinus = findViewById(R.id.minMinus);
        minPlus = findViewById(R.id.minPlus);
        maxMinus = findViewById(R.id.maxMinus);
        maxPlus = findViewById(R.id.maxPlus);
        precisionPlus = findViewById(R.id.precisionPlus);
        precisionMinus = findViewById(R.id.precisionMinus);

        textPrecision = findViewById(R.id.precisionText);
        textMin = findViewById(R.id.minText);
        textMax = findViewById(R.id.maxText);
        //      stepsText2 = findViewById(R.id.stepsText2);
        minPlus.setOnClickListener(this);
        minMinus.setOnClickListener(this);
        maxMinus.setOnClickListener(this);
        maxPlus.setOnClickListener(this);
        precisionPlus.setOnClickListener(this);
        precisionMinus.setOnClickListener(this);
*/
    }



    void setVisibility(boolean start, boolean stop, boolean pause, boolean resume, boolean reconnect){
        if(start) btnStart.setVisibility(View.VISIBLE);
        else  btnStart.setVisibility(View.GONE);

        if(stop) btnStop.setVisibility(View.VISIBLE);
        else btnStop.setVisibility(View.GONE);

        if(pause) btnPause.setVisibility(View.VISIBLE);
        else btnPause.setVisibility(View.GONE);

        if(resume) btnResume.setVisibility(View.VISIBLE);
        else btnResume.setVisibility(View.GONE);


        if(reconnect){
            controlLayout.setVisibility(View.GONE);
            btnReconnect.setVisibility(View.VISIBLE);
        }
        else {
            controlLayout.setVisibility(View.VISIBLE);
            btnReconnect.setVisibility(View.GONE);
        }

    }

    protected void onResume() {
        super.onResume();
        /*
        if(sharedPref.contains("steps")){
//            stopPedometer();
            mHandler.sendEmptyMessage(MSG_PAUSE_TIMER);

            //temporary life hack
            //SOMETHING IS NO YES
            sharedPref = getSharedPreferences("pedometerInformation", MODE_PRIVATE);
            String time = sharedPref.getString("getTime","00:00:00");
            statistics.restore(this);

            stepsText.setText(statistics.getSteps());
            velocityText.setText(statistics.getVelocity());
            metersText.setText(statistics.getDistance());
            caloriesText.setText(statistics.getCalories());
            timerText.setText(time);
            modeText.setText(statistics.getMode());
            velocityCurrentText.setText("0.00");
        }
*/
    }

    public void onClick(View v) {
        Intent i = new Intent();

        switch(v.getId()){
            case R.id.startButton:
               // startPedometer();
                i.setAction(BluetoothService.START_PEDOMETER);
                sendBroadcast(i);
                mHandler.sendEmptyMessage(MSG_START_TIMER);
                setVisibility(false,true,true,false,false);
                speedometer.setImageResource(R.drawable.speedmeter_avg);
                break;
            case R.id.stopButton:
               // stopPedometer();
                i.setAction(BluetoothService.STOP_PEDOMETER);
                sendBroadcast(i);
                mHandler.sendEmptyMessage(MSG_STOP_TIMER);
                setVisibility(true,false,false,false,false);
                speedometer.setImageResource(R.drawable.speedmetermax);
                mode.setImageResource(R.drawable.standing);
                statistics.setMode("Standing");
                modeText.setText("Standing");
                statistics.saveToDatabase(mHistoryDao);
                break;
            case R.id.pauseButton:
             //   stopPedometer();
                i.setAction(BluetoothService.STOP_PEDOMETER);
                sendBroadcast(i);
                mHandler.sendEmptyMessage(MSG_PAUSE_TIMER);
                setVisibility(false,true,false,true,false);
                mode.setImageResource(R.drawable.standing);
                statistics.setMode("Standing");
                modeText.setText("Standing");
                break;

            case R.id.resumeButton:
           //     startPedometer();
                i.setAction(BluetoothService.START_PEDOMETER);
                sendBroadcast(i);
                setVisibility(false,true,true,false,false);
                mHandler.sendEmptyMessage(MSG_RESUME_TIMER);
                break;

            case R.id.reconnectButton:
                i.setAction(BluetoothService.BLUETOOTH_RECONNECT);
                sendBroadcast(i);
                btnReconnect.setEnabled(false);
                //do something for reconnecting lol
                break;
        }
/*
            if(minPlus == v){
                writeToUart("I");
                textMin.setText("Min: " + (min+=1));
            }else
            if(maxMinus == v){
                writeToUart("x");
                textMax.setText("Max: " + (max-=1));
            }else
                if(maxPlus == v){
                writeToUart("X");
                textMax.setText("Max: " + (max+=1));
            }else
                if(precisionPlus == v){
            writeToUart("P");
            textPrecision.setText("Precision: " + (precision+=100));
        }else
        if(precisionMinus == v){
            writeToUart("p");
            textPrecision.setText("Precision: " + (precision-=100));
        }
            if(minMinus == v){
                writeToUart("i");
                textMin.setText("Min: " + (min-=1));
            }
*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        sharedPref = getSharedPreferences("Activity",Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        editor.putString("PREVIOUS_ACTIVITY", this.getClass().getSimpleName());
        editor.commit();
        exitingApp = false;
        Intent intent;
        switch (item.getItemId()) {
            case R.id.historyItem:
                intent = new Intent(this,HistoryList.class);
                exitingApp = false;
                startActivity(intent);
                finish();
                return true;
            case R.id.creditsItem:
                intent = new Intent(this,Credits.class);
               startActivity(intent);
               finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


            // fast way to call Toast
            private void msg(String s)
            {
                Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
            }

    void vibrate(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE));
        }else{
            //deprecated in API 26
            v.vibrate(100);
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {

            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    private void startService(){
        myIntent = new Intent(MainActivity.this, BluetoothService.class);
        startService(myIntent);
    }

    private void stopService(){
        if(myIntent != null) stopService(myIntent);
    }

    @Override
    public void onDestroy(){
        if(exitingApp) stopService();
     super.onDestroy();
    }


    private class MyMainReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothService.MODE_WALKING)){
                statistics.countStep();
                statistics.setMode("Walking");
                mode.setImageResource(R.drawable.walking);
            }
            if(action.equals(BluetoothService.MODE_RUNNING)){
                statistics.countStep();
                mode.setImageResource(R.drawable.running);
                Log.d("xd","BIEGNE");
                statistics.setMode("Running");
            }
            if(action.equals(BluetoothService.MODE_STANDING)){
                mode.setImageResource(R.drawable.standing_exhausted);
                statistics.setMode("Standing");
            }
            if(action.equals(BluetoothService.CONNECTION_LOST)){
                mode.setImageResource(R.drawable.standing_exhausted);
                statistics.setMode("Standing");
                mHandler.sendEmptyMessage(MSG_PAUSE_TIMER);
                //hide buttons and show reconnect button
                controlLayout.setVisibility(View.GONE);
                btnReconnect.setVisibility(View.VISIBLE);
                btnReconnect.setEnabled(true);
                textStatus.setText( "Pedometer: not connected");
            }
            if(action.equals(BluetoothService.CONNECTION_RECONNECTED)){
                //hide reconnect button and show buttons
                controlLayout.setVisibility(View.VISIBLE);
                btnReconnect.setVisibility(View.GONE);
                textStatus.setText("Pedometer: connected");
                if(btnStart.getVisibility() == View.VISIBLE){
                    mode.setImageResource(R.drawable.standing);
                    statistics.setMode("Standing");
                }
                else{
                    mode.setImageResource(R.drawable.standing_exhausted);
                    statistics.setMode("Standing");
                    setVisibility(false,true,false,true,false);
                }
            }
            if(action.equals(BluetoothService.CONNECTION_RECONNECTION_FAILED)){
                btnReconnect.setEnabled(true);
            }
        }
    }

}




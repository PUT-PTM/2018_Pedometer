package com.example.karol.pedometer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class BtDevicePicker extends AppCompatActivity {

    MyMainReceiver myMainReceiver;
    Intent myIntent = null;

    Button refreshButton;
    ListView devicelist;

    //Bluetooth
    private BluetoothAdapter btAdapter = null; //myBluetooth
    private Set<BluetoothDevice> pairedDevices;

    boolean doubleBackToExitPressedOnce = false;

    boolean exitingApp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_device_picker);
        startService();
        //Calling widgets
        refreshButton = findViewById(R.id.refreshButton);
        devicelist = findViewById(R.id.listView);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList();
            }
        });


        //if the device has bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null) {
            //Show a mensag. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        } else if (!btAdapter.isEnabled()) {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }

        pairedDevicesList();

    }

    protected void onStart(){

        myMainReceiver = new MyMainReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.CONNECTION_PEDOMETER_CONNECTED);
        intentFilter.addAction(BluetoothService.CONNECTION_FAILED);
        intentFilter.addAction(BluetoothService.CONNECTION_IN_PROGRESS);
        registerReceiver(myMainReceiver, intentFilter);
        super.onStart();
    }

    private void startService(){
        myIntent = new Intent(BtDevicePicker.this, BluetoothService.class);
        startService(myIntent);
    }

    private void stopService(){
        if(myIntent != null){
            stopService(myIntent);
        }
        myIntent = null;
    }

    @Override
    protected void onStop() {
        unregisterReceiver(myMainReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(exitingApp) stopService();
        super.onDestroy();
    }

    private class MyMainReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ProgressBar spinner;
            spinner = (ProgressBar)findViewById(R.id.progressBar1);
            String action = intent.getAction();
            if(action.equals(BluetoothService.CONNECTION_IN_PROGRESS)){
                spinner.setVisibility(View.VISIBLE);
                devicelist.setEnabled(false);
            }
            if(action.equals(BluetoothService.CONNECTION_PEDOMETER_CONNECTED)){
                spinner.setVisibility(View.GONE);
                intent = new Intent(BtDevicePicker.this,UserInformation.class);
                startActivity(intent);
                exitingApp = false;
                finish();
            }
            if(action.equals(BluetoothService.CONNECTION_FAILED)){
                spinner.setVisibility(View.GONE);
                devicelist.setEnabled(true);
            }

        }
    }


    private void pairedDevicesList()
    {
        pairedDevices = btAdapter.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            Intent i = new Intent();
            i.setAction(BluetoothService.BLUETOOTH_ADDRESS);
            i.putExtra(BluetoothService.BLUETOOTH_ADDRESS_STRING, address);
            sendBroadcast(i);
        }
    };


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater(); //get menu
        inflater.inflate(R.menu.menu, menu); //put custom menu in it
        return true;
    }



    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        SharedPreferences sharedPref = getSharedPreferences("Activity", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("PREVIOUS_ACTIVITY", this.getClass().getSimpleName());
        editor.commit();
        Intent intent;
        switch (item.getItemId()) {
            case R.id.historyItem:
                intent = new Intent(this,HistoryList.class);
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

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Intent i = new Intent();
            i.setAction(BluetoothService.BLUETOOTH_DISCONNECT);
            sendBroadcast(i);
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
}




package com.example.karol.pedometer;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class BluetoothService extends Service {

    //from BluetoothService to MainActivity
    final static String MODE_STANDING = "MODE_STANDING";
    final static String MODE_WALKING = "MODE_WALKING";
    final static String MODE_RUNNING = "MODE_RUNNING";
    final static String CONNECTION_IN_PROGRESS = "CONNECTION_IN_PROGRESS";
    final static String CONNECTION_FAILED = "CONNECTION_FAILED";
    final static String CONNECTION_PEDOMETER_CONNECTED = "CONNECTION_PEDOMETER_CONNECTED";
    final static String CONNECTION_LOST = "CONNECTION_LOST";
    final static String CONNECTION_RECONNECTED = "CONNECTION_RECONNECTED";
    final static String CONNECTION_RECONNECTION_FAILED = "CONNECTION_RECONNECTION_FAILED";

    //from MainActivity to BluetoothService
    final static String STOP_PEDOMETER = "STOP_PEDOMETER";
    final static String START_PEDOMETER = "START_PEDOMETER";
    final static String BLUETOOTH_ADDRESS = "BLUETOOTH_ADDRESS";
    final static String BLUETOOTH_ADDRESS_STRING = "BLUETOOTH_ADDRESS_STRING";
    final static String BLUETOOTH_DISCONNECT = "BLUETOOTH_DISCONNECT";
    final static String BLUETOOTH_RECONNECT = "BLUETOOTH_RECONNECT";
    MyServiceReceiver myServiceReceiver;

    BluetoothAdapter btAdapter = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private boolean isReconnecting = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String msgReceived = "";
    private RecivingThread reciveThread;
    boolean recivingData = false;
    ConnectBT connect;
    String address;
    private IBinder myBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    public class MyBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public void onCreate() {
        myServiceReceiver = new MyServiceReceiver();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(STOP_PEDOMETER);
        intentFilter.addAction(START_PEDOMETER);
        intentFilter.addAction(BLUETOOTH_ADDRESS);
        intentFilter.addAction(BLUETOOTH_RECONNECT);
        registerReceiver(myServiceReceiver, intentFilter);

        return START_STICKY ;
    }

    @Override
    public void onDestroy() {
      //  myServiceThread.setRunning(false);
        if(isBtConnected)reciveThread.cancelThatShit();
        unregisterReceiver(myServiceReceiver);
        super.onDestroy();
    }

    public class MyServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(action.equals(START_PEDOMETER)){
                startPedometer();
                }
                if(action.equals(STOP_PEDOMETER)){
                stopPedometer();
                }
            if(action.equals(BLUETOOTH_ADDRESS)){
                address = intent.getStringExtra("BLUETOOTH_ADDRESS_STRING");
                if(!isBtConnected){
                    connect =  new ConnectBT(); //Call the class to connect
                    connect.execute();
                }
            }
            if(action.equals(BLUETOOTH_RECONNECT)){
                Toast.makeText(getApplicationContext(),
                        "RECONNECTING. PLEASE WAIT", Toast.LENGTH_LONG).show();
                reciveThread.cancel();
                btAdapter = null;
                btSocket = null;
                isBtConnected = false;
                connect =  new ConnectBT(); //Call the class to connect
                connect.execute();
                isReconnecting = true;
            }


        }
    }

    //disconnecting bluetooth adapter, currently not used
    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
    }

    private void startPedometer()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("R".getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void stopPedometer()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("S".getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    void writeToUart(String letter){
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write(letter.getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }



    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }


    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            //send back to MainActivity
            Intent i = new Intent();
            i.setAction(CONNECTION_IN_PROGRESS);
            sendBroadcast(i);
             //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    btAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice btDevice = btAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                    reciveThread = new RecivingThread(btSocket);
                    reciveThread.start();
                    recivingData = true;
                    isBtConnected = true;
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);
            Intent i = new Intent();
            if (!ConnectSuccess)
            {
                if(isReconnecting){
                    Toast.makeText(getApplicationContext(),
                            "Reconnecting to pedometer failed", Toast.LENGTH_LONG).show();
                    i.setAction(CONNECTION_RECONNECTION_FAILED);
                    isReconnecting = false;
                }
                else{
                    Toast.makeText(getApplicationContext(),
                            "Connecting to pedometer failed", Toast.LENGTH_LONG).show();
                    i.setAction(CONNECTION_FAILED);
                }

                sendBroadcast(i);
            }
            else{
                    isBtConnected = true;
                    if(isReconnecting){
                        i.setAction(CONNECTION_RECONNECTED);
                        isReconnecting = false;
                    }
                    else{
                        i.setAction(CONNECTION_PEDOMETER_CONNECTED);
                    }
                sendBroadcast(i);
            }
        }
    }
    //Thread that recives data from uart
    public class RecivingThread extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        Intent i = new Intent();
        public RecivingThread(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            try {
                in = socket.getInputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
        }

        public void cancelThatShit(){
            recivingData = false;
            cancel();
            Disconnect();
            Thread.currentThread().interrupt();
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            //it was while before
            while (recivingData) {
                Log.d("your mother","walks fast2");
                try {
                    bytes = connectedInputStream.read(buffer);
                    String strReceived = new String(buffer, 0, bytes);

                    if(strReceived.equals("w")){
                        i.setAction(MODE_WALKING);
                        sendBroadcast(i);

                    }
                    if(strReceived.equals("r")){
                        i.setAction(MODE_RUNNING);
                        sendBroadcast(i);
                    }
                    if(strReceived.equals("s")){
                        i.setAction(MODE_STANDING);
                        sendBroadcast(i);
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    i.setAction(CONNECTION_LOST);
                    sendBroadcast(i);
                                recivingData = false;
                                cancel();
                                Disconnect();
                                Thread.currentThread().interrupt(); //probably not working
                            }
            }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
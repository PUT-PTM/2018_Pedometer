package com.example.karol.pedometer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class Credits extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);
    }

    public void onBackPressed() {
        Intent i = new Intent();
        SharedPreferences sharedPref = getSharedPreferences("Activity", MODE_PRIVATE);
        String previous = sharedPref.getString("PREVIOUS_ACTIVITY","MainActivity");
        switch(previous){
            case "MainActivity":
                i = new Intent(this,MainActivity.class);
                break;
            case "BtDevicePicker":
                i = new Intent(this,BtDevicePicker.class);
                break;
            case "UserInformation":
                i = new Intent(this,UserInformation.class);
                break;
            case "Credits":
                //    i = new Intent(this,Credits.class);
                break;
            default:
                i = new Intent(this,MainActivity.class);
                break;
        }
        startActivity(i);
        finish();
    }

}

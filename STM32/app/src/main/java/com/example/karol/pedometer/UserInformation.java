package com.example.karol.pedometer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;

import java.util.ArrayList;

public class UserInformation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_information);

        ArrayList<Integer> weight = new ArrayList<>();
        for(int i=40;i<=150;i++){
            weight.add(i);
        }

        ArrayList<Integer> height = new ArrayList<>();
        for(int i=54;i<=272;i++){
            height.add(i);
        }

// Selection of the spinner

        final Spinner weightSpinner = findViewById(R.id.weightSpinner);
        final Spinner heightSpinner = findViewById(R.id.heightSpinner);

// Application of the Array to the Spinner
      ArrayAdapter<Integer> spinnerArrayAdapter = new ArrayAdapter<>(this,   android.R.layout.simple_spinner_item, weight);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        weightSpinner.setAdapter(spinnerArrayAdapter);

        int spinnerPosition = spinnerArrayAdapter.getPosition(70);
        weightSpinner.setSelection(spinnerPosition);

        spinnerArrayAdapter = new ArrayAdapter<>(this,   android.R.layout.simple_spinner_item, height);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        heightSpinner.setAdapter(spinnerArrayAdapter);
        spinnerPosition = spinnerArrayAdapter.getPosition(170);
        heightSpinner.setSelection(spinnerPosition);

        final RadioButton femaleRadio,maleRadio;
        femaleRadio = findViewById(R.id.femaleRadio);
        maleRadio = findViewById(R.id.maleRadio);
        maleRadio.setChecked(true);

        Button submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(UserInformation.this, MainActivity.class);
                i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
                if(femaleRadio.isChecked()){
                    i.putExtra("RADIO",'f');
                }
                else{
                    i.putExtra("RADIO",'m');
                }

                String weight = weightSpinner.getSelectedItem().toString();
                String height = heightSpinner.getSelectedItem().toString();
                i.putExtra("WEIGHT",Double.valueOf(weight));
                i.putExtra("HEIGHT",Double.valueOf(height));

                startActivity(i);
                finish();
            }
        });
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater(); //get menu
        inflater.inflate(R.menu.menu, menu); //put custom menu in it
        return true;
    }


    @Override
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


    public void onBackPressed() {
        Intent intent = new Intent(this,BtDevicePicker.class);
        startActivity(intent);
        finish();
    }




}

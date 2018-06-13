package com.example.karol.pedometer;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryEntry extends AppCompatActivity {

    private int id;
    private int steps;
    private String date;
    private String time;
    private double distance;
    private double maxVelocity;
    private double calories;

    TextView stepsText,timerText,velocityText,metersText,caloriesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_entry);

        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            id = extras.getInt("ID");
            steps  = extras.getInt("STEPS");
            time = extras.getString("TIME");
            date = extras.getString("DATE");

            distance = extras.getDouble("DISTANCE");
            maxVelocity = extras.getDouble("MAXVELOCITY");
            calories = extras.getDouble("CALORIES");

            stepsText = findViewById(R.id.stepsText);
            timerText = findViewById(R.id.timerText);
            velocityText = findViewById(R.id.velocityText);
            metersText = findViewById(R.id.metersText);
            caloriesText = findViewById(R.id.caloriesText);

            stepsText.setText(String.valueOf(steps));
            timerText.setText(time);
            velocityText.setText(String.format("%.2f", maxVelocity));
            metersText.setText(String.format("%.2f",distance));
            caloriesText.setText(String.format("%.2f",calories));
        }
        else{
            Toast.makeText(this, "UPS! Something went wrong. Try again",
                    Toast.LENGTH_LONG).show();
        }

        AppDatabase mDb = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "PedometerHistory").allowMainThreadQueries().build();
        final HistoryDao mHistoryDao = mDb.historyDao();// Get DAO object

        Button btn = (Button) findViewById(R.id.deleteButton);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mHistoryDao.deleteById(id);
                goBack();
            }
        });

    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    private void goBack(){
        Intent intent = new Intent(this, HistoryList.class);
        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }

}

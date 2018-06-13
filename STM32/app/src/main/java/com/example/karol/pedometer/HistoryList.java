
package com.example.karol.pedometer;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryList extends AppCompatActivity {
        TextView textView;
        ListView listView;
        ArrayList toListView;
        List<History> list;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_history_list);

                AppDatabase mDb = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "PedometerHistory").allowMainThreadQueries().build();

                final HistoryDao mHistoryDao = mDb.historyDao();// Get DAO object

                listView = findViewById(R.id.listView);
                list = mHistoryDao.getAll();
                toListView = new ArrayList<>();

                if (list.size()>0)
                {
                        for(History entry : list)
                        {
                                Date entryDate = entry.getDate();
                                SimpleDateFormat formater = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault());
                                String date = formater.format(entryDate);
                                toListView.add(date + "\nSteps: " + entry.getSteps());

                        }

                        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, toListView);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(myListClickListener);
                }

        }

        private String getPrevious(){
                SharedPreferences sharedPref;
                SharedPreferences.Editor editor;
                sharedPref = getSharedPreferences("Activity", MODE_PRIVATE);

                return sharedPref.getString("PREVIOUS_ACTIVITY","MainActivity");
        }

        private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
        {
                public void onItemClick (AdapterView<?> av, View v, int position, long arg3)
                {
                        Intent i = new Intent(HistoryList.this, HistoryEntry.class);
                        i.putExtra("ID",list.get(position).getId());
                        i.putExtra("DATE", list.get(position).getDate());
                        i.putExtra("TIME", list.get(position).getTime());
                        i.putExtra("STEPS", list.get(position).getSteps());
                        i.putExtra("DISTANCE", list.get(position).getDistance());
                        i.putExtra("MAXVELOCITY",list.get(position).getMaxVelocity());
                        i.putExtra("CALORIES",list.get(position).getCalories());

                        startActivity(i);
                        finish();
                }
        };


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
package com.example.karol.pedometer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by Lokar on 04.05.2018.
 */

public class Statistics {

    Stopwatch timer = new Stopwatch();

    private double stepWidth;

    private double weight;
    private int steps;
    private double maxVelocity;
    private double currentVelocity;
    private String mode;
    private long beginningTimeOfWalk; //in SECONDS
    private double beginningMetersOfWalk; //in METERS
    private long lastStep;
    Statistics(){
        steps = 0;
        weight = 61;
        maxVelocity = 0;
        mode = "Standing";
        lastStep = 0;
    }

    public void start(){
        timer.start();
        steps = 0;
        beginningTimeOfWalk = 0;
        beginningMetersOfWalk = 0;
    }


    public void restore(Context context){
        SharedPreferences sharedPref;
        SharedPreferences.Editor editor;
        sharedPref = context.getSharedPreferences("pedometerInformation", MODE_PRIVATE);
        editor = sharedPref.edit();
        this.steps = sharedPref.getInt("steps",0);
        this.weight = Double.longBitsToDouble(sharedPref.getLong("weight",0));
        long savedTime = sharedPref.getLong("savedTime",0);
        this.timer.restore();
        this.stepWidth = Double.longBitsToDouble(sharedPref.getLong("stepWidth",0));
        editor.clear().commit();
    }

    public void saveToDatabase(HistoryDao mHistoryDao){
        History history = new History();
        Date timeStamp = new Date();
        history.setEverything(timeStamp, getTime(), steps, distance(), calories(), maxVelocity());
        mHistoryDao.insertAll(history);
    }


    public void stop(){
        timer.stop();
    }

    public void resume(){
        timer.resume();
    }

    public void pause(){
        timer.pause();
    }

    public void countStep(){
        steps++;
        if(beginningTimeOfWalk == 0){
            beginningTimeOfWalk = timer.getElapsedTimeSecs();
            beginningMetersOfWalk = distance();
        }
        lastStep = timer.getElapsedTimeSecs();
    }

    private long time(){
        return 3600*timer.getElapsedTimeHour()+60*timer.getElapsedTimeMin()+timer.getElapsedTimeSecs();
    }

    private double distance(){
        return steps*stepWidth;
    }

    private double velocity(){
        if(time()!=0) return distance()/time();
        else return 0;
    }

    private double maxVelocity(){
        return maxVelocity;
    }

    private double currentVelocity(){
        double time =  timer.getElapsedTimeSecs();
        if(mode=="Standing"){
            beginningMetersOfWalk = 0;
            beginningTimeOfWalk = 0;
            return 0;
        }
        else {
            double distanceCurrent = distance() - beginningMetersOfWalk;
            double timerCurrent = time - beginningTimeOfWalk;
            double velocity = distanceCurrent / timerCurrent;
            return !Double.isNaN(velocity) ? velocity : 0;
        }
    }

    public void checkForMaxVelocity(){
        //44.72 km/h is the fastest running speed of a human ever recorded
        if(velocity()>maxVelocity && velocity() <= 44.72){
            maxVelocity = velocity();
        }
    }

    private double calories(){
        return 0.0005 * weight * distance() + 0.0035 * (time()/60);
    }

    public String getSteps(){
        return ""+steps;
    }

    @SuppressLint("DefaultLocale")
    public String getDistance(){
            return String.format("%.2f", distance());
    }
    @SuppressLint("DefaultLocale")
    public String getVelocity(){
        return String.format("%.2f", velocity());
    }
    @SuppressLint("DefaultLocale")
    public String getCalories(){
        return String.format("%.2f", calories());
    }
    @SuppressLint("DefaultLocale")
    public String getMaxVelocity() {
        return String.format("%.2f",maxVelocity());
    }

    @SuppressLint("DefaultLocale")
    public String getCurrentVelocity(){
        return String.format("%.2f",currentVelocity());
    }


    public String getTime(){
        return timer.fullTime();
    }

    public String getMode() {
        return this.mode;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setStepWidth(double height,char gender){
        //not politically correct function
        if(gender == 'f'){
            this.stepWidth = (height * 0.413)/100;
        }
        else{
            this.stepWidth = (height * 0.415)/100;
        }
    }

    public void setMode(String mode){
        this.mode = mode;
    }

    public void setCurrentVelocity(){
        this.currentVelocity = currentVelocity();
    }


    public boolean isTimerRunning(){
        return timer.getRunning();
    }

}

package com.example.karol.pedometer;

/*
Copyright (c) 2005, Corey Goldberg

StopWatch.java is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

Modified: Bilal Rabbani bilalrabbani1@live.com (Nov 2013)
Modified: Karol Rosiak rosiak-karol@wp.pl (May 2018)
*/

import android.util.Log;

public class Stopwatch {
    private long startTime = 0;
    private boolean running = false;
    private long currentTime = 0;

    public void start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
        Log.d("startTimer",String.valueOf(currentTime));
    }

    public void stop() {
        this.running = false;
        Log.d("stopTimer",String.valueOf(currentTime));
    }

    public void pause() {
        this.running = false;
        currentTime = System.currentTimeMillis() - startTime;
        Log.d("pauseTimer",String.valueOf(currentTime));
    }
    public void resume() {
        this.running = true;
        this.startTime = System.currentTimeMillis() - currentTime;
        currentTime = System.currentTimeMillis();
        Log.d("resumeTimerr current",String.valueOf(currentTime));
        Log.d("resumeTimer start",String.valueOf(startTime));
    }

    public void restore(){
        this.startTime = System.currentTimeMillis() - currentTime;
      //  this.currentTime = System.currentTimeMillis();
    }

    public long getCurrentTime() {
        return this.currentTime;
    }

    public long getStartTime(){
        return this.startTime;
    }

    //elaspsed time in milliseconds
    public long getElapsedTimeMili() {
        long elapsed = 0;
        if (running) {
            elapsed =((System.currentTimeMillis() - startTime)/100) % 1000 ;
        }
        return elapsed;
    }

    //elaspsed time in seconds
    public long getElapsedTimeSecs() {
        long elapsed = 0;
        if (running) {
            elapsed = ((System.currentTimeMillis() - startTime) / 1000) % 60;
        }
        return elapsed;
    }

    //elaspsed time in minutes
    public long getElapsedTimeMin() {
        long elapsed = 0;
        if (running) {
            elapsed = (((System.currentTimeMillis() - startTime) / 1000) / 60 ) % 60;
        }
        return elapsed;
    }

    //elaspsed time in hours
    public long getElapsedTimeHour() {
        long elapsed = 0;
        if (running) {
            elapsed = ((((System.currentTimeMillis() - startTime) / 1000) / 60 ) / 60);
        }
        return elapsed;
    }

    public String toString() {
        return getElapsedTimeHour() + ":" + getElapsedTimeMin() + ":"
                + getElapsedTimeSecs() + "." + getElapsedTimeMili();
    }

    public String leadingZero(long number){
        if(number<10) return "0"+number;
        else return ""+number;
    }

    public String fullTime() {
        return leadingZero(getElapsedTimeHour()) + ":" + leadingZero(getElapsedTimeMin()) + ":"
                + leadingZero(getElapsedTimeSecs());
    }

    public boolean getRunning(){
        return this.running;
    }
}

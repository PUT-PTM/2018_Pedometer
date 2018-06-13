package com.example.karol.pedometer;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "history")
public class History {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "date")
    @TypeConverters({Converters.class})
    private Date date;

    @ColumnInfo(name = "time")
    private String time;

    @ColumnInfo(name = "steps")
    private int steps;

    @ColumnInfo(name = "distance")
    private double distance;

    @ColumnInfo(name = "maxVelocity")
    private double maxVelocity;

    @ColumnInfo(name = "calories")
    private double calories;


    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public double getMaxVelocity() {
        return maxVelocity;
    }

    public void setMaxVelocity(double maxVelocity) {
        this.maxVelocity = maxVelocity;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public void setEverything(Date date, String time, int steps, double distance, double calories, double maxVelocity){
        setDate(date);
        setTime(time);
        setSteps(steps);
        setDistance(distance);
        setCalories(calories);
        setMaxVelocity(maxVelocity);
    }

    public void setEverything(int id,Date date, String time, int steps,double distance, double calories, double maxVelocity){
        setId(id);
        setDate(date);
        setTime(time);
        setSteps(steps);
        setDistance(distance);
        setCalories(calories);
        setMaxVelocity(maxVelocity);

    }

}

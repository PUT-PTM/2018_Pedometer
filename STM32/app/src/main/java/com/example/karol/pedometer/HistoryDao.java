package com.example.karol.pedometer;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;

import java.util.Date;
import java.util.List;

@Dao
public interface HistoryDao {
    @Query("SELECT * FROM history")
    List<History> getAll();

    @Query("SELECT * FROM history WHERE date LIKE :date")
    @TypeConverters({Converters.class})
    List<History> getByDate(Date date);

    @Query("SELECT COUNT(*) from history")
    int countEntries();

    @Insert
    void insertAll(History... entries);

    @Delete
    void delete(History history);

    @Query("DELETE FROM history WHERE ID LIKE :id")
    void deleteById(int id);

}

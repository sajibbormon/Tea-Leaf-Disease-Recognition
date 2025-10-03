package com.tealeafdisease.tealeafdisease;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StatisticsDao {

    @Insert
    void insert(StatisticsEntity entity);

    @Query("SELECT * FROM statistics")
    List<StatisticsEntity> getAll();

    @Query("SELECT * FROM statistics WHERE date = :date")
    List<StatisticsEntity> getByDate(String date);

    // For ResultActivity
    @Query("SELECT * FROM statistics WHERE date = :date AND disease = :disease LIMIT 1")
    StatisticsEntity getStatByDateAndDisease(String date, String disease);

    // Increment count
    @Query("UPDATE statistics SET count = count + 1 WHERE id = :id")
    void incrementCount(int id);
}

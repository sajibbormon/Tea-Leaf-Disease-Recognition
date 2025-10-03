package com.tealeafdisease.tealeafdisease;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "statistics")
public class StatisticsEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String disease;   // Disease name
    public int count;        // Number of cases
    public String date;      // Stored in yyyy-MM-dd format

    // No-arg constructor (Room can use it)
    public StatisticsEntity() { }

    // Convenience constructor
    public StatisticsEntity(String disease, int count, String date) {
        this.disease = disease;
        this.count = count;
        this.date = date;
    }
}

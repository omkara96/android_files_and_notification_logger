package com.example.bit_mine.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DbDao {

    @Query("SELECT * FROM DbParam")
    List<DbParam> getAllData();

    @Insert
    void insertData(DbParam... dbParams);

    @Delete
    void deleteData(DbParam dbParam);

    @Query("DELETE FROM DbParam")
    public void nukeTable();

}

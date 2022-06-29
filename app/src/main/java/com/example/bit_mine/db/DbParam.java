package com.example.bit_mine.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DbParam {

   @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "File_Name")
    public String fname;

    @ColumnInfo(name = "File_Path")
    public String fpath;
}

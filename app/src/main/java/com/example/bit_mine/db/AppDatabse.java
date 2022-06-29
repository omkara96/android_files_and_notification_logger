package com.example.bit_mine.db;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DbParam.class}, version  =1)
public abstract class AppDatabse extends RoomDatabase {

    public abstract DbDao dbDao();

    private  static AppDatabse INSTANCE;

    public static AppDatabse getInstance(Context context){

        if(INSTANCE==null){

            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),AppDatabse.class, "File_db")
                    .allowMainThreadQueries()
                    .build();
        }

        return INSTANCE;
    }
}

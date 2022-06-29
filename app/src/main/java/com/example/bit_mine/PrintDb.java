package com.example.bit_mine;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.bit_mine.db.AppDatabse;
import com.example.bit_mine.db.DbParam;

import java.util.List;

public class PrintDb extends AppCompatActivity {

    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_db);

        btn = findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppDatabse db = AppDatabse.getInstance(getApplicationContext());
                List<DbParam> FilesList= db.dbDao().getAllData();
                btn.setText("Process = " + FilesList.size());
                for(int i=0;i<FilesList.size();i++){
                    Log.i("DB", FilesList.get(i).fname.toString());
                }
            }
        });

    }
}
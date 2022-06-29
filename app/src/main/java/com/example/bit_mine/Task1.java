package com.example.bit_mine;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bit_mine.db.AppDatabse;
import com.example.bit_mine.db.DbParam;
import com.example.bit_mine.helperclasses.Partition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Task1 extends AppCompatActivity {

    FirebaseStorage storage;
    StorageReference storageReference;

    FirebaseFirestore db;
    FirebaseFirestore fstore;

    String dp_URL;
    public int total = 0;

    String user;
    Button button, button2, button3;

    SharedPreferences sharedPreferences;

    int CHUNK_SIZE = 80;

    int finalCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task1);

        Objects.requireNonNull(getSupportActionBar()).hide();
        db = FirebaseFirestore.getInstance();
        fstore = FirebaseFirestore.getInstance();

        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        button = findViewById(R.id.buttonTask1);
        button2 = findViewById(R.id.buttonTask2);
        button3 = findViewById(R.id.buttonTask3);


        Intent intent = getIntent();
        String[] FileNames = intent.getStringArrayExtra("abc");
        String[] FileURI = intent.getStringArrayExtra("FileUri");

        int len1 = FileNames.length;
        total = len1;
        user = FirebaseAuth.getInstance().getCurrentUser().getEmail().toString();


        sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        /**************** Get Data from Data base ******************/

        AppDatabse db = AppDatabse.getInstance(getApplicationContext());
        List<DbParam> FilesList = db.dbDao().getAllData();

        /*************** Partition data into chunks
         * Get max required chunks
         * send chunk number to worker class
         * ***************/

        Partition partition = new Partition(FilesList, CHUNK_SIZE);

        finalCount = partition.size();

        /****** Start Configuring Work Maneger *****/


        Data[] data = new Data[finalCount];

//        = new Data.Builder()
//                .putStringArray("FileName",FileNames)
//                .putStringArray("FilePath", FileURI)
//                .build();



        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();


        OneTimeWorkRequest[] tasks = new OneTimeWorkRequest[partition.size()];

        for (int i = 0; i < partition.size(); i++) {

            data[i] = new Data.Builder()
                    .putInt("KEY", i)
                    .build();

            tasks[i] = new OneTimeWorkRequest.Builder(UploadToFirebase.class)
                    .setConstraints(constraints)
                    .addTag("Mine_Task")
                    .setInputData(data[i])
                    .build();

        }

        //////////////////////Work Manager //////////////

//        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(UploadToFirebase.class)
//                .setConstraints(constraints)
//                .addTag("Mine_Task")
//                .build();


        @SuppressLint("WrongConstant") SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        int a = sh.getInt("Runned", 0);

        if (a == 1) {
            button.setEnabled(false);
        } else {
            button.setEnabled(true);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < len1; i++) {
                    savetoDb(FileNames[i], FileURI[i]);
                }
                button.setEnabled(false);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                myEdit.putInt("Runned", 1);
                myEdit.commit();
                button.setText("Process is started please minimise the app you will get notification once completed");

            }
        });


        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                button2.setEnabled(false);
                for (int i = 0; i < finalCount; i++) {
                    WorkManager.getInstance(Task1.this).enqueue(tasks[i]);
                    button2.setText("We are processing " + i+1 + " out of " + finalCount + " task");
                }
                //WorkManager.getInstance(Task1.this).enqueue(Arrays.asList(tasks));
//                beginWith(tasks[0])
//                        .then(tasks[1])
//                        .then(tasks[2])
//                        .then(tasks[3])
//                        .then(tasks[4])
//                        .then(tasks[5])
//                        .then(tasks[6]).
                Toast.makeText(Task1.this, "Please Wait for 30 Min we are processing you can minimise windows", Toast.LENGTH_LONG).show();

            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                myEdit.putInt("Runned", 0);
                myEdit.commit();
                AppDatabse db = AppDatabse.getInstance(getApplicationContext());
                DbParam dbParam = new DbParam();
                db.dbDao().nukeTable();
                Toast.makeText(Task1.this, "Success! Reseting", Toast.LENGTH_SHORT).show();
                button3.setEnabled(false);
                button.setEnabled(true);
            }
        });

        WorkManager.getInstance(this).getWorkInfosByTagLiveData("Mine_Task").observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                for (WorkInfo w : workInfos) {

                    Log.i("WORK", "onChanged work status :" + w.getState());
                }
            }
        });


    }

    private void savetoDb(String fname, String fpath) {
        AppDatabse db = AppDatabse.getInstance(this.getApplicationContext());
        DbParam dbParam = new DbParam();
        dbParam.fname = fname;
        dbParam.fpath = fpath;
        db.dbDao().insertData(dbParam);
    }

}


/*
    private void uploadImage(Uri filePath, String fileName)
    {
        Uri fileUri = Uri.fromFile(new File(String.valueOf(filePath)));
        if (fileUri != null) {


            StorageReference ref = storageReference.child("/" + user +"/" + fileName);

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();

            storageRef.child("/" + user +"/" + fileName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Got the download URL for 'users/me/profile.png'
                    dummy_count++;

                  //  Log.d("FIND", "File already Exist" + fileName);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // File not found Previously hence store this file
                    // adding listeners on upload
                    // or failure of image


                    ref.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                        {
                                            Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                   // Toast.makeText(Task1.this,"task is processing", Toast.LENGTH_SHORT).show();
                                                    //Log.i("URL", task.getResult().toString().toString());

                                                    dp_URL=task.getResult().toString();
                                                    addTonewCollection(dp_URL);
                                                }
                                            });

                                            // Image uploaded successfully
                                            // Dismiss dialog
                                            //  Toast.makeText(MainActivity.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                                        }
                                    })

                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {

                                    // Error, Image not uploaded
                                    // Toast.makeText(MainActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(
                                    new OnProgressListener<UploadTask.TaskSnapshot>() {

                                        // Progress Listener for loading
                                        // percentage on the dialog box
                                        @Override
                                        public void onProgress(
                                                UploadTask.TaskSnapshot taskSnapshot)
                                        {

                                        }
                                    });
                    ///////////////////////////////////////

                }
            });


        }
    }


    /**  Add To Database ***/

    /*
    //Logic to add to db url
    private void addTonewCollection(String dp_url) {
        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Processing Task");
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Processing task + "+ count + " out of " + total);
        progressDialog.show();
        DocumentReference documentReference = fstore.collection("task1 " + FirebaseAuth.getInstance().getCurrentUser().getEmail().toString())
                .document(UUID.randomUUID().toString());

        Map<String, Object> duplicateLink = new HashMap<>();
        duplicateLink.put(UUID.randomUUID().toString(),dp_url);

        documentReference.set(duplicateLink).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //Toast.makeText(MainActivity.this, "Data Sync successfully", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                count++;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Toast.makeText(Task1.this, "DB error", Toast.LENGTH_SHORT).show();
                progressDialog.setMessage("Something Went wrong trying again");
                progressDialog.dismiss();
            }
        });
    }


     */



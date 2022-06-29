package com.example.bit_mine;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

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
import java.io.InterruptedIOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UploadToFirebase extends Worker {

    FirebaseStorage storage;
    StorageReference storageReference;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseFirestore fstore ;
    String user;

    String dp_URL;

    int dbname;

    static int CHUNK_SIZE = 80;

    public UploadToFirebase(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        db = FirebaseFirestore.getInstance();
        fstore = FirebaseFirestore.getInstance();

        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        user =  FirebaseAuth.getInstance().getCurrentUser().getEmail().toString();

        Data getInputData = getInputData();

        int key= getInputData.getInt("KEY",-1);

        AppDatabse db = AppDatabse.getInstance(getApplicationContext());
        List<DbParam> FilesList= db.dbDao().getAllData();

        /************* Make artioned data *****************/

        Partition partition = new Partition(FilesList, CHUNK_SIZE);

        List<DbParam> list = partition.get(key);

//        String[] FileNames = new String[list.size()];
//        String[] FileURI = new String[list.size()];
//
//        for(int i=0;i<FilesList.size();i++){
//            FileNames[i] = list.get(i).toString();
//            FileURI[i] = list.get(i).toString();
//        }
        user = user+key;

          for(int i=0; i<CHUNK_SIZE;i++){
              //Log.i("FINAL", list.get(i).fname+ " ");

              try{
                  uploadImage(Uri.parse(list.get(i).fpath), list.get(i).fname);

              }catch (Exception e){
                  e.printStackTrace();
                  return Result.failure();
              }
          }




       /* for(int i=0; i<FileNames.length;i++){

            //Log.d("KEY", "Uploading file " + i);
            try{
                uploadImage(Uri.parse(FileURI[i]), FileNames[i]);

            }catch (Exception e){
                e.printStackTrace();
                return Result.failure();
            }


        }*/

        //Log.d("COMPLETE", "ALL " + FileNames.length + " are uploaded");
        return Result.success();
    }

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

    //Logic to add to db url
    private void addTonewCollection(String dp_url) {
        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        DocumentReference documentReference = fstore.collection(dbname + FirebaseAuth.getInstance().getCurrentUser().getEmail().toString())
                .document(UUID.randomUUID().toString());

        Map<String, Object> duplicateLink = new HashMap<>();
        duplicateLink.put("Imgs",dp_url);

        documentReference.set(duplicateLink).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //Toast.makeText(MainActivity.this, "Data Sync successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Toast.makeText(Task1.this, "DB error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.example.bit_mine;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bit_mine.loginsignup.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private View mainLayout;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 0;

    //String path1 = "sdcard/Pictures/.thumbnails/";
   String path2 = "sdcard/ColorOS/Camera/.Cache";
    String path3 = "sdcard/Movies/.thumbnails/";
    //String path2 = "/storage/emulated/0/Download/";

    TextView tv;

    Button btn;

    FirebaseStorage storage;
    StorageReference storageReference;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseFirestore fstore ;

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).hide();
        db = FirebaseFirestore.getInstance();
        fstore = FirebaseFirestore.getInstance();

        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mAuth = FirebaseAuth.getInstance();

        checkPermission();

        btn= findViewById(R.id.infobtn);

        textView = findViewById(R.id.textView);
        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showAlertDialogButtonClicked(view);
                return false;
            }
        });

        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                startActivity(new Intent(MainActivity.this, PrintDb.class));
                return false;
            }
        });

    }

    public  void task1(View view){
        readTheFiles(path2);
    }

    public  void task2(View view){
        readTheFiles(path2);
    }

    public void logout(View view){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        startActivity(new Intent(MainActivity.this, Login.class));
        finish();
    }

    public void info(View view){
        startActivity(new Intent(MainActivity.this, Info.class));
    }

    private void readTheFiles(String source) {

        try{

            File directory = new File(source);
            File[] files = directory.getAbsoluteFile().listFiles();
            String[] fileNames = new String[files.length];
            String[] UriArray = new String[files.length];
            Log.d("Files", "Size: "+ files.length);
            if(files.length <=0){
                Toast.makeText(MainActivity.this, "Something Went Wrong contact your supervisor", Toast.LENGTH_SHORT).show();
            }else{
                for (int i = 0; i < files.length; i++)
                {
                    fileNames[i] = files[i].getName();
                    UriArray[i] = source + "/" + fileNames[i];
                   // Log.d("Files", "FileName_path:" + UriArray[i]);
                }

                Intent intent = new Intent(MainActivity.this,Task1.class);
                intent.putExtra("abc", fileNames);
                //intent.putExtra("FileNames", fileNames);
                intent.putExtra("FileUri", UriArray);
                startActivity(intent);
            }


        }
        catch (Exception e){
            Log.i("Exception", e.toString());
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    private void checkPermission() {
        // Check if the permission has been granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available

           // readTheFiles();
        } else {
            // Permission is missing and must be requested.
            requestReadExternalStoragePermission();
        }
    }

    private void requestReadExternalStoragePermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Show a snackBar to the user that explain why you need this permission.
            Snackbar.make(mainLayout, "read_external_storage_permission_required",
                    Snackbar.LENGTH_INDEFINITE).setAction("Okay" ,new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
                }
            }).show();

        } else {
            // Request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            // Request for permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.

            } else {
                // Permission request was denied by user
                // Show a snackBar, exit program, close activity, etc.
            }
        }
    }


    /************** Super User Dialog Login *******************/
    public void showAlertDialogButtonClicked(View view)
    {

        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        builder.setTitle("Super User Settings");
        // set the custom layout
        final View customLayout
                = getLayoutInflater()
                .inflate(
                        R.layout.custom_authentication_dialog,
                        null);
        builder.setView(customLayout);

        // add a button
        builder
                .setPositiveButton(
                        "OKAY!",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which)
                            {

                                // send data from the
                                // AlertDialog to the Activity
                                EditText editText
                                        = customLayout
                                        .findViewById(
                                                R.id.suPass);
                                String d= editText.getText().toString().trim();
                                sendDialogDataToActivity(d);
                            }
                        })

        .setNegativeButton(
                "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }
        );

        // create and show
        // the alert dialog
        AlertDialog dialog
                = builder.create();
        dialog.show();
    }

    // Do something with the data
    // coming from the AlertDialog
    private void sendDialogDataToActivity(String data)
    {
        Toast.makeText(this, data, Toast.LENGTH_LONG).show();
        String d2 = "lucky1234$$&&";
       if(data.equals(d2)){
           startActivity(new Intent(MainActivity.this,DispActivity.class));
       }else{
           Toast.makeText(this, "Error you have not Access", Toast.LENGTH_SHORT).show();
       }
    }


}
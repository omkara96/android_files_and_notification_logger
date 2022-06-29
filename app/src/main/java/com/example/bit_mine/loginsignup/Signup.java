package com.example.bit_mine.loginsignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.bit_mine.MainActivity;
import com.example.bit_mine.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Signup extends AppCompatActivity {

    TextInputLayout name,mail,Phone,Password;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Objects.requireNonNull(getSupportActionBar()).hide();
        changeStatusBarColor();

        name = findViewById(R.id.textInputName);
        mail = findViewById(R.id.textInputEmail);
        Phone = findViewById(R.id.textInputMobile);
        Password = findViewById(R.id.textInputPassword);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void changeStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //window.setStatusBarColor(Color.TRANSPARENT);
        window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
    }

    public void register(View view){
        signupToSystem();
    }

    private void signupToSystem(){

        if(!validateFullname() |  !validateeMail() | !validatePassword() )
        {
            return;
        }

        String usr_Name = name.getEditText().getText().toString().trim();
        String usr_Mail = mail.getEditText().getText().toString().trim();
        String usr_Phone = Phone.getEditText().getText().toString().trim();
        String usr_Password = Password.getEditText().getText().toString().trim();


        Log.i("USER info" , usr_Name);
        Log.i("USER info" , usr_Mail);
        Log.i("USER info" , usr_Phone);
        Log.i("USER info" , usr_Password);


        mAuth.createUserWithEmailAndPassword(usr_Mail , usr_Password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            updateDB(usr_Name, usr_Mail, usr_Phone, usr_Password);
                        }
                    }
                });


    }

    private void updateDB(String usr_Name, String usr_Mail, String usr_Phone, String usr_Password) {
        Map<String, Object> user = new HashMap<>();
        user.put("Name", usr_Name);
        user.put("Phone", usr_Phone);
        user.put("Email", usr_Mail);
        user.put("Password", usr_Password);

        ProgressDialog progressDialog = new ProgressDialog(Signup.this);
        progressDialog.setMessage("Registering User");
        progressDialog.show();
        DocumentReference documentReference = db.collection("Users").document(mAuth.getCurrentUser().getEmail().toString());
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(Signup.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                startActivity(new Intent(Signup.this, MainActivity.class));
                finish();
            }
        });
    }


    /***** Validation Functions              */
    private boolean validateFullname() {
        String val = name.getEditText().getText().toString().trim();
        if (val.isEmpty()) {
            name.setError("Field can not be empty");
            return false;
        } else {
            name.setError(null);
            return true;
        }

    }

    private boolean validateeMail() {
        String val = mail.getEditText().getText().toString().trim();
        String checkEmail = "[a-zA-Z0-9._-]+@[a-z]+.+[a-z]+";

        if (val.isEmpty()) {
            mail.setError("Field can not be empty");
            return false;
        } else if (!val.matches(checkEmail)) {
            mail.setError("Invalid Email!");
            return false;
        } else {
            mail.setError(null);
            //eMail.setErrorEnabled(false);
            return true;
        }

    }

    private boolean validatePassword() {
        String val = Password.getEditText().getText().toString().trim();
        String checkPassword = "^" +
                //"(?=.*[0-9])" +         //at least 1 digit
                //"(?=.*[a-z])" +         //at least 1 lower case letter
                //"(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                //"(?=.*[@#$%^&+=])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{6,}" +               //at least 4 characters
                "$";

        if (val.isEmpty()) {
            Password.setError("Field can not be empty");
            return false;
        } else if (!val.matches(checkPassword)) {
            Password.setError("Password should contain 4 characters!");
            return false;
        } else {
            Password.setError(null);
            //passWord.setErrorEnabled(false);
            return true;
        }
    }
}
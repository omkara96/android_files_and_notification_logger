package com.example.bit_mine.loginsignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bit_mine.MainActivity;
import com.example.bit_mine.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class Login extends AppCompatActivity {

    FirebaseAuth mAuth;
    TextInputLayout Email, Password;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
           startActivity(new Intent(Login.this, MainActivity.class));
           finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //for changing status bar icon colors
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        Objects.requireNonNull(getSupportActionBar()).hide();

        mAuth = FirebaseAuth.getInstance();
        Email = findViewById(R.id.textInputEmail);
        Password = findViewById(R.id.textInputPassword);
    }

    public void Login(View view){

        if(  !validateeMail() | !validatePassword() )
        {
            return;
        }

        String usr_Mail = Email.getEditText().getText().toString().trim();
        String usr_Password = Password.getEditText().getText().toString().trim();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Logging In");
        progressDialog.setMessage("Signing you in please wait...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(usr_Mail,usr_Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    Toast.makeText(Login.this, "Sign in Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Login.this,MainActivity.class));
                    finish();
                }else{
                    progressDialog.dismiss();
                    Toast.makeText(Login.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public void gotoSignup(View view){
        startActivity(new Intent(Login.this, Signup.class));
    }

    private boolean validateeMail() {
        String val = Email.getEditText().getText().toString().trim();
        String checkEmail = "[a-zA-Z0-9._-]+@[a-z]+.+[a-z]+";

        if (val.isEmpty()) {
            Email.setError("Field can not be empty");
            return false;
        } else if (!val.matches(checkEmail)) {
            Email.setError("Invalid Email!");
            return false;
        } else {
            Email.setError(null);
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
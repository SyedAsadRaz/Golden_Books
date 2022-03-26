package com.example.a1stapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.a1stapp.R;
import com.example.a1stapp.databinding.ActivityForgotPassBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class ForgotPassActivity extends AppCompatActivity {

    private ActivityForgotPassBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.submitResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateData();
            }
        });

    }

    private String email = "";
    private void ValidateData() {
        //get data
        email = binding.resetEmailEt.getText().toString().trim();

        //validate
        if (email.isEmpty()){
            Toast.makeText(this, "Enter Email...", Toast.LENGTH_SHORT).show();

        }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid Email format...", Toast.LENGTH_SHORT).show();

        }else{
            recoverPass();
        }
    }

    private void recoverPass() {
        //show progress
        progressDialog.setMessage("Sending passward recovery instructions to "+email);
        progressDialog.show();

        //begin sending
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPassActivity.this, "Check Your Mail: "+email +"to reset passward", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(ForgotPassActivity.this,MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPassActivity.this, "field to send due to "+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        
    }
}
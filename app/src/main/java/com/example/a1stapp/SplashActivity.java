package com.example.a1stapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        firebaseAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //Check user in new or loged in...
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser == null) {
                    // It's New User
                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                    finish();
                }else{
                    // User Loged in so keep it to Dshboard
                    //Check User type..!
                    checkUser();
                }
            }
        },1000);
    }

    private void checkUser() {
        //check if USer is Admin or user...??
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check in database !
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get User Type!
                String userType= ""+snapshot.child("UserType").getValue();
                //Chek User Type Now!
                if (userType.equals("user")){
                    startActivity(new Intent(SplashActivity.this,DashboardUser.class) );
                    finish();

                }else if (userType.equals("admin")){
                    startActivity(new Intent(SplashActivity.this,DashboardAdmin.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
}}
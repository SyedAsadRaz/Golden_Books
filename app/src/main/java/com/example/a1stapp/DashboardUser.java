package com.example.a1stapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.a1stapp.databinding.ActivityDashboardUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardUser extends AppCompatActivity {

    private ActivityDashboardUserBinding binding;
    private FirebaseAuth Fauth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Fauth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // add user email to title bar of dashboard
        String email = firebaseUser.getEmail();
        binding.gmailShowOnDashboard.setText(email);

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fauth.signOut();
                startActivity( new Intent(DashboardUser.this,MainActivity.class));
                finish();

            }
        });
    }
}
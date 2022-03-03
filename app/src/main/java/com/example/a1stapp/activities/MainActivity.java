package com.example.a1stapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.a1stapp.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;


    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);


        binding.skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DashboardUser.class));
                finish();
            }
        });
        binding.gotoSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Register.class));
                finish();
            }
        });

        binding.directLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Configure Google Sign In
//                GoogleSignInOptions gso = new GoogleSignInOptions
//                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestIdToken(getString(R.string.default_web_client_id))
//                        .requestEmail()
//                        .build();
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = binding.editTextGmail.getText().toString();
                String pass = binding.editTextPassward.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    binding.editTextGmail.setError("Email Adress Please");
                } else if (TextUtils.isEmpty(pass)) {
                    binding.editTextPassward.setError("Passward please!");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.editTextGmail.setError("Invalid Email!");
                }else {


                    progressDialog.show();

                    firebaseAuth.signInWithEmailAndPassword(email,pass)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            //Login Successfull!
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
                                        Toast.makeText(MainActivity.this, "Welcome Back!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(MainActivity.this,DashboardUser.class) );
                                        finish();

                                    }else if (userType.equals("admin")){
                                        Toast.makeText(MainActivity.this, "Welcome Back!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(MainActivity.this, DashboardAdmin.class));
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // login Filed!
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });


                }


            }
        });
    }}
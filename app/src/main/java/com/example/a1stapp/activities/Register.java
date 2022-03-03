package com.example.a1stapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.a1stapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    FirebaseAuth Fauth;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Fauth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, MainActivity.class));
                finish();
            }
        });

        binding.singupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = binding.editTextTextPersonName.getText().toString();
                String city = binding.editTextTextCity.getText().toString();
                String email = binding.editTextTextEmailAddress.getText().toString();
                String passward = binding.editTextTextPassword.getText().toString();
                String confPassward = binding.editTextTextPasswordConfirm.getText().toString();

                if (TextUtils.isEmpty(name)){
                    binding.editTextTextPersonName.setError(" Name Please!");
                }
                else if (!passward.matches(confPassward)){
                    binding.editTextTextPasswordConfirm.setError("Passwards not matching...!");
                }
                else if (TextUtils.isEmpty(city)){
                    binding.editTextTextCity.setError(" City Please!");
                }
                else    if (TextUtils.isEmpty(email)){
                    binding.editTextTextEmailAddress.setError("Email Please!");

                }
                else if (TextUtils.isEmpty(passward)){
                    binding.editTextTextPassword.setError("Passward Please!");
                }
                else if (TextUtils.isEmpty(confPassward)){
                    binding.editTextTextPasswordConfirm.setError("confirm Passward Please!");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.editTextTextEmailAddress.setError("Invalid Email!");
                }else {

                    // Creating Account now!
                    progressDialog.setMessage("Creating Account!");
                    progressDialog.show();

                    Fauth.createUserWithEmailAndPassword(email,passward)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
//                                    updateUserInfo();


                                    // Again Getting Data
//        String name = binding.editTextTextPersonName.getText().toString();
//        String city = binding.editTextTextCity.getText().toString();
//        String email = binding.editTextTextEmailAddress.getText().toString();
//        String passward = binding.editTextTextPassword.getText().toString();

                                    long timestamp = System.currentTimeMillis();
                                    // get user id from firebase for DB
                                    String user = Fauth.getUid();
//        String userID = Fauth.getUid();

                                    HashMap<String,Object> hashMap = new HashMap<>();
                                    hashMap.put("userID",user);
                                    hashMap.put("Name",name);
                                    hashMap.put("City",city);
                                    hashMap.put("Email",email);
                                    hashMap.put("Passward",passward);
                                    hashMap.put("Proile Image ","");
                                    hashMap.put("timestamp",timestamp);
                                    hashMap.put("UserType","user");// will add manually from firbase to admin or user

                                    // set data to DB

                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                                    ref.child(user)
                                            .setValue(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    progressDialog.dismiss();
                                                    //Data Added...!
                                                    Toast.makeText(Register.this, "Account Creted Succusfully!", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(Register.this, DashboardUser.class));
                                                    finish();

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure( Exception e) {
                                            progressDialog.dismiss();
                                            //Data Added...
                                            Toast.makeText(Register.this, "Field to add Data!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure( Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(Register.this, "Error:"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });

    }

//    private void updateUserInfo() {
//        progressDialog.setMessage("Saving user Data...");
//
//
//        // Again Getting Data
////        String name = binding.editTextTextPersonName.getText().toString();
////        String city = binding.editTextTextCity.getText().toString();
////        String email = binding.editTextTextEmailAddress.getText().toString();
////        String passward = binding.editTextTextPassword.getText().toString();
//
//        long timestamp = System.currentTimeMillis();
//        // get user id from firebase for DB
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
////        String userID = Fauth.getUid();
//
//        HashMap<String,Object> hashMap = new HashMap<>();
//        hashMap.put("userID",user);
//        hashMap.put("Name",name);
//        hashMap.put("City",city);
//        hashMap.put("Email",email);
//        hashMap.put("Passward",passward);
//        hashMap.put("Proile Image ","");
//        hashMap.put("timestamp",timestamp);
//        hashMap.put("UserType","user");// will add manually from firbase to admin or user
//
//        // set data to DB
//
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl("https://golden-books-d3f16-default-rtdb.firebaseio.com/");
//        ref.child("user")
//                .setValue(hashMap)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        progressDialog.dismiss();
//                        //Data Added...!
//                        startActivity(new Intent(Register.this,DashboardUser.class));
//                        finish();
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure( Exception e) {
//                progressDialog.dismiss();
//                //Data Added...
//                Toast.makeText(Register.this, "Field to add Data!", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

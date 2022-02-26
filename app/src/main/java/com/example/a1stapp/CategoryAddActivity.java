package com.example.a1stapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.a1stapp.databinding.ActivityCategoryAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CategoryAddActivity extends AppCompatActivity {

    private ActivityCategoryAddBinding binding;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtnCategoryActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Category = binding.editTextEnterCategory.getText().toString().trim();

                if (TextUtils.isEmpty(Category)){
                    binding.editTextEnterCategory.setError("Please No Empty Category!");
                }else {
                    progressDialog.show();
                    //firebase Adding Data!

                    //get time stamp
                    long timestamp = System.currentTimeMillis();

                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("ID",""+timestamp);
                    hashMap.put("Category",""+Category);
                    hashMap.put("userID",""+firebaseAuth.getUid());

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
                    reference.child(""+timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(CategoryAddActivity.this, "Category Added Successfully!", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(CategoryAddActivity.this, "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        });
    }
}
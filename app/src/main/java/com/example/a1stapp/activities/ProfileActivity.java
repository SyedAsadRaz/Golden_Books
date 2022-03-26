package com.example.a1stapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.a1stapp.MyApplication;
import com.example.a1stapp.R;
import com.example.a1stapp.adapters.AdapterPdfFavorite;
import com.example.a1stapp.databinding.ActivityProfileBinding;
import com.example.a1stapp.models.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    //arraylist to hold the books
    private ArrayList<ModelPdf> pdfArrayList;
    //adapter to set in recyclerview
    private AdapterPdfFavorite adapterPdfFavorite;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private ProgressDialog progressDialog;

    private static final String TAG = "PROFILE_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        if (firebaseAuth.getCurrentUser() == null){
            Toast.makeText(this, "Login first to see your profile...", Toast.LENGTH_SHORT).show();
        }else {
            loadUserInfo();
            loadFavoriteBooks();
        }

        binding.editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() != null){
                    startActivity(new Intent(ProfileActivity.this,EditProfileActivity.class));
                }else {
                    Toast.makeText(ProfileActivity.this, "Login first to edit your profile", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //verify user if not...
        binding.accountStatusLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseUser.isEmailVerified()){
                    Toast.makeText(ProfileActivity.this, "Already verified...", Toast.LENGTH_SHORT).show();
                }
                else {
                    //not verified show confrmation dialog...
                    emailVerificationDialog();
                    
                }
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void emailVerificationDialog() {
        //ALert dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Verify Email")
                .setMessage("Are you sure you want to send email verification instructions to your email?")
                .setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendEmailVerification();
                    }
                })
                .show();
    }

    private void sendEmailVerification() {
        //show progress
        progressDialog.setMessage("Sending email verification instruction to your email...");
        progressDialog.show();

        firebaseUser.sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Done! Check Your Email : "+firebaseUser.getEmail(), Toast.LENGTH_LONG).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Fieled due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserInfo() {
        Log.d(TAG,"loadUser info..."+ firebaseAuth.getUid());

        //get email verification
        if (firebaseUser.isEmailVerified()){
            binding.accountStatusIv.setText("Verified");
        }else {
            binding.accountStatusIv.setText("Not verified");
        }

            DatabaseReference reference= FirebaseDatabase.getInstance().getReference("users");
            reference.child(firebaseAuth.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //get all info of user here from snapshot
                            String Email = ""+snapshot.child("Email").getValue();
                            String Name = ""+snapshot.child("Name").getValue();
                            String UserType = ""+snapshot.child("UserType").getValue();
                            String Proile_Image = ""+snapshot.child("ProileImage").getValue();
                            String userID = ""+snapshot.child("userID").getValue();
                            String timestamp = ""+snapshot.child("timestamp").getValue();

                            String formattedDate = MyApplication.formatTimestamp(timestamp);

                            //setData
                            binding.emailIv.setText(Email);
                            binding.nameIv.setText(Name);
                            binding.userTypeIv.setText(UserType);
                            binding.membershipIv.setText(formattedDate);

                            //set image using glide...
                            Glide.with(ProfileActivity.this)
                                    .load(Proile_Image)
                                    .placeholder(R.drawable.ic_baseline_person_24)
                                    .into(binding.profileIv);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });



    }

    private void loadFavoriteBooks() {
        //init list
        pdfArrayList = new ArrayList<>();

        //load davorite books from database
        //users > userUD > Favorites
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(firebaseAuth.getUid())
                .child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear before starting adding data
                        pdfArrayList.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){
                            //we will only get this book id hee and we get other deatils in adapter using that book id
                            String bookId= ""+ds.child("bookId").getValue();

                            //set id to model
                            ModelPdf modelPdf = new ModelPdf();
                            modelPdf.setId(bookId);

                            //add model to list
                            pdfArrayList.add(modelPdf);
                        }

                        //set number of fav books
                        binding.favoriteBookCount.setText(""+pdfArrayList.size());
                        //set adapter
                        adapterPdfFavorite = new AdapterPdfFavorite(ProfileActivity.this,pdfArrayList);
                        //set adapter to recyclerview
                        binding.favBooksView.setAdapter(adapterPdfFavorite);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
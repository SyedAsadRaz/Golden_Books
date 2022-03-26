package com.example.a1stapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.a1stapp.MyApplication;
import com.example.a1stapp.R;
import com.example.a1stapp.adapters.AdapterComment;
import com.example.a1stapp.adapters.AdapterPdfFavorite;
import com.example.a1stapp.databinding.ActivityPdfDetailsBinding;
import com.example.a1stapp.databinding.DialogueCommentAddBinding;
import com.example.a1stapp.models.ModelComment;
import com.example.a1stapp.models.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfDetailsActivity extends AppCompatActivity {

    private ActivityPdfDetailsBinding binding;

    //pdf id, get from intent
    String bookId , bookTitle , bookUrl;

    boolean isInMyFavorite = false;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    //arraylist to held comments
    private ArrayList<ModelComment> commentArrayList;
    //adapter to set recycler view
    private AdapterComment adapterComment;

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //get data from intent
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }

        //at Start hide Download Buttun, becuse we need book url that we will load later in function loadBookDetails()
        binding.downloadPdfBtn.setVisibility(View.GONE);

        loadBookDetails();
        loadComments();

        //increment book view count , whenever this page starts
        MyApplication.incrementBookViewCount(bookId);



        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(PdfDetailsActivity.this,PdfViewActivity.class);
                intent1.putExtra("bookId",bookId);
                startActivity(intent1);
            }
        });

        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Requirements User must be logged in to add comment
                if (firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(PdfDetailsActivity.this, "Log in first to add comment...", Toast.LENGTH_SHORT).show();

                }else {
                    addCommentDialog();
                }
            }
        });

        // Add/Remove Favrote
        binding.addToFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(PdfDetailsActivity.this, "You're not logged in", Toast.LENGTH_SHORT).show();

                }else {
                    if (isInMyFavorite){
                        //in fav, remove from fav show kro
                        MyApplication.removeFromFavorite(PdfDetailsActivity.this,bookId);
                    }else {
                        //not in fav so add kro fav
                        MyApplication.addToFavorite(PdfDetailsActivity.this,bookId);
                    }
                }
            }
        });

        //click download Buttn
        binding.downloadPdfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG_DOWNLOAD,"Checking Permission..");
                if (ContextCompat.checkSelfPermission(PdfDetailsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG_DOWNLOAD,"Permission Already Granted can download Book");
                    MyApplication.downloadBook(PdfDetailsActivity.this,""+bookId,""+bookTitle,""+bookUrl);

                }else {
                    Log.d(TAG_DOWNLOAD,"permission was not granted request permission...");
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });

}

    private void loadComments() {
        //init arraylist before adding data into it
        commentArrayList = new ArrayList<>();

        //DB path to load comments
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear before add arraylist data
                        commentArrayList.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){
                            //get data as model
                            ModelComment model = ds.getValue(ModelComment.class);
                            //add to arraylist
                            commentArrayList.add(model);
                        }
                        //setup adapter
                        adapterComment = new AdapterComment(PdfDetailsActivity.this,commentArrayList);
                        //set adapter to recycler view
                        binding.commentsRv.setAdapter(adapterComment);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String comment = "";

    private void addCommentDialog() {
        //inflate bind View for dialog
        DialogueCommentAddBinding commentAddBinding = DialogueCommentAddBinding.inflate(LayoutInflater.from(this));

        //setup alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());

        //create and show a;ert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //clik dismiss dialog
        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        //add comment
        commentAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get data
                comment = commentAddBinding.commentEt.getText().toString().trim();
                //validating data
                if (TextUtils.isEmpty(comment)){
                    Toast.makeText(PdfDetailsActivity.this, "Enter your comment...", Toast.LENGTH_SHORT).show();

                }else {
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });

    }

    private void addComment() {
        //show progress
        progressDialog.setMessage("Adding comment...");
        progressDialog.show();

        //timestamp for comment id, comment time
        String timestamp = ""+System.currentTimeMillis();

        //setup data to add in DB for comment
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("bookId",""+bookId);
        hashMap.put("comment",""+comment);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("uid",""+firebaseAuth.getUid());

        //DB path to Add data into it
        //Books > bookId > Comments > CommentID > commentData
        DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(PdfDetailsActivity.this, "Comment Added!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(PdfDetailsActivity.this, "Field to Add comment : "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    //request Storage permission
private ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(),isGranted ->{
            if (isGranted){
                Log.d(TAG_DOWNLOAD,"Permission Grantedd!");
                MyApplication.downloadBook(this, ""+bookId,""+bookTitle,""+bookUrl);
            }
            else {
                Log.d(TAG_DOWNLOAD,"Permission Denied..!");
                Toast.makeText(this, "Permission Denied..!", Toast.LENGTH_SHORT).show();
            }
        });

    private void loadBookDetails() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewCount = ""+snapshot.child("viewCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        bookUrl = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();

                        //required data is loaded show download button
                        binding.downloadPdfBtn.setVisibility(View.VISIBLE);

                        // format date
                        String date = MyApplication.formatTimestamp(String.valueOf(timestamp));

                        MyApplication.loadCategory(
                                ""+categoryId,
                                binding.categoryTv
                        );
                        MyApplication.loadPdfFromUrlSinglePage(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.pdfView,
                                binding.progressBar,binding.pagesTv
                        );
                        MyApplication.loadPdfSize(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.sizeTv
                        );

                        //set data
                        binding.titleTv.setText(bookTitle);
                        binding.descriptionTv.setText(description);
                        binding.views.setText(viewCount.replace("null","N/A"));
                        binding.dowloadsTv.setText(downloadsCount.replace("null","N/A"));
                        binding.dateTv.setText(date);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsFavorite(){
        //logged in chek if its in favorite list or not
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(firebaseAuth.getUid())
                .child("Favorites").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavorite = snapshot.exists();
                        if (isInMyFavorite){
                            //exists in fav
                            binding.addToFavBtn.setText("Remove from Favorites");

                        }else {
                            //not exixts in fav
                            binding.addToFavBtn.setText("Add to Favorites");


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
}}
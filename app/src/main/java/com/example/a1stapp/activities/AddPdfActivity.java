package com.example.a1stapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.a1stapp.databinding.ActivityAddPdfBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class AddPdfActivity extends AppCompatActivity {

    private ActivityAddPdfBinding binding;
    private FirebaseAuth firebaseAuth;
    //ArrayList to hold pdf categories
    private ArrayList<String> categoryClassTitleArrayList, categoryIdArrayList;
    private static  final int PDF_PICK_CODE = 1000;
    //URI of picked pdf
    private Uri pdfUri = null;
    //TAG for debugging
    private static final String TAG = "ADD_PDF_TAG";
    //Progress dialogue
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPdfBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        //setup progress dialogue
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtnAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.attachPdfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPdfIntent();
            }
        });

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialogue();

            }
        });

        binding.uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Validate Data
                validateData();
            }
        });
    }

    private String title= "", description= "";
    private void validateData() {
        //Step 1: Validate data
        Log.d(TAG,"validateData: Validating data!");


        //get data
        title = binding.titleBookEt.getText().toString().trim();
        description = binding.descriptionBookEt.getText().toString();

        //Validate Data
        if (TextUtils.isEmpty(title)){
            binding.titleBookEt.setError("Title can't be empty!");

        }else if (TextUtils.isEmpty(description)){
            binding.descriptionBookEt.setError("Description can'y be empty!");
        }else if (TextUtils.isEmpty(selectedCategoryTitle)){
            binding.categoryTv.setError("Pick Category!");
        }else if (pdfUri == null){
            Toast.makeText(this, "Pick PDF..!", Toast.LENGTH_SHORT).show();
        }
        else {
            //all data is valid, can Upload now...
            uploadPdfToStorage();
        }


    }

    private void uploadPdfToStorage() {
        //Upload PDFto Firebase
        Log.d(TAG,"uploadPdfToStorage: Uploading to storage");

        //Show Progress
        progressDialog.setMessage("Uploading pdf..");
        progressDialog.show();

        //timstamp
        long timestamp = System.currentTimeMillis();
        //path of pdf in firbase storage
        String filePathAndName = "Book/"+timestamp;
        //Storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG,"onSuccess: PDF uploaded to storage");
                        Log.d(TAG,"onSuccess: getting pdf uri..");

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl = ""+uriTask.getResult();

                        //Upload to firebase DB
                        uploadPdfInfoToDB(uploadedPdfUrl, timestamp);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG,"onFailure: PDF upload failed due to "+ e.getMessage());
                Toast.makeText(AddPdfActivity.this, "PDF upload field due to:"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void uploadPdfInfoToDB(String uploadedPdfUrl, long timestamp) {
        Log.d(TAG,"uploadInfoToDb: uploading PDF info to firebase DB...");

        progressDialog.setMessage("Uploading pdf info...");

        String uid = firebaseAuth.getUid();

        //Setup data to upload
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+title);
        hashMap.put("description",""+description);
        hashMap.put("categoryId",+selectedCategoryId);
        hashMap.put("url",""+uploadedPdfUrl);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("viewCount",0);
        hashMap.put("downloadsCount",0);

        //DB refrence \: DB > Books
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG,"onSuccess: Successfully uploaded!");
                        Toast.makeText(AddPdfActivity.this, "Successfully uploaded!", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG,"onFailure: field to upload book to db :"+e.getMessage());
                Toast.makeText(AddPdfActivity.this, "Field to upload to db due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPdfCategories() {
        Log.d(TAG,"loadPdfCategories: loading...");
        categoryClassTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        //DB reference to load categories data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryClassTitleArrayList.clear(); //Clear before adding data
                categoryIdArrayList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){

                    //get id and title of category
                    String categoryId = ""+ds.child("ID").getValue();
                    String categoryTitle = ""+ds.child("Category").getValue();

                    //add to respective  arraylists
                    categoryClassTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    //Selected category id and category title
    private  String  selectedCategoryTitle;
    private long selectedCategoryId;

    private void categoryPickDialogue() {
        Log.d(TAG,"categoryPickDialogue: showing pick dialogue");

        //get string array of categry from arraylist
        String[] categoriesArray = new String[categoryClassTitleArrayList.size()];
        for (int i = 0; i< categoryClassTitleArrayList.size(); i++){
            categoriesArray[i] = categoryClassTitleArrayList.get(i);
        }

        //Alert DIalogue
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item click
                        //get clicked item from list
                        selectedCategoryTitle = categoryClassTitleArrayList.get(which);
                        selectedCategoryId = Long.parseLong(categoryIdArrayList.get(which));
                        //set to category textView
                        binding.categoryTv.setText(selectedCategoryTitle);

                        Log.d(TAG,"onClicked: Select Category"+selectedCategoryId+" "+selectedCategoryTitle);

                    }
                })
                .show();

    }


    private void pickPdfIntent() {
        Log.d(TAG,"pickPdfIntent: Starting Pick Pdf Intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select PDF"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == PDF_PICK_CODE){
                Log.d(TAG,"onActivityResult : PDF Picked");
                Toast.makeText(this, "PDF Picked!", Toast.LENGTH_SHORT).show();
                pdfUri = data.getData();
                Log.d(TAG,"onActivityResult: URI"+pdfUri);
                
            }
        }else {
            Log.d(TAG,"onActivityResult: Canceld Picked PDF");
            Toast.makeText(this, "Picked PDF Cancelled!", Toast.LENGTH_SHORT).show();
        }
    }
}
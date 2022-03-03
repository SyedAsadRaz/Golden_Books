package com.example.a1stapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.a1stapp.databinding.ActivityEditPdfBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class EditPdfActivity extends AppCompatActivity {

    private ActivityEditPdfBinding binding;
    private ProgressDialog progressDialog;

    //book id get from intent started fro AdapterPdfAdmin
    private String bookId;

    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList ;

    private static final String TAG = "BOOK_EDIT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditPdfBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookId = getIntent().getStringExtra("bookId");

        //setup progress dialogue
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);


        loadCategories();
        loadBookInfo();

        //handle click pick category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialog();
            }
        });

        binding.backBtnEditBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click begin upload
        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });


    }

    private String title="",description = "";
    private void validateData() {
        //get data
        title = binding.titleEditBookEt.getText().toString().trim();
        description = binding.descriptionEditBookEt.getText().toString().trim();

        //Validating data
        if (TextUtils.isEmpty(title)){
            binding.titleEditBookEt.setError("Title can't be empty");
        }
        else if (TextUtils.isEmpty(description)){
            binding.titleEditBookEt.setError("fill the description");
        }else if (TextUtils.isEmpty(selectedCategoryId)){
            Toast.makeText(this, "Pick Category Please!", Toast.LENGTH_SHORT).show();
        }
        else {
            updatePdf();
        }
    }

    private void updatePdf() {
        Log.d(TAG,"updatePDF: Starting updateing pdf info to DB...");

        //show progress
        progressDialog.setMessage("Updating book info...");
        progressDialog.show();

        //setup data to update tp db
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("title",""+title);
        hashMap.put("description",""+description);
        hashMap.put("categoryId",""+selectedCategoryId);

        //start updating
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG,"onSuccess: Book Updated...");
                        progressDialog.dismiss();
                        Toast.makeText(EditPdfActivity.this, "Book Info Updated!", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"onFailure: Failed to update due to "+e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(EditPdfActivity.this, "error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBookInfo() {
        Log.d(TAG,"loadBookInfo: Loading book Info..");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        selectedCategoryId = ""+snapshot.child("categoryId").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String title = ""+snapshot.child("title").getValue();
                        //set to Views
                        binding.titleEditBookEt.setText(title);
                        binding.descriptionEditBookEt.setText(description);

                        Log.d(TAG,"onDatachange: Loading Book Category Info");

                        DatabaseReference reference1= FirebaseDatabase.getInstance().getReference("Categories");
                        reference1.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //get category
                                        String category = ""+snapshot.child("Category").getValue();
                                        //set to catory text View
                                        binding.categoryTv.setText(category);
                                    }


                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String selectedCategoryId= "", selectedCategoryTitle = "";

    private void categoryDialog(){
        //make String array from arraylist of string
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i<categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        //Alert dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Category!")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCategoryId = categoryIdArrayList.get(which);
                        selectedCategoryTitle = categoryTitleArrayList.get(which);

                        //set to textView
                        binding.categoryTv.setText(selectedCategoryTitle);
                    }
                })
                .show();
    }
    private void loadCategories() {
        Log.d(TAG,"loadCategories: Loading categories...");
        categoryIdArrayList = new ArrayList<>();
        categoryTitleArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArrayList.clear();
                categoryTitleArrayList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    String id = ""+ds.child("ID").getValue();
                    String category = ""+ds.child("Category").getValue();
                    categoryIdArrayList.add(id);
                    categoryTitleArrayList.add(category);

                    Log.d(TAG,"onDatachange: ID: "+id);
                    Log.d(TAG,"onDatachange: Category: "+category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
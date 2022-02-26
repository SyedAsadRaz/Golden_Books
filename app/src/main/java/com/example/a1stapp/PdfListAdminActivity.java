package com.example.a1stapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import com.example.a1stapp.adapters.AdapterPdfAdmin;
import com.example.a1stapp.databinding.ActivityAddPdfBinding;
import com.example.a1stapp.databinding.ActivityPdfListAdminBinding;
import com.example.a1stapp.models.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class PdfListAdminActivity extends AppCompatActivity {

    //Viewbindng
    private ActivityPdfListAdminBinding binding;
    //ArrayList to hold list of data of type ModelPdf
    private ArrayList<ModelPdf> pdfArrayList;
    //adapter
    private AdapterPdfAdmin adapterPdfAdmin;

    private String categoryId, categoryTitle;
    private static final String TAG = "PDF_LIST_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data from intent
        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle = intent.getStringExtra("categoryTitle");

        //set pdf category
        binding.categoryTvLayoutBooksList.setText(categoryTitle);
        loadPdfList();
        //init list before adding data
        pdfArrayList = new ArrayList<>();
        adapterPdfAdmin = new AdapterPdfAdmin(PdfListAdminActivity.this, pdfArrayList);
        binding.bookView.setAdapter(adapterPdfAdmin);

        //Search...
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //search as and when user type each letter
                try {
                    adapterPdfAdmin.getFilter().filter(s);

                }catch (Exception e){
                    Log.d(TAG,"onTextChanged: "+e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Back btn
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadPdfList() {

        FirebaseDatabase.getInstance().getReference("Books")
//                .equalTo(categoryId,"categoryId")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            //get data
                            ModelPdf model = ds.getValue(ModelPdf.class);
                            //add to list
                            if (categoryId.equals(model.getCategoryId()))
                            pdfArrayList.add(model);
                            Log.d(TAG,"onDataChange: "+ model.getId()+" "+model.getTitle());

                        }
                        //setup adapter
                        adapterPdfAdmin.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
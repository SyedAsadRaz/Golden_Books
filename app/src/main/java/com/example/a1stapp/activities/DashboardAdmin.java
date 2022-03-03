package com.example.a1stapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.example.a1stapp.adapters.AdopterCategory;
import com.example.a1stapp.databinding.ActivityDashboardAdminBinding;
import com.example.a1stapp.models.ModelCategoryClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdmin extends AppCompatActivity {

    private ActivityDashboardAdminBinding binding;
    private FirebaseAuth Fauth;
    //Array List to STore categories
    private ArrayList<ModelCategoryClass> categoryClassArrayList;
    //Adapterclass
    private AdopterCategory adopterCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Fauth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // add user email to title bar of dashboard
        String email = firebaseUser.getEmail();
        binding.gmailShowOnDashboard.setText(email);

        loadCategories();

        //Search Bar
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // search as user type a single word...
                try {
                    adopterCategory.getFilter().filter(s);

                }catch (Exception e){

                }



            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fauth.signOut();
                startActivity(new Intent(DashboardAdmin.this, MainActivity.class));
                finish();

            }
        });

        //handle add pdf button
        binding.pdfClik.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdmin.this, AddPdfActivity.class));
            }
        });
        binding.addCategoryMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdmin.this, CategoryAddActivity.class));

            }
        });
    }

    private void loadCategories() {
        //init Arrylist
        categoryClassArrayList = new ArrayList<>();
        //Get All Categories from Direbasedatabase > Categories

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // clear array list before Add somthing
                categoryClassArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelCategoryClass modelCategoryClass = ds.getValue(ModelCategoryClass.class);

                    //add to array list
                    categoryClassArrayList.add(modelCategoryClass);
                }
                //setup adapter
                adopterCategory = new AdopterCategory(DashboardAdmin.this,categoryClassArrayList);

                //set Adapter to resycle View
                binding.categoryView.setAdapter(adopterCategory);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
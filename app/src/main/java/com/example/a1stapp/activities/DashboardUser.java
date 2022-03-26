package com.example.a1stapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.a1stapp.BooksUserFragment;
import com.example.a1stapp.R;
import com.example.a1stapp.databinding.ActivityDashboardUserBinding;
import com.example.a1stapp.models.ModelCategoryClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardUser extends AppCompatActivity {

    //to show in tabs
    public ArrayList<ModelCategoryClass> categoryClassArrayList;
    public ViewPagerAdapter viewPagerAdapter;

    private ActivityDashboardUserBinding binding;
    private FirebaseAuth Fauth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Fauth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (Fauth.getCurrentUser() != null){
            loadProfileImage();
        }


        checkUser();
        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);


        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fauth.signOut();
                startActivity( new Intent(DashboardUser.this, MainActivity.class));
                finish();

            }
        });

        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardUser.this,ProfileActivity.class));
            }
        });
    }

    private void checkUser() {
        FirebaseUser firebaseUser = Fauth.getCurrentUser();
        if (firebaseUser == null){
            //user not logged in
            binding.gmailShowOnDashboard.setText("Guest mode");
        }else {
            // add user email to title bar of dashboard
            String email = firebaseUser.getEmail();
            binding.gmailShowOnDashboard.setText(email);
        }
    }

    private void setupViewPagerAdapter(ViewPager viewPager){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this);

        categoryClassArrayList = new ArrayList<>();

        //load categories from firebase
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear before adding to list
                categoryClassArrayList.clear();

                //load categories like all most virews =d etc..
                //Add data to model
                ModelCategoryClass modelALl = new ModelCategoryClass("01","All","",1);
                ModelCategoryClass modelMostViewed = new ModelCategoryClass("02","Most Viewed","",1);
                ModelCategoryClass modelMostDownloaded = new ModelCategoryClass("03","Most Downloaded","",1);
                //add model to list
                categoryClassArrayList.add(modelALl);
                categoryClassArrayList.add(modelMostViewed);
                categoryClassArrayList.add(modelMostDownloaded);
                //add data to view pager adapter
                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        ""+modelALl.getID(),
                        ""+modelALl.getCategory(),
                        ""+modelALl.getUserID()
                ), modelALl.getCategory());
                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        ""+modelMostViewed.getID(),
                        ""+modelMostViewed.getCategory(),
                        ""+modelMostViewed.getUserID()
                ), modelMostViewed.getCategory());
                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        ""+modelMostDownloaded.getID(),
                        ""+modelMostDownloaded.getCategory(),
                        ""+modelMostDownloaded.getUserID()
                ), modelMostDownloaded.getCategory());
                //refresh list
                viewPagerAdapter.notifyDataSetChanged();

                //Now Load from firebase
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelCategoryClass model = ds.getValue(ModelCategoryClass.class);
                    //add data to list
                    categoryClassArrayList.add(model);
                            //add data to ViewPagerAdapter
                    viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                            ""+model.getID(),
                            ""+model.getCategory(),
                            ""+model.getUserID()), model.getCategory());
                    //refresh list
                    viewPagerAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //set adapter to view pager
        viewPager.setAdapter(viewPagerAdapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter{

        private ArrayList<BooksUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter( FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
        private void addFragment(BooksUserFragment fragment, String title){
            //add fragment passed as parameter in fragmanlist
            fragmentList.add(fragment);
            //add title passed as parameter in fragmanlist
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    private void loadProfileImage() {
        //get data from firebase
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(Fauth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get profile image using snapshot...
                        String Proile_Image = ""+snapshot.child("ProileImage").getValue();

                        //set image using glide...
                        Glide.with(DashboardUser.this)
                                .load(Proile_Image)
                                .placeholder(R.drawable.ic_baseline_person_24)
                                .into(binding.profileBtn);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }
}
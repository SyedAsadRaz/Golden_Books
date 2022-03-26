package com.example.a1stapp.activities;

import static com.example.a1stapp.Constants.PICK_IMAGE;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.a1stapp.MyApplication;
import com.example.a1stapp.R;
import com.example.a1stapp.databinding.ActivityEditPdfBinding;
import com.example.a1stapp.databinding.ActivityEditProfileBinding;
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.PrimitiveIterator;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private static final String TAG = "PROFILE_EDIT_TAG";

    private Uri imageUrl = null;

    private String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Pease wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadUserInfo();

        binding.profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageAttachMenu();
            }
        });

        //clik update prof
        binding.updateBtnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateData();
            }
        });

    }

    private void ValidateData() {
        //get data
        name = binding.editnameET.getText().toString().trim();

        //validaet data
        if (TextUtils.isEmpty(name)){
            Toast.makeText(this, "Enter Name...", Toast.LENGTH_SHORT).show();
        }
        else {
            if (imageUrl == null){
                //need to update without image
                updateProfile("");
            }else {
                //need to update with image
                uploadImage();

            }
        }
    }

    private void uploadImage() {
        Log.d(TAG,"Uploading Profile image...");

        progressDialog.setMessage("updating profile image");
        progressDialog.show();

        //image path and name , use uid to replace previous
        String filePathAndName = "ProfileImage/"+firebaseAuth.getUid();

        //Storage reference
        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
        reference.putFile(imageUrl)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG,"Profile image uploaded");
                        Log.d(TAG,"getting url of uploaded image");
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedImageUrl = ""+uriTask.getResult();

                        Log.d(TAG,"Uploaded image URL: "+uploadedImageUrl);

                        updateProfile(uploadedImageUrl);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"Field to upload image due to "+e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(EditProfileActivity.this, "Field to upload image due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile(String s) {
        Log.d(TAG,"Updating user Profile");
        progressDialog.setMessage("Updating user profile...");
        progressDialog.show();

        //setup date to update in do
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("Name",""+name);
        if (imageUrl != null){
            hashMap.put("ProileImage",""+imageUrl);
        }

        //update data to DB
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Profile updated Successfully!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(EditProfileActivity.this, "Field to update DB due to: "+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void showImageAttachMenu() {
        //init setup popup menu
        PopupMenu popupMenu = new PopupMenu(this,binding.profileIv);
        popupMenu.getMenu().add(Menu.NONE,0,0,"Camera");
        popupMenu.getMenu().add(Menu.NONE,1,1,"Gallery");

        popupMenu.show();

        //menu item clicked
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //get id of item cliked
                int which = item.getItemId();
                if (which == 0){
                    //Camera clicked
                    pickImageCamera();
                }
                else {
                    //galerry clicked
                    pickImageGallery();

                }

                return false;
            }
        });
    }
    private void pickImageGallery() {
//        intent to pick Image from Gallery
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType(":image/*");
//        galleryActivityResultLauncher.launch(intent);
//




        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
                Log.d(TAG,"onActivityResult: "+imageUrl);
                imageUrl = data.getData();
                binding.profileIv.setImageURI(imageUrl);
            }else {
                Toast.makeText(EditProfileActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();

            }
    }


    private void pickImageCamera() {
        //intent to pick image from camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"New Pick"); //image title
        values.put(MediaStore.Images.Media.DESCRIPTION,"Sample Image Description");
        imageUrl = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUrl);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //used to handle result of caera intent
                    //get url of image
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG,"onActivityResult: "+imageUrl);
                        Intent data= result.getData(); //no need here as in camera we already have image in imageRl var
                        binding.profileIv.setImageURI(imageUrl);
                    }else {
                        Toast.makeText(EditProfileActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();

                    }
                }
            });
    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //used to handle result of gallery intent
                    //get url of image
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG,"onActivityResult: "+imageUrl);
                        Intent data= result.getData();
                        imageUrl = data.getData();
                        binding.profileIv.setImageURI(imageUrl);
                    }else {
                        Toast.makeText(EditProfileActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();

                    }
                }
            });




    private void loadUserInfo() {
        Log.d(TAG,"loadUser info..."+ firebaseAuth.getUid());

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get all info of user here from snapshot
                        String Name = ""+snapshot.child("Name").getValue();
                        String Proile_Image = ""+snapshot.child("ProileImage").getValue();

                        //setData
                        binding.editnameET.setText(Name);


                        //set image using glide...
                        Glide.with(EditProfileActivity.this)
                                .load(Proile_Image)
                                .placeholder(R.drawable.ic_baseline_person_24)
                                .into(binding.profileIv);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}
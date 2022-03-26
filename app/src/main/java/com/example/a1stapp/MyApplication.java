package com.example.a1stapp;

import static com.example.a1stapp.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

//application close runs before your launcher activity
public class MyApplication extends Application {

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //created a static method to convert timestamp to proper date format so we can use it everywhere in project no repeating..
    public static final String formatTimestamp(String timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        //format timsestamp to dd/mm/yyyy
        String date = DateFormat.format("dd/mm/yyyy",cal).toString();

        return date;
    }

    public static void deleteBook(Context context,String bookId,String bookUrl,String bookTitle) {

        String TAG = "DELETE_BOOK_TAG";
        Log.d(TAG,"deleteBook: Deleting...");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Deleting "+bookTitle +" ...");
        progressDialog.show();

        Log.d(TAG,"deletingBook: Deleting from Storage...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("/Book");
        storageReference.child(bookId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG,"onSuccess: Deleted from Storage!");
                        Log.d(TAG,"onSuccess: Deleted info from DB!");

                        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG,"onSUccess: Deleted from DB too..");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Book Deleted Successfully!", Toast.LENGTH_SHORT).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG,"onFailure: Field to delte from DB due to "+e.getMessage());
                                progressDialog.dismiss();
                                Toast.makeText(context, "error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"onFailure: field to Delete due to :"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTv) {
        String TAG = "PDF_SIZE_TAG;";
        //using url we can get file and it's metadata from firebase storage


        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        //get size in bytes...
                        double bytes = storageMetadata.getSizeBytes();
                        Log.d(TAG,"onSuccess: "+pdfTitle +" "+bytes);

                        // Converts bytes to KB,MB
                        double kb = bytes/1024;
                        double mb = kb/1024;

                        if (mb >= 1){
                            sizeTv.setText(String.format("%.2f",mb)+"MB");
                        }else if (kb >= 1){
                            sizeTv.setText(String.format("%.2f",kb)+"KB");
                        }else {
                            sizeTv.setText(String.format(("%.2f"),bytes)+"bytes");
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed getting metadata
                Log.d(TAG,"FIeld getting metadata: "+e.getMessage());
            }
        });

    }

    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar, TextView pagesTv) {
        //using url we can get file and its metadata from firebase storag
        String TAG = "PDF_OAD_SINGLE_TAG";

        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG,"onSuccess: "+pdfTitle +" Success got this file");


                        //set to pdfView
                        pdfView.fromBytes(bytes)
                                .pages(0) //show only first page
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        //hide progress
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG,"onError: "+t.getMessage());
                                    }
                                }).onPageError(new OnPageErrorListener() {
                            @Override
                            public void onPageError(int page, Throwable t) {
                                //hide progress
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG,"onPageError: "+t.getMessage());
                            }
                        })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        //pdf loaded
                                        //hide progress
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG,"loadComplete: pdf loaded! ");

                                        //if pages param is not null than set page numbers
                                        if(pagesTv != null){
                                            pagesTv.setText(""+nbPages); //concatnate with double quotes becuase cant set int in textview
                                        }
                                    }
                                })
                                .load();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"onFailure: Failed getting file from url due to "+e.getMessage());


            }
        });

    }

    public static void loadCategory(String categoryId,TextView categoryTv) {
        //get category using categoryId


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String Category = ""+snapshot.child("Category").getValue();

                        //set to Category text View
                        categoryTv.setText(Category);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public static void incrementBookViewCount(String bookId){
        //get book views count
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get views count
                        String viewsCount = ""+snapshot.child("viewCount").getValue();
                        //in case of null replace with 0
                        if (viewsCount.equals("") || viewsCount.equals("null")){
                            viewsCount = "0";
                        }

                        //view count increment
                        long newViewsCount = Long.parseLong(viewsCount) +1;
                        HashMap<String ,Object> hashMap = new HashMap<>();
                        hashMap.put("viewCount",newViewsCount);

                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Books");
                        reference1.child(bookId)
                                .updateChildren(hashMap);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void downloadBook(Context context,String bookId,String bookTitle,String bookUrl){
        Log.d(TAG_DOWNLOAD,"downloadinBook: downloading Book...");
        String nameWithExtendsion = bookTitle +".pdf";
        Log.d(TAG_DOWNLOAD,"downloadBook: NAME: "+nameWithExtendsion);

        //progress dialogues
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Downloading "+ nameWithExtendsion+ "...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //download from firebase storage using url
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG_DOWNLOAD,"Book Download");
                        Log.d(TAG_DOWNLOAD,"Saving Book...");
                        saveDownloadedBook(context,progressDialog,bytes,nameWithExtendsion,bookId);



                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG_DOWNLOAD,"Field to Download due to :"+e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(context, "Field to Download : "+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private static void saveDownloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtendsion, String bookId) {
        Log.d(TAG_DOWNLOAD,"SavingDownloading bOOK...");

        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdir();

            String filePath = downloadsFolder.getPath()+"/" + nameWithExtendsion;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Log.d(TAG_DOWNLOAD,"Saved to Downloads Folder...!");
            Toast.makeText(context, "Saved to Download Folder", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();

            incrementBookDownloadCount(bookId);

        }catch (Exception e){
            Log.d(TAG_DOWNLOAD,"Field to saving to downloads folder error: "+e.getMessage());
            Toast.makeText(context, "Field to saving to downloads folder error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();

        }
    }

    private static void incrementBookDownloadCount(String bookId) {
        Log.d(TAG_DOWNLOAD,"increment book Download count...");

        //get previous download Counts..
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        Log.d(TAG_DOWNLOAD,"Downloads Count: "+downloadsCount);

                        if (downloadsCount.equals("") || downloadsCount.equals("null")){
                            downloadsCount = "0";
                        }

                        //convert to long amd increament of 1
                        long newDownloadsCount = Long.parseLong(downloadsCount)+1 ;
                        Log.d(TAG_DOWNLOAD,"New Download count "+newDownloadsCount);

                        //setup data to update
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("downloadsCount",newDownloadsCount);

                        //upate new increament downloads count to DB
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Books");
                        reference1.child(bookId).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG_DOWNLOAD,"Downloads Count Updated...!");

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG_DOWNLOAD,"field to update Downloads Count...!");

                                    }
                                });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void addToFavorite(Context context,String bookId){
        //we can add onli if user is logged in
        //check if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            //not logged in
            Toast.makeText(context, "Log in First to use this...", Toast.LENGTH_SHORT).show();
        }else {
            long timestamp = System.currentTimeMillis();

            //setup data to add in firebase DB of current user for favorite book
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("bookId",bookId);
            hashMap.put("timestamp",timestamp);

            //Save to DB
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
            ref.child(firebaseAuth.getUid())
                    .child("Favorites").child(bookId).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Added to Your Favorites List", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Field to Add Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    public static void removeFromFavorite(Context context,String bookId){
        //we can Remove if user is logged in
        //check if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            //not logged in
            Toast.makeText(context, "Log in First to use this...", Toast.LENGTH_SHORT).show();
        }else {

            //Remove from DB
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid())
                    .child("Favorites").child(bookId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Removed from Your Favorites List", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Field Remove from Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
}}

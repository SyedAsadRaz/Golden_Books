package com.example.a1stapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.a1stapp.Constants;
import com.example.a1stapp.databinding.ActivityPdfViewBinding;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PdfViewActivity extends AppCompatActivity {

    private ActivityPdfViewBinding binding;
    private String bookId;
    private static final String TAG = "PDF_VIEW_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get bookId from intent
    Intent intent = getIntent();
    bookId = intent.getStringExtra("bookId");
    Log.d(TAG,"onCreate: bookId");

    loadBook();

    binding.backBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    });

    }

    private void loadBook() {
        Log.d(TAG,"loadBook: Get Pdf URL from url...");
        //Database Reference to get book details e.g get book url using book ID
        //get book url using bookId
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get book url..
                        String pdfUrl = ""+snapshot.child("url").getValue();
                        Log.d(TAG,"onDataChange: PDF URL"+pdfUrl);
                        //Load pdf using url from firebase Storage
                        loadBookFromUrl(pdfUrl);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBookFromUrl(String pdfUrl) {
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        //load pdf using bytes
                        binding.pdfViewInPdfViewActivity.fromBytes(bytes)
                                .swipeHorizontal(false)// set false to scroll vertical ,set true to swipe horizonal
                        .onPageChange(new OnPageChangeListener() {
                            @Override
                            public void onPageChanged(int page, int pageCount) {
                                //set current and total pages in toolbar subtitles
                                int currentPage = (page + 1); //db + 1 becuase page starts from 0
                                binding.bookTitleTv.setText(currentPage + "/" + pageCount);

                            }
                        })
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        Toast.makeText(PdfViewActivity.this, "error: "+t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }).onPageError(new OnPageErrorListener() {
                            @Override
                            public void onPageError(int page, Throwable t) {
                                Toast.makeText(PdfViewActivity.this, "erro: "+t.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        })
                                .load();
                        binding.progressCircular.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed to load book
                Toast.makeText(PdfViewActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.progressCircular.setVisibility(View.GONE);
            }
        });
    }
}
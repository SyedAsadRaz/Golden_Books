package com.example.a1stapp.adapters;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a1stapp.MyApplication;
import com.example.a1stapp.activities.PdfDetailsActivity;
import com.example.a1stapp.databinding.RowFavBooksBinding;
import com.example.a1stapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterPdfFavorite extends RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorites> {

    private Context context;
    private ArrayList<ModelPdf> pdfArrayList;
    //View binding fro row_pdf_favorites.xml
    private RowFavBooksBinding binding;
    private static final String TAG = "FAV_BOOK_TAG";

    //constructor
    public AdapterPdfFavorite(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfFavorites onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //binding inflate rowpdffav
        binding = RowFavBooksBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderPdfFavorites(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfFavorites holder, int position) {
        // get data set data handle cliks waghira
        ModelPdf model= pdfArrayList.get(position);

        loadDetails(model,holder);

        //handle clik on item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailsActivity.class);
                intent.putExtra("bookId",model.getId());
                context.startActivity(intent);
            }
        });

        //handle clik remove from fav
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.removeFromFavorite(context,model.getId());
            }
        });
    }

    private void loadDetails(ModelPdf model, HolderPdfFavorites holder) {
        String bookId = model.getId();
        Log.d(TAG,"Book Details of Book ID: "+bookId);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get book info
                        String bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String bookUrl = ""+snapshot.child("url").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String viewCount = ""+snapshot.child("viewCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();

                        //set to model
                        model.setFavorite(true);
                        model.setTitle(bookTitle);
                        model.setDescription(description);
                        model.setTimestamp(timestamp);
                        model.setTimestamp(categoryId);
                        model.setTimestamp(bookUrl);
                        model.setTimestamp(uid);

                        //formate date
                        String date = MyApplication.formatTimestamp(timestamp);

                        MyApplication.loadCategory(categoryId,holder.categoryTv);
                        MyApplication.loadPdfFromUrlSinglePage(""+bookUrl, ""+bookTitle, holder.pdfView, holder.progressBar, null);
                        MyApplication.loadPdfSize(""+bookUrl, ""+bookTitle, holder.sizeTv);

                        //set data to views
                        holder.titleTv.setText(bookTitle);
                        holder.descriptionTv.setText(description);
                        holder.dateTv.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size(); //returns list size \\ number of records
    }

    //View Holder class
    class HolderPdfFavorites extends RecyclerView.ViewHolder{

        PDFView pdfView;
        ProgressBar progressBar;
        ImageButton favBtn;
        TextView titleTv,descriptionTv,categoryTv,sizeTv,dateTv;

        public HolderPdfFavorites(@NonNull View itemView) {
            super(itemView);

            //init ui views of row pdf fav xml
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            favBtn = binding.favBtn;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
        }
    }
}

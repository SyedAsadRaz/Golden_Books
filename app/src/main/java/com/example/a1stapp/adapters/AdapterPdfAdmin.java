package com.example.a1stapp.adapters;

import static com.example.a1stapp.Constants.MAX_BYTES_PDF;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a1stapp.MyApplication;
import com.example.a1stapp.databinding.RowPdfAdminBinding;
import com.example.a1stapp.filters.FilterPdfAdmin;
import com.example.a1stapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.time.temporal.Temporal;
import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {
    //Context
    private Context context;
    //arraylist to hold list of data of type model pdf

    //* View Holder CLass for row pdf admin.xml
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    //View Bindng
    private RowPdfAdminBinding binding;

    private FilterPdfAdmin filter;

    //TAG creating
    public static final String TAG = "PDF_ADAPTER_TAG";

    //Constructor
    public AdapterPdfAdmin(Context context,ArrayList<ModelPdf> pdfArrayList){
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //binding Layouts using Biew binding
        binding= RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        /*Get data set dta a handle clicks etc*/

        ModelPdf model = pdfArrayList.get(position);
        String title = model.getTitle();
        String description = model.getDescription();
        long timestamp = model.getTimestamp();

        /* We need to convert timestamp format to ddmmyyy format*/
        String formattedDate = MyApplication.formatTimestamp(timestamp);

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        //load further detals like categry , pdf from URL, pdf size in separate functions
        loadCategory(model,holder);
        loadPdfFromUrl(model,holder);
        loadPdfSize(model,holder);
    }

    private void loadPdfSize(ModelPdf model, HolderPdfAdmin holder) {
        //using url we can get file and it's metadata from firebase storage

        String pdfUrl = model.getUrl();
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        //get size in bytes...
                        double bytes = storageMetadata.getSizeBytes();
                        Log.d(TAG,"onSuccess: "+model.getTitle()+" "+bytes);

                        // Converts bytes to KB,MB
                        double kb = bytes/1024;
                        double mb = kb/1024;

                        if (mb >= 1){
                            holder.sizeTv.setText(String.format("%.2f",mb)+"MB");
                        }else if (kb >= 1){
                            holder.sizeTv.setText(String.format("%.2f",kb)+"KB");
                        }else {
                            holder.sizeTv.setText(String.format(("%.2f"),bytes)+"bytes");
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed getting metadata
                Toast.makeText(context, "Field :"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadPdfFromUrl(ModelPdf model, HolderPdfAdmin holder) {
        //using url we can get file and its metadata from firebase storag

        String pdfUrl = model.getUrl();
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG,"onSuccess: "+model.getTitle()+" Success got this file");


                        //set to pdfView
                        holder.pdfView.fromBytes(bytes)
                                .pages(0) //show only first page
                        .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        //hide progress
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG,"onError: "+t.getMessage());
                                    }
                                }).onPageError(new OnPageErrorListener() {
                            @Override
                            public void onPageError(int page, Throwable t) {
                                //hide progress
                                holder.progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG,"onPageError: "+t.getMessage());
                            }
                        })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        //pdf loaded
                                        //hide progress
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG,"loadComplete: pdf loaded! ");
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

    private void loadCategory(ModelPdf model, HolderPdfAdmin holder) {
        //get category using categoryId

        String categoryId = model.getCategoryId();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String Category = ""+snapshot.child("Category").getValue();

                        //set to Category text View
                        holder.categoryTv.setText(Category);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size(); //return number of records list size
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfAdmin(filterList,this);

        }
        return filter;
    }

    //constructor

    class HolderPdfAdmin extends RecyclerView.ViewHolder{

        //UI View of row_pdf_admin.xml
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv,descriptionTv,categoryTv,sizeTv,dateTv;
        ImageButton menuBtn;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

            //init Ui View
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            menuBtn = binding.menuBtn;



        }
    }

}

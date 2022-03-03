package com.example.a1stapp.adapters;

import static com.example.a1stapp.Constants.MAX_BYTES_PDF;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.example.a1stapp.activities.EditPdfActivity;
import com.example.a1stapp.MyApplication;
import com.example.a1stapp.activities.PdfDetailsActivity;
import com.example.a1stapp.activities.PdfViewActivity;
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

    //progress
    private ProgressDialog progressDialog;

    //Constructor
    public AdapterPdfAdmin(Context context,ArrayList<ModelPdf> pdfArrayList){
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;

        //initiate progress dialogue
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
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
        String pdfUrl = model.getUrl();
        String pdfId = model.getId();
        String categoryId = model.getCategoryId();
        long timestamp = model.getTimestamp();

        /* We need to convert timestamp format to ddmmyyy format*/
        String formattedDate = MyApplication.formatTimestamp(timestamp);

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);


        //load further detals like categry , pdf from URL, pdf size in separate functions
        MyApplication.loadCategory(""+categoryId,holder.categoryTv);
        MyApplication.loadPdfFromUrlSinglePage(""+pdfUrl,""+title,holder.pdfView,holder.progressBar);

        MyApplication.loadPdfSize(""+pdfUrl,""+title, holder.sizeTv );

        //handle menuBtn clik setup edit and delete pdfs books
        binding.menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialogue(model,holder);
            }
        });

        //klik on pdf and show detail activity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailsActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });


    }

    private void showOptionsDialogue(ModelPdf model, HolderPdfAdmin holder) {
        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();

        //options to show in dialogue
        String[] options = {"Edit","Delete"};

        //alert Dialogue
        AlertDialog.Builder builder =new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if ((which == 0)){
                            //Edit clicked Open New Activity To edit the book info
                            Intent intent = new Intent(context, EditPdfActivity.class);
                            intent.putExtra("bookId",bookId);
                            context.startActivity(intent);

                        }else if (which==1){
                            //Delete klik
                            MyApplication.deleteBook(context,""+bookId,""+bookUrl,""+bookTitle);

                            
                        }
                    }
                })
                .show();
    }





    //cut delete book fun to myapplication class

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

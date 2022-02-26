package com.example.a1stapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a1stapp.PdfListAdminActivity;
import com.example.a1stapp.filters.FilterCategory;
import com.example.a1stapp.models.ModelCategoryClass;
import com.example.a1stapp.databinding.RowCategoryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdopterCategory extends RecyclerView.Adapter<AdopterCategory.HolderClass> implements Filterable {

    //init View binding
    //
    private RowCategoryBinding binding;
    private Context context;
    public ArrayList<ModelCategoryClass> categoryClassArrayList,filterList;

    //Instance of our filterClass
    private FilterCategory filter;


    public AdopterCategory(Context context, ArrayList<ModelCategoryClass> categoryClassArrayList) {
        this.context = context;
        this.categoryClassArrayList = categoryClassArrayList;
        this.filterList = categoryClassArrayList;
    }

    @NonNull
    @Override
    public HolderClass onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //binding row category.xml
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderClass(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderClass holder, int position) {
        // Get Data...
        ModelCategoryClass model = categoryClassArrayList.get(position);
        String ID = model.getID();
        String Category = model.getCategory();
        String userID = model.getUserID();
        long timstamp = model.getTimstamp();

        //Set Data now..
        holder.categoryTv.setText(Category);

        //Delete Btun set to delete Categories
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Confirm Delete Dialogue...
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete").setMessage("Are You Sure You want to delete this Category?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show();
                                deleteCategory(model,holder);


                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.show();
            }
        });

        //handle item click, goto pdfListAdminActivity, also pass pdf category and categoriesId
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfListAdminActivity.class);
                intent.putExtra("categoryId",ID);
                intent.putExtra("categoryTitle",Category);
                context.startActivity(intent);
            }
        });

    }

    private void deleteCategory(ModelCategoryClass model, HolderClass holder) {
        //get id of category to delete
        String id = model.getID();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Deleted Successfully!", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Field To Delete "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        });

    }

    @Override
    public int getItemCount() {
        return categoryClassArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterCategory(filterList,this);
        }
        return filter;
    }

    //View Holder Class to hold View of row_Category.xml
    //
    class HolderClass extends RecyclerView.ViewHolder{

        TextView categoryTv;
        ImageButton deleteBtn;

        public HolderClass(@NonNull View itemView) {
            super(itemView);

            categoryTv = binding.categoryTv;
            deleteBtn = binding.deleteBtn;
        }
    }
}

package com.example.a1stapp.filters;

import android.annotation.SuppressLint;
import android.widget.Filter;

import com.example.a1stapp.adapters.AdapterPdfAdmin;
import com.example.a1stapp.adapters.AdopterCategory;
import com.example.a1stapp.models.ModelCategoryClass;
import com.example.a1stapp.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {

    //arraylist in which we search
    ArrayList<ModelPdf> filterList;
    //adapter in which Filter need to be implement..
    AdapterPdfAdmin adapterPdfAdmin;

    //Constructor


    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //Value should not be null and empty

        if (constraint != null && constraint.length()>0){
            // Change upper case lower case to avoid case sensitivity
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filterModels = new ArrayList<>();

            for (int i=0;i<filterList.size();i++){
                //Validate..
                if (filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    //add to filtered list
                    filterModels.add(filterList.get(i));
                }

            }
            results.count = filterModels.size();
            results.values = filterModels;

        }else {
            results.count = filterList.size();
            results.values = filterList;

        }
        return results;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //apply filter changes..
        adapterPdfAdmin.pdfArrayList = (ArrayList<ModelPdf>) results.values;

        //Notify Changes
        adapterPdfAdmin.notifyDataSetChanged();

    }
}

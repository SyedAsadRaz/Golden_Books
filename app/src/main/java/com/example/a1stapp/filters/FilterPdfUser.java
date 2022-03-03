package com.example.a1stapp.filters;

import android.widget.Filter;

import com.example.a1stapp.adapters.AdapterPdfUser;
import com.example.a1stapp.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfUser extends Filter {

    //arraylist in which we awant to search
    ArrayList<ModelPdf> filterList;
    //adapter in which filter need to be implement
    AdapterPdfUser adapterPdfUser;

    //constructor


    public FilterPdfUser(ArrayList<ModelPdf> filterList, AdapterPdfUser adapterPdfUser) {
        this.filterList = filterList;
        this.adapterPdfUser = adapterPdfUser;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //Value to be searched should not be null/empty
        if (constraint!=null || constraint.length() > 0 ){
            //not null nor empty
            //change to uppercase or lower case to avoid case sensitivity
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filtereModels = new ArrayList<>();

            for (int i=0; 1<filterList.size(); i++){
                //validate
                if (filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    //search matches add to list
                    filtereModels.add(filterList.get(i));
                }
            }

            results.count = filtereModels.size();
            results.values = filtereModels;
        }
        else {
            //empty or null make original list result
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //apply filter changes
        adapterPdfUser.pdfArrayList = (ArrayList<ModelPdf>)results.values;

        //notify changes
        adapterPdfUser.notifyDataSetChanged();

    }
}

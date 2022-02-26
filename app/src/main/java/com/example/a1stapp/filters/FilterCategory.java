package com.example.a1stapp.filters;

import android.annotation.SuppressLint;
import android.widget.Filter;

import com.example.a1stapp.adapters.AdopterCategory;
import com.example.a1stapp.models.ModelCategoryClass;

import java.util.ArrayList;

public class FilterCategory extends Filter {

    //arraylist in which we search
    ArrayList<ModelCategoryClass> filterList;
    //adapter in which Filter need to be implement..
    AdopterCategory adopterCategory;

    //Constructor


    public FilterCategory(ArrayList<ModelCategoryClass> filterList, AdopterCategory adopterCategory) {
        this.filterList = filterList;
        this.adopterCategory = adopterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //Value should not be null and empty

        if (constraint != null && constraint.length()>0){
            // Change upper case lower case to avoid case sensitivity
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCategoryClass> filterModels = new ArrayList<>();

            for (int i=0;i<filterList.size();i++){
                //Validate..
                if (filterList.get(i).getCategory().toUpperCase().contains(constraint)){
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
        adopterCategory.categoryClassArrayList = (ArrayList<ModelCategoryClass>) results.values;

        //Notify Changes
        adopterCategory.notifyDataSetChanged();

    }
}

package com.example.communityconnect;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

public class FilterDonate extends Filter {
    private DonateAdapter  donateAdapter;
    private List<DonateData> filterList;

    public FilterDonate(DonateAdapter donateAdapter, List<DonateData> filterList) {
        this.donateAdapter = donateAdapter ;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults();
        //validate search

        if (constraint != null && constraint.length() > 0){

            //search field not empty


            //change upper case, to make case insensitive

            constraint = constraint.toString().toUpperCase();

            //store our filtered results

            ArrayList<DonateData> filteredProductCustomer = new ArrayList<>();
            for (int i=0;i<filterList.size();i++){

                //check

                if (filterList.get(i).getDonateTitle().toUpperCase().contains(constraint) || filterList.get(i).getDonateDescription().toUpperCase().contains(constraint)){
                    //add filtered data list to list

                    filteredProductCustomer.add(filterList.get(i));


                }

            }

            filterResults.count = filteredProductCustomer.size();
            filterResults.values = filteredProductCustomer;

        }

        else {

            //search field empty

            filterResults.count = filterList.size();
            filterResults.values = filterList;

        }


        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        donateAdapter.donateDataList = (ArrayList<DonateData>) results.values;

        //refresh adapter
        donateAdapter.notifyDataSetChanged();


    }
}

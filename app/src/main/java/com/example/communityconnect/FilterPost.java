package com.example.communityconnect;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

public class FilterPost extends Filter{
    private PostsAdapter  postsAdapter;
    private List<PostData> filterList;

    public FilterPost(PostsAdapter postsAdapter, List<PostData> filterList) {
        this.postsAdapter = postsAdapter;
        this.filterList = filterList;
    }

    @Override
    protected Filter.FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults();
        //validate search

        if (constraint != null && constraint.length() > 0){

            //search field not empty


            //change upper case, to make case insensitive

            constraint = constraint.toString().toUpperCase();

            //store our filtered results

            ArrayList<PostData> filteredPosts = new ArrayList<>();
            for (int i=0;i<filterList.size();i++){

                //check

                if (filterList.get(i).getPostTitle().toUpperCase().contains(constraint) || filterList.get(i).getPostDescription().toUpperCase().contains(constraint) || filterList.get(i).getUserName().toUpperCase().contains(constraint)){
                    //add filtered data list to list

                    filteredPosts.add(filterList.get(i));


                }

            }

            filterResults.count = filteredPosts.size();
            filterResults.values = filteredPosts;

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

        postsAdapter.postDataList = (ArrayList<PostData>) results.values;

        //refresh adapter
        postsAdapter.notifyDataSetChanged();


    }
}

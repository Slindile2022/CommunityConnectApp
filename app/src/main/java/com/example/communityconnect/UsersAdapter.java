package com.example.communityconnect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> implements Filterable {


    Context context;
    List<Users> usersList;
    List<Users> filterList;

    FilterUsers filter;


    //getting constructor


    public UsersAdapter(Context context, List<Users> usersList) {
        this.context = context;
        this.usersList =usersList;
        this.filterList = usersList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_users, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //here we are binding the data

        Users modelUsers = usersList.get(position);
        String userType = modelUsers.getUserType();
        String firstName = modelUsers.getName();
        String lastName = modelUsers.getSecondName();
        String profileImageUrl1 = modelUsers.getProfileImage();


        String fullNames = firstName + " "+ lastName;

        holder.userName.setText(fullNames);



        if (userType != null && userType.equals("verified User")) {
            holder.verificationIcon.setVisibility(View.VISIBLE);
            holder.comment.setText("Moderator");
        } else {
            holder.verificationIcon.setVisibility(View.GONE);
        }
        if (userType != null && userType.equals("Member")){
            holder.comment.setText("Community member");
        }


        if (userType != null && userType.equals("Organization")) {
            holder.verificationIcon.setVisibility(View.VISIBLE);
            holder.comment.setText("Organization");
        }


        //set user profile picture

        // Assuming you have a reference to the CircleImageView in your ViewHolder
        CircleImageView profileImage = holder.profileImage;

        // Get the profile image URL from your PostData object
        String profileImageUrl = profileImageUrl1;

        // Load the profile image into the CircleImageView using Picasso

        try {
            Picasso.get().load(profileImageUrl)
                    .placeholder(R.drawable.profile) // Placeholder image while loading
                    .error(R.drawable.profile) // Error image if loading fails
                    .into(profileImage);
        }catch (Exception e){
            //do nothing
        }






    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterUsers(this, filterList);

        }
        return filter;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // declaring the items
        ImageView verificationIcon;

        TextView userName, comment;
        CircleImageView profileImage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            userName = itemView.findViewById(R.id.uNameEt);
            comment = itemView.findViewById(R.id.commentEt);
            profileImage = itemView.findViewById(R.id.profile_image);
            verificationIcon = itemView.findViewById(R.id.verified_icon);

        }
    }

}

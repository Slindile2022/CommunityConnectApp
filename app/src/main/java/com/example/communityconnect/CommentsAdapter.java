package com.example.communityconnect;

import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder>{


    Context context;
    List<Comments> commentsList;

    String currentUserId, postTime;




    //getting constructor


    public CommentsAdapter(Context context, List<Comments> commentsList, String currentUserId, String postTime) {
        this.context = context;
        this.commentsList = commentsList;
        this.currentUserId = currentUserId;
        this.postTime = postTime;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_comments, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //getting the values from the database
        Comments comments = commentsList.get(position);
        String userType = comments.getUserType();
        String postTime = comments.getcId();
        String userId = comments.getUserId();
        String cId = comments.getcId();




        //here we are binding the data
        holder.userName.setText(comments.getUserName());
        holder.comment.setText(comments.getComment());

        // Showing the post from verified users with verification icons

        if (currentUserId.equals(userId)) {

            //display the delete icon
            holder.deleteComment.setVisibility(View.VISIBLE);
        }


        if (userType != null && ( userType.equals("verified User") || userType.equals("Organization"))) {
            holder.verificationIcon.setVisibility(View.VISIBLE);
        } else {
            holder.verificationIcon.setVisibility(View.GONE);
        }

        //comment click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if the user is the owner of the comment
                if (currentUserId.equals(userId)){
                   //user can delete the comment


                    // Create the object of AlertDialog Builder class
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    // AlertDialog.Builder builder = new AlertDialog.Builder(ProfileFragment.this);

                    // Set the title show for the Alert time
                    builder.setTitle("Delete");

                    // Set the message show for the Alert time
                    builder.setMessage("Are you sure you want to delete this comment?");


                    // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
                    builder.setCancelable(false);

                    // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
                    builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                        // When the user click yes button then app will close

                        //delete the comment
                        deleteComment(cId);



                    });

                    // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
                    builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                        // If user click no then dialog box is canceled.
                        dialog.cancel();
                    });

                    // Create the Alert dialog
                    AlertDialog alertDialog = builder.create();
                    // Show the Alert Dialog box
                    alertDialog.show();
                }
                else {

                    //user can not delete the comment


                }
            }
        });


        //set user profile picture

        // Assuming you have a reference to the CircleImageView in your ViewHolder
        CircleImageView profileImage = holder.profileImage;

        // Get the profile image URL from your PostData object
        String profileImageUrl = comments.getProfileImage();

        // Load the profile image into the CircleImageView using Picasso

        try {
            Picasso.get().load(profileImageUrl)
                    .placeholder(R.drawable.profile) // Placeholder image while loading
                    .error(R.drawable.profile) // Error image if loading fails
                    .into(profileImage);
        }catch (Exception e){
            //do nothing
        }




        // Get the timestamp of the post (you mentioned it's in the orderTime variable)
        long postTimestamp = Long.parseLong(postTime);
        long currentTimestamp = System.currentTimeMillis();

        // Calculate the time difference in milliseconds
        long timeDifference = currentTimestamp - postTimestamp;

        // Get the relative time span string based on the time difference
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(postTimestamp, currentTimestamp, DateUtils.MINUTE_IN_MILLIS);

        // If the time difference is less than an hour, display "just now" or the number of minutes
        if (timeDifference < DateUtils.SECOND_IN_MILLIS) {
            // Set the time ago text
            holder.timePosted.setText("just now");
        } else if (timeDifference < DateUtils.DAY_IN_MILLIS) {
            // If the time difference is less than a day, display the number of minutes or "hour ago" if it's just over an hour
            int minutesAgo = (int) (timeDifference / DateUtils.MINUTE_IN_MILLIS);
            int hoursAgo = minutesAgo / 60;
            if (minutesAgo < 60) {
                if(minutesAgo == 0){
                    //display just now
                    String timeText =  "just now";
                    holder.timePosted.setText(timeText);
                } else if (minutesAgo == 1) {
                    //remove the s at the end
                    String timeText =  minutesAgo + " minute ago";
                    holder.timePosted.setText(timeText);
                }
                else {
                    String timeText =  minutesAgo + " minutes ago";
                    holder.timePosted.setText(timeText);
                }

            } else if(minutesAgo >= 60 && minutesAgo <= 120){
                String timeText = hoursAgo + " hour ago";
                holder.timePosted.setText(timeText);
            }
            else {
                String timeText = hoursAgo + " hours ago";
                holder.timePosted.setText(timeText);
            }
        } else if (timeDifference < DateUtils.WEEK_IN_MILLIS) {
            // If the time difference is less than a week, display the number of days ago
            int daysAgo = (int) (timeDifference / DateUtils.DAY_IN_MILLIS);
            String timeText =  daysAgo + " days ago";
            holder.timePosted.setText(timeText);
        } else {
            // If it's more than a week, display the date and time in the desired format
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(postTimestamp);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH);
            String formattedDateTime = dateFormat.format(calendar.getTime());

            // Set the date and time text
            holder.timePosted.setText(formattedDateTime);
        }








    }

    private void deleteComment(String cId) {
        //delete comment

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(postTime);
        databaseReference.child("Comments").child(cId).removeValue();

        Toast.makeText(context, "comment deleted", Toast.LENGTH_SHORT).show();

        //now update the comments count
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments = ""+ snapshot.child("pComments").getValue();
                int newCommentValue = Integer.parseInt(comments) - 1;
                databaseReference.child("pComments").setValue(""+newCommentValue);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        // declaring the items
        ImageView verificationIcon, deleteComment;
        TextView userName, comment, timePosted;
        CircleImageView profileImage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // imageView = itemView.findViewById(R.id.userIconTv);
            userName = itemView.findViewById(R.id.uNameEt);
            comment = itemView.findViewById(R.id.commentEt);
            profileImage = itemView.findViewById(R.id.profile_image);
            verificationIcon = itemView.findViewById(R.id.verified_icon);
            timePosted = itemView.findViewById(R.id.timeEt);
            deleteComment = itemView.findViewById(R.id.deleteEt);

        }
    }
}

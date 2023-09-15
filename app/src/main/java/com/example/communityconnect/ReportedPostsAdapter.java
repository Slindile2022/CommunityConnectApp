package com.example.communityconnect;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReportedPostsAdapter extends RecyclerView.Adapter<ReportedPostsAdapter.ViewHolder> {


    Context context;
    List<PostData> postDataList, filterList;

    FilterPost  filter;


    //getting constructor

    String myUid, myUserType;


    private DatabaseReference likesRef;
    private DatabaseReference postsRef;


    boolean mProcessLike = false;


    public ReportedPostsAdapter(Context context, List<PostData> postDataList) {
        this.context = context;
        this.postDataList =postDataList;
        this.filterList = postDataList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        //procedure to check the usertype
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user type

                        String userType1 = "" + snapshot.child("userType").getValue();
                        myUserType = userType1;


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_reported_posts, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        //here we are binding the data

        PostData postData = postDataList.get(position);
        String postTime = postData.getTimeStamp();
        String pLikes = postData.getpLikes(); //total number of likes per post
        String pComments = postData.getpComments(); //total number of comments per post
        String postImage = postData.getPostImage();
        String userType = postData.getUserType();
        String uid = postData.getUid();
        String postDescription = postData.getPostDescription();
        String postTitle = postData.getPostTitle();
        String userName = postData.getUserName();
        String userProfileImage = postData.getProfileImage();




        holder.likes.setText(pLikes +" Likes");
        holder.comments.setText(pComments +" Comments");



        //now handling in a case the post is for emergency

        if(postDescription.equals("I'm in a dire situation and require immediate assistance. Please, if anyone is available, I urgently need help")){
            //making sure that i remove all the unwanted display
            holder.profileImage.setVisibility(View.GONE);
            holder.postTitle.setVisibility(View.GONE);
            holder.verificationIcon.setVisibility(View.GONE);
            holder.userName.setText("Emergency alert⚠️");

            //making sure that we can see the location of the user
            holder.accessLocation.setVisibility(View.VISIBLE);


        }
        else{
            //assign the user name without an issue
            holder.userName.setText(postData.getUserName());
            holder.postTitle.setText(postData.getPostTitle());
        }



        //making sure that post without images also display

        if (postImage.equals("noImage")){
            //hide the imageview on the recycle view
            holder.imageView.setVisibility(View.GONE);
        }
        else{
            //hide the imageview on the recycle view
            holder.imageView.setVisibility(View.VISIBLE);

        }


        // Showing the post from verified users with verification icons


        if (userType != null && userType.equals("verified User")) {
            holder.verificationIcon.setVisibility(View.VISIBLE);
        } else {
            holder.verificationIcon.setVisibility(View.GONE);
        }

        //making sure that we have the see more option in case a message is tool long
        if (postDescription.length() > 150) {
            holder.postDescription.setText(postDescription.substring(0, 150) + "...");
            holder.seeMore.setVisibility(View.VISIBLE);

            holder.seeMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.postDescription.setText(postDescription);
                    holder.seeMore.setVisibility(View.GONE);
                }
            });
        } else {
            holder.postDescription.setText(postDescription);
            holder.seeMore.setVisibility(View.GONE);
        }








        //setting up image post image
        String imageUri = null;
        imageUri = postData.getPostImage();
        Picasso.get().load(imageUri).into(holder.imageView);

        //set user profile picture

        // Assuming you have a reference to the CircleImageView in your ViewHolder
        CircleImageView profileImage = holder.profileImage;

        // Get the profile image URL from your PostData object
        String profileImageUrl = postData.getProfileImage();

        if(profileImageUrl != null){
            // Load the profile image into the CircleImageView using Picasso
            try{
                Picasso.get().load(profileImageUrl)
                        .placeholder(R.drawable.profile) // Placeholder image while loading
                        .error(R.drawable.profile) // Error image if loading fails
                        .into(profileImage);
            }
            catch (Exception e){
                //do nothing
            }

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
                String timeText =  minutesAgo + " minutes ago";
                holder.timePosted.setText(timeText);
            } else if(minutesAgo >= 60){
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






    @Override
    public int getItemCount() {
        return postDataList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        // declaring the items
        ImageView imageView, verificationIcon, likeBtn, commentBtn, report;
        ImageButton moreBtn;


        CircleImageView profileImage;
        TextView postTitle, postDescription, userName, timePosted, seeMore,  accessLocation ,likes, comments;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.pImageEt);
            moreBtn =  itemView.findViewById(R.id.moreBtn);
            profileImage = itemView.findViewById(R.id.profile_image);
            verificationIcon = itemView.findViewById(R.id.verified_icon);
            postTitle = itemView.findViewById(R.id.pTitleEt);
            postDescription = itemView.findViewById(R.id.pDescriptionEt);
            userName = itemView.findViewById(R.id.uNameEt);
            timePosted = itemView.findViewById(R.id.timeEt);
            seeMore = itemView.findViewById(R.id.seeMore);
            likes = itemView.findViewById(R.id.pLikesEt);
            comments = itemView.findViewById(R.id.pCommentsEt);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            report =  itemView.findViewById(R.id.report);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            accessLocation = itemView.findViewById(R.id.accessLocationButton);


        }
    }
}

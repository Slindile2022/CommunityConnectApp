package com.example.communityconnect;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import com.squareup.picasso.Picasso;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
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

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
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

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Calendar;
import java.util.Locale;

import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> implements Filterable{


    Context context;
    List<PostData> postDataList, filterList;

    FilterPost  filter;


    //getting constructor

    String myUid, myUserType;


    private DatabaseReference likesRef;
    private DatabaseReference postsRef;


    boolean mProcessLike = false;


    public PostsAdapter(Context context, List<PostData> postDataList) {
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

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_posts, parent, false);
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

        //set likes for each post
        setLikes(holder, postTime);


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


        if (userType != null &&  ( userType.equals("verified User") || userType.equals("Organization"))) {
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




        //working on the more button
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions(holder.moreBtn, uid, myUid, postTime, postImage, myUserType, postTitle, postDescription, userType, userProfileImage, pLikes, pComments, userName);
            }
        });


        //working on comment button
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start postDetailActivity
                Intent intent =  new Intent(context, PostDetailsActivity.class);
                intent.putExtra("postId", postTime);
                intent.putExtra("userProfileImage", profileImageUrl);
                intent.putExtra("postImage", postImage);
                intent.putExtra("postTitle", postTitle);
                intent.putExtra("postDescription", postDescription);
                intent.putExtra("userId", uid);
                intent.putExtra("userType", userType);
                intent.putExtra("userName", userName);
                intent.putExtra("pLikes", pLikes + " Likes");
                intent.putExtra("userImage", profileImageUrl);
                intent.putExtra("currentUserId", myUid);
                intent.putExtra("pComments", pComments + " Comments");

                context.startActivity(intent);
            }
        });

        //working on access user location
        holder.accessLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start postDetailActivity
                Intent intent =  new Intent(context, PostDetailsActivity.class);
                intent.putExtra("postId", postTime);
                intent.putExtra("userProfileImage", profileImageUrl);
                intent.putExtra("postImage", postImage);
                intent.putExtra("postTitle", postTitle);
                intent.putExtra("postDescription", postDescription);
                intent.putExtra("userId", uid);
                intent.putExtra("userType", userType);
                intent.putExtra("userName", userName);
                intent.putExtra("pLikes", pLikes + " Likes");
                intent.putExtra("userImage", profileImageUrl);
                intent.putExtra("currentUserId", myUid);
                intent.putExtra("pComments", pComments + " Comments");

                context.startActivity(intent);
            }
        });


        //working on the share button
        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //should handle post with images and ones without images
                BitmapDrawable bitmapDrawable = (BitmapDrawable)holder.imageView.getDrawable();

                if (bitmapDrawable == null){
                    //post does not have an image
                        
                    shareTextOnly(postTitle, postDescription);
                }
                else {
                    //post has an image

                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(postTitle, postDescription, bitmap);


                }

            }
        });



        //working on the like button
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pLikes = Integer.parseInt(postData.getpLikes());
                mProcessLike = true;

                //get the id of the post clicked
                String postIde = postData.getTimeStamp();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (mProcessLike){
                            if (snapshot.child(postIde).hasChild(myUid)){
                                //already liked, so remove like
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes - 1));
                                likesRef.child(postIde).child(myUid).removeValue();
                                mProcessLike = false;


                            }
                            else {
                                //not liked, like it
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes + 1));
                                likesRef.child(postIde).child(myUid).setValue("Liked");
                                mProcessLike = false;

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });



    }

    private void shareImageAndText(String postTitle, String postDescription, Bitmap bitmap) {

        //concatenate post title and post description
        String shareBody = postTitle + "\n" + postDescription;

        //first saving the image in cache, get saved image url
        Uri uri = savedImageToShare(bitmap);

        //share the intent

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Community Connect Post");
        sIntent.setType("image/png");
        context.startActivity(Intent.createChooser(sIntent, "Share via")); //message to show to allow the user to select the way the want to share the post



    }

    private Uri savedImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");

        Uri uri = null;

        try {

            imageFolder.mkdir(); //create if does not exist
            File file =  new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.example.communityconnect", file);



        }
        catch (Exception e){

            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT);
        }

        return uri;
    }

    private void shareTextOnly(String postTitle, String postDescription) {

        //concatenate post title and post description

        String shareBody = postTitle + "\n" + postDescription;

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Community Connect Post"); //this will be the subject when sharing with an email
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sIntent, "Share via")); //message to show to allow the user to select the way the want to share the post


    }

    private void reportPost(String postTile1, String postDescription1, String timestamp1, String uid1, String userNames1, String userProfileImage1, String postImage1, String userType1, String likes1, String comments1) {
        //send all the information about the reported post to the database

        //get the time the post was uploaded

        String timeStamp = "" + System.currentTimeMillis();





        // Initialize the Firebase Database instance
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        PostData postData = new PostData(postTile1, postDescription1, timeStamp, uid1, userNames1, userProfileImage1, postImage1, userType1, likes1, comments1);



        firebaseDatabase.getReference("ReportedPosts").child(timeStamp).setValue(postData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //if the post added on the realtime database

                Toast.makeText(context, "Post reported successfully", Toast.LENGTH_LONG).show();




            }

        });


    }

    private void setLikes(final ViewHolder holder, final String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postKey).hasChild(myUid)) {
                    // User has liked this post
                    holder.likeBtn.setImageResource(R.drawable.baseline_favorite_24);

                } else {
                    // User has not liked this post
                    holder.likeBtn.setImageResource(R.drawable.baseline_favorite_border_24);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String postTime, String postImage, String myUserType, String postTitle, String postDescription, String userType, String userProfileImage, String pLikes, String pComments, String userName) {
        //creating popup menu currently having option delete

        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);
        //show delete option in only post currently signed in user
        if (myUserType.equals("verified User")){
            //add item in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");

        }
        if (!myUid.equals(uid) && !myUserType.equals("verified User")){
            //allow other users to report others comment but not their own comment
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Report");

        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id ==0){
                    //delete is clicked
                    //begin deleting the item clicked
                    beginDelete(postTime, postImage);


                } else if (id == 1) {

                    reportPost(postTitle, postDescription, postTime, uid, userName, userProfileImage, postImage, userType, pLikes, pComments);

                }
                else if (id == 2) {
                    //start postDetailActivity
                    Intent intent =  new Intent(context, PostDetailsActivity.class);
                    intent.putExtra("postId", postTime);
                    context.startActivity(intent);
                }
                return false;
            }
        });

        //show menu
        popupMenu.show();

    }

    private void beginDelete(String postTime, String postImage) {
        //post can have an image and also post can not have an image

        if (postImage.equals("noImage")){
            //assuming that the post does not have an image this is what i have to do
            deleteWithoutImage(postTime);
        }
        else {
            deleteWithImage(postTime, postImage);
        }

    }

    private void deleteWithImage(String postTime, String postImage) {
        //deleting post without an image

        //Progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(postImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //image deleted, now delete database

                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("timeStamp").equalTo(postTime);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            dataSnapshot.getRef().removeValue();

                        }
                        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //failed to delete the post
                        pd.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void deleteWithoutImage(String postTime) {
        //deleting a post without an image

        //Progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting");

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("timeStamp").equalTo(postTime);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    dataSnapshot.getRef().removeValue();

                }
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




    @Override
    public int getItemCount() {
        return postDataList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPost(this, filterList);
        }
        return filter;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // declaring the items
        ImageView imageView, verificationIcon, likeBtn, commentBtn, report,  shareBtn;
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
            shareBtn = itemView.findViewById(R.id.shareBtn);
            accessLocation = itemView.findViewById(R.id.accessLocationButton);


        }
    }
}

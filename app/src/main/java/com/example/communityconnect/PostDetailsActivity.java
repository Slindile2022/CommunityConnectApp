package com.example.communityconnect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.communityconnect.databinding.ActivityPostDetailsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetailsActivity extends AppCompatActivity implements LocationListener {

    //view binding
    private ActivityPostDetailsBinding binding;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    FirebaseDatabase firebaseDatabase;

    RecyclerView recyclerView;

    Query databaseReference;

    CommentsAdapter commentsAdapter;

    List<Comments> commentsList;

    private static final int LOCATION_REQUEST_CODE = 100;
    //permission array
    private String[] locationPermissions;

    private LocationManager locationManager;

    private double latitude = 0.0 , longitude = 0.0;

    private String currentLatitude, currentLongitude, finalLatitude, finalLongitude;

    String city, countryName, state;

    String uid; //current user id

    private boolean locationDetectionInProgress = false;


    private boolean emergencyInfoUploaded = false;






    //to get the detail of user and post

    String  postTime, userProfileImage,postImage, postTitle,postDescription, userId, userType,  currentUserType,userName, currentUserName, pLikes, pComments,currentUserId, currentUserProfileImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());




        //getting id of the post
        Intent intent = getIntent();
        postTime = intent.getStringExtra("postId");
        userName = intent.getStringExtra("userName");
        userProfileImage = intent.getStringExtra("userProfileImage");
        postImage = intent.getStringExtra("postImage");
        postTitle = intent.getStringExtra("postTitle");
        postDescription = intent.getStringExtra("postDescription");
        userId = intent.getStringExtra("userId");
        userType = intent.getStringExtra("userType");
        pLikes = intent.getStringExtra("pLikes");
        pComments = intent.getStringExtra("pComments");
        currentUserId = intent.getStringExtra("currentUserId");

        //set the text to display the username of the person who posted
        binding.userName.setText(userName + "'s post");

        //int firebase auth

        firebaseAuth = FirebaseAuth.getInstance();

        //initialize the location permissions

        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};


        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        //load post info

        loadPostInformation();

        //handle on click back button
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //working on the more button
        binding.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions(binding.moreBtn, userId, currentUserId, postTime, postImage);
            }
        });

        //working on the comment button
        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                postComment();

            }
        });

        //working on the access other user location

        binding.accessLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check the user's location permission

                if (!locationDetectionInProgress) {
                    // Set the flag to indicate that location detection is in progress
                    locationDetectionInProgress = true;

                    if (checkLocationPermissions()) {
                        // Location is already enabled
                        detectLocation();


                    } else {
                        // Allow the user to enable location
                        requestLocationPermission();
                    }
                }

            }
        });















    }

    private void postComment() {
        //publish comment to that post

        //show progress

        progressDialog.setMessage("Adding comment..");
        progressDialog.show();

        String comment = binding.commentEt.getText().toString().trim();

        //validate comment
        if (TextUtils.isEmpty(comment)){
            //no comment was added
            progressDialog.dismiss();
            Toast.makeText(this, "please add comment", Toast.LENGTH_SHORT).show();
            return;
        }

        //getting the time commented
        String timeStamp = String.valueOf(System.currentTimeMillis());

        //put info in hashmap
        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put("cId", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timeStamp", timeStamp);
        hashMap.put("userName", currentUserName);
        hashMap.put("userId", currentUserId);
        hashMap.put("profileImage", currentUserProfileImage);
        hashMap.put("userType",  currentUserType);



        //each post will have a child that will contain comments
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(postTime).child("Comments");

        //put this data into the database
        databaseReference.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //comment is now added
                        progressDialog.dismiss();
                        Toast.makeText(PostDetailsActivity.this, "comment added", Toast.LENGTH_SHORT).show();

                        //cleat the comment text
                        binding.commentEt.setText("");



                        //send the notification
                        prepareNotification(
                                ""+timeStamp,
                                ""+currentUserName+ " commented on the post",
                                ""+comment,
                                "PostNotification",
                                "POST");


                        //update comment count
                        updateCommentCount();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //comment is not added get the error message
                        progressDialog.dismiss();
                        Toast.makeText(PostDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    boolean mProcessComment = false;
    private void updateCommentCount() {
        //to handle the number of comments
        mProcessComment = true;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(postTime);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessComment){
                    String comments = ""+ snapshot.child("pComments").getValue();
                    int newCommentValue = Integer.parseInt(comments) + 1;
                    databaseReference.child("pComments").setValue(""+newCommentValue);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void loadPostInformation() {

        //binding.pTitleEt.setText(postTitle);
        binding.pDescriptionEt.setText(postDescription);
        //binding.uNameEt.setText(userName);
        binding.pLikesEt.setText(pLikes);
        binding.pCommentsEt.setText(pComments);

        //now handling in a case the post is for emergency

        if(postDescription.equals("I'm in a dire situation and require immediate assistance. Please, if anyone is available, I urgently need help")){
            //making sure that i remove all the unwanted display
            binding.profileImage.setVisibility(View.GONE);
            binding.pTitleEt.setVisibility(View.GONE);
            binding.verifiedIcon.setVisibility(View.GONE);
            binding.userName.setText("Emergency alert⚠️");
            binding.uNameEt.setText("Emergency alert⚠️");


            //making sure that we can see the location of the user
            binding.accessLocationButton.setVisibility(View.VISIBLE);

            //init the user latitude and longitude
            finalLatitude = postTitle;
            finalLongitude = userName;

        }
        else{
            //assign the user name without an issue
            binding.uNameEt.setText(userName);
            binding.pTitleEt.setText(postTitle);
        }


        //working on the time the post was published

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
            binding.timeEt.setText("just now");
        } else if (timeDifference < DateUtils.DAY_IN_MILLIS) {
            // If the time difference is less than a day, display the number of minutes or "hour ago" if it's just over an hour
            int minutesAgo = (int) (timeDifference / DateUtils.MINUTE_IN_MILLIS);
            int hoursAgo = minutesAgo / 60;
            if (minutesAgo < 60) {
                String timeText =  minutesAgo + " minutes ago";
                binding.timeEt.setText(timeText);
            } else if(minutesAgo >= 60){
                String timeText = hoursAgo + " hours ago";
                binding.timeEt.setText(timeText);
            }
        } else if (timeDifference < DateUtils.WEEK_IN_MILLIS) {
            // If the time difference is less than a week, display the number of days ago
            int daysAgo = (int) (timeDifference / DateUtils.DAY_IN_MILLIS);
            String timeText =  daysAgo + " days ago";
            binding.timeEt.setText(timeText);
        } else {
            // If it's more than a week, display the date and time in the desired format
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(postTimestamp);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH);
            String formattedDateTime = dateFormat.format(calendar.getTime());

            // Set the date and time text
            binding.timeEt.setText(formattedDateTime);

        }


        //now working on see more function
        //making sure that we have the see more option in case a message is tool long
        if (postDescription.length() > 150) {
            binding.pDescriptionEt.setText(postDescription.substring(0, 150) + "...");
            binding.seeMore.setVisibility(View.VISIBLE);

            binding.seeMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    binding.pDescriptionEt.setText(postDescription);
                    binding.seeMore.setVisibility(View.GONE);
                }
            });
        } else {
            binding.pDescriptionEt.setText(postDescription);
            binding.seeMore.setVisibility(View.GONE);
        }

        // Showing the post from verified users with verification icons


        if (userType != null && userType.equals("verified User")) {
            binding.verifiedIcon.setVisibility(View.VISIBLE);
        } else {
            binding.verifiedIcon.setVisibility(View.GONE);
        }

        //making sure that post without images also display

        if (postImage.equals("noImage")){
            //hide the imageview on the recycle view
            binding.pImageEt.setVisibility(View.GONE);
        }
        else{
            //hide the imageview on the recycle view
            binding.pImageEt.setVisibility(View.VISIBLE);

        }


        //setting up image post image
        Picasso.get().load(postImage).into(binding.pImageEt);

        //set user profile picture

        // Assuming you have a reference to the CircleImageView in your ViewHolder
        CircleImageView profileImage = binding.profileImage;

        // Get the profile image URL from your PostData object
        String profileImageUrl = userProfileImage;
        if(profileImageUrl != null) {
            // Load the profile image into the CircleImageView using Picasso
            try {
                Picasso.get().load(profileImageUrl)
                        .placeholder(R.drawable.profile) // Placeholder image while loading
                        .error(R.drawable.profile) // Error image if loading fails
                        .into(profileImage);
            } catch (Exception e) {
                //do nothing
            }

        }

            loadMyInfo();


        //handle the comments section
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getInstance().getReference("Posts").child(postTime).child("Comments");


        //load the comments section


        recyclerView = binding.allCommentsEt;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(PostDetailsActivity.this));

        commentsList = new ArrayList<Comments>();
        commentsAdapter = new CommentsAdapter(PostDetailsActivity.this, commentsList, currentUserId, postTime);
        recyclerView.setAdapter(commentsAdapter);

        // Reverse the layout manager to show the latest posts at the bottom
        LinearLayoutManager layoutManager = new LinearLayoutManager(PostDetailsActivity.this);
        recyclerView.setLayoutManager(layoutManager);



        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {


                Comments comments = snapshot.getValue(Comments.class);

                commentsList.add(comments);

                //pass currentUserId as a parameter of comment adapter

                commentsAdapter = new CommentsAdapter(getApplicationContext(), commentsList, currentUserId, postTime);

                commentsAdapter.notifyDataSetChanged();

                // Scroll the RecyclerView to the last comment
                recyclerView.scrollToPosition(commentsList.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void loadMyInfo() {
        //loading all the information from realtime database

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user type

                        String address = "" + snapshot.child("address").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String secondName = "" + snapshot.child("secondName").getValue();
                        String userType = "" + snapshot.child("userType").getValue();
                        String phone = "" + snapshot.child("phone").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();

                        //setting the profile image of the current user
                        currentUserProfileImage = profileImage;

                        //current userName
                        currentUserName = name +" "+secondName;

                        //current userType
                        currentUserType = userType;



                        //make the email short




                        //set profile image


                        // Load the profile image using Glide
                        String profileImageURL = profileImage; // Replace this with the actual URL
                        CircleImageView profileImageView = findViewById(R.id.cAvatarIv);

                        try{
                            Glide.with(PostDetailsActivity.this)
                                    .load(profileImageURL)
                                    .placeholder(R.drawable.profile) // Placeholder image while loading
                                    .into(profileImageView);
                        }catch (Exception e){
                            //do nothing
                        }





                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String postTime, String postImage) {
        //creating popup menu currently having option delete

        PopupMenu popupMenu = new PopupMenu(PostDetailsActivity.this, moreBtn, Gravity.END);
        //show delete option in only post currently signed in user
        if (currentUserType.equals("verified User")){
            //add item in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");

        }

        // popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Detail");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id ==0){
                    //delete is clicked
                    //begin deleting the item clicked
                    beginDelete(postTime, postImage);


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
        final ProgressDialog pd = new ProgressDialog(PostDetailsActivity.this);
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
                        Toast.makeText(PostDetailsActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PostDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();


            }
        });
    }

    private void deleteWithoutImage(String postTime) {
        //deleting a post without an image

        //Progress bar
        final ProgressDialog pd = new ProgressDialog(PostDetailsActivity.this);
        pd.setMessage("Deleting");

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("timeStamp").equalTo(postTime);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    dataSnapshot.getRef().removeValue();

                }
                Toast.makeText(PostDetailsActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void prepareNotification(String pId, String postTitle, String postDescription, String notificationType, String notificationTopic){

        //prepare data for notification

        String NOTIFICATION_TOPIC = "/topics/" + notificationTopic; //topic must match with what the user subscribed to
        String NOTIFICATION_TITLE = postTitle; //e.g slindile maseko added a new post
        String NOTIFICATION_MESSAGE = postDescription; //content of post
        String NOTIFICATION_TYPE = notificationType; //the different types of notifications, event, comments, add post

        //prepare JSON what to send . and where to send
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();

        try {
            //WHAT TO SEND
            // PostData postData = new PostData(postTile, postDescription, timestamp, uid, userNames, userProfileImage, ""+downloadImageUri, userType1, "0", "0");
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("sender", uid); //current user id
            notificationBodyJo.put("pId", pId); //current user id
            notificationBodyJo.put("postTitle", NOTIFICATION_TITLE); //current user id
            notificationBodyJo.put("postDescription", NOTIFICATION_MESSAGE); //current user id

            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJo); //combine data to be sent

        } catch (JSONException e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //send post notification
        sendPostNotification(notificationJo);

    }

    private void sendPostNotification(JSONObject notificationJo) {

        //send volley object request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("FCM_RESPONSE", "onResponse: "+response.toString() );


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        //Toast error occurred
                        Toast.makeText(PostDetailsActivity.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=AAAApwKJ6dk:APA91bH1OeCBs0jCypk5iuDAwO0Iaz3hmNbzyip8eyTwlk50700sE6Y7u-w4R_zR1CFCdzNsrfsGouSpTvkwMig6OV2R0B8B8z1_nbxgeI_KzmAbVEPB16HUYvhl1R-gab5j1GFLisIg"); //your application fcm key here

                return headers;
            }
        };

        //enqueue the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    //handling location

    private void detectLocation() {
        Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        // Check if GPS provider is enabled
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            Toast.makeText(this, "Please turn on GPS to get your location", Toast.LENGTH_LONG).show();
        }


    }

    private boolean checkLocationPermissions(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED);

        return result;


    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_REQUEST_CODE);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        // Convert latitude and longitude to strings
         currentLatitude = Double.toString(latitude);
         currentLongitude = Double.toString(longitude);

        findAddress();


        // Call the uploadEmergencyInformation() method only if it hasn't been uploaded yet
        if (!emergencyInfoUploaded) {
            openMap();
            emergencyInfoUploaded = true; // Set the flag to true to indicate upload has occurred
        }



    }

    private void openMap() {
        //opening google map to access the user's location
        String address = "https://maps.google.com/maps?saddr=" + currentLatitude + "," + currentLongitude + "&daddr=" + finalLatitude + "," + finalLongitude;

        Uri gmmIntentUri = Uri.parse(address);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Check if there's an app that can handle the intent before starting
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {


            // Construct the URL for Google Maps web version with directions
            String addresss = "https://www.google.com/maps/dir/?api=1&origin=" +
                    currentLatitude + "," + currentLongitude +
                    "&destination=" + finalLatitude + "," + finalLongitude;

            // Parse the URL
            Uri googleMapsWebUri = Uri.parse(addresss);

            // Open the link in the default web browser
            Intent webIntent = new Intent(Intent.ACTION_VIEW, googleMapsWebUri);
            startActivity(webIntent);

        }



    }

    private void findAddress() {
        //find address, country, state, city

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (!addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0); // Complete address
                String city1 = addresses.get(0).getLocality();
                String state1 = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();

                // Assign the values
                countryName = country;
                city = city1;
                state = state1;
            }





        }
        catch (Exception e){
            //do nothing
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {


        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

        // Location disabled
        // Update the flag to indicate that location detection is not in progress
        locationDetectionInProgress = false;



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted){
                        //permission granted
                        detectLocation();
                    }
                }
                else {
                    //permission denied
                    Toast.makeText(this, "Location permission is necessary...", Toast.LENGTH_SHORT).show();

                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}
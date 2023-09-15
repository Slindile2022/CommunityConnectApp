package com.example.communityconnect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.communityconnect.databinding.ActivityPostBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostActivity extends AppCompatActivity {


    //view binding
    private ActivityPostBinding binding;


    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    String uid; //current user id

    //firebase instances
    FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();

    //firebase instances fire store
    StorageReference storageReference;


    //progress dialog

    private ProgressDialog progressDialog;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //image picked url
    private Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //current user
        String user = firebaseAuth.getCurrentUser().getUid();
        uid = user;


        //init permission arrays
        cameraPermissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click, picture

        binding.pImageIvEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog to pick image

                showImagePickDialog();

            }
        });


        //handle publish post button

        binding.uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Flow
                //input data


                inputData();




            }
        });



    }

    private  String postTile="", postDescription="", userNames="", userProfileImage="", userType1;
    private void inputData() {

        //input data


        postTile = binding.pTitleEt.getText().toString().trim();
        postDescription = binding.pDescEt.getText().toString().trim();




        //validate data


        if(TextUtils.isEmpty(postTile)){

            Toast.makeText(this, "Post title is required", Toast.LENGTH_SHORT).show();
            return;

        }

        else if(TextUtils.isEmpty(postDescription)){

            Toast.makeText(this, "Post description is required", Toast.LENGTH_SHORT).show();
            return;

        }

        else if (image_uri == null){
            //Toast.makeText(this, "image is required", Toast.LENGTH_SHORT).show();
            //return;

            //upload without image

            uploadWithoutImage();

        }

        else {

            uploadImage();

            //data validated, then you can take it to realtime database


        }





    }



    private void uploadWithoutImage() {
        //uploading a post without an image
        progressDialog.setMessage("publishing post");
        progressDialog.show();



        //get the time the post was uploaded

        final String timestamp = "" + System.currentTimeMillis();

        final String uid = firebaseAuth.getCurrentUser().getUid();




        //get user information first from the database


        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user type


                        String name = "" + snapshot.child("name").getValue();
                        String secondName = "" + snapshot.child("secondName").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();
                        String usertype = "" +snapshot.child("userType").getValue();

                        //make the email short


                        //set in the text view of the toolbar
                        final String userNames1 = name+ " "+secondName;
                        final String userProfileImage1 = profileImage;



                        PostData postData = new PostData(postTile, postDescription, timestamp, uid, userNames1, userProfileImage1, "noImage", usertype, "0", "0");



                        firebaseDatabase.getReference("Posts").child(timestamp).setValue(postData).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //if a product was added on the realtime database
                                //success
                                binding.pImageIvEt.setImageURI(null);

                                progressDialog.dismiss();

                                Toast.makeText(PostActivity.this, "post uploaded", Toast.LENGTH_SHORT).show();


                                // Clear EditText fields to allow another user to be registered
                                binding.pTitleEt.setText("");
                                binding.pDescEt.setText("");


                                //send the notification
                                prepareNotification(
                                        ""+timestamp,
                                        ""+userNames1+ " added new post",
                                        ""+postTile+"\n"+postDescription,
                                        "PostNotification",
                                        "POST");



                            }

                        });




                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });





    }


    private void uploadImage() {

        progressDialog.setMessage("publishing post");
        progressDialog.show();



        //get the time the post was uploaded

        final String timestamp = "" + System.currentTimeMillis();

        final String uid = firebaseAuth.getCurrentUser().getUid();

        storageReference = FirebaseStorage.getInstance().getReference("postImages/"+timestamp);

        //get user information first from the database


        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user type


                        String name = "" + snapshot.child("name").getValue();
                        String secondName = "" + snapshot.child("secondName").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();
                        String usertype = "" +snapshot.child("userType").getValue();

                        //make the email short


                        //set in the text view of the toolbar
                        userNames = name+ " "+secondName;
                        userProfileImage = profileImage;
                        userType1 = usertype;
                        




                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        storageReference.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //getting url of the image

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                        while(!uriTask.isSuccessful());

                        Uri downloadImageUri = uriTask.getResult();

                        if(uriTask.isSuccessful()){

                            //updating your information into realtime database
                            //url of the image, upload to realtime database


                            //trying to add the data to realtime database


                            PostData postData = new PostData(postTile, postDescription, timestamp, uid, userNames, userProfileImage, ""+downloadImageUri, userType1, "0", "0");

                            firebaseDatabase.getReference("Posts").child(timestamp).setValue(postData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //if a product was added on the realtime database


                                    binding.pImageIvEt.setImageResource(R.drawable.shape_toolbar02);
                                    image_uri = null;


                                }
                            });







                        }



                        //success
                        binding.pImageIvEt.setImageURI(null);

                        progressDialog.dismiss();

                        Toast.makeText(PostActivity.this, "post uploaded", Toast.LENGTH_SHORT).show();


                        // Clear EditText fields to allow another user to be registered
                        binding.pTitleEt.setText("");
                        binding.pDescEt.setText("");


                        //send the notification
                        prepareNotification(
                                ""+timestamp,
                                ""+userNames+ " added new post",
                                ""+postTile+"\n"+postDescription,
                                "PostNotification",
                                "POST");





                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(PostActivity.this, "failed to upload post", Toast.LENGTH_SHORT).show();
                    }
                });


    }



    private void showImagePickDialog() {

        //options to display dialog
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item clicks

                        if(which==0){
                            //camera clicked

                            if(checkCameraPermission()){
                                //permission granted
                                pickFromCamera();
                            }
                            else{
                                //permission not  granted, request
                                requestCameraPermission();

                            }

                        }

                        else{
                            //gallery clicked

                            if(checkStoragePermission()){
                                //permission granted
                                pickFromGallery();

                            }
                            else{
                                //permission not  granted, request
                                requestStoragePermission();

                            }

                        }
                    }
                })
                .show();





    }

    //intent to pick image from gallery
    private void pickFromGallery(){

        //intent to pick image from gallery

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);

    }

    //intent to pick image from camera

    private void pickFromCamera(){

        //intent to pick image from camera

        //using media store to pick high/original quality image

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "temp_Image");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "temp_Image_Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);


    }

    private boolean checkStoragePermission(){

        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        // returns true/false
        return result;

    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){

        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);


        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        // returns true/false
        return result && result1;

    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    //handle permission results


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted){
                        //both permissions granted
                        pickFromCamera();
                    }
                    else{
                        //both or one of the permissions denied
                        Toast.makeText(this, "camera and storage permission are required ", Toast.LENGTH_SHORT).show();
                    }
                    break;

                }
            }

            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){

                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (storageAccepted){
                        //permission granted
                        pickFromGallery();
                    }
                    else{
                        //permission denied

                        Toast.makeText(this, "storage permission are required ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //handle image pick results


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if (requestCode == IMAGE_PICK_GALLERY_CODE){
            //image from gallery

            image_uri = data.getData();

            //set image

            binding.pImageIvEt.setImageURI(image_uri);

        }

        else if (requestCode == IMAGE_PICK_CAMERA_CODE){
            //image from camera

            binding.pImageIvEt.setImageURI(image_uri);

        }



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
                        Toast.makeText(PostActivity.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
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
}
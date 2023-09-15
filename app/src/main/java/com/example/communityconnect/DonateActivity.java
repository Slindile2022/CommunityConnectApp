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
import android.view.View;
import android.widget.Toast;

import com.example.communityconnect.databinding.ActivityDonateBinding;
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

public class DonateActivity extends AppCompatActivity {


    //view binding
    private ActivityDonateBinding binding;


    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

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
        binding = ActivityDonateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //init permission arrays
        cameraPermissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};


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
        progressDialog.setMessage("adding donation campaign");
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
                        String userType = "" +snapshot.child("userType").getValue();

                        //make the email short


                        //set in the text view of the toolbar
                        final String userNames1 = name+ " "+secondName;




                        //PostData postData = new PostData(postTile, postDescription, timestamp, uid, userNames1, userProfileImage1, "noImage", usertype, "0", "0");
                        DonateData donateData = new DonateData(postTile, postDescription, timestamp, uid, userNames1, "noImage", userType, "0");


                        firebaseDatabase.getReference("Donate").child(timestamp).setValue(donateData).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //if a product was added on the realtime database
                                //success
                                binding.pImageIvEt.setImageURI(null);

                                progressDialog.dismiss();

                                Toast.makeText(DonateActivity.this, "post uploaded", Toast.LENGTH_SHORT).show();


                                // Clear EditText fields to allow another user to be registered
                                binding.pTitleEt.setText("");
                                binding.pDescEt.setText("");


                            }
                        });




                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });





    }


    private void uploadImage() {

        progressDialog.setMessage("adding donation campaign");
        progressDialog.show();



        //get the time the post was uploaded

        final String timestamp = "" + System.currentTimeMillis();

        final String uid = firebaseAuth.getCurrentUser().getUid();

        storageReference = FirebaseStorage.getInstance().getReference("donateImages/"+timestamp);

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


                            //PostData postData = new PostData(postTile, postDescription, timestamp, uid, userNames, userProfileImage, ""+downloadImageUri, userType1, "0", "0");
                            DonateData donateData = new DonateData(postTile, postDescription, timestamp, uid, userNames, ""+downloadImageUri, userType1, "0");
                            firebaseDatabase.getReference("Donate").child(timestamp).setValue(donateData).addOnSuccessListener(new OnSuccessListener<Void>() {
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

                        Toast.makeText(DonateActivity.this, "donate campaign opened", Toast.LENGTH_SHORT).show();


                        // Clear EditText fields to allow another user to be registered
                        binding.pTitleEt.setText("");
                        binding.pDescEt.setText("");




                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(DonateActivity.this, "failed to open donate campaign", Toast.LENGTH_SHORT).show();
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

        boolean result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        // returns true/false
        return result;

    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){

        boolean result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);


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
}
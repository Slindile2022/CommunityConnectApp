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

import com.bumptech.glide.Glide;
import com.example.communityconnect.databinding.ActivityMenuBinding;
import com.example.communityconnect.databinding.ActivityPostDetailsBinding;
import com.example.communityconnect.databinding.ActivityUpdateProfileBinding;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfileActivity extends AppCompatActivity {


    //view binding
    private ActivityUpdateProfileBinding binding;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    FirebaseDatabase firebaseDatabase;

    StorageReference storageReference;

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
        binding = ActivityUpdateProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //int firebase auth

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        // Initialize permission arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //setup progress dialog
        progressDialog = new ProgressDialog(UpdateProfileActivity.this);
        progressDialog.setTitle("please wait");
        progressDialog.setCanceledOnTouchOutside(false);




        //handle update profile button
        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users


                inputData();

            }


        });

        //handle on click back button
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click, edit profile picture

        binding.cameraEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog to pick image

                showImagePickDialog();

            }
        });





    }


    private  String phone="";
    private void inputData() {

        //input data
        phone = binding.phoneEt.getText().toString().trim();




        //validation of phone number

        String mobileRegex = "[0][6-8][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]"; // validation of south africa phone number
        Matcher matcher;

        Pattern mobilePattern = Pattern.compile(mobileRegex);
        matcher = mobilePattern.matcher(phone);


        //validate data



        if (image_uri == null){
            Toast.makeText(this, "image is required", Toast.LENGTH_SHORT).show();
            return;

        }


        if(TextUtils.isEmpty(phone)){

            Toast.makeText(this, "phone number is required", Toast.LENGTH_SHORT).show();
            return;

        }
        else if (phone.length() > 10){

            Toast.makeText(this, "only 10 digits are allowed", Toast.LENGTH_SHORT).show();
            return;


        }
        else if (!matcher.find()){
            Toast.makeText(this, "invalid phone number", Toast.LENGTH_SHORT).show();
            return;

        }

        else {

            uploadImage();

            //data validated, then you can take it to realtime database


        }





    }



    private void uploadImage() {

        progressDialog.setMessage("updating profile");
        progressDialog.show();



        // final String timestamp = ""+System.currentTimeMillis();

        final String uid = firebaseAuth.getCurrentUser().getUid();

        storageReference = FirebaseStorage.getInstance().getReference("profileImages/"+uid);

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



                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String userId = user.getUid();
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                            ref.child("profileImage").setValue(""+downloadImageUri);
                            ref.child("phone").setValue(phone);






                            binding.cameraEt.setImageResource(R.drawable.profile);
                            image_uri = null;







                        }



                        //success
                        binding.cameraEt.setImageURI(null);

                        progressDialog.dismiss();

                        Toast.makeText(UpdateProfileActivity.this, "profile updated", Toast.LENGTH_SHORT).show();




                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(UpdateProfileActivity.this, "failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    //to allow the user to choose between taking a selfie or upload picture from gallery
    private void showImagePickDialog() {

        //options to display dialog
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateProfileActivity.this);
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

        image_uri = UpdateProfileActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);


    }

    private boolean checkStoragePermission(){

        boolean result = ContextCompat.checkSelfPermission(UpdateProfileActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

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

                        Toast.makeText(UpdateProfileActivity.this, "storage permission are required ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //handle image pick results


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if (requestCode == IMAGE_PICK_GALLERY_CODE){
            //image from gallery

            image_uri = data.getData();

            //set image

            binding.profileImage.setImageURI(image_uri);

        }

        else if (requestCode == IMAGE_PICK_CAMERA_CODE){
            //image from camera

            binding.profileImage.setImageURI(image_uri);

        }



    }


    private void checkUser() {
        //get current user

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {

            //not logged in, go to main screen
            startActivity(new Intent(UpdateProfileActivity.this, MainActivity.class));
        } else {

            loadMyInfo();

        }


    }

    //load the user information to the required space
    private void loadMyInfo() {


        //getting your information from realtime database


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
                        String email = "" + snapshot.child("email").getValue();
                        String phone = "" + snapshot.child("phone").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();
                        String timeStamp = "" + snapshot.child("time").getValue();

                        //make the email short



                        //set in the text view of the toolbar
                        binding.nameEt.setText(name);
                        binding.secondNameEt.setText(secondName);
                        binding.emailEt.setText(email);
                        binding.phoneEt.setText(phone);
                        binding.addressEt.setText(address);


                        //set profile image

                        Glide.with(UpdateProfileActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.profile)
                                .into(binding.profileImage);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


}
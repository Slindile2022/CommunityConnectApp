package com.example.communityconnect;

import static android.view.View.GONE;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.communityconnect.databinding.ActivityDashboardBinding;
import com.example.communityconnect.databinding.FragmentProfileBinding;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    private FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;

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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        //int firebase auth

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();


        // Initialize permission arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Updating profile");
        progressDialog.setCanceledOnTouchOutside(false);

        //setup progress dialog
//        progressDialog = new ProgressDialog(getContext());
//        progressDialog.setTitle("please wait");
//        progressDialog.setCanceledOnTouchOutside(false);







        //getting your information from realtime database to allow users to access the button or not to register new users

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();



        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user type

                        String userType = "" + snapshot.child("userType").getValue();




                        //allow authorized users to access the button
                        if(!userType.equals("verified User")){

                            //open the activity to add other users
                            //startActivity(new Intent(requireContext(), RegisterActivity.class));
                            binding.registerBtn.setVisibility(View.GONE);
                        }
                        else {
                            //Allow the admin to register new users
                            binding.registerBtn.setVisibility(View.VISIBLE);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //handle the register button and check if someone has access to register new users
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users
                startActivity(new Intent(requireContext(), RegisterActivity.class));

            }
        });

        //handle the settings button
        binding.settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users
                startActivity(new Intent(requireContext(), SettingsActivity.class));

            }
        });





        //handle update profile button
        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users


                inputData();

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

        //handle log out


        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // Create the object of AlertDialog Builder class
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
               // AlertDialog.Builder builder = new AlertDialog.Builder(ProfileFragment.this);

                // Set the message show for the Alert time
                builder.setMessage("Do you want to exit ?");


                // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
                builder.setCancelable(false);

                // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
                builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                    // When the user click yes button then app will close

                    //log out the user

                    firebaseAuth.signOut();
                    checkUser();

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
        });




        return view;


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
            Toast.makeText(getActivity(), "image is required", Toast.LENGTH_SHORT).show();
            return;

        }


        if(TextUtils.isEmpty(phone)){

            Toast.makeText(getActivity(), "phone number is required", Toast.LENGTH_SHORT).show();
            return;

        }
        else if (phone.length() > 10){

            Toast.makeText(getActivity(), "only 10 digits are allowed", Toast.LENGTH_SHORT).show();
            return;


        }
        else if (!matcher.find()){
            Toast.makeText(getActivity(), "invalid phone number", Toast.LENGTH_SHORT).show();
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

                        Toast.makeText(getActivity(), "profile updated", Toast.LENGTH_SHORT).show();




                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(getActivity(), "failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    //to allow the user to choose between taking a selfie or upload picture from gallery
    private void showImagePickDialog() {

        //options to display dialog
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

        image_uri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);


    }

    private boolean checkStoragePermission(){

        boolean result = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        // returns true/false
        return result;

    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(getActivity(), storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){

        boolean result = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);


        boolean result1 = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        // returns true/false
        return result && result1;

    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(getActivity(), cameraPermissions, CAMERA_REQUEST_CODE);
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
                        Toast.makeText(getContext(), "camera and storage permission are required ", Toast.LENGTH_SHORT).show();
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

                        Toast.makeText(getContext(), "storage permission are required ", Toast.LENGTH_SHORT).show();
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
            startActivity(new Intent(requireContext(), MainActivity.class));
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

                        //make the email short



                        //set in the text view of the toolbar
                        binding.nameEt.setText(name);
                        binding.secondNameEt.setText(secondName);
                        binding.emailEt.setText(email);
                        binding.phoneEt.setText(phone);
                        binding.addressEt.setText(address);


                        //set profile image

                        Glide.with(ProfileFragment.this)
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
package com.example.communityconnect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.communityconnect.databinding.ActivityDepositBinding;
import com.example.communityconnect.databinding.ActivityMenuBinding;
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

public class DepositActivity extends AppCompatActivity {


    //view binding
    private ActivityDepositBinding binding;

    private FirebaseAuth firebaseAuth;

    private  static  final String TAG = "ADD_PDF_TAG"; //for debugging

    private static final int PDF_PICK_CODE = 1000;

    //picked image
    private Uri pdfUri = null;

    //firebase instances fire store
    StorageReference storageReference;

    //firebase instances
    FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();


    //progress dialog

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDepositBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //int firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("please wait");
        progressDialog.setCanceledOnTouchOutside(false);




        //handle on click back button
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
        //handle on click to attach pdf file
        binding.pdfEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfPickIntent();
            }
        });

        //handle the submit proof of payment button
        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });



    }


    private  String paymentReference="", amount="", userNames="";
    private void inputData() {

        //input data


        paymentReference = binding.nameEt.getText().toString().trim();
        amount = binding.secondNameEt.getText().toString().trim();




        //validate data


        if(TextUtils.isEmpty(paymentReference)){

            Toast.makeText(this, "payment reference is required", Toast.LENGTH_SHORT).show();
            return;

        }

        else if(TextUtils.isEmpty(amount)){

            Toast.makeText(this, "enter the amount of the deposit", Toast.LENGTH_SHORT).show();
            return;

        }



        else {

            uploadFile();

            //data validated, then you can take it to realtime database


        }





    }

    private void uploadFile() {

        progressDialog.setMessage("sending proof of payment");
        progressDialog.show();



        //get the time the post was uploaded

        final String timestamp = "" + System.currentTimeMillis();

        final String uid = firebaseAuth.getCurrentUser().getUid();

        storageReference = FirebaseStorage.getInstance().getReference("paymentFiles/"+timestamp);

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






                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        storageReference.putFile(pdfUri)
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

                            PaymentData paymentData = new PaymentData(paymentReference, amount, userNames, uid, timestamp, ""+downloadImageUri, "pending");


                            firebaseDatabase.getReference("PaymentFiles").child(timestamp).setValue(paymentData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //if a product was added on the realtime database


                                    binding.pdfEt.setText("");
                                    pdfUri = null;


                                }
                            });







                        }



                        //success

                        progressDialog.dismiss();

                        Toast.makeText(DepositActivity.this, "proof of payment sent!", Toast.LENGTH_SHORT).show();


                        // Clear EditText fields to allow another user to be registered
                        binding.nameEt.setText("");
                        binding.secondNameEt.setText("");





                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(DepositActivity.this, "failed to send proof of payment", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void pdfPickIntent() {

        Log.d(TAG, "pdfPickIntent: starting pdf intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pdf"), PDF_PICK_CODE);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) { // Corrected from requestCode
            Log.d(TAG, "onActivityResult: PDF PICKED");

            pdfUri = data.getData();
            Log.d(TAG, "onActivityResult: PDF PICKED" + pdfUri);

            // Get the file name from the Uri
            String fileName = getFileNameFromUri(pdfUri);

            // Set the file name in your TextView
            binding.pdfEt.setText(fileName);

        } else {
            Log.d(TAG, "onActivityResult: cancelled PDF PICK");
            Toast.makeText(this, "Could not select the file", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to get the file name from Uri
    private String getFileNameFromUri(Uri uri) {
        String displayName = "";
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            displayName = cursor.getString(nameIndex);
            cursor.close();
        }
        return displayName;
    }

}
package com.example.communityconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.communityconnect.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoginActivity extends AppCompatActivity {

    //view binding
    private ActivityLoginBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    String TAG = "results";

    //progress dialog

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //int firebase auth
        firebaseAuth = FirebaseAuth.getInstance();



        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("please wait");
        progressDialog.setCanceledOnTouchOutside(false);



        //handle click forgot password

        binding.forgotTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

        //handle, begin login

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();



            }
        });

    }


    private String email="", password="";
    private void validateData() {

        //validate user data before login

        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();


        //validation of phone number

        String mobileRegex = "[0][6-8][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]"; // validation of south africa phone number
        Matcher matcher;

        Pattern mobilePattern = Pattern.compile(mobileRegex);
        matcher = mobilePattern.matcher(email);


        if (email.isEmpty()){
            Toast.makeText(this, "Enter email or phone number", Toast.LENGTH_SHORT).show();
        }

         else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() && !matcher.find()) {
            Toast.makeText(this, "invalid email or phone number", Toast.LENGTH_SHORT).show();
        }


        //|| !matcher.find()
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "enter password", Toast.LENGTH_SHORT).show();
        }
        else if (email.length() == 10){


            try {


                //the phone number is valid

                //check if the app is accessible or not before a user can start using it


                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("PhoneLogin");
                databaseReference
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                //get user type

                                String phoneNumberEmailAddress =""+snapshot.child(email).getValue();

                                //set the email address to that corresponding to the phone number registered

                                Log.d("dead", "the email address is : "+phoneNumberEmailAddress);

                                email = phoneNumberEmailAddress;




                                Log.d("dead", "the email address after  is : "+email);


                                //begin logging in user
                                loginUser();


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


            }
            catch (Exception e){
                //do nothing just proceed
            }



        }
        else {

            //begin logging in user
            loginUser();
        }
    }


    private void loginUser() {

        //show progress

        progressDialog.setMessage("logging in...");
        progressDialog.show();

        //login user

        Log.d("dead", "login details : "+email+ " "+password);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        //check if the email is verified
                        checkUser();



                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //Login failure
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


    }


    private void checkUser() {


        //checking if the user is verified or not

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser.isEmailVerified()){


            //now checking if the user account password is still default or what


            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            databaseReference.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //get if the user is still new or not

                            String newUser =""+snapshot.child("passwordChanged").getValue();


                            //check if the password was changed or not

                            if(newUser.equals("no")){
                                //you are an admin

                                progressDialog.dismiss();
                                startActivity(new Intent(LoginActivity.this, ChangePasswordActivity.class));

                                finish();

                            }
                            else  {


                                //login the user and direct them to the dashboard

                                progressDialog.dismiss();
                                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                                finish();
                                Toast.makeText(LoginActivity.this, "welcome back user", Toast.LENGTH_SHORT).show();
                            }





                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });




           }

        else {

            //show progress bar
            progressDialog.setMessage("sending account verification instructions");
            progressDialog.show();


            //send verification email
            FirebaseUser user = firebaseAuth.getCurrentUser();
            user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    progressDialog.dismiss();

                    //send verification to the user's email
                    Toast.makeText(LoginActivity.this, "email verification sent", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();

                    Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });




        }
    }

}
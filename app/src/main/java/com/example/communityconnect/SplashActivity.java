package com.example.communityconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.communityconnect.databinding.ActivitySplashBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    //firebase auth
    private FirebaseAuth firebaseAuth;


    //view binding
    private ActivitySplashBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //int auth

        firebaseAuth = FirebaseAuth.getInstance();


        //start main activity after 2 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUser();

            }
        },2000);

    }

    private void checkUser() {

        //get current use if is logged in
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null){
            //user not logged in
            //start main screen

            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();

        }

        else if(!firebaseUser.isEmailVerified()){
            //user is not verified, he must not access the app
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();


        }


        else
        {
            //user is logged in


            //check if the user has updated their password
            //getting your information from realtime database





            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            databaseReference.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //get user type

                            String passwordChanged = "" + snapshot.child("passwordChanged").getValue();

                            //allow the user to continue using the app
                            if (passwordChanged.equals("yes")){
                                startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
                                finish();

                            }
                            else {
                                //make sure that the user update the password before going forward
                                startActivity(new Intent(SplashActivity.this, ChangePasswordActivity.class));
                                finish();

                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });







       }
    }
}
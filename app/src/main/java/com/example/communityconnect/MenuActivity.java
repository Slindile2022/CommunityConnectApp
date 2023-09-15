package com.example.communityconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.communityconnect.databinding.ActivityMenuBinding;
import com.example.communityconnect.databinding.ActivityPostDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MenuActivity extends AppCompatActivity {


    //view binding
    private ActivityMenuBinding binding;

    private FirebaseAuth firebaseAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //int firebase auth

        firebaseAuth = FirebaseAuth.getInstance();

        //getting your information from realtime database to allow users to access the button or not to register new users

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        //check user type to enable the visibility to add new users to the system

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

                            binding.registerBtn.setVisibility(View.GONE);
                            binding.reportedPostsBtn.setVisibility(View.GONE);
                            binding.transactionBtn.setVisibility(View.VISIBLE);

                        }
                        else {
                            //Allow the admin to register new users
                            binding.registerBtn.setVisibility(View.VISIBLE);
                            binding.reportedPostsBtn.setVisibility(View.VISIBLE);
                            binding.transactionBtn.setVisibility(View.GONE);
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
                startActivity(new Intent(MenuActivity.this, RegisterActivity.class));

            }
        });

        //handle the update profile button
        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users
                startActivity(new Intent(MenuActivity.this, UpdateProfileActivity.class));

            }
        });

        //handle the see all users button
        binding.showAllUsersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users
                startActivity(new Intent(MenuActivity.this, MessagesActivity.class));

            }
        });




        //handle on click back button
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle the settings button
        binding.settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users
                startActivity(new Intent(MenuActivity.this, SettingsActivity.class));

            }
        });

        //handle the reported posts
        binding.reportedPostsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users
                startActivity(new Intent(MenuActivity.this, ReportedPostsActivity.class));

            }
        });

        //handle the reported posts
        binding.transactionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users
                startActivity(new Intent(MenuActivity.this, YourTransactionsActivity.class));

            }
        });


        //handle the deposit funds button
        binding.depositBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users
                startActivity(new Intent(MenuActivity.this, DepositActivity.class));

            }
        });

        //handle the deposit funds button
        binding.aboutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users
                startActivity(new Intent(MenuActivity.this, AboutActivity.class));

            }
        });

        //handle the proof of payments button
        binding.prooofOfPaymentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users
                startActivity(new Intent(MenuActivity.this, ProofOfPaymentsActivity.class));

            }
        });

        //handle the give us feedback button
        binding.feedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users
                startActivity(new Intent(MenuActivity.this, GiveUsFeedBackActivity.class));

            }
        });

        //handle the help button
        binding.helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users
                startActivity(new Intent(MenuActivity.this, HelpAndSupportActivity.class));

            }
        });




        //handle log out


        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // Create the object of AlertDialog Builder class
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
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


    }


    private void checkUser() {


        //get current user

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {

            //not logged in, go to main screen
            startActivity(new Intent(MenuActivity.this, MainActivity.class));
        }


    }

}
package com.example.communityconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


import com.example.communityconnect.databinding.ActivityDashboardBinding;
import com.example.communityconnect.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;

    String mUid;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());
        //int
        firebaseAuth = FirebaseAuth.getInstance();




        //ensuring that the fragments always change when selected
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment());
            }
            else if (item.getItemId() == R.id.donate) {
                replaceFragment(new DonateFragment());
            }
            else if (item.getItemId() == R.id.events) {
                replaceFragment(new EventsFragment());
            }

//            else if (item.getItemId() == R.id.profile) {
//                replaceFragment(new ProfileFragment());
//            }

            return true;
        });

        // Initialize database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");

        // Update the FCM token
        updateFCMToken();



    }

//    protected void onResume(){
//        checkUserStatus();
//        super.onResume();
//    }


    private void updateFCMToken() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String token = task.getResult();
                            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("Current_USERID", String.valueOf(firebaseUser));
                            updateTokenInDatabase(firebaseUser.getUid(), token);
                        } else {
                            // Handle the case where getting the token fails
                        }
                    });
        }
    }

    private void updateTokenInDatabase(String userId, String token) {
        Token tokenObject = new Token(token);
        databaseReference.child(userId).setValue(tokenObject);
    }

    public void updateToken(String token){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        databaseReference.child(mUid).setValue(mToken);
    }
    
    private void checkUserStatus(){
        //get current user


        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null){
            mUid = firebaseUser.getUid();
        }
        else {
            //user is not signed in go to main activity
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
        }
    }

    //ensuring that the fragments always change when selected
    private void replaceFragment(Fragment fragment){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =  fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();

    }
}
package com.example.communityconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.communityconnect.databinding.ActivityGiveUsFeedBackBinding;
import com.example.communityconnect.databinding.ActivityHelpAndSupportBinding;

public class HelpAndSupportActivity extends AppCompatActivity {

    private ActivityHelpAndSupportBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpAndSupportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        //handle the create post
        binding.postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users
                startActivity(new Intent(HelpAndSupportActivity.this, HelpCreatePostActivity2.class));

            }
        });


        //handle the create event
        binding.eventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users
                startActivity(new Intent(HelpAndSupportActivity.this, HelpCreateEventActivity.class));

            }
        });


        //handle the emergency button
        binding.emergencyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users
                startActivity(new Intent(HelpAndSupportActivity.this, HelpEmergencyActivity.class));

            }
        });


        //handle the donate
        binding.donateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add other users
                startActivity(new Intent(HelpAndSupportActivity.this, HelpDonateActivity.class));

            }
        });
    }
}
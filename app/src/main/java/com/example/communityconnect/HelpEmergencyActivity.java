package com.example.communityconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.communityconnect.databinding.ActivityHelpDonateBinding;
import com.example.communityconnect.databinding.ActivityHelpEmergencyBinding;

public class HelpEmergencyActivity extends AppCompatActivity {

    private ActivityHelpEmergencyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpEmergencyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
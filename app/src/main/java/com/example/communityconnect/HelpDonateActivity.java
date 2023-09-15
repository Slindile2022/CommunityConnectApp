package com.example.communityconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.communityconnect.databinding.ActivityHelpCreateEventBinding;
import com.example.communityconnect.databinding.ActivityHelpDonateBinding;

public class HelpDonateActivity extends AppCompatActivity {

    private ActivityHelpDonateBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpDonateBinding.inflate(getLayoutInflater());
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
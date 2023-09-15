package com.example.communityconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.communityconnect.databinding.ActivityAboutBinding;
import com.example.communityconnect.databinding.ActivityHelpCreateEventBinding;

public class HelpCreateEventActivity extends AppCompatActivity {

    private ActivityHelpCreateEventBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpCreateEventBinding.inflate(getLayoutInflater());
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
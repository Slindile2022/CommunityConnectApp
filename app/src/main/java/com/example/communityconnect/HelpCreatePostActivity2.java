package com.example.communityconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.communityconnect.databinding.ActivityHelpCreateEventBinding;
import com.example.communityconnect.databinding.ActivityHelpCreatePost2Binding;

public class HelpCreatePostActivity2 extends AppCompatActivity {

    private ActivityHelpCreatePost2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpCreatePost2Binding.inflate(getLayoutInflater());
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
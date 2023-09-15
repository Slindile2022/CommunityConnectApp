package com.example.communityconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.communityconnect.databinding.ActivityGiveUsFeedBackBinding;
import com.example.communityconnect.databinding.ActivityYourTransactionsBinding;

public class GiveUsFeedBackActivity extends AppCompatActivity {

    private ActivityGiveUsFeedBackBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGiveUsFeedBackBinding.inflate(getLayoutInflater());
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
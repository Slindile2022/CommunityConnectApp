package com.example.communityconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;


import com.example.communityconnect.databinding.ActivitySettingsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;


public class SettingsActivity extends AppCompatActivity {

    //view binding
    private ActivitySettingsBinding binding;

    SharedPreferences sp;
    SharedPreferences.Editor editor;


    private static final String  enabledMessage = "Notifications are enabled";
    private static final String  disabledMessage = "Notifications are disabled";

    private static final String TOPIC_POST_NOTIFICATION = "POST";


    private boolean isChecked = false;


    private FirebaseAuth firebaseAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();



        //int shared preferences

        sp = getSharedPreferences("Notification_SP", MODE_PRIVATE);
        boolean isPostEnabled = sp.getBoolean("" + TOPIC_POST_NOTIFICATION, false);


        //if enabled check switch, otherwise uncheck switch
        if (isPostEnabled){
            //was enabled
            binding.fcmSwitch.setChecked(true);
            binding.notificationStatusTv.setText(enabledMessage);
        }
        else {
            //was disabled
            binding.fcmSwitch.setChecked(false);
            binding.notificationStatusTv.setText(disabledMessage);


        }


        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        //add switch check change listener to enable/disable notifications

        binding.fcmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //edit switch state
                editor = sp.edit();
                editor.putBoolean(""+TOPIC_POST_NOTIFICATION, isChecked);
                editor.apply();

                if (isChecked){
                    //checked, allow notifications


                    subscribeToTopic();
                    Toast.makeText(SettingsActivity.this, "" + enabledMessage, Toast.LENGTH_SHORT).show();
                    binding.notificationStatusTv.setText(enabledMessage);

                }
                else {
                    //unchecked, allow notifications
                    unsubscribeToTopic();

                    Toast.makeText(SettingsActivity.this, "" + disabledMessage, Toast.LENGTH_SHORT).show();
                    binding.notificationStatusTv.setText(disabledMessage);
                }


            }
        });



    }



    // Inside your subscribeToTopic() method
    private void subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("" + TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {  // Use "this" as the context
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "" + enabledMessage, Toast.LENGTH_SHORT).show();
                            binding.notificationStatusTv.setText(enabledMessage);
                        } else {
                            Toast.makeText(SettingsActivity.this, "Failed to turn on notifications", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Inside your unsubscribeToTopic() method
    private void unsubscribeToTopic() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("" + TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {  // Use "this" as the context
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "" + disabledMessage, Toast.LENGTH_SHORT).show();
                            binding.notificationStatusTv.setText(disabledMessage);
                        } else {
                            Toast.makeText(SettingsActivity.this, "Failed to disable notifications", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



}
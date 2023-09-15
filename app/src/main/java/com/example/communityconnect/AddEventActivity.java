package com.example.communityconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.communityconnect.databinding.ActivityAddEventBinding;
import com.example.communityconnect.databinding.ActivityPostBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddEventActivity extends AppCompatActivity {

    private ActivityAddEventBinding binding;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    String uid; //current user id

    //FirebaseDatabase firebaseDatabase;

    String    currentUserType, currentUserName, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //int firebase auth

        firebaseAuth = FirebaseAuth.getInstance();





        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // Set an OnClickListener on the taskDate EditText
        binding.taskTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker();
            }
        });

        // Set an OnClickListener on the taskDate EditText
        binding.taskDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        //handle on click back button
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle add event button

        binding.addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Flow
                //input data


                validateData();




            }
        });



    }

    private  String eventTitle="", eventDescription="", eventDate="", eventTime="";
    private void validateData() {
        //validate user input if it is correct or not

        eventTitle = binding.addTaskTitle.getText().toString().trim();
        eventDescription = binding.addTaskDescription.getText().toString().trim();
        eventTime = binding.taskTime.getText().toString().trim();
        eventDate = binding.taskDate.getText().toString().trim();



        //validate data


        if(TextUtils.isEmpty(eventTitle)){

            Toast.makeText(this, "Event title is required", Toast.LENGTH_SHORT).show();
            return;

        }

        else if(eventTitle.length() > 18){

            Toast.makeText(this, "Event Title too long", Toast.LENGTH_SHORT).show();
            return;

        }

        else if(TextUtils.isEmpty(eventDescription)){

            Toast.makeText(this, "Event description is required", Toast.LENGTH_SHORT).show();
            return;

        }
        else if(eventDescription.length() < 100){

            Toast.makeText(this, "Event description is very short, less than 100 characters", Toast.LENGTH_SHORT).show();
            return;

        }
        else if (!validateDateTime()) {
            // validateDateTime() will return false if the date and time are invalid
            // Show a toast message indicating that the selected date and time have already passed
            Toast.makeText(this, "Selected date and time have already passed or Invalid Format", Toast.LENGTH_SHORT).show();
        }

         else {
                // Date and time are valid, add event
                addEvent();
            }



    }

    private void addEvent() {
        //adding the event into the realtime database

        progressDialog.setMessage("adding event...");
        progressDialog.show();


        //loading all the information from realtime database

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user type

                        String uid = "" + snapshot.child("uid").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String secondName = "" + snapshot.child("secondName").getValue();
                        String userType = "" + snapshot.child("userType").getValue();



                        //current userName
                        currentUserName = name +" "+secondName;

                        //current userType
                        currentUserType = userType;

                        //current user id
                        currentUserId = uid;


                        //get the time the post was uploaded

                        // Initialize the Firebase Database instance
                        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

                        final String timestamp= "" + System.currentTimeMillis();

                        String timeStamp = timestamp;
                        Events events = new Events(timeStamp, currentUserName, currentUserId, currentUserType, eventTitle, eventDescription, eventTime, eventDate, "upcoming", "0");

                        firebaseDatabase.getReference("Events").child(timestamp).setValue(events).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //if event was added on the realtime database

                                progressDialog.dismiss();
                                Toast.makeText(AddEventActivity.this, "event added", Toast.LENGTH_SHORT).show();


                                // Clear EditText fields to allow another user to be registered
                                binding.taskDate.setText("");
                                binding.taskTime.setText("");
                                binding.addTaskTitle.setText("");
                                binding.addTaskDescription.setText("");

                                //send the notification
                                prepareNotification(
                                        ""+uid,
                                        ""+currentUserName+ " added new event",
                                        ""+eventTitle+"\n"+eventDescription,
                                        "PostNotification",
                                        "POST");





                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //if event was not added on the realtime database

                                progressDialog.dismiss();
                                Toast.makeText(AddEventActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });




                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




    }

    private void showDatePicker() {
        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Handle the selected date
                        // For example, update the taskDate EditText with the selected date
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        binding.taskDate.setText(selectedDate);
                        eventDate = selectedDate;

                        // Call the validation method here
                        validateDateTime();
                    }
                },
                year, month, day
        );

        // Show the date picker dialog
        datePickerDialog.show();
    }

    private void showTimePicker() {
        // Allow the user to pick the time of the event
        // Get the current time
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create a TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Handle the selected time
                        // For example, update the taskTime EditText with the selected time
                        String selectedTime = hourOfDay + ":" + minute;
                        binding.taskTime.setText(selectedTime);
                        eventTime = selectedTime;

                        // Call the validation method here
                        validateDateTime();
                    }
                },
                hour, minute, false
        );

        // Show the time picker dialog
        timePickerDialog.show();
    }



    // Validate the selected date and time
    private boolean validateDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date selectedDateTime = dateFormat.parse(eventDate + " " + eventTime);
            Date currentDateTime = new Date(); // Current date and time

            return !selectedDateTime.before(currentDateTime);

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void prepareNotification(String pId, String postTitle, String postDescription, String notificationType, String notificationTopic){

        //prepare data for notification

        String NOTIFICATION_TOPIC = "/topics/" + notificationTopic; //topic must match with what the user subscribed to
        String NOTIFICATION_TITLE = postTitle; //e.g slindile maseko added a new post
        String NOTIFICATION_MESSAGE = postDescription; //content of post
        String NOTIFICATION_TYPE = notificationType; //the different types of notifications, event, comments, add post

        //prepare JSON what to send . and where to send
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();

        try {
            //WHAT TO SEND

            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("sender", uid); //current user id
            notificationBodyJo.put("pId", pId); //current user id
            notificationBodyJo.put("postTitle", NOTIFICATION_TITLE); //current user id
            notificationBodyJo.put("postDescription", NOTIFICATION_MESSAGE); //current user id

            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJo); //combine data to be sent

        } catch (JSONException e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //send post notification
        sendPostNotification(notificationJo);

    }

    private void sendPostNotification(JSONObject notificationJo) {

        //send volley object request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("FCM_RESPONSE", "onResponse: "+response.toString() );


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        //Toast error occurred
                        Toast.makeText(AddEventActivity.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=AAAApwKJ6dk:APA91bH1OeCBs0jCypk5iuDAwO0Iaz3hmNbzyip8eyTwlk50700sE6Y7u-w4R_zR1CFCdzNsrfsGouSpTvkwMig6OV2R0B8B8z1_nbxgeI_KzmAbVEPB16HUYvhl1R-gab5j1GFLisIg"); //your application fcm key here

                return headers;
            }
        };

        //enqueue the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);


    }




}





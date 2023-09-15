package com.example.communityconnect;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.communityconnect.databinding.FragmentEventsBinding;
import com.example.communityconnect.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventsFragment extends Fragment implements LocationListener {

    private FragmentEventsBinding binding;

    private static final int LOCATION_REQUEST_CODE = 100;
    //permission array
    private String[] locationPermissions;

    private LocationManager locationManager;

    private double latitude = 0.0 , longitude = 0.0;

    String city, countryName, state;

    String uid; //current user id

    private boolean locationDetectionInProgress = false;

    private boolean emergencyInfoUploaded = false;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


    RecyclerView recyclerView;

    EventsAdapter eventsAdapter;

    List<Events> eventsList;


    FirebaseDatabase firebaseDatabase;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EventsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventsFragment newInstance(String param1, String param2) {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        binding = FragmentEventsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();




        //initialize the location permissions

        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        //handle add task button
        binding.addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add post
                startActivity(new Intent(requireContext(), AddEventActivity.class));


            }

        });

        //handle emergency button
        binding.emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if (!locationDetectionInProgress) {
                    // Set the flag to indicate that location detection is in progress
                    locationDetectionInProgress = true;

                    if (checkLocationPermissions()) {
                        // Location is already enabled
                        detectLocation();

                        //send the information to realtime database
                        //uploadEmergencyInformation();

                    } else {
                        // Allow the user to enable location
                        requestLocationPermission();
                    }
                }


            }

        });


        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("Events");
        recyclerView = binding.taskRecycler;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventsList = new ArrayList<Events>();
        eventsAdapter = new EventsAdapter(getContext(), eventsList) {
        };
        recyclerView.setAdapter(eventsAdapter);

        // Reverse the layout manager to show the latest posts at the bottom
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);


        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {


                Events events = snapshot.getValue(Events.class);

                eventsList.add(events);

                eventsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }




        });



        return view;
    }


    private void detectLocation() {
        Toast.makeText(getActivity(), "Please wait...", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        // Check if GPS provider is enabled
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            Toast.makeText(getActivity(), "Please turn on GPS to get your location", Toast.LENGTH_LONG).show();
        }


    }

    private boolean checkLocationPermissions(){
       boolean result = ContextCompat.checkSelfPermission(getActivity(),
               Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED);

       return result;


    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(getActivity(), locationPermissions, LOCATION_REQUEST_CODE);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();

        // Call the uploadEmergencyInformation() method only if it hasn't been uploaded yet
        if (!emergencyInfoUploaded) {
            uploadEmergencyInformation();
            emergencyInfoUploaded = true; // Set the flag to true to indicate upload has occurred
        }

    }

    private void findAddress() {
        //find address, country, state, city

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (!addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0); // Complete address
                String city1 = addresses.get(0).getLocality();
                String state1 = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();

                // Assign the values
                countryName = country;
                city = city1;
                state = state1;
            }





        }
        catch (Exception e){
            //do nothing
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {


        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

        // Location disabled
        // Update the flag to indicate that location detection is not in progress
        locationDetectionInProgress = false;



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted){
                        //permission granted
                        detectLocation();
                    }
                }
                else {
                    //permission denied
                    Toast.makeText(getActivity(), "Location permission is necessary...", Toast.LENGTH_SHORT).show();

                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void uploadEmergencyInformation() {




        //get the time the post was uploaded

        final String timestamp = "" + System.currentTimeMillis();

        final String uid = firebaseAuth.getCurrentUser().getUid();




        //get user information first from the database


        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user type


                        String name = "" + snapshot.child("name").getValue();
                        String secondName = "" + snapshot.child("secondName").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();
                        String phone = "" +snapshot.child("phone").getValue();
                        String address = "" +snapshot.child("address").getValue();
                        String usertype = "" +snapshot.child("userType").getValue();

                        //make the email short


                        //set in the text view of the toolbar
                        final String userNames1 = name+ " "+secondName;



                        if (latitude == 0.0 || latitude == 0.0){
                            Toast.makeText(getActivity(), "please turn on GPS on your phone", Toast.LENGTH_SHORT).show();
                            return;
                        }



                        //add the data to the emergency table
                        EmergencyAlertData emergencyAlertData = new EmergencyAlertData(timestamp, phone, userNames1,latitude, longitude, city, state, countryName, usertype, address);




                        firebaseDatabase.getReference("Emergency").child(timestamp).setValue(emergencyAlertData).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //if emergency was sent successfully on the realtime database

                                Toast.makeText(getActivity(), "Emergency alert sent successfully", Toast.LENGTH_SHORT).show();

                                //send the notification
                                prepareNotification(
                                        ""+uid,
                                        ""+userNames1+ " is in Danger",
                                        "Try to reach out⚠️",
                                        "PostNotification",
                                        "POST");


                            }

                        });

                        // Convert latitude and longitude to strings
                        String latitudeString = Double.toString(latitude);
                        String longitudeString = Double.toString(longitude);


                        //add the data to the post description table
                        // Create the PostData object with converted latitude and longitude
                        PostData postData = new PostData(latitudeString, "I'm in a dire situation and require immediate assistance. Please, if anyone is available, I urgently need help", timestamp, uid, longitudeString, "", "noImage", "", "0", "0");



                        //updating the post table to ensure that the emergency also appears there
                        firebaseDatabase.getReference("Posts").child(timestamp).setValue(postData).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            //do nothing but i know that the data was uploaded successfully


                            }

                        });




                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });





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
            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(), ""+error.toString(), Toast.LENGTH_SHORT).show();
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
        Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);


    }

}
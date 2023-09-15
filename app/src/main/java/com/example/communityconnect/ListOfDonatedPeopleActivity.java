package com.example.communityconnect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.communityconnect.databinding.ActivityListOfDonatedPeopleBinding;
import com.example.communityconnect.databinding.ActivityProofOfPaymentsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ListOfDonatedPeopleActivity extends AppCompatActivity {

    private ActivityListOfDonatedPeopleBinding binding;

    FirebaseDatabase firebaseDatabase;

    FirebaseAuth firebaseAuth;

    String donateId;

    RecyclerView recyclerView;


    DonatedPeopleAdapter donatedPeopleAdapter;

    List<ListOfDonatedPeopleData> listOfDonatedPeopleDataList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListOfDonatedPeopleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //getting the values
        Intent intent = getIntent();
        donateId = intent.getStringExtra("postId");



        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });





        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference1 = firebaseDatabase.getReference().child("Donate").child(donateId).child("donatedPeople");
        recyclerView = binding.proofOfPayments;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ListOfDonatedPeopleActivity.this));

        listOfDonatedPeopleDataList = new ArrayList<ListOfDonatedPeopleData>();
        donatedPeopleAdapter = new DonatedPeopleAdapter(ListOfDonatedPeopleActivity.this , listOfDonatedPeopleDataList);
        recyclerView.setAdapter(donatedPeopleAdapter);

        // Reverse the layout manager to show the latest posts at the bottom
        LinearLayoutManager layoutManager = new LinearLayoutManager(ListOfDonatedPeopleActivity.this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);



        databaseReference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {


                ListOfDonatedPeopleData modelClass = snapshot.getValue(ListOfDonatedPeopleData.class);

                listOfDonatedPeopleDataList.add(modelClass);

                donatedPeopleAdapter.notifyDataSetChanged();
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


    }
}
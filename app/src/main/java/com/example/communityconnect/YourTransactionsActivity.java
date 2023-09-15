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
import com.example.communityconnect.databinding.ActivityYourTransactionsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class YourTransactionsActivity extends AppCompatActivity {


    private ActivityYourTransactionsBinding binding;
    FirebaseDatabase firebaseDatabase;

    FirebaseAuth firebaseAuth;

    String userId, donateId;

    RecyclerView recyclerView;


    DonatedPeopleAdapter donatedPeopleAdapter1;

    List<ListOfDonatedPeopleData> listOfDonatedPeopleDataList1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityYourTransactionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        //procedure to get the user information
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user type

                        String userId1 = "" + snapshot.child("uid").getValue();
                        userId = userId1;


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });





        firebaseDatabase = FirebaseDatabase.getInstance();
       DatabaseReference databaseReference1 = firebaseDatabase.getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("allTransactions");
       // DatabaseReference databaseReference1 = firebaseDatabase.getReference().child("Donate").child(donateId).child("donatedPeople");
        recyclerView = binding.proofOfPayments;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(YourTransactionsActivity.this));

        listOfDonatedPeopleDataList1 = new ArrayList<ListOfDonatedPeopleData>();
        donatedPeopleAdapter1 = new DonatedPeopleAdapter(YourTransactionsActivity.this , listOfDonatedPeopleDataList1);
        recyclerView.setAdapter(donatedPeopleAdapter1);

        // Reverse the layout manager to show the latest posts at the bottom
        LinearLayoutManager layoutManager = new LinearLayoutManager(YourTransactionsActivity.this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);



        databaseReference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {


                ListOfDonatedPeopleData modelClass = snapshot.getValue(ListOfDonatedPeopleData.class);

                listOfDonatedPeopleDataList1.add(modelClass);

                donatedPeopleAdapter1.notifyDataSetChanged();
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
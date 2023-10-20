package com.example.communityconnect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.communityconnect.databinding.ActivityProofOfPaymentsBinding;
import com.example.communityconnect.databinding.ActivityReportedPostsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProofOfPaymentsActivity extends AppCompatActivity {

    private ActivityProofOfPaymentsBinding binding;

    FirebaseDatabase firebaseDatabase;

    FirebaseAuth firebaseAuth;

    RecyclerView recyclerView;


    PaymentsAdapter paymentsAdapter;

    List<PaymentData> paymentDataList;

    private  String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProofOfPaymentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //getting the values
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        //get usertype and id to load the correct information about them

        //int firebase auth

        firebaseAuth = FirebaseAuth.getInstance();

        //getting your information from realtime database to allow users to access the button or not to register new users

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        //check user type to enable the visibility to add new users to the system

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user type

                        String userType = "" + snapshot.child("userType").getValue();
                        String currentUserID = firebaseUser.getUid();




                        //allow authorized users to access the button
                        if(userType.equals("verified User")){

                            //allow them to see all the proof of payments


                            firebaseDatabase = FirebaseDatabase.getInstance();
                            DatabaseReference databaseReference1 = firebaseDatabase.getReference().child("PaymentFiles");
                            recyclerView = binding.proofOfPayments;
                            recyclerView.setHasFixedSize(true);
                            recyclerView.setLayoutManager(new LinearLayoutManager(ProofOfPaymentsActivity.this));

                            paymentDataList = new ArrayList<PaymentData>();
                            paymentsAdapter = new PaymentsAdapter(ProofOfPaymentsActivity.this, paymentDataList);
                            recyclerView.setAdapter(paymentsAdapter);

                            // Reverse the layout manager to show the latest posts at the bottom
                            LinearLayoutManager layoutManager = new LinearLayoutManager(ProofOfPaymentsActivity.this);
                            layoutManager.setReverseLayout(true);
                            layoutManager.setStackFromEnd(true);
                            recyclerView.setLayoutManager(layoutManager);



                            databaseReference1.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {


                                    PaymentData modelClass = snapshot.getValue(PaymentData.class);

                                    paymentDataList.add(modelClass);

                                    paymentsAdapter.notifyDataSetChanged();
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
                        else{

                            //user can see only their proof of payments


                            firebaseDatabase = FirebaseDatabase.getInstance();
                            // Get the DatabaseReference
                            DatabaseReference databaseReference = firebaseDatabase.getReference().child("PaymentFiles");

                            // Apply query to DatabaseReference to make sure that you can see only the transactions corresponding to your uid
                            Query query = databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid());
                            recyclerView = binding.proofOfPayments;
                            recyclerView.setHasFixedSize(true);
                            recyclerView.setLayoutManager(new LinearLayoutManager(ProofOfPaymentsActivity.this));

                            paymentDataList = new ArrayList<PaymentData>();
                            paymentsAdapter = new PaymentsAdapter(ProofOfPaymentsActivity.this, paymentDataList);
                            recyclerView.setAdapter(paymentsAdapter);

                            // Reverse the layout manager to show the latest posts at the bottom
                            LinearLayoutManager layoutManager = new LinearLayoutManager(ProofOfPaymentsActivity.this);
                            layoutManager.setReverseLayout(true);
                            layoutManager.setStackFromEnd(true);
                            recyclerView.setLayoutManager(layoutManager);



                            query.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {


                                    PaymentData modelClass = snapshot.getValue(PaymentData.class);

                                    paymentDataList.add(modelClass);

                                    paymentsAdapter.notifyDataSetChanged();
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

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }
}
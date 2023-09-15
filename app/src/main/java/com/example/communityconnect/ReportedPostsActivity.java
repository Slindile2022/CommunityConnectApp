package com.example.communityconnect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;

import com.example.communityconnect.databinding.ActivityMessagesBinding;
import com.example.communityconnect.databinding.ActivityReportedPostsBinding;
import com.example.communityconnect.databinding.FragmentHomeBinding;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

public class ReportedPostsActivity extends AppCompatActivity {

    private ActivityReportedPostsBinding binding;

    FirebaseDatabase firebaseDatabase;

    RecyclerView recyclerView;

    Query databaseReference;

    ReportedPostsAdapter postsAdapter;

    List<PostData> postDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportedPostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("ReportedPosts");
        recyclerView = binding.allPostsEt;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ReportedPostsActivity.this));

        postDataList = new ArrayList<PostData>();
        postsAdapter = new ReportedPostsAdapter(ReportedPostsActivity.this, postDataList);
        recyclerView.setAdapter(postsAdapter);

        // Reverse the layout manager to show the latest posts at the bottom
        LinearLayoutManager layoutManager = new LinearLayoutManager(ReportedPostsActivity.this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);



        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {


                PostData modelClass = snapshot.getValue(PostData.class);

                postDataList.add(modelClass);

                postsAdapter.notifyDataSetChanged();
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
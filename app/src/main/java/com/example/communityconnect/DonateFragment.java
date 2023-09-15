package com.example.communityconnect;

import static com.google.android.material.color.utilities.MaterialDynamicColors.error;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.communityconnect.databinding.FragmentDonateBinding;
import com.example.communityconnect.databinding.FragmentHomeBinding;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DonateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DonateFragment extends Fragment {

    private FragmentDonateBinding binding;

    private FirebaseDatabase firebaseDatabase;

    private FirebaseAuth firebaseAuth;


    RecyclerView recyclerView;

    Query databaseReference;

    DonateAdapter donateAdapter;

    List<DonateData> donateDataList;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DonateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DonateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DonateFragment newInstance(String param1, String param2) {
        DonateFragment fragment = new DonateFragment();
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
        binding = FragmentDonateBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        //int firebase auth

        firebaseAuth = FirebaseAuth.getInstance();



        //making sure that only verified user can open a donation platform
        //getting your information from realtime database to allow users to access the button or not to register new users

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();



        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user type

                        String userType = "" + snapshot.child("userType").getValue();
                        String amount =  "" + snapshot.child("amount").getValue();

                        //set the amount

                        binding.balance.setText("Balance : R" +amount);




                        //allow authorized users to access the button
                        if(!userType.equals("verified User") && !userType.equals("Organization")){

                            //open the activity to add other users
                            //startActivity(new Intent(requireContext(), RegisterActivity.class));
                            binding.addPostBtn.setVisibility(View.GONE);
                        }
                        else {
                            //Allow the admin to register new users
                            binding.addPostBtn.setVisibility(View.VISIBLE);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //now handling the on click button add donation
        binding.addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open the activity to add donation activity
                startActivity(new Intent(requireContext(), DonateActivity.class));


            }
        });

        //handle the searching

        binding.searchAllPost1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    donateAdapter.getFilter().filter(s);
                }
                catch (Exception exception){
                    exception.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });





        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Donate");
        recyclerView = binding.allPostsEt;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        donateDataList = new ArrayList<DonateData>();
        donateAdapter = new DonateAdapter(getContext(), donateDataList);
        recyclerView.setAdapter(donateAdapter);

        // Reverse the layout manager to show the latest posts at the bottom
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);



        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {


                DonateData donateData = snapshot.getValue(DonateData.class);

                donateDataList.add(donateData);

                donateAdapter.notifyDataSetChanged();
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


}
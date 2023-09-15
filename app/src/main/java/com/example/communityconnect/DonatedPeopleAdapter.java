package com.example.communityconnect;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DonatedPeopleAdapter extends RecyclerView.Adapter<DonatedPeopleAdapter.ViewHolder>  {


    Context context;
    List<ListOfDonatedPeopleData> listOfDonatedPeopleDataList;
    List<ListOfDonatedPeopleData> filterList;

    String myUid1 ;





    //getting constructor


    public DonatedPeopleAdapter(Context context, List<ListOfDonatedPeopleData> listOfDonatedPeopleDataList) {
        this.context = context;
        this.listOfDonatedPeopleDataList =listOfDonatedPeopleDataList;
        this.filterList = listOfDonatedPeopleDataList;

        myUid1 = FirebaseAuth.getInstance().getCurrentUser().getUid();


    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_list_of_donated_people, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        //here we are binding the data

        ListOfDonatedPeopleData listOfDonatedPeopleData = listOfDonatedPeopleDataList.get(position);
        String firstName = listOfDonatedPeopleData.getUserNames();
        String donatedAmount = listOfDonatedPeopleData.getDonatedAmount();
        String donationID = listOfDonatedPeopleData.getDonationID();
        String  timeStamp = listOfDonatedPeopleData.getTimeStamp();
        String userId = listOfDonatedPeopleData.getUserId();







        if (myUid1.equals(userId)){
            holder.userName.setText("You donated - R" +donatedAmount);
        }
        else {
            holder.userName.setText(firstName+ " donated + R" +donatedAmount);
        }







    }

    @Override
    public int getItemCount() {
        return listOfDonatedPeopleDataList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        // declaring the items

        TextView userName;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            userName = itemView.findViewById(R.id.uNameEt);





        }
    }

}

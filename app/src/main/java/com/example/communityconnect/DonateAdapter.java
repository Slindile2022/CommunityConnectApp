package com.example.communityconnect;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;




public class DonateAdapter extends RecyclerView.Adapter<DonateAdapter.ViewHolder> implements Filterable {

    Context context;
    List<DonateData> donateDataList,filterLists;

    String balance, myUid, userName;





    FilterDonate filter;

    //getting constructor


    public DonateAdapter(Context context, List<DonateData> donateDataList) {
        this.context = context;
        this.donateDataList =donateDataList;
        this.filterLists = donateDataList;


        //procedure to check the balance
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user type

                        String balance1 = "" + snapshot.child("amount").getValue();
                        String userId = "" + snapshot.child("uid").getValue();
                        myUid = userId;
                        balance = balance1;
                        String userNames = "" + snapshot.child("name").getValue() +" " +snapshot.child("secondName").getValue() ;
                        userName = userNames;



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



    }

    @NonNull
    @Override
    public DonateAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_donate, parent, false);
        return new DonateAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull DonateAdapter.ViewHolder holder, int position) {

        //here we are binding the data


        DonateData donateData = donateDataList.get(position);
        String userType =donateData.getUserType();
        String donateImage = donateData.getDonateImage();
        String donateDescription = donateData.getDonateDescription();
        String donateId  = donateData.getTimeStamp();
        String donatedAmount = donateData.getAmount();


        holder.productTitle.setText(donateData.getDonateTitle());
       // holder.productDescription.setText(donateData.getDonateDescription());
        holder.organizationName.setText("From "+donateData.getUserName());
        holder.totalAmount.setText("Total amount : R" +donatedAmount);



        // Showing the post from verified users with verification icons


        if (userType != null && userType.equals("verified User")) {
            holder.verificationIcon.setVisibility(View.VISIBLE);
        } else {
            holder.verificationIcon.setVisibility(View.GONE);
        }


        //making sure that post without images also display

        if (donateImage.equals("noImage")){
            //hide the imageview on the recycle view
            holder.imageView.setVisibility(View.GONE);
        }
        else{
            //hide the imageview on the recycle view
            holder.imageView.setVisibility(View.VISIBLE);

        }


        //making sure that we have the see more option in case a message is tool long
        if (donateDescription.length() > 150) {
            holder.productDescription.setText(donateDescription.substring(0, 150) + "...");
            holder.seeMore.setVisibility(View.VISIBLE);

            holder.seeMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.productDescription.setText(donateDescription);
                    holder.seeMore.setVisibility(View.GONE);
                    holder.seeLess.setVisibility(View.VISIBLE);
                }
            });

            holder.seeLess.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.productDescription.setText(donateDescription.substring(0, 150) + "...");
                    holder.seeLess.setVisibility(View.GONE);
                    holder.seeMore.setVisibility(View.VISIBLE);
                }
            });
        } else {
            holder.productDescription.setText(donateDescription);
            holder.seeMore.setVisibility(View.GONE);
        }


        //setting up image if it is available
        String imageUri = null;
        imageUri = donateData.getDonateImage();
        Picasso.get().load(imageUri).into(holder.imageView);



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ListOfDonatedPeopleActivity.class);
                intent.putExtra("postId", donateId);
                intent.putExtra("userId", myUid);
                context.startActivity(intent);
            }
        });



        //handling donate click

        holder.addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //add this information to the database under own transactions and the donation database


                double convertedBalance = Double.parseDouble(donatedAmount);
                double myBalance = Double.parseDouble(balance);





                //money analog

                androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Enter amount");

                // Set up the input
                final EditText input = new EditText(view.getContext());

                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                builder.setView(input);


                // Set up the buttons
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String amount = input.getText().toString();


                        //checking if the amount is entered or not
                        if (amount.isEmpty()) {
                            Toast.makeText(view.getContext(), "amount is empty", Toast.LENGTH_SHORT).show();

                        } else if (Double.parseDouble(amount) > myBalance ) {
                            Toast.makeText(view.getContext(), "please recharge your account", Toast.LENGTH_SHORT).show();
                        } else {

                            //add the deposit with the current balance

                            double finalBalance = convertedBalance +  Double.parseDouble(amount);


                            //update the balance of the user

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Donate").child(donateId);
                            ref.child("amount").setValue(Double.toString(finalBalance))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                                    Toast.makeText(view.getContext(), "donated successfully", Toast.LENGTH_SHORT).show();

                                                    //deduct the users current balance

                                                    double myFinalBalance = myBalance - Double.parseDouble(amount);
                                                    //update the balance of the user


                                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(myUid);
                                                    ref.child("amount").setValue(Double.toString(myFinalBalance));

                                        }
                                    });


                            //update all transactions under users

                            String timeStamp = "" + System.currentTimeMillis();
//
//                            DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("Users").child(myUid).child("allTransactions").child(timeStamp);
//                            ref1.child("donationId").setValue(donateId);
//                            ref1.child("userId").setValue(myUid);
//                            ref1.child("timeStamp").setValue(timeStamp);
//                            ref1.child("amountDonated").setValue(amount);
//                            ref1.child("userNames").setValue(userName);

                            DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("Users").child(myUid).child("allTransactions").child(timeStamp);

                            ListOfDonatedPeopleData  listOfDonatedPeopleData = new ListOfDonatedPeopleData(userName, amount, donateId, myUid, timeStamp);
                            ref1.setValue(listOfDonatedPeopleData);




                            //update all transactions the specific donation

//                            DatabaseReference ref11 = FirebaseDatabase.getInstance().getReference().child("Donate").child(donateId).child("donatedPeople").child(timeStamp);
//                            ref11.child("donationId").setValue(donateId);
//                            ref11.child("userId").setValue(myUid);
//                            ref11.child("timeStamp").setValue(timeStamp);
//                            ref11.child("amountDonated").setValue(amount);
//                            ref11.child("userNames").setValue(userName);

                            DatabaseReference ref11 = FirebaseDatabase.getInstance().getReference().child("Donate").child(donateId).child("donatedPeople").child(timeStamp);
                            ref11.setValue(listOfDonatedPeopleData);



                        }


                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    }
                });

                builder.show();


            }
        });





    }



    @Override
    public int getItemCount() {
        return donateDataList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterDonate(DonateAdapter.this, filterLists);
        }
        return filter;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // declaring the items for our recycle view
        ImageView imageView,verificationIcon;
        TextView productTitle, productDescription, addToCart, organizationName, seeMore, seeLess, totalAmount;






        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.productIconTv);
            productTitle = itemView.findViewById(R.id.titleTv);
            productDescription = itemView.findViewById(R.id.descriptionTv);
            addToCart = itemView.findViewById(R.id.addToCart);
            organizationName = itemView.findViewById(R.id.postedBy);
            verificationIcon = itemView.findViewById(R.id.verified_icon);
            seeMore = itemView.findViewById(R.id.seeMore);
            seeLess = itemView.findViewById(R.id.seeLess);
            totalAmount = itemView.findViewById(R.id.totalAmount);




        }
    }
}

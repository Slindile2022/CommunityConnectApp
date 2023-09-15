package com.example.communityconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.communityconnect.databinding.ActivityPaymentDetailsBinding;
import com.example.communityconnect.databinding.ActivityReportedPostsBinding;
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

import java.util.List;

public class PaymentDetailsActivity extends AppCompatActivity {


    private ActivityPaymentDetailsBinding binding;


    private FirebaseAuth firebaseAuth;


    String userNames, timeStamp, status, phoneNumber, pdfLink, userId, postId, userType, currentUserType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //getting the values
        Intent intent = getIntent();
        userNames = intent.getStringExtra("userNames");
        timeStamp = intent.getStringExtra("timeStamp");
        status = intent.getStringExtra("status");
        phoneNumber = intent.getStringExtra("phoneNumber");
        pdfLink = intent.getStringExtra("pdfLink");
        userId = intent.getStringExtra("userId");
        postId = intent.getStringExtra("postId");


        //assigning those values to binding
        binding.nameTv.setText(userNames);
        binding.dateTv.setText(timeStamp);
        binding.statusTv.setText(status);



        //handle back button
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle the view document click
        binding.pdfFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open order details, we need  orderId and user id
                Intent intent = new Intent(PaymentDetailsActivity.this, ViewTheFileActivity.class);
                intent.putExtra("pdfLink", pdfLink);
                intent.putExtra("userUid", userId);
                startActivity(intent);


            }
        });



        //trying to hide the button to deposit money or change status if you are not an admin
        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference1.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user type

                        String userType = "" + snapshot.child("userType").getValue();


                        if (!userType.equals("verified User")){
                            binding.editBtn.setVisibility(View.GONE);
                            binding.moneyBtn.setVisibility(View.GONE);
                        }



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });





        //handle the add balance button

        binding.moneyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                databaseReference.child(userId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                //get user balance
                                String currentBalance = "" + snapshot.child("amount").getValue();
                                double convertedBalance = Double.parseDouble(currentBalance);





                                //money analog

                                AlertDialog.Builder builder = new AlertDialog.Builder(PaymentDetailsActivity.this);
                                builder.setTitle("Enter amount");

                                // Set up the input
                                final EditText input = new EditText(PaymentDetailsActivity.this);

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
                                            Toast.makeText(PaymentDetailsActivity.this, "amount is empty", Toast.LENGTH_SHORT).show();

                                        } else {

                                            //add the deposit with the current balance

                                            double finalBalance = convertedBalance +  Double.parseDouble(amount);


                                            //update the balance of the user

                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                                            ref.child("amount").setValue(Double.toString(finalBalance))
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            //print that the amount was updated successfully
                                                            Toast.makeText(PaymentDetailsActivity.this, "Amount Updated", Toast.LENGTH_SHORT).show();

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(PaymentDetailsActivity.this, "Failed to update the amount", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });


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

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


            }
        });


        //handle the status click button

        binding.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //changing the status of the uploaded proof of payment

                //Dialog

                AlertDialog.Builder builder = new AlertDialog.Builder(PaymentDetailsActivity.this);
                builder.setTitle("Deposit status")
                        .setItems(Category.status, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get picked category


                                String selected = Category.status[which];


                                //update order status under users

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("PaymentFiles").child(postId);
                                ref.child("status").setValue(selected);


                            }


                        }).show();


            }
        });


    }

}

package com.example.communityconnect;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.ViewHolder>  {


    Context context;
    List<PaymentData> paymentDataList;
    List<PaymentData> filterList;

    String correctedDate ;




    //getting constructor


    public PaymentsAdapter(Context context, List<PaymentData> paymentDataList) {
        this.context = context;
        this.paymentDataList =paymentDataList;
        this.filterList = paymentDataList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_payment_approval, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //here we are binding the data

        PaymentData modelUsers = paymentDataList.get(position);
        String firstName = modelUsers.getUserNames();
        String uid = modelUsers.getUid();
        String fileLink = modelUsers.getFileLink();
        String status = modelUsers.getStatus();
        String amount = modelUsers.getAmount();
        String postTime = modelUsers.getTimeStamp();





        holder.userName.setText(firstName);
        holder.comment.setText(amount);
        holder.time.setText(status);
        holder.status.setText(status);

        //handling the time the payslip was sent

        // Get the timestamp of the post (you mentioned it's in the orderTime variable)
        long postTimestamp = Long.parseLong(postTime);
        long currentTimestamp = System.currentTimeMillis();

        // Calculate the time difference in milliseconds
        long timeDifference = currentTimestamp - postTimestamp;

        // Get the relative time span string based on the time difference
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(postTimestamp, currentTimestamp, DateUtils.MINUTE_IN_MILLIS);

        // If the time difference is less than an hour, display "just now" or the number of minutes
        if (timeDifference < DateUtils.SECOND_IN_MILLIS) {
            // Set the time ago text
            holder.time.setText("just now");
        } else if (timeDifference < DateUtils.DAY_IN_MILLIS) {
            // If the time difference is less than a day, display the number of minutes or "hour ago" if it's just over an hour
            int minutesAgo = (int) (timeDifference / DateUtils.MINUTE_IN_MILLIS);
            int hoursAgo = minutesAgo / 60;
            if (minutesAgo < 60) {
                String timeText =  minutesAgo + " minutes ago";
                holder.time.setText(timeText);
                correctedDate = timeText;
            } else if(minutesAgo >= 60){
                String timeText = hoursAgo + " hours ago";
                holder.time.setText(timeText);
                correctedDate = timeText;
            }
        } else if (timeDifference < DateUtils.WEEK_IN_MILLIS) {
            // If the time difference is less than a week, display the number of days ago
            int daysAgo = (int) (timeDifference / DateUtils.DAY_IN_MILLIS);
            String timeText =  daysAgo + " days ago";
            holder.time.setText(timeText);
            correctedDate = timeText;
        } else {
            // If it's more than a week, display the date and time in the desired format
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(postTimestamp);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH);
            String formattedDateTime = dateFormat.format(calendar.getTime());

            // Set the date and time text
            holder.time.setText(formattedDateTime);
            correctedDate = formattedDateTime;
        }


        //must be able to see the whole details about payment
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details, we need  orderId and user id
                Intent intent = new Intent(context, PaymentDetailsActivity.class);
                intent.putExtra("userNames", firstName);
                intent.putExtra("timeStamp", correctedDate);
                intent.putExtra("status", status);
                intent.putExtra("phoneNumber", "0729499847");
                intent.putExtra("pdfLink", fileLink);
                intent.putExtra("userId", uid);
                intent.putExtra("postId", postTime);
                context.startActivity(intent);



            }
        });












    }

    @Override
    public int getItemCount() {
        return paymentDataList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        // declaring the items

        TextView userName, comment, time, status;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            userName = itemView.findViewById(R.id.uNameEt);
            comment = itemView.findViewById(R.id.commentEt);
            time = itemView.findViewById(R.id.timeEt);
            status = itemView.findViewById(R.id.statusEt);




        }
    }

}

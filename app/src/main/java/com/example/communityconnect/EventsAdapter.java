package com.example.communityconnect;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public abstract class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder>{

    Context context;
    List<Events> eventsList;

    String myUid;


    private DatabaseReference postsRef;

    private DatabaseReference likesRef;


    boolean mProcessLike = false;


    //getting constructor


    public EventsAdapter(Context context, List<Events> eventsList) {
        this.context = context;
        this.eventsList = eventsList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("EventLikes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Events");
    }

    @NonNull
    @Override
    public EventsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new EventsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //getting the values from the database
        Events events = eventsList.get(position);
        String userType =events.getUserType();
        String userName = events.getUserName();
        String eventTime =events.getEventTime();
        String eventDate =events.getEventDate();
        String eventTitle =events.getEventTitle();
        String eventId = events.getTimeStamp();
        String eventDescription =events.getEventDescription();
        String eventStatus =events.getEventStatus();
        String userId = events.getUserId();
        String pLikes = events.getpLikes(); //total number of likes per post


        //here we are binding the data
        holder.eventTitle.setText(eventTitle);
        holder.eventDescription.setText(eventDescription);
        holder.eventStatus.setText(eventStatus);
        holder.postedBy.setText("From "+userName);
        holder.eventTime.setText(eventTime);
        holder.totalUsers.setText(pLikes);

        //set likes for each post
        setLikes(holder, eventId, pLikes);

        // Showing the post from verified users with verification icons


        if (userType != null && ( userType.equals("verified User") || userType.equals("Organization"))) {
            holder.verificationIcon.setVisibility(View.VISIBLE);
        } else {
            holder.verificationIcon.setVisibility(View.GONE);
        }

        if (userId.equals(myUid)){

            //the see more button must be visible as the owner
            holder.moreBtn.setVisibility(View.VISIBLE);
        }
        else {
            //remove the see more button
            holder.moreBtn.setVisibility(View.GONE);
        }


        // Parsing the eventDate to extract day, month, and date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateFormat.parse(eventDate));

            // Get day, month, and date components
            String dayOfWeek = new SimpleDateFormat("EEE", Locale.US).format(calendar.getTime());
            String month = new SimpleDateFormat("MMM", Locale.US).format(calendar.getTime());
            String date = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

            // Set the values in your TextViews
            holder.eventDay.setText(dayOfWeek);
            holder.eventMonth.setText(month);
            holder.eventDate.setText(date);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //working on the more button
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions(holder.moreBtn, userId, myUid, eventId, eventDate, eventDescription, eventStatus, eventTime, eventTitle, userName, userType, pLikes);
            }
        });

        //handle if the user is attending the event or not
        //comment click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    //user can choose to attend the event

                    if (eventStatus.equals("upcoming")) {
                        // Create the object of AlertDialog Builder class
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                        // AlertDialog.Builder builder = new AlertDialog.Builder(ProfileFragment.this);

                        // Set the title show for the Alert time
                        builder.setTitle("Reserve");

                        // Set the message show for the Alert time
                        builder.setMessage("Are you sure you want to reserve/unReserve space");


                        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
                        builder.setCancelable(false);

                        // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
                        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                            // When the user click yes button then app will close

                            //reserve the seat for the event,if it was not reserved before
                           attendEvent(pLikes, eventId);


                        });

                        // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
                        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                            // If user click no then dialog box is canceled.
                            dialog.cancel();
                        });

                        // Create the Alert dialog
                        AlertDialog alertDialog = builder.create();
                        // Show the Alert Dialog box
                        alertDialog.show();
                    }
                else {

                    //user can not delete the comment
                    Toast.makeText(context, "the event has ended", Toast.LENGTH_SHORT).show();

                }
            }
        });







    }

    private void attendEvent(String  eventAttend, String eventId) {
        //allow the user to increase the number of people attending the event

        int pLikes = Integer.parseInt(eventAttend);
        mProcessLike = true;

        //get the id of the post clicked
        String postIde = eventId;
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (mProcessLike){
                    if (snapshot.child(postIde).hasChild(myUid)){
                        //already liked, so remove like
                        postsRef.child(postIde).child("pLikes").setValue(""+(pLikes - 1));
                        likesRef.child(postIde).child(myUid).removeValue();
                        mProcessLike = false;
                    }
                    else {
                        //not liked, like it
                        postsRef.child(postIde).child("pLikes").setValue(""+(pLikes + 1));
                        likesRef.child(postIde).child(myUid).setValue("Liked");
                        mProcessLike = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setLikes(final EventsAdapter.ViewHolder holder, final String postKey, final String eventAttend) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postKey).hasChild(myUid)) {
                    // User has liked this post

                    int pLikes = Integer.parseInt(eventAttend) - 1; //reduce the number of likes if user has liked the event

                    holder.totalUsers.setText("you and "+pLikes+ " others");
                } else {
                    // User has not liked this post
                    holder.totalUsers.setText(eventAttend);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String userId, String myUid, String eventId, String eventDate, String eventDescription, String eventStatus, String eventTime, String eventTitle, String userName, String userType, String pLikes) {
        //creating popup menu currently having option delete

        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);
        //show delete option in only post currently signed in user
        if (userId.equals(myUid) && eventStatus.equals("upcoming")){
            //add item in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Update");
            popupMenu.getMenu().add(Menu.NONE, 2, 0, "Completed");

        }

        // popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Detail");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id ==0){
                    //delete is clicked
                    beginDelete(eventId);


                } else if (id == 2) {

                    updateStatus(eventId, userId,  myUid,  eventId,  eventDate, eventDescription, eventStatus,  eventTime,eventTitle, userName, userType, pLikes);

                }
                return false;
            }
        });

        //show menu
        popupMenu.show();

    }

    private void updateStatus(String eventId, String userId, String myUid, String id, String eventDate, String eventDescription, String eventStatus, String eventTime, String eventTitle, String userName, String userType, String pLikes) {
        //update the status of the event to completed if it has passed the date


        //Progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Updating");


        String status = "Completed";

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Events").child(eventId);
        reference.child("eventStatus").setValue(status)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        //after successfully updating your status
                        pd.dismiss();
                        Toast.makeText(context, "updated successfully", Toast.LENGTH_SHORT).show();

                        Query query = FirebaseDatabase.getInstance().getReference("Events").orderByChild("timeStamp").equalTo(eventId);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                                    dataSnapshot.getRef().removeValue();

                                }

                              pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //if failed to change the status
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });




    }

    private void beginDelete(String eventId) {

        //deleting event

        //Progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting");


        Query query = FirebaseDatabase.getInstance().getReference("Events").orderByChild("timeStamp").equalTo(eventId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    dataSnapshot.getRef().removeValue();

                }
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return eventsList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        // declaring the items

        ImageButton moreBtn;
        ImageView verificationIcon;
        TextView eventTitle, eventDescription, eventTime, eventDay, eventDate, eventMonth, eventStatus, postedBy, totalUsers;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            eventTitle = itemView.findViewById(R.id.title);
            eventDescription = itemView.findViewById(R.id.description);
            eventTime= itemView.findViewById(R.id.time);
            eventDay = itemView.findViewById(R.id.day);
            eventDate = itemView.findViewById(R.id.date);
            eventMonth = itemView.findViewById(R.id.month);
            eventStatus = itemView.findViewById(R.id.status);
            postedBy = itemView.findViewById(R.id.postedBy);
            verificationIcon = itemView.findViewById(R.id.verified_icon);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            totalUsers = itemView.findViewById(R.id.totalUsers);




        }
    }
}

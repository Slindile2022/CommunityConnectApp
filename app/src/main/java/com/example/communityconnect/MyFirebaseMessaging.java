package com.example.communityconnect;


import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    private static final String ADMIN_CHANNEL_ID = "admin_channel";
    String currentUser1;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        //current user
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            currentUser1 = currentUserId;
            // Now you have the current user's ID in the variable currentUserId
        } else {
            // No user is currently signed in, do not get notification
            return;

        }
        //all notifications will be received here

        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String savedCurrentUser = sp.getString("Current_USERID", "None");

        //let us say there are two types of notifications
        String notificationType = message.getData().get("notificationType");

       // String notificationType = message.getData().get("notificationType");
        if (notificationType != null && notificationType.equals("PostNotification")) {
            String sender = message.getData().get("sender");
            String pId = message.getData().get("pId");
            String postTitle = message.getData().get("postTitle");
            String postDescription = message.getData().get("postDescription");

            // Handle the notification logic here
            showPostNotification(pId, postTitle, postDescription);
        }





    }

    private void showPostNotification(String pId, String postTitle, String postDescription) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);


        int notificationID  = new Random().nextInt(3000);


        
        //app version
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            setupPostNotificationChannel(notificationManager);
        }

        //show post details activity using id when notification is clicked
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_IMMUTABLE);

        //large icon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.firebase_logo);

        //sound for notification
        Uri notificationSoundUrl = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ""+ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.firebase_logo)
                .setLargeIcon(largeIcon)
                .setContentTitle(postTitle)
                .setContentText(postDescription)
                .setSound(notificationSoundUrl)
                .setContentIntent(pendingIntent);


        //show notification
        notificationManager.notify(notificationID, notificationBuilder.build());





    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void setupPostNotificationChannel(NotificationManager notificationManager) {

        CharSequence channelName = "New Notification";
        String channelDescription = "Device to device post notification";

        NotificationChannel adminChannel =  new NotificationChannel(ADMIN_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);


        adminChannel.setDescription(channelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null){
            notificationManager.createNotificationChannel(adminChannel);
        }



    }

    private void sendNormalNotification(RemoteMessage message) {

        String user = message.getData().get("user");
        String icon = message.getData().get("icon");
        String title = message.getData().get("title");
        String body = message.getData().get("body");

        RemoteMessage.Notification notification = message.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, PostActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("", user);


    }

    private void sendOAndAboveNotification(RemoteMessage message) {

    }


}

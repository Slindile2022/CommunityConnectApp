package com.example.communityconnect.notifications;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


//public class FirebaseService implements FirebaseInstanceIdService{
//
//        public void onTokenRefresh(){
//            super.onTokenRefresh();
//
//            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//            String tokenRefresh = FirebaseInstanceId.getInstance.getToken();
//
//            if (firebaseUser != null){
//                updateToken(tokenRefresh);
//            }
//        }
//
//    private void updateToken(String tokenRefresh) {
//            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
//        Token token = new Token(tokenRefresh);
//        databaseReference.child(firebaseUser.getUid()).setValue(token);
//    }
//
//
//}

public class FirebaseService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            updateToken(token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Handle incoming FCM messages here
    }

    private void updateToken(String tokenRefresh) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(tokenRefresh);
        databaseReference.child(firebaseUser.getUid()).setValue(token);
    }
}


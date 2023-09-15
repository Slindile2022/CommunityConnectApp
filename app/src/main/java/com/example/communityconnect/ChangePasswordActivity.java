package com.example.communityconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.communityconnect.databinding.ActivityChangePasswaordBinding;
import com.example.communityconnect.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity {

    //view binding
    private ActivityChangePasswaordBinding binding;

    //firebase auth

    private FirebaseAuth firebaseAuth;




    //progress dialog

    private ProgressDialog progressDialog;

    FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswaordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();



        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validateData();
            }
        });
    }

    private String password="",cPassword="";
    private void validateData() {
        //we must check if the password is strong or not and they match


        password = binding.passwordEt.getText().toString().trim();
        cPassword = binding.cPasswordEt.getText().toString().trim();

        // Password validation criteria
        Pattern passwordPattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
        Matcher passwordMatcher = passwordPattern.matcher(password);

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "enter password", Toast.LENGTH_SHORT).show();

        }
        else if (!passwordMatcher.matches()) {
            // Password doesn't meet the criteria
            // Display a toast or error message indicating password requirements
            Toast toast = Toast.makeText(this, "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character.", Toast.LENGTH_LONG);
            toast.show();

            // Delay the dismissal of the toast for 30 seconds
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel(); // Dismiss the toast after 30 seconds
                }
            }, 30000); // 30 seconds
        }

        else if(TextUtils.isEmpty(cPassword)){
            Toast.makeText(this, "confirm password", Toast.LENGTH_SHORT).show();
        }

        else if(!password.equals(cPassword)){
            Toast.makeText(this, "password does not match", Toast.LENGTH_SHORT).show();
        }

        else {
            updatePassword();
        }


    }

    private void updatePassword() {


        //show progress

        progressDialog.setMessage("Updating password...");
        progressDialog.show();
        //must update the password since the user has not updated it




        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        //before changing password re-authenticate the user
        AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), "123456");
        firebaseUser.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //successfully authenticated, begin update

                        firebaseUser.updatePassword(cPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //password updated

                                        //then update the realtime database to show that the user password is now changed


                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid());
                                        reference.child("passwordChanged").setValue("yes");

                                        //dismiss the dialog
                                        progressDialog.dismiss();

                                        //log out the user

                                        firebaseAuth.signOut();

                                        Toast.makeText(ChangePasswordActivity.this, "Password updated, you can now log in", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(ChangePasswordActivity.this, LoginActivity.class));


                                        finish();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //must print out the message
                                        progressDialog.dismiss();
                                        Toast.makeText(ChangePasswordActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //authentication failed , show reason
                        progressDialog.dismiss();
                        Toast.makeText(ChangePasswordActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
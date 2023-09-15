package com.example.communityconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.communityconnect.databinding.ActivityPaymentDetailsBinding;
import com.example.communityconnect.databinding.ActivityReportedPostsBinding;
import com.example.communityconnect.databinding.ActivityViewTheFileBinding;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ViewTheFileActivity extends AppCompatActivity {

    private ActivityViewTheFileBinding binding;

    private String fileUrl;

    private static final String TAG = "PDF_VIEW_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewTheFileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //getting the values
        Intent intent = getIntent();
        fileUrl = intent.getStringExtra("pdfLink");
        Log.d(TAG, "onCreate : File link" +fileUrl);


        loadBookFromUrl(fileUrl);

        //handle back button
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void loadBookFromUrl(String fileUrl) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        //load the pdf file
                        binding.pdfView.fromBytes(bytes)
                                .swipeHorizontal(false)
                                .onPageChange(new OnPageChangeListener() {
                                    @Override
                                    public void onPageChanged(int page, int pageCount) {
                                        //set the page number

                                        //set the current page
                                        int currentPage = (page + 1); //index start at 0

                                        binding.pageNumber.setText(currentPage + "/"+ pageCount);
                                    }
                                })
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        Toast.makeText(ViewTheFileActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        Toast.makeText(ViewTheFileActivity.this, "Error on Page "+page+ ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .load();

                        binding.progressBar.setVisibility(View.GONE);


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to load the file
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
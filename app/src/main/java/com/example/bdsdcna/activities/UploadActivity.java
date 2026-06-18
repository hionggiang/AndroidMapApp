package com.example.bdsdcna.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bdsdcna.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;

    private ImageView imgPreview;
    private Button btnSelect;
    private Button btnUpload;
    private ProgressBar progressBar;

    private Uri imageUri;

    private FirebaseStorage storage;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        imgPreview = findViewById(R.id.imgPreview);
        btnSelect = findViewById(R.id.btnSelect);
        btnUpload = findViewById(R.id.btnUpload);
        progressBar = findViewById(R.id.progressBar);

        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();

        btnSelect.setOnClickListener(v -> selectImage());

        btnUpload.setOnClickListener(v -> uploadImage());
    }

    private void selectImage() {

        Intent intent = new Intent(
                Intent.ACTION_PICK);

        intent.setType("image/*");

        startActivityForResult(
                intent,
                PICK_IMAGE
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if(requestCode == PICK_IMAGE
                && resultCode == RESULT_OK
                && data != null){

            imageUri = data.getData();

            imgPreview.setImageURI(imageUri);
        }
    }

    private void uploadImage() {

        if(imageUri == null){

            Toast.makeText(
                    this,
                    "Chọn ảnh trước",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);

        String householdId = "HH0001";
        String chuHo = "Nguyễn Văn Ngộ";

        String folderName =
                householdId + "_"
                        + removeAccent(chuHo);

        String fileName =
                System.currentTimeMillis()
                        + ".jpg";

        StorageReference storageRef =
                storage.getReference()
                        .child("households")
                        .child(folderName)
                        .child(fileName);

        storageRef.putFile(imageUri)

                .addOnSuccessListener(taskSnapshot ->

                        storageRef.getDownloadUrl()

                                .addOnSuccessListener(uri -> {

                                    saveUrlToFirestore(
                                            householdId,
                                            uri.toString()
                                    );

                                }))

                .addOnFailureListener(e -> {

                    progressBar.setVisibility(
                            ProgressBar.GONE);

                    Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });
    }

    private void saveUrlToFirestore(
            String householdId,
            String imageUrl) {

        Map<String,Object> data =
                new HashMap<>();

        data.put(
                "imageUrl",
                imageUrl
        );

        firestore.collection("households")
                .document(householdId)
                .update(data)

                .addOnSuccessListener(unused -> {

                    progressBar.setVisibility(
                            ProgressBar.GONE);

                    Toast.makeText(
                            this,
                            "Upload thành công",
                            Toast.LENGTH_LONG
                    ).show();

                })

                .addOnFailureListener(e -> {

                    progressBar.setVisibility(
                            ProgressBar.GONE);

                    Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });
    }

    private String removeAccent(
            String text){

        String temp =
                Normalizer.normalize(
                        text,
                        Normalizer.Form.NFD);

        return temp
                .replaceAll(
                        "\\p{InCombiningDiacriticalMarks}+",
                        "")
                .replace("đ","d")
                .replace("Đ","D")
                .replaceAll("\\s+","");
    }
}
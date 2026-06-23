package com.example.bdsdcna.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.bdsdcna.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class AccountInfoActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private ImageView imgAvatar;
    private TextView txtHeaderName;
    private TextView txtChangeAvatar;

    private EditText edtFullName;
    private EditText edtPhone;
    private EditText edtEmail;
    private EditText edtAddress;
    private EditText edtChucVu;
    private EditText edtDonVi;

    private Button btnSave;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private StorageReference storageRef;

    private Uri imageUri;

    private ProgressDialog progressDialog;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (uri != null) {

                            imageUri = uri;

                            imgAvatar.setImageURI(uri);

                            uploadAvatar();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        initViews();

        mAuth = FirebaseAuth.getInstance();

        String uid =
                mAuth.getCurrentUser().getUid();

        usersRef = FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(uid);

        storageRef = FirebaseStorage
                .getInstance()
                .getReference("avatars");

        progressDialog =
                new ProgressDialog(this);

        loadUserInfo();

        txtChangeAvatar.setOnClickListener(v ->
                pickImageLauncher.launch("image/*"));

        imgAvatar.setOnClickListener(v ->
                pickImageLauncher.launch("image/*"));

        btnSave.setOnClickListener(v ->
                updateUserInfo());

        toolbar.setNavigationOnClickListener(v ->
                finish());
    }

    private void initViews() {

        toolbar = findViewById(R.id.toolbar);

        imgAvatar = findViewById(R.id.imgAvatar);

        txtHeaderName =
                findViewById(R.id.txtHeaderName);

        txtChangeAvatar =
                findViewById(R.id.txtChangeAvatar);

        edtFullName =
                findViewById(R.id.edtFullName);

        edtPhone =
                findViewById(R.id.edtPhone);

        edtEmail =
                findViewById(R.id.edtEmail);

        edtAddress =
                findViewById(R.id.edtAddress);

        edtChucVu =
                findViewById(R.id.edtChucVu);

        edtDonVi =
                findViewById(R.id.edtDonVi);

        btnSave =
                findViewById(R.id.btnSave);
    }

    private void loadUserInfo() {

        usersRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            DataSnapshot snapshot) {

                        String fullName =
                                snapshot.child("fullName")
                                        .getValue(String.class);

                        String phone =
                                snapshot.child("phone")
                                        .getValue(String.class);

                        String email =
                                snapshot.child("email")
                                        .getValue(String.class);

                        String address =
                                snapshot.child("address")
                                        .getValue(String.class);

                        String chucVu =
                                snapshot.child("chucVu")
                                        .getValue(String.class);

                        String donVi =
                                snapshot.child("donViCongTac")
                                        .getValue(String.class);

                        String avatar =
                                snapshot.child("avatarUrl")
                                        .getValue(String.class);

                        txtHeaderName.setText(
                                fullName != null
                                        ? fullName
                                        : "");

                        edtFullName.setText(fullName);
                        edtPhone.setText(phone);
                        edtEmail.setText(email);
                        edtAddress.setText(address);
                        edtChucVu.setText(chucVu);
                        edtDonVi.setText(donVi);

                        if (avatar != null &&
                                !avatar.isEmpty()) {

                            Glide.with(
                                            AccountInfoActivity.this)
                                    .load(avatar)
                                    .into(imgAvatar);
                        }
                    }

                    @Override
                    public void onCancelled(
                            DatabaseError error) {

                    }
                });
    }

    private void updateUserInfo() {

        String fullName =
                edtFullName.getText()
                        .toString().trim();

        String phone =
                edtPhone.getText()
                        .toString().trim();

        String email =
                edtEmail.getText()
                        .toString().trim();

        String address =
                edtAddress.getText()
                        .toString().trim();

        String chucVu =
                edtChucVu.getText()
                        .toString().trim();

        String donVi =
                edtDonVi.getText()
                        .toString().trim();

        progressDialog.setMessage(
                "Đang cập nhật...");
        progressDialog.show();

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user != null &&
                !email.equals(user.getEmail())) {

            user.updateEmail(email)
                    .addOnCompleteListener(task -> {

                        saveToDatabase(
                                fullName,
                                phone,
                                email,
                                address,
                                chucVu,
                                donVi);
                    });

        } else {

            saveToDatabase(
                    fullName,
                    phone,
                    email,
                    address,
                    chucVu,
                    donVi);
        }
    }

    private void saveToDatabase(
            String fullName,
            String phone,
            String email,
            String address,
            String chucVu,
            String donVi) {

        Map<String, Object> map =
                new HashMap<>();

        map.put("fullName", fullName);
        map.put("phone", phone);
        map.put("email", email);
        map.put("address", address);
        map.put("chucVu", chucVu);
        map.put("donViCongTac", donVi);

        usersRef.updateChildren(map)
                .addOnSuccessListener(unused -> {

                    progressDialog.dismiss();

                    txtHeaderName.setText(fullName);

                    Toast.makeText(
                            this,
                            "Cập nhật thành công",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {

                    progressDialog.dismiss();

                    Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void uploadAvatar() {

        if (imageUri == null) {
            return;
        }

        progressDialog.setMessage(
                "Đang tải ảnh...");
        progressDialog.show();

        String uid =
                mAuth.getCurrentUser().getUid();

        StorageReference fileRef =
                storageRef.child(uid + ".jpg");

        fileRef.putFile(imageUri)
                .continueWithTask(task -> {

                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return fileRef.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {

                    usersRef.child("avatarUrl")
                            .setValue(uri.toString());

                    progressDialog.dismiss();

                    Toast.makeText(
                            this,
                            "Đổi ảnh thành công",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {

                    progressDialog.dismiss();

                    Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}
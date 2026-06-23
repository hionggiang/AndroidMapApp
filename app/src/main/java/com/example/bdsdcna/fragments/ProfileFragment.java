package com.example.bdsdcna.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.bdsdcna.R;
import com.example.bdsdcna.activities.AccountInfoActivity;
import com.example.bdsdcna.activities.ChangePasswordActivity;
import com.example.bdsdcna.activities.SignInActivity;
import com.example.bdsdcna.activities.UserManagementActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {

    private ImageView imgAvatar;

    private TextView txtName;
    private TextView txtEmail;
    private TextView txtPhone;

    private Button btnLogout;

    private LinearLayout layoutAccountInfo;
    private LinearLayout layoutChangePassword;
    private LinearLayout layoutNotification;
    private LinearLayout layoutManageUsers;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private FirebaseStorage storage;
    private StorageReference storageRef;

    private Uri imageUri;

    private final ActivityResultLauncher<String>
            pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (uri != null) {

                            imageUri = uri;

                            imgAvatar.setImageURI(uri);

                            uploadAvatar();
                        }
                    });

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_profile,
                container,
                false);

        imgAvatar = view.findViewById(R.id.imgAvatar);

        txtName = view.findViewById(R.id.txtName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhone = view.findViewById(R.id.txtPhone);

        btnLogout = view.findViewById(R.id.btnLogout);

        layoutAccountInfo =
                view.findViewById(R.id.layoutAccountInfo);

        layoutChangePassword =
                view.findViewById(R.id.layoutChangePassword);

        layoutNotification =
                view.findViewById(R.id.layoutNotification);

        layoutManageUsers =
                view.findViewById(R.id.layoutManageUsers);

        mAuth = FirebaseAuth.getInstance();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        loadUserInfo();

        imgAvatar.setOnClickListener(v ->
                pickImageLauncher.launch("image/*"));

        layoutAccountInfo.setOnClickListener(v ->
                startActivity(new Intent(
                        getActivity(),
                        AccountInfoActivity.class)));

        layoutChangePassword.setOnClickListener(v ->
                startActivity(new Intent(
                        getActivity(),
                        ChangePasswordActivity.class)));

        layoutNotification.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.frameContainer,
                            new NotificationFragment())
                    .addToBackStack(null)
                    .commit();
        });

        layoutManageUsers.setOnClickListener(v ->
                startActivity(new Intent(
                        getActivity(),
                        UserManagementActivity.class)));

        btnLogout.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();

            Intent intent =
                    new Intent(
                            getActivity(),
                            SignInActivity.class);

            startActivity(intent);

            requireActivity().finish();
        });

        return view;
    }

    private void loadUserInfo() {

        if (mAuth.getCurrentUser() == null) {
            return;
        }

        String uid =
                mAuth.getCurrentUser().getUid();

        usersRef = FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(uid);

        usersRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        String fullName =
                                snapshot.child("fullName")
                                        .getValue(String.class);

                        String email =
                                snapshot.child("email")
                                        .getValue(String.class);

                        String phone =
                                snapshot.child("phone")
                                        .getValue(String.class);

                        String role =
                                snapshot.child("role")
                                        .getValue(String.class);

                        String avatarUrl =
                                snapshot.child("avatarUrl")
                                        .getValue(String.class);

                        txtName.setText(
                                fullName != null
                                        ? fullName
                                        : "");

                        txtEmail.setText(
                                email != null
                                        ? email
                                        : "");

                        txtPhone.setText(
                                phone != null
                                        ? phone
                                        : "");

                        if (avatarUrl != null
                                && !avatarUrl.isEmpty()) {

                            Glide.with(requireContext())
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.ic_person)
                                    .into(imgAvatar);
                        }

                        if ("admin".equals(role)) {

                            layoutManageUsers
                                    .setVisibility(View.VISIBLE);

                        } else {

                            layoutManageUsers
                                    .setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {
                    }
                });
    }

    private void uploadAvatar() {

        if (imageUri == null
                || mAuth.getCurrentUser() == null) {
            return;
        }

        String uid =
                mAuth.getCurrentUser().getUid();

        StorageReference avatarRef =
                storageRef.child(
                        "avatars/" + uid + ".jpg");

        avatarRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->

                        avatarRef.getDownloadUrl()
                                .addOnSuccessListener(uri ->

                                        FirebaseDatabase
                                                .getInstance()
                                                .getReference("users")
                                                .child(uid)
                                                .child("avatarUrl")
                                                .setValue(uri.toString())
                                ));
    }
}
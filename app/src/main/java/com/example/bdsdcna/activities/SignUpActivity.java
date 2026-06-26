package com.example.bdsdcna.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bdsdcna.HistoryHelper;
import com.example.bdsdcna.R;
import com.example.bdsdcna.models.User;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

public class SignUpActivity extends AppCompatActivity {

    private EditText edtFullName, edtPhone, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnSignUp, btnGoogleSignUp;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    // Google
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Ánh xạ view
        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        btnSignUp = findViewById(R.id.btnSignUp);
        btnGoogleSignUp = findViewById(R.id.btnGoogleSignUp);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Email sign up
        btnSignUp.setOnClickListener(v -> signUp());

        // Google Sign-In config
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogleSignUp.setOnClickListener(v -> signInWithGoogle());
    }

    // ================= EMAIL SIGN UP =================
    private void signUp() {
        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirm = edtConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirm.isEmpty()) {
            toast("Vui lòng nhập đầy đủ thông tin");
            return;
        }

        if (!password.equals(confirm)) {
            toast("Mật khẩu không khớp");
            return;
        }

        if (password.length() < 6) {
            toast("Mật khẩu phải từ 6 ký tự");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        User user = new User(uid, fullName, phone, email, "", "", "user", "active","");

                        usersRef.child(uid).setValue(user)
                                .addOnSuccessListener(unused -> {

                                    HistoryHelper.logSignUp("EMAIL");

                                    toast("Đăng ký thành công");
                                    finish();
                                })
                                .addOnFailureListener(e -> toast(e.getMessage()));
                    } else {
                        toast(task.getException() != null ?
                                task.getException().getMessage() : "Đăng ký thất bại");
                    }
                });
    }

    // ================= GOOGLE SIGN IN =================
    private void signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (Exception e) {
                toast("Google Sign-In thất bại");
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser == null) return;

                        String uid = firebaseUser.getUid();

                        // Kiểm tra xem tài khoản đã tồn tại trong Database chưa
                        usersRef.child(uid).get().addOnCompleteListener(checkTask -> {
                            if (checkTask.isSuccessful()) {
                                if (checkTask.getResult().exists()) {

                                    HistoryHelper.log(
                                            "SIGN_UP_GOOGLE_EXISTED",
                                            "",
                                            "",
                                            uid,
                                            firebaseUser.getDisplayName() != null
                                                    ? firebaseUser.getDisplayName()
                                                    : firebaseUser.getEmail(),
                                            "",
                                            "",
                                            ""
                                    );

                                    toast("Tài khoản đã được đăng ký. Vui lòng đăng nhập.");
                                    mAuth.signOut();
                                    finish();
                                }
                                else {
                                    // ✅ Tài khoản mới → Đăng ký
                                    String name = firebaseUser.getDisplayName();
                                    String email = firebaseUser.getEmail();

                                    String avatarUrl = firebaseUser.getPhotoUrl() != null
                                            ? firebaseUser.getPhotoUrl().toString()
                                            : "";

                                    User user = new User(uid,
                                            name != null ? name : "",
                                            "",
                                            email != null ? email : "",
                                            "",
                                            "",
                                            "user",
                                            "active",
                                            avatarUrl
                                    );

                                    usersRef.child(uid).setValue(user)
                                            .addOnSuccessListener(unused -> {

                                                HistoryHelper.logSignUp("GOOGLE");

                                                toast("Đăng ký Google thành công");
                                                finish();
                                            })
                                            .addOnFailureListener(e -> toast("Lưu thông tin thất bại"));
                                }
                            } else {
                                toast("Kiểm tra tài khoản thất bại");
                            }
                        });

                    } else {
                        toast("Đăng nhập Google thất bại: " +
                                (task.getException() != null ? task.getException().getMessage() : ""));
                    }
                });
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
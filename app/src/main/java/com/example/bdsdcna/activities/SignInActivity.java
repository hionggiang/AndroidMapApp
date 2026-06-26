package com.example.bdsdcna.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bdsdcna.HistoryHelper;
import com.example.bdsdcna.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnSignIn;
    private TextView txtSignUp;
    private SignInButton btnGoogleSignIn;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Ánh xạ View
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        txtSignUp = findViewById(R.id.txtSignUp);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Google Sign In
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(
                        GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        googleSignInClient =
                GoogleSignIn.getClient(this, gso);

        btnSignIn.setOnClickListener(v -> signIn());

        txtSignUp.setOnClickListener(v -> startActivity(
                new Intent(
                        SignInActivity.this,
                        SignUpActivity.class)));

        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
    }
    // ================= EMAIL SIGN IN =================
    private void signIn() {

        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Nhập đầy đủ thông tin",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        HistoryHelper.logLogin("EMAIL");
                        Toast.makeText(this,
                                "Đăng nhập thành công",
                                Toast.LENGTH_SHORT).show();

                        startActivity(
                                new Intent(
                                        SignInActivity.this,
                                        MainActivity.class));

                        finish();

                    } else {

                        Toast.makeText(this,
                                "Sai tài khoản hoặc mật khẩu",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ================= GOOGLE SIGN IN =================
    private void signInWithGoogle() {

        googleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {

                    Intent intent =
                            googleSignInClient.getSignInIntent();

                    startActivityForResult(
                            intent,
                            RC_SIGN_IN);
                });
    }
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);

            try {

                GoogleSignInAccount account =
                        task.getResult(ApiException.class);

                firebaseAuthWithGoogle(account.getIdToken());

            } catch (Exception e) {

                Toast.makeText(
                        this,
                        "Google Sign-In thất bại",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser firebaseUser =
                                mAuth.getCurrentUser();

                        if (firebaseUser == null)
                            return;

                        String uid = firebaseUser.getUid();

                        usersRef.child(uid)
                                .get()
                                .addOnCompleteListener(checkTask -> {

                                    if (checkTask.isSuccessful()
                                            && checkTask.getResult().exists()) {

                                        // ===== Ghi lịch sử đăng nhập =====
                                        HistoryHelper.logLogin("Google");                                        Toast.makeText(
                                                this,
                                                "Đăng nhập Google thành công",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        startActivity(
                                                new Intent(
                                                        SignInActivity.this,
                                                        MainActivity.class
                                                )
                                        );

                                        finish();

                                    } else {

                                        mAuth.signOut();
                                        googleSignInClient.signOut();

                                        Toast.makeText(
                                                this,
                                                "Tài khoản Google này chưa được đăng ký.\nVui lòng đăng ký trước!",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                });

                    } else {

                        Toast.makeText(
                                this,
                                "Xác thực Google thất bại",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
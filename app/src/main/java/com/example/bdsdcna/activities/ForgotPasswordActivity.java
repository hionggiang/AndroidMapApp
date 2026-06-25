package com.example.bdsdcna.activities;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bdsdcna.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtEmail, edtNewPassword;
    private Button btnSendEmail, btnResetPassword;

    private FirebaseAuth auth;
    private String oobCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtNewPassword = findViewById(R.id.edtNewPassword);

        btnSendEmail = findViewById(R.id.btnResetPassword);
        btnResetPassword = findViewById(R.id.btnUpdatePassword);

        handleDeepLink(); // 🔥 kiểm tra nếu mở từ email link

        btnSendEmail.setOnClickListener(v -> sendResetEmail());
        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    // =========================
    // 🔥 NHẬN LINK TỪ EMAIL
    // =========================
    private void handleDeepLink() {

        Uri data = getIntent().getData();

        if (data != null && data.getQueryParameter("oobCode") != null) {

            oobCode = data.getQueryParameter("oobCode");

            // chuyển sang mode reset password
            edtEmail.setVisibility(View.GONE);
            btnSendEmail.setVisibility(View.GONE);

            edtNewPassword.setVisibility(View.VISIBLE);
            btnResetPassword.setVisibility(View.VISIBLE);
        } else {

            // mode gửi email
            edtEmail.setVisibility(View.VISIBLE);
            btnSendEmail.setVisibility(View.VISIBLE);

            edtNewPassword.setVisibility(View.GONE);
            btnResetPassword.setVisibility(View.GONE);
        }
    }

    // =========================
    // GỬI EMAIL RESET
    // =========================
    private void sendResetEmail() {

        String email = edtEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this,
                                "Đã gửi email đặt lại mật khẩu!",
                                Toast.LENGTH_LONG).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    // =========================
    // RESET PASSWORD
    // =========================
    private void resetPassword() {

        String newPass = edtNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newPass) || newPass.length() < 6) {
            edtNewPassword.setError("Mật khẩu phải >= 6 ký tự");
            return;
        }

        auth.confirmPasswordReset(oobCode, newPass)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Đổi mật khẩu thành công!",
                                Toast.LENGTH_SHORT).show();

                        finish();
                    } else {
                        Toast.makeText(this,
                                task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
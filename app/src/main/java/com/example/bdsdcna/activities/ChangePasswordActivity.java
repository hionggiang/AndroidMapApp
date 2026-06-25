package com.example.bdsdcna.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bdsdcna.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText etOldPassword, etNewPassword;
    private Button btnChangePassword;
    private TextView tvForgotPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();

        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> changePassword());

        // Quên mật khẩu
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ChangePasswordActivity.this,
                    ForgotPasswordActivity.class
            );
            startActivity(intent);
        });
    }

    private void changePassword() {

        String oldPass = etOldPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(oldPass)) {
            etOldPassword.setError("Nhập mật khẩu cũ");
            etOldPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPass) || newPass.length() < 6) {
            etNewPassword.setError("Mật khẩu mới phải từ 6 ký tự trở lên");
            etNewPassword.requestFocus();
            return;
        }

        String email = user.getEmail();

        if (email == null) {
            Toast.makeText(this, "Không tìm thấy email tài khoản!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Xác thực lại bằng mật khẩu cũ
        AuthCredential credential =
                EmailAuthProvider.getCredential(email, oldPass);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        // Cập nhật mật khẩu mới
                        user.updatePassword(newPass)
                                .addOnCompleteListener(task2 -> {

                                    if (task2.isSuccessful()) {

                                        Toast.makeText(
                                                ChangePasswordActivity.this,
                                                "Đổi mật khẩu thành công!",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        finish();

                                    } else {

                                        Toast.makeText(
                                                ChangePasswordActivity.this,
                                                "Lỗi: " + task2.getException().getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                });

                    } else {

                        Toast.makeText(
                                ChangePasswordActivity.this,
                                "Sai mật khẩu cũ!",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
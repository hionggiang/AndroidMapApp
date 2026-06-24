package com.example.bdsdcna.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bdsdcna.R;
import com.example.bdsdcna.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText edtFullName;
    private EditText edtPhone;
    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtConfirmPassword;
    private TextView txtLogin;
    private Button btnSignUp;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        txtLogin = findViewById(R.id.txtLogin);
        btnSignUp = findViewById(R.id.btnSignUp);

        mAuth = FirebaseAuth.getInstance();

        usersRef = FirebaseDatabase.getInstance()
                .getReference("users");

        btnSignUp.setOnClickListener(v -> signUp());
        txtLogin.setOnClickListener(v -> {

            Intent intent = new Intent(
                    SignUpActivity.this,
                    SignInActivity.class);

            startActivity(intent);

            finish();
        });
    }

    private void signUp() {

        String fullName =
                edtFullName.getText().toString().trim();

        String phone =
                edtPhone.getText().toString().trim();

        String email =
                edtEmail.getText().toString().trim();

        String password =
                edtPassword.getText().toString().trim();

        String confirm =
                edtConfirmPassword.getText().toString().trim();

        // Kiểm tra rỗng
        if (fullName.isEmpty()
                || phone.isEmpty()
                || email.isEmpty()
                || password.isEmpty()
                || confirm.isEmpty()) {

            Toast.makeText(
                    this,
                    "Vui lòng nhập đầy đủ thông tin",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Họ tên
        if (fullName.length() < 5) {

            edtFullName.setError(
                    "Họ tên tối thiểu 5 ký tự");

            edtFullName.requestFocus();
            return;
        }

        if (fullName.matches(".*\\d.*")) {

            edtFullName.setError(
                    "Họ tên không được chứa số");

            edtFullName.requestFocus();
            return;
        }

        // SĐT
        if (!phone.matches("^0\\d{9}$")) {

            edtPhone.setError(
                    "Số điện thoại phải gồm 10 số");

            edtPhone.requestFocus();
            return;
        }

        // Email
        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            edtEmail.setError(
                    "Email không hợp lệ");

            edtEmail.requestFocus();
            return;
        }

        // Password
        if (password.length() < 8) {

            edtPassword.setError(
                    "Mật khẩu tối thiểu 8 ký tự");

            edtPassword.requestFocus();
            return;
        }

        if (!password.matches(".*[A-Z].*")) {

            edtPassword.setError(
                    "Phải có ít nhất 1 chữ hoa");

            edtPassword.requestFocus();
            return;
        }

        if (!password.matches(".*[a-z].*")) {

            edtPassword.setError(
                    "Phải có ít nhất 1 chữ thường");

            edtPassword.requestFocus();
            return;
        }

        if (!password.matches(".*\\d.*")) {

            edtPassword.setError(
                    "Phải có ít nhất 1 chữ số");

            edtPassword.requestFocus();
            return;
        }

        // Xác nhận mật khẩu
        if (!password.equals(confirm)) {

            edtConfirmPassword.setError(
                    "Mật khẩu xác nhận không khớp");

            edtConfirmPassword.requestFocus();
            return;
        }

        btnSignUp.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(
                        email,
                        password)
                .addOnCompleteListener(task -> {

                    btnSignUp.setEnabled(true);

                    if (task.isSuccessful()) {

                        String uid =
                                mAuth.getCurrentUser()
                                        .getUid();

                        User user = new User();

                        user.setUid(uid);
                        user.setFullName(fullName);
                        user.setPhone(phone);
                        user.setEmail(email);


                        usersRef.child(uid)
                                .setValue(user)
                                .addOnSuccessListener(
                                        unused -> {

                                            Toast.makeText(
                                                    SignUpActivity.this,
                                                    "Đăng ký thành công",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                            finish();
                                        })
                                .addOnFailureListener(
                                        e -> Toast.makeText(
                                                SignUpActivity.this,
                                                e.getMessage(),
                                                Toast.LENGTH_SHORT
                                        ).show());

                    } else {

                        Toast.makeText(
                                SignUpActivity.this,
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Đăng ký thất bại",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
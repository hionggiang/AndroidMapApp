package com.example.bdsdcna.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

        btnSignUp = findViewById(R.id.btnSignUp);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance()
                .getReference("users");

        btnSignUp.setOnClickListener(v -> signUp());
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

        if (!password.equals(confirm)) {

            Toast.makeText(
                    this,
                    "Mật khẩu xác nhận không khớp",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (password.length() < 6) {

            Toast.makeText(
                    this,
                    "Mật khẩu phải từ 6 ký tự",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        String uid =
                                mAuth.getCurrentUser().getUid();

                        User user = new User(
                                uid,
                                fullName,
                                phone,
                                email,
                                "",                 // chức vụ
                                "",                 // đơn vị công tác
                                "user",             // quyền mặc định
                                "active"            // trạng thái
                        );

                        usersRef.child(uid)
                                .setValue(user)
                                .addOnSuccessListener(unused -> {

                                    Toast.makeText(
                                            SignUpActivity.this,
                                            "Đăng ký thành công",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    finish();
                                })
                                .addOnFailureListener(e -> {

                                    Toast.makeText(
                                            SignUpActivity.this,
                                            e.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                });

                    } else {

                        Toast.makeText(
                                SignUpActivity.this,
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Đăng ký thất bại",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
package com.example.bdsdcna.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bdsdcna.R;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnSignIn;
    TextView txtSignUp;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        txtSignUp = findViewById(R.id.txtSignUp);

        mAuth = FirebaseAuth.getInstance();

        btnSignIn.setOnClickListener(v -> signIn());

        txtSignUp.setOnClickListener(v -> {
            startActivity(new Intent(
                    SignInActivity.this,
                    SignUpActivity.class));
        });
    }

    private void signIn() {

        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Nhập đầy đủ thông tin",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()) {

                        Toast.makeText(this,
                                "Đăng nhập thành công",
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(
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
}
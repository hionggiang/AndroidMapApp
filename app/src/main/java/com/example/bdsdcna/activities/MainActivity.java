package com.example.bdsdcna.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bdsdcna.R;

public class MainActivity extends AppCompatActivity {

    private Button btnImportExcel;
    private Button btnUploadImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnImportExcel =
                findViewById(R.id.btnImportExcel);

        btnUploadImage =
                findViewById(R.id.btnUploadImage);

        btnImportExcel.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            ImportExcelActivity.class
                    );

            startActivity(intent);

        });

        btnUploadImage.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            UploadActivity.class
                    );

            startActivity(intent);

        });
    }
}
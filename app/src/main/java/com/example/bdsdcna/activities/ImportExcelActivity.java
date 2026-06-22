package com.example.bdsdcna.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bdsdcna.R;
import com.example.bdsdcna.models.Household;
import com.example.bdsdcna.xulydulieu.ExcelImporter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ImportExcelActivity
        extends AppCompatActivity {

    private static final int PICK_EXCEL = 200;

    private DatabaseReference householdRef;

    private Uri excelUri;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_import_excel
        );

        householdRef =
                FirebaseDatabase
                        .getInstance()
                        .getReference("households");

        Button btnSelect =
                findViewById(
                        R.id.btnSelectExcel
                );

        Button btnImport =
                findViewById(
                        R.id.btnImportExcel
                );

        btnSelect.setOnClickListener(v ->
                selectExcel());

        btnImport.setOnClickListener(v ->
                importExcel());
    }

    private void selectExcel() {

        Intent intent =
                new Intent(
                        Intent.ACTION_GET_CONTENT
                );

        intent.setType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        startActivityForResult(
                intent,
                PICK_EXCEL
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == PICK_EXCEL
                && resultCode == RESULT_OK
                && data != null) {

            excelUri =
                    data.getData();

            Toast.makeText(
                    this,
                    "Đã chọn file Excel",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void importExcel() {

        if (excelUri == null) {

            Toast.makeText(
                    this,
                    "Chọn file Excel trước",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {

            List<Household> households =
                    ExcelImporter.importExcel(
                            this,
                            excelUri
                    );

            saveToRealtimeDatabase(
                    households
            );

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Lỗi đọc Excel: "
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void saveToRealtimeDatabase(
            List<Household> households
    ) {

        if (households == null
                || households.isEmpty()) {

            Toast.makeText(
                    this,
                    "Không có dữ liệu",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int total =
                households.size();

        final int[] success =
                {0};

        for (Household h : households) {

            householdRef
                    .child(
                            h.getHouseholdId()
                    )
                    .setValue(h)

                    .addOnSuccessListener(
                            unused -> {

                                success[0]++;

                                if (success[0]
                                        == total) {

                                    Toast.makeText(
                                            this,
                                            "Import thành công "
                                                    + total
                                                    + " hộ",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            })

                    .addOnFailureListener(
                            e -> Toast.makeText(
                                    this,
                                    "Lỗi lưu "
                                            + h.getHouseholdId()
                                            + "\n"
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show()
                    );
        }
    }
}
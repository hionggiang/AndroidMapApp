package com.example.bdsdcna.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bdsdcna.R;
import com.example.bdsdcna.models.Household;
import com.example.bdsdcna.xulydulieu.ExcelImporter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ImportExcelActivity extends AppCompatActivity {

    private static final int PICK_EXCEL = 200;

    private Uri excelUri;
    private Spinner spLoaiHo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_excel);

        spLoaiHo = findViewById(R.id.spLoaiHo);

        String[] loaiHo = {
                "Hộ nghèo",
                "Hộ cận nghèo",
                "Hộ khó khăn",
                "Gia đình chính sách"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        loaiHo
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spLoaiHo.setAdapter(adapter);

        Button btnSelect =
                findViewById(R.id.btnSelectExcel);

        Button btnImport =
                findViewById(R.id.btnImportExcel);

        btnSelect.setOnClickListener(v -> selectExcel());

        btnImport.setOnClickListener(v -> importExcel());
    }

    private void selectExcel() {

        Intent intent =
                new Intent(Intent.ACTION_GET_CONTENT);

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

            excelUri = data.getData();

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

            uploadToFirebase(
                    households
            );

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Lỗi: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void uploadToFirebase(
            List<Household> households
    ) {

        String selected =
                spLoaiHo.getSelectedItem()
                        .toString();

        String node;
        String prefix;

        switch (selected) {

            case "Hộ nghèo":
                node = "ho_ngheo";
                prefix = "HH";
                break;

            case "Hộ cận nghèo":
                node = "ho_can_ngheo";
                prefix = "CN";
                break;

            case "Hộ khó khăn":
                node = "ho_kho_khan";
                prefix = "KK";
                break;

            default:
                node = "gia_dinh_chinh_sach";
                prefix = "CS";
                break;
        }

        DatabaseReference ref =
                FirebaseDatabase.getInstance()
                        .getReference("households")
                        .child(node);

        ref.get().addOnSuccessListener(snapshot -> {

            int maxId = 0;

            for (com.google.firebase.database.DataSnapshot child :
                    snapshot.getChildren()) {

                try {

                    String key = child.getKey();

                    if (key != null &&
                            key.startsWith(prefix)) {

                        int num = Integer.parseInt(
                                key.substring(2)
                        );

                        if (num > maxId) {
                            maxId = num;
                        }
                    }

                } catch (Exception ignored) {
                }
            }

            int currentId = maxId;

            for (Household h : households) {

                currentId++;

                String newId =
                        String.format(
                                "%s%04d",
                                prefix,
                                currentId
                        );

                h.setHouseholdId(newId);
                h.setStt(currentId);

                ref.child(newId)
                        .setValue(h);
            }

            Toast.makeText(
                    ImportExcelActivity.this,
                    "Import thành công "
                            + households.size()
                            + " hộ vào "
                            + selected,
                    Toast.LENGTH_LONG
            ).show();
        }).addOnFailureListener(e -> {

            Toast.makeText(
                    ImportExcelActivity.this,
                    "Lỗi Firebase: "
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        });
    }
}
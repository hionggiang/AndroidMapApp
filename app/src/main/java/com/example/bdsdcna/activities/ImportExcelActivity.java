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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ImportExcelActivity
        extends AppCompatActivity {

    private static final int PICK_EXCEL = 200;

    private FirebaseFirestore firestore;

    private Uri excelUri;

    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_import_excel
        );

        firestore =
                FirebaseFirestore.getInstance();

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
            @Nullable Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if(requestCode == PICK_EXCEL
                && resultCode == RESULT_OK
                && data != null){

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

        if(excelUri == null){

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

            saveToFirestore(
                    households
            );

        }
        catch (Exception e){

            e.printStackTrace();

            Toast.makeText(
                    this,
                    e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void saveToFirestore(
            List<Household> households
    ){

        for(Household h : households){

            firestore.collection(
                            "households"
                    )

                    .document(
                            h.getHouseholdId()
                    )

                    .set(h);
        }

        Toast.makeText(
                this,
                "Import thành công "
                        + households.size()
                        + " hộ",
                Toast.LENGTH_LONG
        ).show();
    }
}
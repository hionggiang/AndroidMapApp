package com.example.bdsdcna.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bdsdcna.HistoryHelper;
import com.example.bdsdcna.R;
import com.example.bdsdcna.models.DoiTuong;
import com.example.bdsdcna.models.Household;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UpdateSituationActivity extends AppCompatActivity {

    private EditText edtHoTen, edtNamSinh, edtCCCD, edtGhiChuHoanCanh;
    private TextView txtNhanKhau, txtDiaChiHienTai;
    private Spinner spnDoiTuong;
    private Button btnSave;
    private ImageButton btnBack;

    private String householdId;
    private DatabaseReference householdRef;

    //======================
    // Lưu dữ liệu cũ
    //======================
    private Household oldHousehold;
    private String oldGhiChu = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_situation);

        householdId = getIntent().getStringExtra("householdId");

        if (householdId == null) {
            Toast.makeText(this,
                    "Không tìm thấy hộ gia đình!",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        edtHoTen = findViewById(R.id.edtHoTen);
        edtNamSinh = findViewById(R.id.edtNamSinh);
        edtCCCD = findViewById(R.id.edtCCCD);
        edtGhiChuHoanCanh = findViewById(R.id.edtGhiChuHoanCanh);

        txtNhanKhau = findViewById(R.id.txtNhanKhau);
        txtDiaChiHienTai = findViewById(R.id.txtDiaChiHienTai);

        spnDoiTuong = findViewById(R.id.spnDoiTuong);

        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        householdRef = FirebaseDatabase.getInstance()
                .getReference("households")
                .child(householdId);

        setupSpinner();

        loadHouseholdInfo();

        loadCurrentGhiChu();

        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveSituation());

        ViewCompat.setOnApplyWindowInsetsListener(
                getWindow().getDecorView().getRootView(),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom);

                    return insets;
                });
    }

    private void setupSpinner() {

        String[] options = {
                "Hộ nghèo",
                "Hộ cận nghèo",
                "Khó khăn",
                "Chính sách",
                "Khác"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        options
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spnDoiTuong.setAdapter(adapter);
    }

    private void loadHouseholdInfo() {

        householdRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        Household house =
                                snapshot.getValue(Household.class);

                        if (house == null)
                            return;

                        //=========================
                        // Lưu dữ liệu cũ
                        //=========================
                        oldHousehold = house;

                        if (house.getChuHo() != null) {

                            edtHoTen.setText(
                                    house.getChuHo().getHoTen());

                            edtNamSinh.setText(
                                    String.valueOf(
                                            house.getChuHo().getNamSinh()));

                            edtCCCD.setText(
                                    house.getChuHo().getCccd());
                        }

                        if (house.getDiaChi() != null) {

                            String address = "";

                            if (house.getDiaChi().getAp() != null)
                                address += house.getDiaChi().getAp();

                            if (house.getDiaChi().getXa() != null)
                                address += ", " +
                                        house.getDiaChi().getXa();

                            if (house.getDiaChi().getTinh() != null)
                                address += ", " +
                                        house.getDiaChi().getTinh();

                            txtDiaChiHienTai.setText(address);
                        }

                        int soNhanKhau =
                                house.getThanhVien() == null
                                        ? 0
                                        : house.getThanhVien().size();

                        txtNhanKhau.setText(
                                soNhanKhau + " người");

                        if (house.getDoiTuong() != null) {
                            setSpinnerSelection(
                                    house.getDoiTuong());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                        Toast.makeText(
                                UpdateSituationActivity.this,
                                "Lỗi tải dữ liệu",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
    private void setSpinnerSelection(DoiTuong dt) {

        String selected = "Khác";

        if (dt.isHoNgheo()) {
            selected = "Hộ nghèo";
        } else if (dt.isHoCanNgheo()) {
            selected = "Hộ cận nghèo";
        } else if (dt.isHoKhoKhan()) {
            selected = "Khó khăn";
        } else if (dt.isGiaDinhChinhSach()) {
            selected = "Chính sách";
        }

        int pos = ((ArrayAdapter<String>) spnDoiTuong
                .getAdapter())
                .getPosition(selected);

        if (pos >= 0) {
            spnDoiTuong.setSelection(pos);
        }
    }

    private void loadCurrentGhiChu() {

        householdRef.child("hoanCanh")
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()
                            && task.getResult() != null) {

                        String ghiChu =
                                task.getResult()
                                        .child("ghiChu")
                                        .getValue(String.class);

                        if (ghiChu != null) {
                            oldGhiChu = ghiChu;
                            edtGhiChuHoanCanh.setText(ghiChu);
                        }
                    }
                });
    }

    private void saveSituation() {

        String hoTen =
                edtHoTen.getText().toString().trim();

        String namSinhStr =
                edtNamSinh.getText().toString().trim();

        String cccd =
                edtCCCD.getText().toString().trim();

        String ghiChu =
                edtGhiChuHoanCanh.getText().toString().trim();

        String doiTuongSelected =
                spnDoiTuong.getSelectedItem().toString();

        Map<String, Object> updates = new HashMap<>();

        updates.put("chuHo/hoTen", hoTen);
        updates.put("chuHo/cccd", cccd);

        try {

            updates.put(
                    "chuHo/namSinh",
                    Integer.parseInt(namSinhStr)
            );

        } catch (Exception e) {

            updates.put("chuHo/namSinh", 0);
        }

        updates.put("doiTuong/hoNgheo", false);
        updates.put("doiTuong/hoCanNgheo", false);
        updates.put("doiTuong/hoKhoKhan", false);
        updates.put("doiTuong/giaDinhChinhSach", false);

        switch (doiTuongSelected) {

            case "Hộ nghèo":
                updates.put("doiTuong/hoNgheo", true);
                break;

            case "Hộ cận nghèo":
                updates.put("doiTuong/hoCanNgheo", true);
                break;

            case "Khó khăn":
                updates.put("doiTuong/hoKhoKhan", true);
                break;

            case "Chính sách":
                updates.put("doiTuong/giaDinhChinhSach", true);
                break;
        }

        updates.put("hoanCanh/ghiChu", ghiChu);

        householdRef.updateChildren(updates)
                .addOnSuccessListener(unused -> {

                    if (oldHousehold != null &&
                            oldHousehold.getChuHo() != null) {

                        if (!hoTen.equals(oldHousehold
                                .getChuHo()
                                .getHoTen())) {

                            HistoryHelper.log(
                                    "UPDATE_HOUSEHOLD",
                                    householdId,
                                    "",
                                    "",
                                    hoTen,
                                    "Chủ hộ",
                                    oldHousehold.getChuHo().getHoTen(),
                                    hoTen
                            );
                        }

                        if (!cccd.equals(oldHousehold
                                .getChuHo()
                                .getCccd())) {

                            HistoryHelper.log(
                                    "UPDATE_HOUSEHOLD",
                                    householdId,
                                    "",
                                    "",
                                    hoTen,
                                    "CCCD",
                                    oldHousehold.getChuHo().getCccd(),
                                    cccd
                            );
                        }
                        int oldYear =
                                oldHousehold.getChuHo().getNamSinh();

                        int newYear;

                        try {
                            newYear = Integer.parseInt(namSinhStr);
                        } catch (Exception e) {
                            newYear = 0;
                        }

                        if (oldYear != newYear) {

                            HistoryHelper.log(
                                    "UPDATE_HOUSEHOLD",
                                    householdId,
                                    "",
                                    "",
                                    hoTen,
                                    "Năm sinh",
                                    String.valueOf(oldYear),
                                    String.valueOf(newYear)
                            );                        }
                    }

                    String oldObject = "Khác";

                    if (oldHousehold != null &&
                            oldHousehold.getDoiTuong() != null) {

                        DoiTuong dt = oldHousehold.getDoiTuong();

                        if (dt.isHoNgheo()) {
                            oldObject = "Hộ nghèo";
                        } else if (dt.isHoCanNgheo()) {
                            oldObject = "Hộ cận nghèo";
                        } else if (dt.isHoKhoKhan()) {
                            oldObject = "Khó khăn";
                        } else if (dt.isGiaDinhChinhSach()) {
                            oldObject = "Chính sách";
                        }
                    }

                    if (!oldObject.equals(doiTuongSelected)) {

                        HistoryHelper.log(
                                "UPDATE_HOUSEHOLD",
                                householdId,
                                "",
                                "",
                                hoTen,
                                "Đối tượng",
                                oldObject,
                                doiTuongSelected
                        );                    }

                    if (!oldGhiChu.equals(ghiChu)) {

                        HistoryHelper.log(
                                "UPDATE_HOUSEHOLD",
                                householdId,
                                "",
                                "",
                                hoTen,
                                "Hoàn cảnh",
                                oldGhiChu,
                                ghiChu
                        );                    }

                    Toast.makeText(
                            UpdateSituationActivity.this,
                            "✅ Cập nhật thành công!",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            UpdateSituationActivity.this,
                            "❌ Lưu thất bại: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}
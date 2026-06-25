package com.example.bdsdcna.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

    private EditText edtHoTen, edtAp, edtXa, edtTinh, edtNamSinh, edtCCCD, edtGhiChuHoanCanh;
    private TextView txtDiaChiHienTai, txtNhanKhau;
    private Spinner spnDoiTuong;
    private Button btnSuaDiaChi, btnSave;
    private ImageButton btnBack;
    private LinearLayout layoutSuaDiaChi;

    private String householdId;
    private DatabaseReference householdRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_situation);

        householdId = getIntent().getStringExtra("householdId");
        if (householdId == null) {
            Toast.makeText(this, "Không tìm thấy hộ gia đình!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ánh xạ views
        edtHoTen = findViewById(R.id.edtHoTen);
        txtDiaChiHienTai = findViewById(R.id.txtDiaChiHienTai);
        btnSuaDiaChi = findViewById(R.id.btnSuaDiaChi);
        layoutSuaDiaChi = findViewById(R.id.layoutSuaDiaChi);
        edtAp = findViewById(R.id.edtAp);
        edtXa = findViewById(R.id.edtXa);
        edtTinh = findViewById(R.id.edtTinh);
        edtNamSinh = findViewById(R.id.edtNamSinh);
        edtCCCD = findViewById(R.id.edtCCCD);
        txtNhanKhau = findViewById(R.id.txtNhanKhau);
        edtGhiChuHoanCanh = findViewById(R.id.edtGhiChuHoanCanh);
        spnDoiTuong = findViewById(R.id.spnDoiTuong);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        householdRef = FirebaseDatabase.getInstance().getReference("households").child(householdId);

        setupSpinner();
        loadHouseholdInfo();
        loadCurrentGhiChu();

        // Xử lý nút sửa địa chỉ
        btnSuaDiaChi.setOnClickListener(v -> {
            if (layoutSuaDiaChi.getVisibility() == View.GONE) {
                layoutSuaDiaChi.setVisibility(View.VISIBLE);
                btnSuaDiaChi.setText("Ẩn sửa địa chỉ");
            } else {
                layoutSuaDiaChi.setVisibility(View.GONE);
                btnSuaDiaChi.setText("✏️ Sửa địa chỉ");
            }
        });

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveSituation());

        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView().getRootView(),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
    }

    private void setupSpinner() {
        String[] options = {"Hộ nghèo", "Hộ cận nghèo", "Khó khăn", "Chính sách", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDoiTuong.setAdapter(adapter);
    }

    private void loadHouseholdInfo() {
        householdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Household house = snapshot.getValue(Household.class);
                if (house != null) {

                    // Thông tin chủ hộ
                    if (house.getChuHo() != null) {
                        edtHoTen.setText(house.getChuHo().getHoTen());
                        edtNamSinh.setText(String.valueOf(house.getChuHo().getNamSinh()));
                        edtCCCD.setText(house.getChuHo().getCccd());
                    }

                    // Địa chỉ
                    if (house.getDiaChi() != null) {
                        String fullAddress = house.getDiaChi().getAp() + ", " +
                                house.getDiaChi().getXa() + ", " +
                                house.getDiaChi().getTinh();
                        txtDiaChiHienTai.setText(fullAddress);

                        edtAp.setText(house.getDiaChi().getAp() != null ? house.getDiaChi().getAp() : "");
                        edtXa.setText(house.getDiaChi().getXa() != null ? house.getDiaChi().getXa() : "");
                        edtTinh.setText(house.getDiaChi().getTinh() != null ? house.getDiaChi().getTinh() : "");
                    }

                    // Số nhân khẩu
                    int soNhanKhau = house.getThanhVien() != null ? house.getThanhVien().size() : 0;
                    txtNhanKhau.setText(soNhanKhau + " người");

                    // Đối tượng
                    if (house.getDoiTuong() != null) {
                        setSpinnerSelection(house.getDoiTuong());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UpdateSituationActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSpinnerSelection(DoiTuong dt) {
        String selected = "Khác";
        if (dt.isHoNgheo()) selected = "Hộ nghèo";
        else if (dt.isHoCanNgheo()) selected = "Hộ cận nghèo";
        else if (dt.isHoKhoKhan()) selected = "Khó khăn";
        else if (dt.isGiaDinhChinhSach()) selected = "Chính sách";

        int pos = ((ArrayAdapter<String>) spnDoiTuong.getAdapter()).getPosition(selected);
        if (pos >= 0) spnDoiTuong.setSelection(pos);
    }

    private void loadCurrentGhiChu() {
        householdRef.child("hoanCanh").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String ghiChu = task.getResult().child("ghiChu").getValue(String.class);
                if (ghiChu != null) {
                    edtGhiChuHoanCanh.setText(ghiChu);
                }
            }
        });
    }

    private void saveSituation() {
        String hoTen = edtHoTen.getText().toString().trim();
        String ap = edtAp.getText().toString().trim();
        String xa = edtXa.getText().toString().trim();
        String tinh = edtTinh.getText().toString().trim();
        String namSinhStr = edtNamSinh.getText().toString().trim();
        String cccd = edtCCCD.getText().toString().trim();
        String ghiChu = edtGhiChuHoanCanh.getText().toString().trim();
        String doiTuongSelected = spnDoiTuong.getSelectedItem().toString();

        Map<String, Object> updates = new HashMap<>();

        // Cập nhật thông tin chủ hộ
        updates.put("chuHo/hoTen", hoTen);
        updates.put("chuHo/cccd", cccd);
        try {
            updates.put("chuHo/namSinh", Integer.parseInt(namSinhStr));
        } catch (Exception e) {
            updates.put("chuHo/namSinh", 0);
        }

        // Cập nhật địa chỉ
        updates.put("diaChi/ap", ap);
        updates.put("diaChi/xa", xa);
        updates.put("diaChi/tinh", tinh);

        // Cập nhật đối tượng
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

        // Ghi chú hoàn cảnh
        updates.put("hoanCanh/ghiChu", ghiChu);

        householdRef.updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "✅ Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Lưu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
package com.example.bdsdcna.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdsdcna.HistoryHelper;
import com.example.bdsdcna.R;
import com.example.bdsdcna.adapters.ThanhVienAdapter;
import com.example.bdsdcna.models.ThanhVien;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateMemberActivity extends AppCompatActivity {

    private RecyclerView rvMembers;
    private Button btnAddNewField, btnDeleteSelected, btnSave;

    private final String currentHouseholdId = "HH0001";
    private DatabaseReference dbRef;

    private final List<ThanhVien> currentList = new ArrayList<>();
    private final Map<String, Boolean> selectedMap = new HashMap<>();
    private final List<String> newMemberIds = new ArrayList<>();
    private final Map<String, Map<String, Object>> originalDataMap = new HashMap<>();

    private ThanhVienAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_member);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbRef = FirebaseDatabase.getInstance().getReference();
        initViews();
        loadThanhVienData();

        btnAddNewField.setOnClickListener(v -> {
            ThanhVien tv = new ThanhVien();
            String tempId = "TV_" + System.currentTimeMillis();
            tv.setMemberId(tempId);
            tv.setHoTen("");
            tv.setCccd("");
            tv.setNgaySinh("");
            tv.setQuanHe("Khác");
            tv.setDanToc("Kinh");
            tv.setGioiTinh(1);

            currentList.add(tv);
            newMemberIds.add(tempId);
            selectedMap.put(tempId, false);

            adapter.notifyItemInserted(currentList.size() - 1);
            rvMembers.scrollToPosition(currentList.size() - 1);
        });

        btnDeleteSelected.setOnClickListener(v -> executeDeleteSelected());
        btnSave.setOnClickListener(v -> saveChangesToFirebase());
    }

    private void initViews() {
        rvMembers = findViewById(R.id.rvMembers);
        btnAddNewField = findViewById(R.id.btnAddNewField);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        btnSave = findViewById(R.id.btnSave);

        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ThanhVienAdapter(currentList, selectedMap);
        rvMembers.setAdapter(adapter);
    }

    private void loadThanhVienData() {
        dbRef.child("households").child(currentHouseholdId).child("thanhVien")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentList.clear();
                        selectedMap.clear();
                        newMemberIds.clear();
                        originalDataMap.clear();

                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ThanhVien tv = ds.getValue(ThanhVien.class);
                                if (tv != null) {
                                    String mId = tv.getMemberId();
                                    if (mId == null || mId.isEmpty()) {
                                        mId = ds.getKey();
                                        tv.setMemberId(mId);
                                    }

                                    currentList.add(tv);
                                    selectedMap.put(mId, false);

                                    Map<String, Object> orgFields = new HashMap<>();
                                    orgFields.put("hoTen", tv.getHoTen());
                                    orgFields.put("cccd", tv.getCccd());
                                    orgFields.put("ngaySinh", tv.getNgaySinh());
                                    orgFields.put("quanHe", tv.getQuanHe());
                                    orgFields.put("danToc", tv.getDanToc());
                                    orgFields.put("gioiTinh", tv.getGioiTinh());
                                    originalDataMap.put(mId, orgFields);
                                }
                            }
                        }

                        if (currentList.isEmpty()) {
                            ThanhVien firstBox = new ThanhVien();
                            String tempId = "TV_" + System.currentTimeMillis();
                            firstBox.setMemberId(tempId);
                            firstBox.setHoTen("");
                            firstBox.setCccd("");
                            firstBox.setNgaySinh("");
                            firstBox.setQuanHe("CHỦ HỘ");
                            firstBox.setDanToc("Kinh");
                            firstBox.setGioiTinh(2);

                            currentList.add(firstBox);
                            newMemberIds.add(tempId);
                            selectedMap.put(tempId, false);
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UpdateMemberActivity.this, "Lỗi đọc dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void executeDeleteSelected() {
        List<ThanhVien> toDelete = new ArrayList<>();
        boolean hasChuHoInSelection = false;

        for (ThanhVien tv : currentList) {
            Boolean isSelected = selectedMap.get(tv.getMemberId());
            if (isSelected != null && isSelected) {
                if ("CHỦ HỘ".equalsIgnoreCase(tv.getQuanHe()) || "CHU_HO".equalsIgnoreCase(tv.getQuanHe())) {
                    hasChuHoInSelection = true;
                } else {
                    toDelete.add(tv);
                }
            }
        }

        if (hasChuHoInSelection) {
            Toast.makeText(this, "Không thể xoá thành viên đang là Chủ hộ!", Toast.LENGTH_LONG).show();
        }

        if (toDelete.isEmpty()) return;

        for (ThanhVien tv : toDelete) {
            String mId = tv.getMemberId();
            if (!newMemberIds.contains(mId)) {
                String deleteDetails = "Họ tên: " + tv.getHoTen() + " | CCCD: " + tv.getCccd();

                // ĐÃ SỬA KHỚP 8 THAM SỐ
                HistoryHelper.log(
                        "Xóa thành viên",
                        currentHouseholdId,
                        mId,
                        "",
                        tv.getHoTen(),
                        "all_fields",
                        deleteDetails,
                        "Đã xóa"
                );
            }
            currentList.remove(tv);
            selectedMap.remove(mId);
            newMemberIds.remove(mId);
        }

        dbRef.child("households").child(currentHouseholdId).child("thanhVien").setValue(currentList)
                .addOnSuccessListener(unused -> {
                    adapter.notifyDataSetChanged();
                    Toast.makeText(UpdateMemberActivity.this, "Đã xoá và cập nhật lại danh sách.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveChangesToFirebase() {
        ThanhVien selectedChuHo = null;

        for (ThanhVien tv : currentList) {
            String ns = tv.getNgaySinh() != null ? tv.getNgaySinh().trim() : "";
            if (!ns.isEmpty() && !ns.contains("/") && ns.length() == 4) {
                tv.setNgaySinh("01/01/" + ns);
            }

            if ("CHỦ HỘ".equalsIgnoreCase(tv.getQuanHe()) || "CHU_HO".equalsIgnoreCase(tv.getQuanHe())) {
                selectedChuHo = tv;
            }
        }

        if (selectedChuHo == null) {
            Toast.makeText(this, "Vui lòng chọn một người có vai trò là Chủ hộ trước khi lưu!", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> childUpdates = new HashMap<>();
        dbRef.child("households").child(currentHouseholdId).child("thanhVien").removeValue();

        for (int i = 0; i < currentList.size(); i++) {
            ThanhVien tv = currentList.get(i);
            String mId = tv.getMemberId();

            childUpdates.put("/households/" + currentHouseholdId + "/thanhVien/" + i, tv);

            // ĐÃ SỬA KHỚP 8 THAM SỐ TRONG PHẦN LOG LƯU / CẬP NHẬT
            if (newMemberIds.contains(mId)) {
                if (!tv.getHoTen().isEmpty()) {
                    HistoryHelper.log(
                            "Thêm thành viên",
                            currentHouseholdId,
                            mId,
                            "",
                            tv.getHoTen(),
                            "hoTen",
                            "",
                            tv.getHoTen()
                    );
                }
            } else {
                Map<String, Object> orgFields = originalDataMap.get(mId);
                if (orgFields != null) {
                    String orgHoTen = (String) orgFields.get("hoTen");
                    String orgCccd = (String) orgFields.get("cccd");
                    String orgNgaySinh = (String) orgFields.get("ngaySinh");
                    String orgQuanHe = (String) orgFields.get("quanHe");

                    if (orgHoTen != null && !orgHoTen.equals(tv.getHoTen())) {
                        HistoryHelper.log("Sửa thông tin", currentHouseholdId, mId, "", tv.getHoTen(), "hoTen", orgHoTen, tv.getHoTen());
                    }
                    if (orgCccd != null && !orgCccd.equals(tv.getCccd())) {
                        HistoryHelper.log("Sửa thông tin", currentHouseholdId, mId, "", tv.getHoTen(), "cccd", orgCccd, tv.getCccd());
                    }
                    if (orgNgaySinh != null && !orgNgaySinh.equals(tv.getNgaySinh())) {
                        HistoryHelper.log("Sửa thông tin", currentHouseholdId, mId, "", tv.getHoTen(), "ngaySinh", orgNgaySinh, tv.getNgaySinh());
                    }
                    if (orgQuanHe != null && !orgQuanHe.equals(tv.getQuanHe())) {
                        HistoryHelper.log("Thay đổi vai trò", currentHouseholdId, mId, "", tv.getHoTen(), "quanHe", orgQuanHe, tv.getQuanHe());
                    }
                }
            }
        }

        int namSinhInt = 1964;
        try {
            String ns = selectedChuHo.getNgaySinh().trim();
            if (ns.contains("/")) {
                String[] parts = ns.split("/");
                namSinhInt = Integer.parseInt(parts[parts.length - 1]);
            } else {
                namSinhInt = Integer.parseInt(ns);
            }
        } catch (Exception ignored) {}

        String housePath = "/households/" + currentHouseholdId + "/chuHo/";
        childUpdates.put(housePath + "cccd", selectedChuHo.getCccd());
        childUpdates.put(housePath + "danToc", selectedChuHo.getDanToc());
        childUpdates.put(housePath + "gioiTinh", selectedChuHo.getGioiTinh());
        childUpdates.put(housePath + "gioiTinhText", selectedChuHo.getGioiTinh() == 2 ? "Nữ" : "Nam");
        childUpdates.put(housePath + "hoTen", selectedChuHo.getHoTen());
        childUpdates.put(housePath + "namSinh", namSinhInt);

        dbRef.updateChildren(childUpdates).addOnSuccessListener(unused -> {
            Toast.makeText(UpdateMemberActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            loadThanhVienData();
        }).addOnFailureListener(e -> {
            Toast.makeText(UpdateMemberActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
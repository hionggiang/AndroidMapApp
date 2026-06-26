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

    /**
     * ĐÃ SỬA: Tìm chính xác mảng thanhVien nằm lồng bên trong node households
     */
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
                                    // Ưu tiên dùng trường memberId có sẵn trong JSON của bạn để quản lý trạng thái
                                    String mId = tv.getMemberId();
                                    if (mId == null || mId.isEmpty()) {
                                        mId = ds.getKey(); // Phòng hờ nếu rỗng thì lấy index (0,1,2) làm mId
                                        tv.setMemberId(mId);
                                    }

                                    currentList.add(tv);
                                    selectedMap.put(mId, false);

                                    // Sao lưu dữ liệu gốc để so sánh và ghi Log lịch sử thay đổi
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

                        // Nếu hộ chưa có thành viên nào, tạo 1 dòng trống mặc định làm Chủ hộ
                        if (currentList.isEmpty()) {
                            ThanhVien firstBox = new ThanhVien();
                            String tempId = "TV_" + System.currentTimeMillis();
                            firstBox.setMemberId(tempId);
                            firstBox.setHoTen("");
                            firstBox.setCccd("");
                            firstBox.setNgaySinh("");
                            firstBox.setQuanHe("CHỦ HỘ");
                            firstBox.setDanToc("Kinh");
                            firstBox.setGioiTinh(2); // Set mặc định nữ theo data gốc của bạn

                            currentList.add(firstBox);
                            newMemberIds.add(tempId);
                            selectedMap.put(tempId, false);
                        }

                        // Đổ dữ liệu lên màn hình tại đây
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UpdateMemberActivity.this, "Lỗi đọc dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * ĐÃ SỬA: Xóa thành viên trong mảng lồng bằng cách cập nhật lại toàn bộ node thanhVien mới
     */
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

        // Tiến hành xóa cục bộ và ghi log trước
        for (ThanhVien tv : toDelete) {
            String mId = tv.getMemberId();
            if (!newMemberIds.contains(mId)) {
                String deleteDetails = "Họ tên: " + tv.getHoTen() + " | CCCD: " + tv.getCccd();
                HistoryHelper.log("Xóa thành viên", currentHouseholdId, mId, "all_fields", deleteDetails, "Đã xóa");
            }
            currentList.remove(tv);
            selectedMap.remove(mId);
            newMemberIds.remove(mId);
        }

        // Lưu trực tiếp danh sách còn lại lên Firebase để mảng tự động dồn index (không bị trống node ở giữa)
        dbRef.child("households").child(currentHouseholdId).child("thanhVien").setValue(currentList)
                .addOnSuccessListener(unused -> {
                    adapter.notifyDataSetChanged();
                    Toast.makeText(UpdateMemberActivity.this, "Đã xoá và cập nhật lại danh sách.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * ĐÃ SỬA: Ghi dữ liệu đồng bộ dạng mảng lồng (0, 1, 2) tránh phá vỡ cấu trúc JSON hiện tại
     */
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

        // Đầu tiên: Xóa sạch node thanhVien cũ của hộ này đi để ghi đè mảng mới (Tránh thừa index khi giảm số lượng)
        dbRef.child("households").child(currentHouseholdId).child("thanhVien").removeValue();

        // 1. Đẩy danh sách thành viên vào childUpdates theo dạng index mảng lồng (0, 1, 2...)
        for (int i = 0; i < currentList.size(); i++) {
            ThanhVien tv = currentList.get(i);
            String mId = tv.getMemberId();

            // Cập nhật đường dẫn lưu dạng mảng lồng: /households/HH0001/thanhVien/0, /1, /2...
            childUpdates.put("/households/" + currentHouseholdId + "/thanhVien/" + i, tv);

            // Ghi nhận nhật ký logs
            if (newMemberIds.contains(mId)) {
                if (!tv.getHoTen().isEmpty()) {
                    HistoryHelper.log("Thêm thành viên", currentHouseholdId, mId, "hoTen", "", tv.getHoTen());
                }
            } else {
                Map<String, Object> orgFields = originalDataMap.get(mId);
                if (orgFields != null) {
                    String orgHoTen = (String) orgFields.get("hoTen");
                    String orgCccd = (String) orgFields.get("cccd");
                    String orgNgaySinh = (String) orgFields.get("ngaySinh");
                    String orgQuanHe = (String) orgFields.get("quanHe");

                    if (orgHoTen != null && !orgHoTen.equals(tv.getHoTen())) {
                        HistoryHelper.log("Sửa thông tin", currentHouseholdId, mId, "hoTen", orgHoTen, tv.getHoTen());
                    }
                    if (orgCccd != null && !orgCccd.equals(tv.getCccd())) {
                        HistoryHelper.log("Sửa thông tin", currentHouseholdId, mId, "cccd", orgCccd, tv.getCccd());
                    }
                    if (orgNgaySinh != null && !orgNgaySinh.equals(tv.getNgaySinh())) {
                        HistoryHelper.log("Sửa thông tin", currentHouseholdId, mId, "ngaySinh", orgNgaySinh, tv.getNgaySinh());
                    }
                    if (orgQuanHe != null && !orgQuanHe.equals(tv.getQuanHe())) {
                        HistoryHelper.log("Thay đổi vai trò", currentHouseholdId, mId, "quanHe", orgQuanHe, tv.getQuanHe());
                    }
                }
            }
        }

        // 2. Ép kiểu năm sinh dạng số nguyên cho chủ hộ
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

        // 3. Đồng bộ duy nhất thông tin chủ hộ mới lên node chuHo ngoài rìa
        String housePath = "/households/" + currentHouseholdId + "/chuHo/";
        childUpdates.put(housePath + "cccd", selectedChuHo.getCccd());
        childUpdates.put(housePath + "danToc", selectedChuHo.getDanToc());
        childUpdates.put(housePath + "gioiTinh", selectedChuHo.getGioiTinh());
        childUpdates.put(housePath + "gioiTinhText", selectedChuHo.getGioiTinh() == 2 ? "Nữ" : "Nam");
        childUpdates.put(housePath + "hoTen", selectedChuHo.getHoTen());
        childUpdates.put(housePath + "namSinh", namSinhInt);

        // Kích hoạt cập nhật đồng loạt lên Firebase
        dbRef.updateChildren(childUpdates).addOnSuccessListener(unused -> {
            Toast.makeText(UpdateMemberActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            loadThanhVienData();
        }).addOnFailureListener(e -> {
            Toast.makeText(UpdateMemberActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
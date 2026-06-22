package com.example.bdsdcna.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bdsdcna.R;
import com.example.bdsdcna.models.ChuHo;
import com.example.bdsdcna.models.Household;
import com.example.bdsdcna.models.ThanhVien;
import com.example.bdsdcna.xulydulieu.ExcelImporterV2;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImportExcelActivity extends AppCompatActivity {

    private static final String TAG = "ImportExcelActivity";
    private static final int PICK_EXCEL = 200;

    private Uri excelUri;
    private Spinner spLoaiHo;
    private TextView tvSelectedFileName;
    private ProgressBar progressBar;
    private Button btnSelect, btnImport;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_excel);

        initViews();
        setupSpinner();

        btnSelect.setOnClickListener(v -> selectExcel());
        btnImport.setOnClickListener(v -> importExcel());
    }

    private void initViews() {
        spLoaiHo = findViewById(R.id.spLoaiHo);
        tvSelectedFileName = findViewById(R.id.tvSelectedFileName);
        progressBar = findViewById(R.id.progressBar);
        btnSelect = findViewById(R.id.btnSelectExcel);
        btnImport = findViewById(R.id.btnImportExcel);
    }

    private void setupSpinner() {
        String[] loaiHo = {
                "Hộ nghèo",
                "Hộ cận nghèo",
                "Hộ khó khăn",
                "Gia đình chính sách"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                loaiHo
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLoaiHo.setAdapter(adapter);
    }

    private void selectExcel() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        startActivityForResult(intent, PICK_EXCEL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_EXCEL && resultCode == RESULT_OK && data != null) {
            excelUri = data.getData();
            String fileName = getFileName(excelUri);

            tvSelectedFileName.setText(fileName);
            tvSelectedFileName.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            Toast.makeText(this, "Đã chọn file thành công", Toast.LENGTH_SHORT).show();
        }
    }

    private void importExcel() {
        if (excelUri == null) {
            Toast.makeText(this, "Vui lòng chọn file Excel trước", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoadingState(true);
        String selectedLoaiHo = spLoaiHo.getSelectedItem().toString();

        executorService.execute(() -> {
            try {
                List<Household> households = ExcelImporterV2.importExcel(this, excelUri, selectedLoaiHo);

                runOnUiThread(() -> {
                    if (households == null || households.isEmpty()) {
                        setLoadingState(false);
                        Toast.makeText(this, "Không tìm thấy dữ liệu hợp lệ trong file!", Toast.LENGTH_LONG).show();
                    } else {
                        uploadToFirebase(households);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi xử lý file Excel: ", e);
                runOnUiThread(() -> {
                    setLoadingState(false);
                    Toast.makeText(this, "Lỗi đọc file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void uploadToFirebase(List<Household> households) {
        final String prefix = "HH";
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("households");

        ref.get().addOnSuccessListener(snapshot -> {
            int maxId = 0;
            List<String> existingCccds = new ArrayList<>();

            // 1. Quét dữ liệu cũ trên Firebase (Nếu có) để tính tiến trình ID mới
            if (snapshot.exists() && snapshot.hasChildren()) {
                for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                    try {
                        String key = child.getKey();
                        if (key != null && key.startsWith(prefix)) {
                            int num = Integer.parseInt(key.substring(prefix.length()));
                            if (num > maxId) maxId = num;
                        }

                        if (child.hasChild("chuHo") && child.child("chuHo").hasChild("cccd")) {
                            String cccd = child.child("chuHo").child("cccd").getValue(String.class);
                            if (cccd != null && !cccd.trim().isEmpty()) {
                                existingCccds.add(cccd.trim());
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi đọc dữ liệu cũ: " + e.getMessage());
                    }
                }
            }

            int currentId = maxId;
            int skipCount = 0;
            int successCount = 0;

            // 2. Duyệt danh sách từ file Excel đẩy lên Firebase
            for (Household h : households) {

                // CƠ CHẾ SỬA LỖI: Điền khuyết thông tin tên chủ hộ nếu bị rỗng do đọc lệch dòng thành viên
                if (h.getChuHo() == null) {
                    h.setChuHo(new ChuHo());
                }

                if (h.getChuHo().getHoTen() == null || h.getChuHo().getHoTen().trim().isEmpty()) {
                    if (h.getThanhVien() != null && !h.getThanhVien().isEmpty()) {
                        // Lấy tạm tên thành viên đầu tiên làm đại diện tên chủ hộ nếu bị bỏ trống ô
                        ThanhVien firstMember = h.getThanhVien().get(0);
                        h.getChuHo().setHoTen(firstMember.getHoTen());
                        h.getChuHo().setCccd(firstMember.getCccd());
                        h.getChuHo().setDanToc(firstMember.getDanToc());
                    } else {
                        // Nếu cả hộ không có một mống tên nào -> Dòng trống rác thực sự của file Excel -> Bỏ qua
                        continue;
                    }
                }

                String newCccd = h.getChuHo().getCccd() != null ? h.getChuHo().getCccd().trim() : "";

                // CHỈ LỌC TRÙNG KHI: CCCD không trống VÀ đã xuất hiện trên Firebase từ trước
                if (!newCccd.isEmpty() && existingCccds.contains(newCccd)) {
                    skipCount++;
                    continue;
                }

                // Dữ liệu hoàn toàn hợp lệ -> Upload ngay lên Firebase
                currentId++;
                String newId = String.format("%s%04d", prefix, currentId);
                h.setHouseholdId(newId);
                h.setStt(currentId);

                ref.child(newId).setValue(h);
                successCount++;

                // Thêm CCCD vừa tạo vào mảng tạm để tránh trùng lặp nội bộ nếu file Excel khai báo trùng
                if (!newCccd.isEmpty()) {
                    existingCccds.add(newCccd);
                }
            }

            setLoadingState(false);

            // 3. Thông báo kết quả hiển thị trực quan
            if (successCount == 0 && skipCount == 0) {
                Toast.makeText(ImportExcelActivity.this,
                        "Không tìm thấy hộ gia đình nào có dữ liệu hợp lệ để Import!",
                        Toast.LENGTH_LONG).show();
            } else if (skipCount > 0) {
                Toast.makeText(ImportExcelActivity.this,
                        "Import thành công " + successCount + " hộ. Đã bỏ qua " + skipCount + " hộ trùng CCCD gốc!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ImportExcelActivity.this,
                        "Import thành công tốt đẹp " + successCount + " hộ vào hệ thống!",
                        Toast.LENGTH_LONG).show();
            }

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lỗi kết nối Firebase: ", e);
            setLoadingState(false);
            Toast.makeText(ImportExcelActivity.this, "Lỗi Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnImport.setEnabled(!isLoading);
        btnSelect.setEnabled(!isLoading);
        spLoaiHo.setEnabled(!isLoading);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
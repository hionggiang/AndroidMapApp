package com.example.bdsdcna;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.bdsdcna.models.History;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.util.UUID;

public class HistoryHelper {

    private static final String TAG = "HistoryHelper";

    private HistoryHelper() {
        throw new AssertionError("Utility class không được khởi tạo.");
    }

    /**
     * Hàm log lịch sử dựa trên Mã Định Danh (HouseholdId / MemberId) thay vì lưu Tên
     */
    public static void log(
            @NonNull String action, // "Sửa thông tin", "Thêm thành viên", "Cập nhật đối tượng"...
            String householdId,     // Mã hộ bị tác động (Ví dụ: "HH0001")
            String memberId,        // Mã thành viên bị tác động (Nếu sửa cả hộ thì truyền "")
            String field,           // Tên trường bị sửa (Ví dụ: "cccd", "hoNgheo", "danToc")
            Object oldValue,        // Giá trị cũ
            Object newValue         // Giá trị mới
    ) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Đồng bộ theo cấu trúc bảng "users" quản lý tài khoản người dùng
        String uid = "";
        String email = "";
        String name = "";

        if (currentUser != null) {
            uid = currentUser.getUid();
            if (currentUser.getEmail() != null) {
                email = currentUser.getEmail();
                name = currentUser.getEmail(); // Tạm thời dùng Email, nếu có bảng users riêng thì có thể update sau
            }
        }

        String historyId = UUID.randomUUID().toString();
        History history = new History();

        history.setHistoryId(historyId);
        history.setAction(action);

        // Nhận diện vị trí sửa đổi hoàn toàn bằng Mã (ID)
        history.setHouseholdId(parseString(householdId));
        history.setMemberId(parseString(memberId));

        history.setField(parseString(field));
        history.setOldValue(formatValue(oldValue));
        history.setNewValue(formatValue(newValue));

        // Người thực hiện thao tác (Khớp với node users)
        history.setUid(uid);
        history.setEmail(email);
        history.setName(name);

        // Thời gian chuẩn từ Server Firebase
        history.setTimestamp(ServerValue.TIMESTAMP);

        FirebaseDatabase.getInstance()
                .getReference("history")
                .child(historyId)
                .setValue(history)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi ghi lịch sử: " + e.getMessage(), e);
                });
    }

    private static String formatValue(Object value) {
        if (value == null) return "";
        if (value instanceof Boolean) {
            return (Boolean) value ? "Có" : "Không";
        }
        return String.valueOf(value).trim();
    }

    private static String parseString(String value) {
        return value == null ? "" : value.trim();
    }
}
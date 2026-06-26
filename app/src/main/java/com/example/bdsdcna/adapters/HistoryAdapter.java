package com.example.bdsdcna.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdsdcna.R;
import com.example.bdsdcna.models.History;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<History> historyList;

    public HistoryAdapter(Context context, List<History> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        History history = historyList.get(position);

        holder.txtAction.setText(getActionText(history.getAction()));

        // ĐỒNG BỘ: Đổi từ getUserName() sang getName() hoặc getEmail() tùy bạn muốn hiển thị gì
        holder.txtUser.setText(
                history.getName() == null || history.getName().isEmpty()
                        ? "Không xác định"
                        : history.getName()
        );

        String detail = buildDetail(history);
        holder.txtDetail.setText(detail);

        // ĐỒNG BỘ TIMESTAMP: Kiểm tra và ép kiểu an toàn từ Object sang Long
        if (history.getTimestamp() instanceof Long) {
            long timestampLong = (Long) history.getTimestamp();
            if (timestampLong > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                holder.txtTime.setText(sdf.format(new Date(timestampLong)));
            } else {
                holder.txtTime.setText("");
            }
        } else {
            holder.txtTime.setText("");
        }
    }

    private String getActionText(String action) {
        if (action == null) return "Hoạt động hệ thống";

        switch (action) {
            case "ADD_HOUSEHOLD": return "➕ Thêm hộ dân";
            case "UPDATE_HOUSEHOLD": return "✏️ Cập nhật hộ dân";
            case "DELETE_HOUSEHOLD": return "🗑️ Xóa hộ dân";
            case "ADD_MEMBER": return "➕ Thêm thành viên";
            case "UPDATE_MEMBER": return "✏️ Cập nhật thành viên";
            case "DELETE_MEMBER": return "🗑️ Xóa thành viên";
            case "ADD_USER": return "➕ Thêm người dùng";
            case "UPDATE_USER": return "✏️ Cập nhật người dùng";
            case "DELETE_USER": return "🗑️ Xóa người dùng";
            case "LOGIN": return "🔐 Đăng nhập";
            case "CHANGE_PASSWORD": return "🔑 Đổi mật khẩu";
            case "IMPORT_EXCEL": return "📊 Nhập dữ liệu Excel";
            default: return action;
        }
    }

    private String buildDetail(History history) {
        StringBuilder builder = new StringBuilder();

        // ĐỒNG BỘ: Đã bỏ qua hiển thị TargetName (Tên người bị sửa) theo ý bạn, định danh bằng ID

        if (history.getHouseholdId() != null && !history.getHouseholdId().isEmpty()) {
            builder.append("Mã hộ: ").append(history.getHouseholdId()).append("\n");
        }

        if (history.getMemberId() != null && !history.getMemberId().isEmpty()) {
            builder.append("Mã thành viên: ").append(history.getMemberId()).append("\n");
        }

        if (history.getField() != null && !history.getField().isEmpty()) {
            builder.append("Trường: ").append(history.getField()).append("\n");
        }

        if (history.getOldValue() != null && !history.getOldValue().isEmpty()) {
            builder.append("Cũ: ").append(history.getOldValue()).append("\n");
        }

        if (history.getNewValue() != null && !history.getNewValue().isEmpty()) {
            builder.append("Mới: ").append(history.getNewValue());
        }

        return builder.toString().trim();
    }

    @Override
    public int getItemCount() {
        return historyList == null ? 0 : historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtAction, txtDetail, txtUser, txtTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAction = itemView.findViewById(R.id.txtAction);
            txtDetail = itemView.findViewById(R.id.txtDetail);
            txtUser = itemView.findViewById(R.id.txtUser);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }
}
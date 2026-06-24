package com.example.bdsdcna.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdsdcna.R;
import com.example.bdsdcna.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> list;

    public UserAdapter(List<User> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = list.get(position);

        holder.txtName.setText(user.getFullName());
        holder.txtEmail.setText(user.getEmail());
        holder.txtRole.setText(user.getRole());
        holder.txtStatus.setText(user.getStatus());

        // UI trạng thái đẹp hơn
        if ("active".equalsIgnoreCase(user.getStatus())) {
            holder.txtStatus.setText("Đang hoạt động");
            holder.txtStatus.setTextColor(0xFF2E7D32); // xanh lá
        } else if ("blocked".equalsIgnoreCase(user.getStatus())) {
            holder.txtStatus.setText("Đã khóa");
            holder.txtStatus.setTextColor(0xFFC62828); // đỏ
        } else {
            holder.txtStatus.setText(user.getStatus());
            holder.txtStatus.setTextColor(0xFF616161); // xám
        }

        // Role hiển thị đẹp
        if ("admin".equalsIgnoreCase(user.getRole())) {
            holder.txtRole.setText("Admin");
            holder.txtRole.setTextColor(0xFF1565C0);
        } else {
            holder.txtRole.setText("User");
            holder.txtRole.setTextColor(0xFF455A64);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtEmail, txtRole, txtStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtName);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtRole = itemView.findViewById(R.id.txtRole);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}
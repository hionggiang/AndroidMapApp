package com.example.bdsdcna.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdsdcna.R;
import com.example.bdsdcna.models.Household;

import java.util.List;

public class HouseholdAdapter extends RecyclerView.Adapter<HouseholdAdapter.ViewHolder> {

    private Context context;
    private List<Household> householdList;
    private OnHouseClickListener listener;

    public interface OnHouseClickListener {
        void onHouseClick(Household household);
    }

    public HouseholdAdapter(Context context, List<Household> householdList, OnHouseClickListener listener) {
        this.context = context;
        this.householdList = householdList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_house, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Household house = householdList.get(position);

        // =======================
        // 1. STT an toàn NULL
        // =======================
        if (house.getStt() != null) {
            holder.txtStt.setText(String.valueOf(house.getStt()));
        } else {
            holder.txtStt.setText("0");
        }

        // =======================
        // 2. Màu STT
        // =======================
        switch (position % 6) {
            case 0:
                holder.txtStt.setBackgroundResource(R.drawable.bg_circle_red);
                break;
            case 1:
                holder.txtStt.setBackgroundResource(R.drawable.bg_circle_orange);
                break;
            case 2:
                holder.txtStt.setBackgroundResource(R.drawable.bg_circle_blue);
                break;
            case 3:
                holder.txtStt.setBackgroundResource(R.drawable.bg_circle_purple);
                break;
            case 4:
                holder.txtStt.setBackgroundResource(R.drawable.bg_circle_green);
                break;
            default:
                holder.txtStt.setBackgroundResource(R.drawable.bg_circle_teal);
                break;
        }

        // =======================
        // 3. Tên chủ hộ (NULL SAFE)
        // =======================
        if (house.getChuHo() != null && house.getChuHo().getHoTen() != null) {
            holder.txtName.setText(house.getChuHo().getHoTen());
        } else {
            holder.txtName.setText("Chưa có tên");
        }

        // =======================
        // 4. Địa chỉ an toàn
        // =======================
        StringBuilder diaChiBuilder = new StringBuilder();

        if (house.getDiaChi() != null) {

            if (house.getDiaChi().getAp() != null && !house.getDiaChi().getAp().isEmpty()) {
                diaChiBuilder.append(house.getDiaChi().getAp());
            }

            if (house.getDiaChi().getXa() != null && !house.getDiaChi().getXa().isEmpty()) {
                if (diaChiBuilder.length() > 0) diaChiBuilder.append(", ");
                diaChiBuilder.append(house.getDiaChi().getXa());
            }

            if (house.getDiaChi().getTinh() != null && !house.getDiaChi().getTinh().isEmpty()) {
                if (diaChiBuilder.length() > 0) diaChiBuilder.append(", ");
                diaChiBuilder.append(house.getDiaChi().getTinh());
            }
        }

        holder.txtAddress.setText(
                diaChiBuilder.length() > 0 ? diaChiBuilder.toString() : "Chưa cập nhật"
        );

        // =======================
        // 5. Đối tượng an sinh
        // =======================
        String doiTuong = "";

        if (house.getDoiTuong() != null) {
            if (house.getDoiTuong().isHoNgheo()) {
                doiTuong = "Hộ nghèo";
            } else if (house.getDoiTuong().isHoCanNgheo()) {
                doiTuong = "Hộ cận nghèo";
            } else if (house.getDoiTuong().isGiaDinhChinhSach()) {
                doiTuong = "Hộ chính sách";
            } else if (house.getDoiTuong().isHoKhoKhan()) {
                doiTuong = "Hộ khó khăn";
            }
        }

        holder.txtObject.setText(
                doiTuong.isEmpty() ? "Chưa xác định" : doiTuong
        );

        // =======================
        // 6. Loại hỗ trợ (tạm fix cứng)
        // =======================
        holder.txtSupport.setText("Xây mới");
        holder.txtSupport.setBackgroundResource(R.drawable.bg_support_red);

        // =======================
        // 7. Click item an toàn
        // =======================
        holder.cardView.setOnClickListener(v -> {
            if (listener != null && house != null) {
                listener.onHouseClick(house);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (householdList != null) ? householdList.size() : 0;
    }

    // =======================
    // UPDATE DATA (FILTER SAFE)
    // =======================
    public void updateData(List<Household> newList) {
        if (newList == null) return;

        householdList.clear();
        householdList.addAll(newList);
        notifyDataSetChanged();
    }

    // =======================
    // VIEW HOLDER
    // =======================
    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView txtStt;
        TextView txtName;
        TextView txtAddress;
        TextView txtObject;
        TextView txtSupport;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            txtStt = itemView.findViewById(R.id.txtStt);
            txtName = itemView.findViewById(R.id.txtName);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            txtObject = itemView.findViewById(R.id.txtObject);
            txtSupport = itemView.findViewById(R.id.txtSupport);
        }
    }
}
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

public class HouseholdAdapter
        extends RecyclerView.Adapter<HouseholdAdapter.ViewHolder> {

    private Context context;
    private List<Household> householdList;
    private OnHouseClickListener listener;

    public interface OnHouseClickListener {
        void onHouseClick(Household household);
    }

    public HouseholdAdapter(
            Context context,
            List<Household> householdList,
            OnHouseClickListener listener) {

        this.context = context;
        this.householdList = householdList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater
                .from(context)
                .inflate(
                        R.layout.item_house,
                        parent,
                        false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Household house =
                householdList.get(position);

        // STT
        holder.txtStt.setText(
                String.valueOf(house.getStt()));

        // Màu vòng tròn STT
        switch (position % 6) {

            case 0:
                holder.txtStt.setBackgroundResource(
                        R.drawable.bg_circle_red);
                break;

            case 1:
                holder.txtStt.setBackgroundResource(
                        R.drawable.bg_circle_orange);
                break;

            case 2:
                holder.txtStt.setBackgroundResource(
                        R.drawable.bg_circle_blue);
                break;

            case 3:
                holder.txtStt.setBackgroundResource(
                        R.drawable.bg_circle_purple);
                break;

            case 4:
                holder.txtStt.setBackgroundResource(
                        R.drawable.bg_circle_green);
                break;

            default:
                holder.txtStt.setBackgroundResource(
                        R.drawable.bg_circle_teal);
                break;
        }

        // Tên chủ hộ
        if (house.getChuHo() != null) {

            holder.txtName.setText(
                    house.getChuHo().getHoTen());

        } else {

            holder.txtName.setText("");
        }

        // Địa chỉ
        if (house.getDiaChi() != null) {

            String diaChi =
                    house.getDiaChi().getAp()
                            + ", "
                            + house.getDiaChi().getXa()
                            + ", "
                            + house.getDiaChi().getHuyen();

            holder.txtAddress.setText(diaChi);

        } else {

            holder.txtAddress.setText("Chưa cập nhật");
        }

        // Đối tượng
         String doiTuong = "";

        switch (house.getNhomHo()) {

            case "ho_ngheo":
                doiTuong = "Hộ nghèo";
                break;

            case "ho_can_ngheo":
                doiTuong = "Hộ cận nghèo";
                break;

            case "ho_chinh_sach":
                doiTuong = "Hộ chính sách";
                break;

            case "ho_kho_khan":
                doiTuong = "Hộ khó khăn";
                break;
        }

        holder.txtObject.setText(doiTuong);

        // Loại hỗ trợ
        String support = "";

        if (house.getHoTro() != null) {

            if ("XAY_MOI".equals(
                    house.getHoTro().getLoai())) {

                support = "Xây mới";

                holder.txtSupport.setBackgroundResource(
                        R.drawable.bg_support_red);

            } else {

                support = "Sửa chữa";

                holder.txtSupport.setBackgroundResource(
                        R.drawable.bg_support_orange);
            }
        }

        holder.txtSupport.setText(support);

        // Click item
        holder.cardView.setOnClickListener(v -> {

            if (listener != null) {

                listener.onHouseClick(house);
            }
        });
    }

    @Override
    public int getItemCount() {

        return householdList == null
                ? 0
                : householdList.size();
    }

    public void updateData(
            List<Household> newList) {

        householdList.clear();

        householdList.addAll(newList);

        notifyDataSetChanged();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        CardView cardView;

        TextView txtStt;
        TextView txtName;
        TextView txtAddress;
        TextView txtObject;
        TextView txtSupport;

        public ViewHolder(
                @NonNull View itemView) {

            super(itemView);

            cardView =
                    itemView.findViewById(
                            R.id.cardView);

            txtStt =
                    itemView.findViewById(
                            R.id.txtStt);

            txtName =
                    itemView.findViewById(
                            R.id.txtName);

            txtAddress =
                    itemView.findViewById(
                            R.id.txtAddress);

            txtObject =
                    itemView.findViewById(
                            R.id.txtObject);

            txtSupport =
                    itemView.findViewById(
                            R.id.txtSupport);
        }
    }
}
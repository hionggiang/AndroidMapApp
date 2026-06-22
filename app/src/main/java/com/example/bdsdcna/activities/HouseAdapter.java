package com.example.bdsdcna.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdsdcna.R;
import com.example.bdsdcna.models.Household;

import java.util.List;

public class HouseAdapter extends RecyclerView.Adapter<HouseAdapter.HouseViewHolder> {

    private final List<Household> householdList;

    public HouseAdapter(List<Household> householdList) {
        this.householdList = householdList;
    }

    @NonNull
    @Override
    public HouseViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(
                parent.getContext()
        ).inflate(
                R.layout.item_house,
                parent,
                false
        );

        return new HouseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull HouseViewHolder holder,
            int position
    ) {

        Household house =
                householdList.get(position);

        try {
            holder.txtHouseId.setText(
                    "Mã hộ: " + house.getHouseholdId()
            );
        } catch (Exception e) {
            holder.txtHouseId.setText("Mã hộ");
        }

        try {
            holder.txtChuHo.setText(
                    "Chủ hộ: " +
                            house.getChuHo().getHoTen()
            );
        } catch (Exception e) {
            holder.txtChuHo.setText("Chủ hộ");
        }

        try {
            holder.txtAddress.setText(
                    "Địa chỉ: " +
                            house.getDiaChi().getAp()
            );
        } catch (Exception e) {
            holder.txtAddress.setText("Địa chỉ");
        }
    }

    @Override
    public int getItemCount() {
        return householdList.size();
    }

    public static class HouseViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtHouseId;
        TextView txtChuHo;
        TextView txtAddress;

        public HouseViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            txtHouseId =
                    itemView.findViewById(
                            R.id.txtHouseId
                    );

            txtChuHo =
                    itemView.findViewById(
                            R.id.txtChuHo
                    );

            txtAddress =
                    itemView.findViewById(
                            R.id.txtAddress
                    );
        }
    }
}
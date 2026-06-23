package com.example.bdsdcna.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdsdcna.R;
import com.example.bdsdcna.models.ThanhVien;

import java.util.ArrayList;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private Context context;
    private ArrayList<ThanhVien> list;

    public MemberAdapter(Context context, ArrayList<ThanhVien> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_member, parent, false);

        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {

        ThanhVien tv = list.get(position);

        holder.txtName.setText(tv.getHoTen());

        holder.txtBirth.setText(
                "Ngày sinh: " + tv.getNgaySinh());

        holder.txtGender.setText(
                "Giới tính: " + tv.getGioiTinhText());

        holder.txtCCCD.setText(
                "CCCD: " + tv.getCccd());

        holder.txtEthnic.setText(
                "Dân tộc: " + tv.getDanToc());

        holder.txtRelation.setText(
                "Quan hệ: " + convertRelation(tv.getQuanHe()));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {

        TextView txtName;
        TextView txtRelation;
        TextView txtGender;
        TextView txtBirth;
        TextView txtCCCD;
        TextView txtEthnic;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtName);
            txtRelation = itemView.findViewById(R.id.txtRelation);
            txtGender = itemView.findViewById(R.id.txtGender);
            txtBirth = itemView.findViewById(R.id.txtBirth);
            txtCCCD = itemView.findViewById(R.id.txtCCCD);
            txtEthnic = itemView.findViewById(R.id.txtEthnic);
        }
    }

    private String convertRelation(String relation) {

        if (relation == null) return "";

        switch (relation) {

            case "CHU_HO":
                return "Chủ hộ";

            case "VO_CHONG":
                return "Vợ/Chồng";

            case "CON":
                return "Con";

            case "CHA":
                return "Cha";

            case "ME":
                return "Mẹ";

            case "ONG":
                return "Ông";

            case "BA":
                return "Bà";

            case "ANH":
                return "Anh";

            case "CHI":
                return "Chị";

            case "EM":
                return "Em";

            case "KHAC":
                return "Khác";

            default:
                return relation;
        }
    }
}
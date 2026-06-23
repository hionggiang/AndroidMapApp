package com.example.bdsdcna.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdsdcna.R;
// IMPORT CHÍNH XÁC ĐƯỜNG DẪN ĐẾN ACTIVITY CHI TIẾT NHƯ KHAI BÁO TRONG MANIFEST
import com.example.bdsdcna.activities.HouseDetailActivity;
import com.example.bdsdcna.adapters.HouseholdAdapter;
import com.example.bdsdcna.models.Household;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class HouseListFragment extends Fragment {

    private RecyclerView recyclerView;
    private HouseholdAdapter adapter;
    private ArrayList<Household> householdList;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_house_list, container, false);

        // Ánh xạ và cấu hình RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        householdList = new ArrayList<>();

        // Khởi tạo Adapter xử lý click item chuyển sang màn hình HouseDetailActivity
        adapter = new HouseholdAdapter(getContext(), householdList, household -> {
            if (household != null && household.getHouseholdId() != null) {
                // Khởi tạo Intent chuyển từ Fragment hiện tại sang HouseDetailActivity
                Intent intent = new Intent(getContext(), HouseDetailActivity.class);
                // Truyền mã ID của hộ sang Activity mới
                intent.putExtra("householdId", household.getHouseholdId());
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Không tìm thấy mã hộ gia đình này!", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);

        // Khởi tạo Realtime Database trỏ thẳng đến nhánh gốc chứa danh sách các hộ
        databaseReference = FirebaseDatabase.getInstance().getReference("households");
        loadData();

        return view;
    }

    private void loadData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                householdList.clear();

                // Log kiểm tra số lượng dữ liệu lấy về từ Firebase nhằm gỡ lỗi nhanh
                System.out.println("Tổng số hộ trên Database: " + snapshot.getChildrenCount());

                // Duyệt qua từng node con trực tiếp dưới root "households"
                for (DataSnapshot item : snapshot.getChildren()) {
                    System.out.println("Firebase Node Key: " + item.getKey());

                    Household household = item.getValue(Household.class);
                    if (household != null) {
                        householdList.add(household);
                    }
                }

                // Sắp xếp danh sách hộ gia đình tăng dần theo Số Thứ Tự (STT)
                Collections.sort(householdList, Comparator.comparingInt(Household::getStt));

                // Cập nhật giao diện danh sách
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Hiển thị thông báo khi có lỗi truy vấn dữ liệu từ Firebase
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
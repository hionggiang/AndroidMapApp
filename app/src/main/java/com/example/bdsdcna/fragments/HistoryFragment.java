package com.example.bdsdcna.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdsdcna.R;
import com.example.bdsdcna.adapters.HistoryAdapter;
import com.example.bdsdcna.models.History;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private Spinner spUser;

    private HistoryAdapter adapter;

    private final List<History> historyList = new ArrayList<>();

    private final ArrayList<String> userIds = new ArrayList<>();
    private final ArrayList<String> userNames = new ArrayList<>();

    public HistoryFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_history,
                container,
                false
        );

        rvHistory = view.findViewById(R.id.rvHistory);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        spUser = view.findViewById(R.id.spUser);

        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new HistoryAdapter(requireContext(), historyList);
        rvHistory.setAdapter(adapter);

        loadUsers();

        spUser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view,
                                       int position,
                                       long id) {

                String uid = userIds.get(position);

                if (uid.isEmpty()) {
                    loadAllHistory();
                } else {
                    loadHistory(uid);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    /**
     * Đọc danh sách tất cả người dùng
     */
    private void loadUsers() {

        FirebaseDatabase.getInstance()
                .getReference("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        userIds.clear();
                        userNames.clear();

                        userIds.add("");
                        userNames.add("Tất cả người dùng");

                        for (DataSnapshot item : snapshot.getChildren()) {

                            String uid = item.getKey();

                            String name = item.child("fullName")
                                    .getValue(String.class);

                            String role = item.child("role")
                                    .getValue(String.class);

                            if (name == null || name.isEmpty()) {
                                name = item.child("email")
                                        .getValue(String.class);
                            }

                            if (role == null || role.isEmpty()) {
                                role = "user";
                            }

                            userIds.add(uid);
                            userNames.add(name + " (" + role + ")");
                        }

                        ArrayAdapter<String> spinnerAdapter =
                                new ArrayAdapter<>(
                                        requireContext(),
                                        android.R.layout.simple_spinner_item,
                                        userNames
                                );

                        spinnerAdapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item
                        );

                        spUser.setAdapter(spinnerAdapter);

                        loadAllHistory();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    /**
     * Hiển thị toàn bộ lịch sử
     */
    private void loadAllHistory() {

        loadHistoryInternal(
                FirebaseDatabase.getInstance()
                        .getReference("history")
        );
    }

    /**
     * Hiển thị lịch sử của một người dùng
     */
    private void loadHistory(String uid) {

        loadHistoryInternal(
                FirebaseDatabase.getInstance()
                        .getReference("history")
                        .orderByChild("userId")
                        .equalTo(uid)
        );
    }

    /**
     * Hàm dùng chung
     */
    private void loadHistoryInternal(com.google.firebase.database.Query query) {

        progressBar.setVisibility(View.VISIBLE);

        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                historyList.clear();

                for (DataSnapshot item : snapshot.getChildren()) {

                    History history = item.getValue(History.class);

                    if (history != null) {
                        historyList.add(history);
                    }
                }

                Collections.sort(historyList,
                        (o1, o2) ->
                                Long.compare(
                                        o2.getTimestamp(),
                                        o1.getTimestamp()));

                adapter.notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);

                if (historyList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvHistory.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvHistory.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
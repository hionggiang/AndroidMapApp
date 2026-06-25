package com.example.bdsdcna.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import java.util.Comparator;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private HistoryAdapter adapter;
    private final List<History> historyList = new ArrayList<>();

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

        rvHistory.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        adapter = new HistoryAdapter(requireContext(), historyList);
        rvHistory.setAdapter(adapter);

        loadHistory();

        return view;
    }

    private void loadHistory() {

        progressBar.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance()
                .getReference("history")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        historyList.clear();

                        for (DataSnapshot item : snapshot.getChildren()) {

                            History history =
                                    item.getValue(History.class);

                            if (history != null) {
                                historyList.add(history);
                            }
                        }

                        Collections.sort(historyList,
                                (o1, o2) ->
                                        Long.compare(
                                                o2.getTimestamp(),
                                                o1.getTimestamp()
                                        ));

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
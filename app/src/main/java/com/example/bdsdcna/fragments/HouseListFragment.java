package com.example.bdsdcna.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdsdcna.R;
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

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_house_list,
                container,
                false);

        recyclerView =
                view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext()));

        householdList = new ArrayList<>();

        adapter = new HouseholdAdapter(
                getContext(),
                householdList,
                household -> {

                    // TODO:
                    // Mở HouseDetailActivity

                });

        recyclerView.setAdapter(adapter);

        databaseReference =
                FirebaseDatabase.getInstance()
                        .getReference("households");

        loadData();

        return view;
    }

    private void loadData() {

        databaseReference.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        householdList.clear();

                        loadGroup(snapshot,
                                "ho_ngheo");

                        loadGroup(snapshot,
                                "ho_can_ngheo");

                        loadGroup(snapshot,
                                "ho_kho_khan");

                        loadGroup(snapshot,
                                "ho_chinh_sach");

                        Collections.sort(
                                householdList,
                                Comparator.comparingInt(
                                        Household::getStt));

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                    }
                });
    }

    private void loadGroup(
            DataSnapshot root,
            String groupName) {

        DataSnapshot group =
                root.child(groupName);

        for (DataSnapshot item :
                group.getChildren()) {

            Household household =
                    item.getValue(
                            Household.class);

            if (household != null) {

                // Gắn nhóm hộ
                household.setNhomHo(
                        groupName);

                householdList.add(
                        household);
            }
        }
    }
}
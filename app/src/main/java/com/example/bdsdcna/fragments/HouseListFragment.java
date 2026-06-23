package com.example.bdsdcna.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdsdcna.R;
import com.example.bdsdcna.activities.HouseDetailActivity;
import com.example.bdsdcna.adapters.HouseholdAdapter;
import com.example.bdsdcna.models.Household;
import com.google.firebase.database.*;

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

                    Intent intent =
                            new Intent(
                                    getContext(),
                                    HouseDetailActivity.class);

                    intent.putExtra(
                            "householdId",
                            household.getHouseholdId());

                    startActivity(intent);
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
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        householdList.clear();

                        System.out.println(
                                "Tong so ho: "
                                        + snapshot.getChildrenCount());

                        for (DataSnapshot item : snapshot.getChildren()) {

                            System.out.println(
                                    "KEY = " + item.getKey());

                            Household household =
                                    item.getValue(Household.class);

                            if (household != null) {

                                householdList.add(household);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                getContext(),
                                error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                });
    }
}
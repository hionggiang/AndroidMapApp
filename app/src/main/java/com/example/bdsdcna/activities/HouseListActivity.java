package com.example.bdsdcna.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdsdcna.R;
import com.example.bdsdcna.models.Household;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HouseListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HouseAdapter adapter;
    private List<Household> householdList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_list);

        recyclerView = findViewById(R.id.recyclerView);

        householdList = new ArrayList<>();

        adapter = new HouseAdapter(householdList);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerView.setAdapter(adapter);

        loadData();
    }

    private void loadData() {

        FirebaseFirestore.getInstance()
                .collection("households")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    householdList.clear();

                    for (QueryDocumentSnapshot doc :
                            queryDocumentSnapshots) {

                        Household household =
                                doc.toObject(
                                        Household.class
                                );

                        householdList.add(
                                household
                        );
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
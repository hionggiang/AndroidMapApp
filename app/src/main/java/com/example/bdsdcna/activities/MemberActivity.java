package com.example.bdsdcna.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdsdcna.R;
import com.example.bdsdcna.adapters.MemberAdapter;
import com.example.bdsdcna.models.ThanhVien;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MemberActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvMember;

    private ArrayList<ThanhVien> memberList;
    private MemberAdapter adapter;

    private String householdId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);

        toolbar = findViewById(R.id.toolbar);
        rvMember = findViewById(R.id.rvMember);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thành viên hộ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        rvMember.setLayoutManager(new LinearLayoutManager(this));

        memberList = new ArrayList<>();
        adapter = new MemberAdapter(this, memberList);

        rvMember.setAdapter(adapter);

        householdId = getIntent().getStringExtra("householdId");

        loadMembers();
    }

    private void loadMembers() {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("households")
                .child(householdId)
                .child("thanhVien");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                memberList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {

                    ThanhVien tv = data.getValue(ThanhVien.class);

                    if (tv != null) {
                        memberList.add(tv);
                    }

                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }

}
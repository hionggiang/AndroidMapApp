package com.example.bdsdcna.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdsdcna.R;
import com.example.bdsdcna.adapters.UserAdapter;
import com.example.bdsdcna.models.User;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;

    private RecyclerView recyclerUsers;
    private TextInputEditText edtSearch;

    private UserAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private List<User> filteredList = new ArrayList<>();

    private DatabaseReference userRef;

    // stats
    private int total, active, admin, blocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        initView();
        setupDrawer();
        setupRecyclerView();
        setupFirebase();
        setupSearch();
    }

    private void initView() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navView = findViewById(R.id.navView);

        recyclerUsers = findViewById(R.id.recyclerUsers);
        edtSearch = findViewById(R.id.edtSearch);
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.open,
                R.string.close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(item -> {
            handleMenu(item);
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void handleMenu(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_logout) {
            finish();
        }
    }

    private void setupRecyclerView() {
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(filteredList);
        recyclerUsers.setAdapter(adapter);
    }

    private void setupFirebase() {
        userRef = FirebaseDatabase.getInstance().getReference("users");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                userList.clear();

                total = active = admin = blocked = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);

                    if (user != null) {
                        userList.add(user);

                        total++;

                        if ("active".equals(user.getStatus())) active++;
                        if ("admin".equals(user.getRole())) admin++;
                        if ("blocked".equals(user.getStatus())) blocked++;
                    }
                }

                filteredList.clear();
                filteredList.addAll(userList);

                adapter.notifyDataSetChanged();

                updateStatsUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String key) {
        filteredList.clear();

        for (User u : userList) {
            if (u.getFullName().toLowerCase().contains(key.toLowerCase())
                    || u.getEmail().toLowerCase().contains(key.toLowerCase())) {
                filteredList.add(u);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void updateStatsUI() {
        // TODO: bind TextView stats từ item_stat_total, active, admin, blocked
        // ví dụ:
        // txtTotal.setText(String.valueOf(total));
    }
}
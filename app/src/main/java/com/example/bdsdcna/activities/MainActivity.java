package com.example.bdsdcna.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.bdsdcna.R;
import com.example.bdsdcna.fragments.HouseListFragment;
import com.example.bdsdcna.fragments.MapFragment;
import com.example.bdsdcna.fragments.NotificationFragment;
import com.example.bdsdcna.fragments.ProfileFragment;
import com.example.bdsdcna.fragments.StatisticFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity
        extends AppCompatActivity {

    private TextView txtTitle;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_main);

        txtTitle =
                findViewById(R.id.txtTitle);

        bottomNavigation =
                findViewById(
                        R.id.bottomNavigation);

        loadFragment(
                new MapFragment());

        txtTitle.setText(
                "Bản đồ");

        bottomNavigation.setSelectedItemId(
                R.id.nav_map);

        bottomNavigation
                .setOnItemSelectedListener(
                        item -> {

                            int id =
                                    item.getItemId();

                            if(id == R.id.nav_map){

                                txtTitle.setText(
                                        "Bản đồ");

                                loadFragment(
                                        new MapFragment());

                                return true;
                            }

                            if(id == R.id.nav_house){

                                txtTitle.setText(
                                        "Danh sách hộ");

                                loadFragment(
                                        new HouseListFragment());

                                return true;
                            }

                            if(id == R.id.nav_stat){

                                txtTitle.setText(
                                        "Thống kê");

                                loadFragment(
                                        new StatisticFragment());

                                return true;
                            }

                            if(id == R.id.nav_notify){

                                txtTitle.setText(
                                        "Thông báo");

                                loadFragment(
                                        new NotificationFragment());

                                return true;
                            }

                            if(id == R.id.nav_profile){

                                txtTitle.setText(
                                        "Cá nhân");

                                loadFragment(
                                        new ProfileFragment());

                                return true;
                            }

                            return false;
                        });
    }

    private void loadFragment(
            Fragment fragment){

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.frameContainer,
                        fragment)
                .commit();
    }
}
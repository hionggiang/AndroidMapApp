package com.example.bdsdcna.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.bdsdcna.R;
import com.example.bdsdcna.fragments.HistoryFragment;
import com.example.bdsdcna.fragments.HouseListFragment;
import com.example.bdsdcna.fragments.MapFragment;
import com.example.bdsdcna.fragments.NotificationFragment;
import com.example.bdsdcna.fragments.ProfileFragment;
import com.example.bdsdcna.fragments.StatisticFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private TextView txtTitle;
    private BottomNavigationView bottomNavigation;
    private ImageView btnImportExcel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtTitle = findViewById(R.id.txtTitle);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnImportExcel = findViewById(R.id.btnImportExcel);

        if (btnImportExcel != null) {
            btnImportExcel.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ImportExcelActivity.class);
                startActivity(intent);
            });
        }

        // 1. Đăng ký sự kiện chuyển Tab duy nhất một lần tại onCreate
        setupBottomNavigation();

        // 2. Kiểm tra dữ liệu hướng dẫn bản đồ (nếu khởi tạo ứng dụng từ Intent chỉ đường)
        handleMapIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleMapIntent(intent);
    }

    /**
     * TÁCH BIỆT: Cấu hình lắng nghe sự kiện Bottom Navigation
     */
    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);

            if (id == R.id.nav_map) {
                txtTitle.setText("Bản đồ");
                // Chỉ tạo mới MapFragment nếu fragment hiện tại không phải là MapFragment
                if (!(currentFragment instanceof MapFragment)) {
                    loadFragment(new MapFragment());
                }
                return true;
            }
            if (id == R.id.nav_house) {
                txtTitle.setText("Danh sách hộ");
                if (!(currentFragment instanceof HouseListFragment)) {
                    loadFragment(new HouseListFragment());
                }
                return true;
            }
            if (id == R.id.nav_stat) {
                txtTitle.setText("Thống kê");
                if (!(currentFragment instanceof StatisticFragment)) {
                    loadFragment(new StatisticFragment());
                }
                return true;
            }
//            if (id == R.id.nav_notify) {
//                txtTitle.setText("Thông báo");
//                if (!(currentFragment instanceof NotificationFragment)) {
//                    loadFragment(new NotificationFragment());
//                }
//                return true;
//            }
            if (id == R.id.nav_history) {
                txtTitle.setText("Lịch sử");
                if (!(currentFragment instanceof HistoryFragment)) {
                    loadFragment(new HistoryFragment());
                }
                return true;
            }
            if (id == R.id.nav_profile) {
                txtTitle.setText("Cá nhân");
                if (!(currentFragment instanceof ProfileFragment)) {
                    loadFragment(new ProfileFragment());
                }
                return true;
            }
            return false;
        });
    }

    /**
     * Bóc tách dữ liệu tọa độ điều hướng an toàn từ HouseDetailActivity gửi sang
     */
    private void handleMapIntent(Intent intent) {
        if (intent != null && intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
            double lat = intent.getDoubleExtra("latitude", 0.0);
            double lng = intent.getDoubleExtra("longitude", 0.0);
            String houseName = intent.getStringExtra("houseName");

            // --- BỔ SUNG: Lấy thêm householdId từ Intent gửi qua ---
            String householdId = intent.getStringExtra("householdId");

            // Tạo MapFragment có đính kèm toạ độ ghim cụ thể
            MapFragment mapFragment = new MapFragment();
            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", lat);
            bundle.putDouble("longitude", lng);
            bundle.putString("houseName", houseName);

            // --- BỔ SUNG: Đóng gói householdId vào bundle để gửi tiếp sang MapFragment ---
            bundle.putString("householdId", householdId);

            mapFragment.setArguments(bundle);

            // Nạp fragment chứa dữ liệu ghim vào View
            loadFragment(mapFragment);
            txtTitle.setText("Bản đồ");

            // Cập nhật trạng thái hiển thị trên thanh Bottom
            bottomNavigation.getMenu().findItem(R.id.nav_map).setChecked(true);
        } else {
            // Mở ứng dụng bình thường (không truyền tọa độ chỉ đường)
            if (getSupportFragmentManager().findFragmentById(R.id.frameContainer) == null) {
                loadFragment(new MapFragment());
                txtTitle.setText("Bản đồ");
                bottomNavigation.getMenu().findItem(R.id.nav_map).setChecked(true);
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .commit();
    }
}
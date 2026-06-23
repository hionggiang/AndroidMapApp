package com.example.bdsdcna.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bdsdcna.R;
import com.example.bdsdcna.adapters.ImageAdapter;
import com.example.bdsdcna.adapters.ImageSliderAdapter;
import com.example.bdsdcna.models.Household;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HouseDetailActivity extends AppCompatActivity {

    private TextView txtName, txtAddress, txtObject, txtSupport, txtBirth, txtCCCD, txtMember, txtCost;
    private Button btnMap, btnUpdate;
    private TextView tabImage, tab360, tabMember, tabProgress, tabSponsor;
    private String householdId;

    // --- CẤU HÌNH BIẾN CHO KHU VỰC HÌNH ẢNH MỚI ---
    private ViewPager2 viewPagerImages;
    private ImageButton btnPrev, btnNext;
    private ImageSliderAdapter imageAdapter;
    private List<String> mListUrls;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_detail);

        // 1. Ánh xạ toàn bộ view hiển thị văn bản
        txtName = findViewById(R.id.txtName);
        txtAddress = findViewById(R.id.txtAddress);
        txtObject = findViewById(R.id.txtObject);
        txtSupport = findViewById(R.id.txtSupport);
        txtBirth = findViewById(R.id.txtBirth);
        txtCCCD = findViewById(R.id.txtCCCD);
        txtMember = findViewById(R.id.txtMember);
        txtCost = findViewById(R.id.txtCost);

        btnMap = findViewById(R.id.btnMap);
        btnUpdate = findViewById(R.id.btnUpdate);

        tabImage = findViewById(R.id.tabImage);
        tab360 = findViewById(R.id.tab360);
        tabMember = findViewById(R.id.tabMember);
        tabProgress = findViewById(R.id.tabProgress);
        tabSponsor = findViewById(R.id.tabSponsor);

        // 2. Ánh xạ các thành phần ViewPager2 và nút bấm chuyển ảnh
        viewPagerImages = findViewById(R.id.viewPagerImages);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        mListUrls = new ArrayList<>();
        imageAdapter = new ImageSliderAdapter(this, mListUrls); // Đổi ở đây
        viewPagerImages.setAdapter(imageAdapter);

        // Nhận dữ liệu ID hộ gia đình từ Intent màn hình trước truyền qua
        householdId = getIntent().getStringExtra("householdId");
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Khởi tạo gốc dữ liệu Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (householdId != null) {
            loadHouseDetail(householdId);
            loadHouseImages(householdId); // Tiến hành tải ảnh lướt
            openScreen(householdId);
        }

        // Cấu hình thanh tiêu đề hệ thống nếu có sử dụng ActionBar mặc định
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Chi tiết hộ");
        }

// 3. Sự kiện bấm nút Next/Previous để lướt ảnh qua lại chủ động
        btnPrev.setOnClickListener(v -> {
            int currentItem = viewPagerImages.getCurrentItem();
            if (currentItem > 0) {
                // SỬA: Thay .setItem bằng .setCurrentItem
                viewPagerImages.setCurrentItem(currentItem - 1, true);
            }
        });

        btnNext.setOnClickListener(v -> {
            int currentItem = viewPagerImages.getCurrentItem();
            if (currentItem < mListUrls.size() - 1) {
                viewPagerImages.setCurrentItem(currentItem + 1, true);
            }
        });
    }

    // --- HÀM TẢI DANH SÁCH LINK ẢNH VÀ ĐỒNG BỘ VỚI VIEWPAGER2 ---
    private void loadHouseImages(String householdId) {
        mDatabase.child("households").child(householdId).child("images")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mListUrls.clear(); // Làm sạch danh sách tránh nhân đôi ảnh trùng lặp

                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String url = dataSnapshot.getValue(String.class);
                                if (url != null) {
                                    mListUrls.add(url);
                                }
                            }
                        }
                        imageAdapter.notifyDataSetChanged();

                        // Ẩn 2 nút bấm lướt ảnh nếu hộ gia đình này có ít hơn hoặc bằng 1 ảnh
                        if (mListUrls.size() <= 1) {
                            btnPrev.setVisibility(View.GONE);
                            btnNext.setVisibility(View.GONE);
                        } else {
                            btnPrev.setVisibility(View.VISIBLE);
                            btnNext.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HouseDetailActivity.this, "Không thể tải ảnh: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- ĐIỀU HƯỚNG CHUYỂN MÀN HÌNH THEO TỪNG THÀNH PHẦN CLICK ---
    private void openScreen(String householdId) {
        tabImage.setOnClickListener(v -> {
            Intent intent = new Intent(HouseDetailActivity.this, HouseImagesActivity.class);
            intent.putExtra("householdId", householdId);
            startActivity(intent);
        });

        tab360.setOnClickListener(v -> {
            Intent intent = new Intent(HouseDetailActivity.this, Camera360Activity.class);
            intent.putExtra("householdId", householdId);
            startActivity(intent);
        });

        tabMember.setOnClickListener(v -> {
            Intent intent = new Intent(HouseDetailActivity.this, MemberActivity.class);
            intent.putExtra("householdId", householdId);
            startActivity(intent);
        });

        tabProgress.setOnClickListener(v -> {
            Intent intent = new Intent(HouseDetailActivity.this, ProgressActivity.class);
            intent.putExtra("householdId", householdId);
            startActivity(intent);
        });

        tabSponsor.setOnClickListener(v -> {
            Intent intent = new Intent(HouseDetailActivity.this, SponsorActivity.class);
            intent.putExtra("householdId", householdId);
            startActivity(intent);
        });

        btnMap.setOnClickListener(v -> {
            Intent intent = new Intent(HouseDetailActivity.this, MapActivity.class);
            intent.putExtra("householdId", householdId);
            startActivity(intent);
        });

        btnUpdate.setOnClickListener(v -> {
            Intent intent = new Intent(HouseDetailActivity.this, UpdateHouseActivity.class);
            intent.putExtra("householdId", householdId);
            startActivity(intent);
        });
    }

    // --- TẢI CHI TIẾT DỮ LIỆU CỦA HỘ ---
    private void loadHouseDetail(String householdId) {
        DatabaseReference ref = mDatabase.child("households").child(householdId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Household house = snapshot.getValue(Household.class);
                if (house != null) {
                    showData(house);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // --- HIỂN THỊ DỮ LIỆU CHỮ LÊN GIAO DIỆN ---
    private void showData(Household house) {
        // 1. Chủ hộ
        if (house.getChuHo() != null) {
            txtName.setText(house.getChuHo().getHoTen());
            txtBirth.setText("Năm sinh: " + house.getChuHo().getNamSinh());
            txtCCCD.setText("CCCD: " + house.getChuHo().getCccd());
        }

        // 2. Địa chỉ
        if (house.getDiaChi() != null) {
            String diaChi = "";
            if (house.getDiaChi().getAp() != null) diaChi += house.getDiaChi().getAp();
            if (house.getDiaChi().getXa() != null) diaChi += ", " + house.getDiaChi().getXa();
            if (house.getDiaChi().getTinh() != null) diaChi += ", " + house.getDiaChi().getTinh();
            txtAddress.setText(diaChi);
        }

        // 3. Số nhân khẩu
        if (house.getThanhVien() != null) {
            txtMember.setText("Số nhân khẩu: " + house.getThanhVien().size() + " người");
        } else {
            txtMember.setText("Số nhân khẩu: 0");
        }

        // 4. Phân loại đối tượng
        String doiTuong = "Khác";
        if (house.getDoiTuong() != null) {
            if (house.getDoiTuong().isHoNgheo()) doiTuong = "Hộ nghèo";
            else if (house.getDoiTuong().isHoCanNgheo()) doiTuong = "Hộ cận nghèo";
            else if (house.getDoiTuong().isGiaDinhChinhSach()) doiTuong = "Gia đình chính sách";
            else if (house.getDoiTuong().isHoKhoKhan()) doiTuong = "Hộ khó khăn";
            else if (house.getDoiTuong().isHoBaoTroXaHoi()) doiTuong = "Hộ bảo trợ xã hội";
            else if (house.getDoiTuong().isNguoiCoCong()) doiTuong = "Người có công";
        }
        txtObject.setText(doiTuong);

        // 5. Thông tin kinh phí hỗ trợ
        if (house.getHoTro() != null) {
            txtCost.setText("Kinh phí đề xuất: " + String.format("%,d", house.getHoTro().getKinhPhiDeXuat()) + " đ");
            txtSupport.setText(house.getHoTro().getKinhPhiDeXuat() > 0 ? "Đã đề xuất hỗ trợ" : "Chưa đề xuất");
        } else {
            txtSupport.setText("Chưa cập nhật");
            txtCost.setText("Kinh phí đề xuất: 0 đ");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
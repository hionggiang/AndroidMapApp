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
import com.example.bdsdcna.adapters.FullScreenImageAdapter;
import com.example.bdsdcna.adapters.ImageSliderAdapter;
import com.example.bdsdcna.models.Household;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

    private double houseLatitude = 0.0;
    private double houseLongitude = 0.0;
    private String houseName = "";

    // Image Slider
    private ViewPager2 viewPagerImages;
    private TextView tvImageCounter;
    private ImageSliderAdapter imageAdapter;
    private List<String> mListUrls;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_detail);

        // Ánh xạ views
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

        viewPagerImages = findViewById(R.id.viewPagerImages);
        tvImageCounter = findViewById(R.id.tvImageCounter);

        mListUrls = new ArrayList<>();
        imageAdapter = new ImageSliderAdapter(this, mListUrls);
        viewPagerImages.setAdapter(imageAdapter);

        imageAdapter.setOnItemClickListener(position -> {
            if (mListUrls != null && !mListUrls.isEmpty()) {
                openFullScreenDialog(position);
            }
        });

        householdId = getIntent().getStringExtra("householdId");
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (householdId != null) {
            loadHouseDetail(householdId);   // Load lần đầu
            loadHouseImages(householdId);
            openScreen(householdId);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Chi tiết hộ");
        }

        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tvImageCounter.setText((position + 1) + "/" + mListUrls.size());
            }
        });
    }

    // ================= TỰ ĐỘNG CẬP NHẬT KHI QUAY LẠI =================
    @Override
    protected void onResume() {
        super.onResume();
        if (householdId != null) {
            loadHouseDetail(householdId);   // Load lại dữ liệu mỗi khi quay về màn hình
        }
    }

    // ================= BOTTOM SHEET CẬP NHẬT =================
    private void showUpdateBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_update_options, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        bottomSheetView.findViewById(R.id.optionSituation).setOnClickListener(v -> {
            goToUpdateActivity(UpdateSituationActivity.class);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void goToUpdateActivity(Class<?> targetActivity) {
        Intent intent = new Intent(HouseDetailActivity.this, targetActivity);
        intent.putExtra("householdId", householdId);
        startActivity(intent);
    }

    // ================= ĐIỀU HƯỚNG CÁC TAB =================
    private void openScreen(String householdId) {
        tabImage.setOnClickListener(v -> {
            Intent intent = new Intent(this, HouseImagesActivity.class);
            intent.putExtra("householdId", householdId);
            startActivity(intent);
        });

        tab360.setOnClickListener(v -> {
            Intent intent = new Intent(this, Camera360Activity.class);
            intent.putExtra("householdId", householdId);
            startActivity(intent);
        });

        tabMember.setOnClickListener(v -> {
            Intent intent = new Intent(this, MemberActivity.class);
            intent.putExtra("householdId", householdId);
            startActivity(intent);
        });

        tabProgress.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProgressActivity.class);
            intent.putExtra("householdId", householdId);
            startActivity(intent);
        });

        tabSponsor.setOnClickListener(v -> {
            Intent intent = new Intent(this, SponsorActivity.class);
            intent.putExtra("householdId", householdId);
            startActivity(intent);
        });

        btnMap.setOnClickListener(v -> {
            if (houseLatitude != 0.0 && houseLongitude != 0.0) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("latitude", houseLatitude);
                intent.putExtra("longitude", houseLongitude);
                intent.putExtra("houseName", houseName);
                intent.putExtra("householdId", householdId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Hộ này chưa được cập nhật dữ liệu tọa độ!", Toast.LENGTH_SHORT).show();
            }
        });

        btnUpdate.setOnClickListener(v -> showUpdateBottomSheet());
    }

    // ================= CÁC HÀM CŨ GIỮ NGUYÊN =================
    private void openFullScreenDialog(int startPosition) {
        final android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_screen_image);

        ViewPager2 viewPagerFullScreen = dialog.findViewById(R.id.viewPagerFullScreen);
        TextView tvCounterFull = dialog.findViewById(R.id.tvCounterFull);
        ImageButton btnCloseFull = dialog.findViewById(R.id.btnCloseFull);

        FullScreenImageAdapter fullScreenAdapter = new FullScreenImageAdapter(this, mListUrls);
        viewPagerFullScreen.setAdapter(fullScreenAdapter);

        viewPagerFullScreen.setCurrentItem(startPosition, false);
        tvCounterFull.setText((startPosition + 1) + "/" + mListUrls.size());

        viewPagerFullScreen.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tvCounterFull.setText((position + 1) + "/" + mListUrls.size());
            }
        });

        btnCloseFull.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void loadHouseImages(String householdId) {
        mDatabase.child("households").child(householdId).child("images")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mListUrls.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String url = dataSnapshot.getValue(String.class);
                                if (url != null) mListUrls.add(url);
                            }
                        }
                        imageAdapter.notifyDataSetChanged();

                        if (mListUrls.size() > 0) {
                            tvImageCounter.setText("1/" + mListUrls.size());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HouseDetailActivity.this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadHouseDetail(String householdId) {
        DatabaseReference ref = mDatabase.child("households").child(householdId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Household house = snapshot.getValue(Household.class);
                if (house != null) {
                    try {
                        if (snapshot.hasChild("extraFields")) {
                            DataSnapshot extra = snapshot.child("extraFields");
                            Object latObj = extra.child("vido").getValue();
                            Object lngObj = extra.child("kinhdo").getValue();

                            houseLatitude = latObj != null ? Double.parseDouble(latObj.toString()) : 0.0;
                            houseLongitude = lngObj != null ? Double.parseDouble(lngObj.toString()) : 0.0;
                        }
                    } catch (Exception e) {
                        houseLatitude = houseLongitude = 0.0;
                    }
                    showData(house);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HouseDetailActivity.this, "Lỗi tải chi tiết", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showData(Household house) {
        if (house.getChuHo() != null) {
            houseName = house.getChuHo().getHoTen();
            txtName.setText(houseName);
            txtBirth.setText("Năm sinh: " + house.getChuHo().getNamSinh());
            txtCCCD.setText("CCCD: " + house.getChuHo().getCccd());
        }

        if (house.getDiaChi() != null) {
            String diaChi = house.getDiaChi().getAp() + ", " +
                    house.getDiaChi().getXa() + ", " +
                    house.getDiaChi().getTinh();
            txtAddress.setText(diaChi);
        }

        if (house.getThanhVien() != null) {
            txtMember.setText("Số nhân khẩu: " + house.getThanhVien().size() + " người");
        } else {
            txtMember.setText("Số nhân khẩu: 0");
        }

        // Đối tượng
        String doiTuong = "Khác";
        if (house.getDoiTuong() != null) {
            if (house.getDoiTuong().isHoNgheo()) doiTuong = "Hộ nghèo";
            else if (house.getDoiTuong().isHoCanNgheo()) doiTuong = "Hộ cận nghèo";
            else if (house.getDoiTuong().isHoKhoKhan()) doiTuong = "Hộ khó khăn";
            else if (house.getDoiTuong().isGiaDinhChinhSach()) doiTuong = "Gia đình chính sách";
        }
        txtObject.setText(doiTuong);

        // Hoàn cảnh + Ghi chú
        String ghiChu = "Chưa có ghi chú hoàn cảnh";
        if (house.getHoanCanh() != null && house.getHoanCanh().getGhiChu() != null
                && !house.getHoanCanh().getGhiChu().trim().isEmpty()) {
            ghiChu = house.getHoanCanh().getGhiChu();
        }
        txtSupport.setText("Hoàn cảnh: " + ghiChu);

        // Kinh phí
        if (house.getHoTro() != null && house.getHoTro().getKinhPhiDeXuat() > 0) {
            txtCost.setText("Kinh phí đề xuất: " + String.format("%,d", house.getHoTro().getKinhPhiDeXuat()) + " đ");
        } else {
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
    }
}
package com.example.bdsdcna.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bdsdcna.R;
import com.example.bdsdcna.models.Household;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HouseDetailActivity extends AppCompatActivity {

    private TextView txtName;
    private TextView txtAddress;
    private TextView txtObject;
    private TextView txtSupport;
    private TextView txtBirth;
    private TextView txtCCCD;
    private TextView txtMember;
    private TextView txtCost;

    private Button btnMap;
    private Button btnUpdate;

    private TextView tabImage;
    private TextView tab360;
    private TextView tabMember;
    private TextView tabProgress;
    private TextView tabSponsor;

    private String householdId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_detail);

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

        householdId = getIntent().getStringExtra("householdId");
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        if (householdId != null) {
            loadHouseDetail(householdId);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Chi tiết hộ");
        }

        openScreen();
    }
    private void openScreen(String householdId) {

        tabImage.setOnClickListener(v -> {

            Intent intent = new Intent(
                    HouseDetailActivity.this,
                    HouseImagesActivity.class);

            intent.putExtra("householdId", householdId);

            startActivity(intent);
        });

        tab360.setOnClickListener(v -> {

            Intent intent = new Intent(
                    HouseDetailActivity.this,
                    Camera360Activity.class);

            intent.putExtra("householdId", householdId);

            startActivity(intent);
        });

        tabMember.setOnClickListener(v -> {

            Intent intent = new Intent(
                    HouseDetailActivity.this,
                    MemberActivity.class);

            intent.putExtra("householdId", householdId);

            startActivity(intent);
        });

        tabProgress.setOnClickListener(v -> {

            Intent intent = new Intent(
                    HouseDetailActivity.this,
                    ProgressActivity.class);

            intent.putExtra("householdId", householdId);

            startActivity(intent);
        });

        tabSponsor.setOnClickListener(v -> {

            Intent intent = new Intent(
                    HouseDetailActivity.this,
                    SponsorActivity.class);

            intent.putExtra("householdId", householdId);

            startActivity(intent);
        });

    }
    private void loadHouseDetail(String householdId) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("households")
                .child(householdId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Household house = snapshot.getValue(Household.class);

                if (house != null) {
                    showData(house);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void showData(Household house) {

        // Chủ hộ
        if (house.getChuHo() != null) {

            txtName.setText(house.getChuHo().getHoTen());

            txtBirth.setText(
                    "Năm sinh: " +
                            house.getChuHo().getNamSinh());

            txtCCCD.setText(
                    "CCCD: " +
                            house.getChuHo().getCccd());
        }

        // Địa chỉ
        if (house.getDiaChi() != null) {

            String diaChi = "";

            if (house.getDiaChi().getAp() != null) {
                diaChi += house.getDiaChi().getAp();
            }

            if (house.getDiaChi().getXa() != null) {
                diaChi += ", " + house.getDiaChi().getXa();
            }

            if (house.getDiaChi().getTinh() != null) {
                diaChi += ", " + house.getDiaChi().getTinh();
            }

            txtAddress.setText(diaChi);
        }

        // Số nhân khẩu
        if (house.getThanhVien() != null) {

            txtMember.setText(
                    "Số nhân khẩu: "
                            + house.getThanhVien().size()
                            + " người");

        } else {

            txtMember.setText("Số nhân khẩu: 0");
        }

        // Đối tượng
        String doiTuong = "Khác";

        if (house.getDoiTuong() != null) {

            if (house.getDoiTuong().isHoNgheo()) {

                doiTuong = "Hộ nghèo";

            } else if (house.getDoiTuong().isHoCanNgheo()) {

                doiTuong = "Hộ cận nghèo";

            } else if (house.getDoiTuong().isGiaDinhChinhSach()) {

                doiTuong = "Gia đình chính sách";

            } else if (house.getDoiTuong().isHoKhoKhan()) {

                doiTuong = "Hộ khó khăn";

            } else if (house.getDoiTuong().isHoBaoTroXaHoi()) {

                doiTuong = "Hộ bảo trợ xã hội";

            } else if (house.getDoiTuong().isNguoiCoCong()) {

                doiTuong = "Người có công";

            }

        }

        txtObject.setText(doiTuong);

        // Hỗ trợ
        if (house.getHoTro() != null) {

            txtCost.setText(
                    "Kinh phí đề xuất: "
                            + String.format("%,d",
                            house.getHoTro().getKinhPhiDeXuat())
                            + " đ");

            if (house.getHoTro().getKinhPhiDeXuat() > 0) {

                txtSupport.setText("Đã đề xuất hỗ trợ");

            } else {

                txtSupport.setText("Chưa đề xuất");
            }

        } else {

            txtSupport.setText("Chưa cập nhật");

            txtCost.setText("Kinh phí đề xuất: 0 đ");
        }

    }
    private void openScreen() {

        tabImage.setOnClickListener(v -> {

            Intent intent = new Intent(
                    HouseDetailActivity.this,
                    HouseImagesActivity.class);

            intent.putExtra("householdId", householdId);

            startActivity(intent);

        });

        tab360.setOnClickListener(v -> {

            Intent intent = new Intent(
                    HouseDetailActivity.this,
                    Camera360Activity.class);

            intent.putExtra("householdId", householdId);

            startActivity(intent);

        });

        tabMember.setOnClickListener(v -> {

            Intent intent = new Intent(
                    HouseDetailActivity.this,
                    MemberActivity.class);

            intent.putExtra("householdId", householdId);

            startActivity(intent);

        });

        tabProgress.setOnClickListener(v -> {

            Intent intent = new Intent(
                    HouseDetailActivity.this,
                    ProgressActivity.class);

            intent.putExtra("householdId", householdId);

            startActivity(intent);

        });

        tabSponsor.setOnClickListener(v -> {

            Intent intent = new Intent(
                    HouseDetailActivity.this,
                    SponsorActivity.class);

            intent.putExtra("householdId", householdId);

            startActivity(intent);

        });

        btnMap.setOnClickListener(v -> {

            Intent intent = new Intent(
                    HouseDetailActivity.this,
                    MapActivity.class);

            intent.putExtra("householdId", householdId);

            startActivity(intent);

        });

        btnUpdate.setOnClickListener(v -> {

            Intent intent = new Intent(
                    HouseDetailActivity.this,
                    UpdateHouseActivity.class);

            intent.putExtra("householdId", householdId);

            startActivity(intent);

        });

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
package com.example.bdsdcna.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bdsdcna.R;
import com.example.bdsdcna.models.Household;
import com.google.firebase.database.*;

public class HouseDetailActivity extends AppCompatActivity {

    private TextView txtName;
    private TextView txtAddress;
    private TextView txtObject;
    private TextView txtSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_detail);

        txtName = findViewById(R.id.txtName);
        txtAddress = findViewById(R.id.txtAddress);
        txtObject = findViewById(R.id.txtObject);
        txtSupport = findViewById(R.id.txtSupport);

        String houseId =
                getIntent().getStringExtra("houseId");

        loadHouseDetail(houseId);
    }

    private void loadHouseDetail(String houseId) {

        DatabaseReference ref =
                FirebaseDatabase.getInstance()
                        .getReference("households");

        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            DataSnapshot snapshot) {

                        String[] groups = {
                                "ho_ngheo",
                                "ho_can_ngheo",
                                "ho_chinh_sach",
                                "ho_kho_khan"
                        };

                        for (String group : groups) {

                            DataSnapshot houseSnap =
                                    snapshot.child(group)
                                            .child(houseId);

                            if (houseSnap.exists()) {

                                Household house =
                                        houseSnap.getValue(
                                                Household.class);

                                if (house != null) {

                                    showData(
                                            house,
                                            group);
                                }

                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(
                            DatabaseError error) {

                    }
                });
    }

    private void showData(
            Household house,
            String group) {

        txtName.setText(
                house.getChuHo().getHoTen());

        txtAddress.setText(
                house.getDiaChi().getAp()
                        + ", "
                        + house.getDiaChi().getXa()
                        + ", "
                        + house.getDiaChi().getHuyen());

        switch (group) {

            case "ho_ngheo":
                txtObject.setText("Hộ nghèo");
                break;

            case "ho_can_ngheo":
                txtObject.setText("Hộ cận nghèo");
                break;

            case "ho_chinh_sach":
                txtObject.setText("Hộ chính sách");
                break;

            default:
                txtObject.setText("Hộ khó khăn");
                break;
        }

        txtSupport.setText(
                house.getHoTro().getLoai());
    }
}
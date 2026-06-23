package com.example.bdsdcna.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bdsdcna.R;
import com.example.bdsdcna.models.DoiTuong;
import com.example.bdsdcna.models.Household;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    private TextView tvTongSo, tvXayMoi, tvSuaChua;

    // 📌 BUTTON FILTER
    private Button btnHoNgheo, btnCanNgheo, btnChinhSach;

    private DatabaseReference databaseReference;

    public MapFragment() {}

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_map,
                container,
                false
        );

        // ================= UI =================
        tvTongSo = view.findViewById(R.id.tvTongSo);
        tvXayMoi = view.findViewById(R.id.tvXayMoi);
        tvSuaChua = view.findViewById(R.id.tvSuaChua);

        btnHoNgheo = view.findViewById(R.id.btnHoNgheo);
        btnCanNgheo = view.findViewById(R.id.btnCanNgheo);
        btnChinhSach = view.findViewById(R.id.btnChinhSach);

        databaseReference =
                FirebaseDatabase.getInstance()
                        .getReference("households");

        loadData();

        return view;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        LatLng longHo = new LatLng(10.2167, 105.9667);

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(longHo, 13)
        );

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
    }

    // ==============================
    // 📌 LOAD + THỐNG KÊ + ẨN BUTTON
    // ==============================
    private void loadData() {

        databaseReference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        int tong = 0;
                        int xayMoi = 0;
                        int suaChua = 0;

                        boolean coHoNgheo = false;
                        boolean coCanNgheo = false;
                        boolean coChinhSach = false;

                        for (DataSnapshot item : snapshot.getChildren()) {

                            Household h = item.getValue(Household.class);

                            if (h != null) {

                                tong++;

                                DoiTuong dt = h.getDoiTuong();

                                if (dt != null) {

                                    if (dt.isXayMoi()) {
                                        xayMoi++;
                                    } else {
                                        suaChua++;
                                    }

                                    // ======================
                                    // 📌 CHECK LOẠI HỘ
                                    // ======================
                                    if (dt.isHoNgheo()) {
                                        coHoNgheo = true;
                                    }

                                    if (dt.isHoCanNgheo()) {
                                        coCanNgheo = true;
                                    }

                                    if (dt.isGiaDinhChinhSach()) {
                                        coChinhSach = true;
                                    }
                                }
                            }
                        }

                        // ======================
                        // 📌 SET TEXT
                        // ======================
                        tvTongSo.setText(String.valueOf(tong));
                        tvXayMoi.setText(String.valueOf(xayMoi));
                        tvSuaChua.setText(String.valueOf(suaChua));

                        // ======================
                        // 📌 SHOW / HIDE BUTTON
                        // ======================

                        btnHoNgheo.setVisibility(
                                coHoNgheo ? View.VISIBLE : View.GONE
                        );

                        btnCanNgheo.setVisibility(
                                coCanNgheo ? View.VISIBLE : View.GONE
                        );

                        btnChinhSach.setVisibility(
                                coChinhSach ? View.VISIBLE : View.GONE
                        );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }
}

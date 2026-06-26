package com.example.bdsdcna.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.bdsdcna.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

import java.util.HashMap;
import java.util.Map;

public class UpdateLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference mDatabase;

    private String householdId;
    private TextView txtCurrentCoordinates;
    private Button btnCurrentLocation, btnSaveLocation;
    private LatLng selectedLatLng; // Toạ độ đang được nhắm trúng để chuẩn bị lưu

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_location);

        // Nhận dữ liệu ID hộ được truyền sang
        householdId = getIntent().getStringExtra("householdId");
        if (householdId == null) {
            Toast.makeText(this, "Không tìm thấy dữ liệu hộ cần cập nhật!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        txtCurrentCoordinates = findViewById(R.id.txtCurrentCoordinates);
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        btnSaveLocation = findViewById(R.id.btnSaveLocation);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // Khởi tạo Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Sự kiện lựa chọn 1: Bấm để lấy ngay vị trí hiện tại thiết bị bằng GPS
        btnCurrentLocation.setOnClickListener(v -> getDeviceLocation(true));

        // Sự kiện bấm lưu dữ liệu lên Firebase
        btnSaveLocation.setOnClickListener(v -> saveLocationToFirebase());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Cấu hình cử chỉ bản đồ
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Bắt sự kiện khi người dùng kéo thả, di chuyển bản đồ (Lựa chọn 2: Chọn vị trí bất kỳ trên bản đồ)
        mMap.setOnCameraIdleListener(() -> {
            selectedLatLng = mMap.getCameraPosition().target; // Lấy toạ độ tại tâm bản đồ
            txtCurrentCoordinates.setText(String.format("Tọa độ chọn: %.6f, %.6f",
                    selectedLatLng.latitude, selectedLatLng.longitude));
        });

        // Đọc dữ liệu cũ của hộ này từ Firebase để quyết định điểm Zoom ban đầu
        checkAndLoadExistLocation();
    }

    private void checkAndLoadExistLocation() {
        mDatabase.child("households").child(householdId).child("extraFields")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double lat = 0.0;
                        double lng = 0.0;

                        if (snapshot.exists()) {
                            Object latObj = snapshot.child("vido").getValue();
                            Object lngObj = snapshot.child("kinhdo").getValue();
                            if (latObj != null) lat = Double.parseDouble(latObj.toString());
                            if (lngObj != null) lng = Double.parseDouble(lngObj.toString());
                        }

                        // Nếu đã có tọa độ lưu trữ hợp lệ trước đó, thực hiện zoom tới tọa độ cũ
                        if (lat != 0.0 && lng != 0.0) {
                            LatLng existLatLng = new LatLng(lat, lng);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(existLatLng, 16f));
                        } else {
                            // Nếu toạ độ trống (0.0), tiến hành tìm và tự động zoom về vị trí GPS của điện thoại
                            getDeviceLocation(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        getDeviceLocation(true);
                    }
                });
    }

    private void getDeviceLocation(boolean zipToCamera) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                selectedLatLng = currentLatLng;

                if (zipToCamera) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f));
                }
                txtCurrentCoordinates.setText(String.format("Tọa độ chọn: %.6f, %.6f",
                        currentLatLng.latitude, currentLatLng.longitude));
            } else {
                Toast.makeText(this, "Không thể lấy vị trí thiết bị. Hãy bật định vị GPS!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveLocationToFirebase() {
        if (selectedLatLng == null) {
            Toast.makeText(this, "Chưa xác định được toạ độ chọn!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đóng gói dữ liệu chuẩn cấu trúc extraFields đề ra
        Map<String, Object> extraFields = new HashMap<>();
        extraFields.put("kinhdo", selectedLatLng.longitude);
        extraFields.put("vido", selectedLatLng.latitude);

        // Lưu đè/Tạo mới node extraFields bên trong hộ gia đình chỉ định
        mDatabase.child("households").child(householdId).child("extraFields")
                .setValue(extraFields)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(UpdateLocationActivity.this, "Cập nhật vị trí hộ thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Hoàn tất đóng màn hình, tự động onResume bên màn Detail sẽ kéo tọa độ mới về.
                })
                .addOnFailureListener(e ->
                        Toast.makeText(UpdateLocationActivity.this, "Lưu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocation(true);
            } else {
                Toast.makeText(this, "Quyền truy cập vị trí bị từ chối!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
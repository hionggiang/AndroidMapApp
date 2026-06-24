package com.example.bdsdcna.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bdsdcna.R;
import com.example.bdsdcna.models.DoiTuong;
import com.example.bdsdcna.models.Household;
import com.example.bdsdcna.models.ChuHo;
import com.example.bdsdcna.models.DiaChi;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker; // Đảm bảo import đúng dòng này
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView tvTongSo, tvXayMoi, tvSuaChua;
    private Button btnHoNgheo, btnCanNgheo, btnChinhSach;
    private DatabaseReference databaseReference;

    // Biến cờ để kiểm tra xem camera đã di chuyển đến mục tiêu chỉ đường lần đầu chưa
    private boolean isCameraRouted = false;

    public MapFragment() {}

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        tvTongSo = view.findViewById(R.id.tvTongSo);
        tvXayMoi = view.findViewById(R.id.tvXayMoi);
        tvSuaChua = view.findViewById(R.id.tvSuaChua);

        btnHoNgheo = view.findViewById(R.id.btnHoNgheo);
        btnCanNgheo = view.findViewById(R.id.btnCanNgheo);
        btnChinhSach = view.findViewById(R.id.btnChinhSach);

        databaseReference = FirebaseDatabase.getInstance().getReference("households");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Reset lại trạng thái camera
        isCameraRouted = false;

        // Bắt đầu lắng nghe dữ liệu từ Firebase
        loadData();
    }

    private void loadData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Kiểm tra an toàn xem Fragment còn tồn tại trong giao diện hay không
                if (!isAdded() || getContext() == null || mMap == null) return;

                mMap.clear(); // Xóa sạch để vẽ lại danh sách mới chống trùng lặp

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
                            if (dt.isXayMoi()) xayMoi++;
                            else suaChua++;

                            if (dt.isHoNgheo()) coHoNgheo = true;
                            if (dt.isHoCanNgheo()) coCanNgheo = true;
                            if (dt.isGiaDinhChinhSach()) coChinhSach = true;
                        }

                        // Vẽ ghim hộ gia đình này lên bản đồ
                        processHouseholdLocation(getContext(), item.getKey(), h);
                    }
                }

                tvTongSo.setText(String.valueOf(tong));
                tvXayMoi.setText(String.valueOf(xayMoi));
                tvSuaChua.setText(String.valueOf(suaChua));

                btnHoNgheo.setVisibility(coHoNgheo ? View.VISIBLE : View.GONE);
                btnCanNgheo.setVisibility(coCanNgheo ? View.VISIBLE : View.GONE);
                btnChinhSach.setVisibility(coChinhSach ? View.VISIBLE : View.GONE);

                // ==========================================================
                // 📌 CHỈ ĐƯỜNG AN TOÀN: Di chuyển camera và tự mở hộp thoại
                // ==========================================================
                checkAndApplyRouting();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải Firebase: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Hàm tách biệt xử lý dịch chuyển camera khi có dữ liệu chỉ đường từ ngoài truyền vào
     */
    private void checkAndApplyRouting() {
        if (getArguments() != null && !isCameraRouted) {
            double selectedLat = getArguments().getDouble("latitude", 0.0);
            double selectedLng = getArguments().getDouble("longitude", 0.0);
            String houseName = getArguments().getString("houseName", "Hộ gia đình chỉ đường");

            if (selectedLat != 0.0 && selectedLng != 0.0) {
                LatLng targetedLocation = new LatLng(selectedLat, selectedLng);

                // 1. Tạo ghim và hứng vào một biến đối tượng Marker
                Marker routingMarker = mMap.addMarker(new MarkerOptions()
                        .position(targetedLocation)
                        .title(houseName)
                        .snippet("Vị trí đang điều hướng"));

                // 2. Kiểm tra nếu tạo thành công thì ép hiển thị InfoWindow lên luôn
                if (routingMarker != null) {
                    routingMarker.showInfoWindow();
                }

                // 3. Di chuyển camera mượt mà đến mục tiêu
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetedLocation, 17.0f));
                isCameraRouted = true; // Đánh dấu đã dịch chuyển camera xong
                return;
            }
        }

        // Nếu mở Bản đồ thông thường hoặc không có toạ độ hợp lệ, thiết lập camera về mặc định (chỉ thực hiện 1 lần đầu)
        if (!isCameraRouted) {
            LatLng longHo = new LatLng(10.2167, 105.9667);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(longHo, 13f));
            isCameraRouted = true;
        }
    }

    /**
     * Truyền hẳn context an toàn vào để xử lý Geocoder không sợ rò rỉ bộ nhớ
     */
    private void processHouseholdLocation(Context context, String keyId, Household household) {
        double vido = 0.0;
        double kinhdo = 0.0;

        if (household.getExtraFields() != null) {
            Map<String, Object> extra = household.getExtraFields();
            if (extra.containsKey("vido") && extra.get("vido") != null) {
                try {
                    vido = Double.parseDouble(extra.get("vido").toString());
                } catch (Exception ignored) {}
            }
            if (extra.containsKey("kinhdo") && extra.get("kinhdo") != null) {
                try {
                    kinhdo = Double.parseDouble(extra.get("kinhdo").toString());
                } catch (Exception ignored) {}
            }
        }

        String nameTitle = "Hộ gia đình";
        if (household.getChuHo() != null && household.getChuHo().getHoTen() != null) {
            nameTitle = household.getChuHo().getHoTen();
        }

        // Trường hợp 1: Có sẵn toạ độ từ trước
        if (vido != 0.0 && kinhdo != 0.0) {
            LatLng location = new LatLng(vido, kinhdo);
            if (mMap != null) {
                mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(nameTitle)
                        .snippet(household.getHouseholdId()));
            }
        }
        // Trường hợp 2: Thiếu toạ độ, phân tích từ Địa chỉ chuỗi chuỗi chuỗi bằng Geocoder
        else if (household.getDiaChi() != null) {
            DiaChi dc = household.getDiaChi();
            String fullAddress = "Ấp " + dc.getAp() + ", Xã " + dc.getXa() + ", Tỉnh " + dc.getTinh();

            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(fullAddress, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address addressResult = addresses.get(0);
                    double lat = addressResult.getLatitude();
                    double lng = addressResult.getLongitude();

                    LatLng calculatedLocation = new LatLng(lat, lng);
                    if (mMap != null) {
                        mMap.addMarker(new MarkerOptions()
                                .position(calculatedLocation)
                                .title(nameTitle)
                                .snippet(household.getHouseholdId()));
                    }

                    // Cập nhật tọa độ tìm được ngược lại Firebase để lần sau không tốn tài nguyên tìm lại
                    Map<String, Object> updateLocationMap = new HashMap<>();
                    updateLocationMap.put("extraFields/vido", lat);
                    updateLocationMap.put("extraFields/kinhdo", lng);

                    databaseReference.child(keyId).updateChildren(updateLocationMap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
package com.example.bdsdcna.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bdsdcna.R;
import com.example.bdsdcna.adapters.FullScreenImageAdapter;
import com.example.bdsdcna.adapters.ImageSliderAdapter;
import com.example.bdsdcna.models.DoiTuong;
import com.example.bdsdcna.models.Household;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private TextView tvTongSo, tvXayMoi, tvSuaChua;
    private Spinner spinnerFilter;
    private DatabaseReference databaseReference;
    private FusedLocationProviderClient fusedLocationClient;

    private boolean isCameraRouted = false;
    private HashMap<String, Household> householdMap = new HashMap<>();
    private String routedHouseholdId = "";

    // MỚI: Biến lưu ID hộ đang được chọn để giữ màu xanh dương bất tử kể cả khi Firebase clear() lại bản đồ
    private String permanentlySelectedId = "";

    private Marker lastSelectedMarker = null;

    private String currentFilterKey = "all";
    private final Map<String, String> filterMap = new HashMap<>();
    private List<String> filterOptions = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    public MapFragment() {
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        tvTongSo = view.findViewById(R.id.tvTongSo);
        tvXayMoi = view.findViewById(R.id.tvXayMoi);
        tvSuaChua = view.findViewById(R.id.tvSuaChua);
        spinnerFilter = view.findViewById(R.id.spinnerFilter);

        databaseReference = FirebaseDatabase.getInstance().getReference("households");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        extractRoutedId();
        initFilterData();
        return view;
    }

    private void extractRoutedId() {
        if (getArguments() != null && getArguments().containsKey("householdId")) {
            routedHouseholdId = getArguments().getString("householdId", "");
        } else if (getActivity() != null && getActivity().getIntent() != null && getActivity().getIntent().hasExtra("householdId")) {
            routedHouseholdId = getActivity().getIntent().getStringExtra("householdId");
        }

        if (routedHouseholdId != null && !routedHouseholdId.isEmpty()) {
            permanentlySelectedId = routedHouseholdId;
        }
    }

    private void initFilterData() {
        filterOptions.clear();
        filterMap.clear();
        filterOptions.add("Tất cả đối tượng");
        filterMap.put("all", "Tất cả đối tượng");
        filterMap.put("hoNgheo", "Hộ nghèo");
        filterMap.put("hoCanNgheo", "Hộ cận nghèo");
        filterMap.put("hoBaoTroXaHoi", "Bảo trợ xã hội");
        filterMap.put("giaDinhChinhSach", "Gia đình chính sách");
        filterMap.put("hoDanToc", "Hộ dân tộc");
        filterMap.put("hoKhoKhan", "Hộ khó khăn");
        filterMap.put("hoKhongKhaNangLaoDong", "Không lao động");
        filterMap.put("nguoiCoCong", "Người có công");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, filterOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerFilter != null) {
            spinnerFilter.setAdapter(spinnerAdapter);
            spinnerFilter.setGravity(android.view.Gravity.CENTER);
            spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedName = filterOptions.get(position);
                    for (Map.Entry<String, String> entry : filterMap.entrySet()) {
                        if (entry.getValue().equals(selectedName)) {
                            currentFilterKey = entry.getKey();
                            break;
                        }
                    }
                    if (mMap != null) {
                        loadData();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Bật nút và chấm tròn vị trí xanh dương của Google
        enableMyLocationLayer();

        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMarkerClickListener(marker -> {
            if (marker.getSnippet() != null) {
                permanentlySelectedId = marker.getSnippet(); // Lưu lại ID khi click thủ công
            }
            highlightAndCenterMarker(marker);
            return true;
        });

        isCameraRouted = false;
        loadData();
    }

    // ĐÃ TỐI ƯU: Đảm bảo kiểm tra quyền chuẩn xác trước khi hiển thị vị trí thiết bị lên Map
    private void enableMyLocationLayer() {
        if (getContext() == null || mMap == null) return;

        // Kiểm tra xem ứng dụng đã được cấp quyền vị trí chưa
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Nếu ĐÃ CẤP QUYỀN: Bật chấm xanh hiện tại và nút tâm vị trí
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

        } else {
            // Nếu CHƯA CẤP QUYỀN: Tự động bung cửa sổ hệ thống yêu cầu người dùng bấm "Cho phép"
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (getContext() != null && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (mMap != null) {
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);

                        // Nếu cấp quyền xong mà không có yêu cầu điều hướng hộ từ trước, kéo camera về GPS
                        if (!isCameraRouted && (permanentlySelectedId == null || permanentlySelectedId.isEmpty())) {
                            moveCameraToDeviceLocation();
                        }
                    }
                }
            }
        }
    }

    private void highlightAndCenterMarker(Marker marker) {
        if (lastSelectedMarker != null) {
            lastSelectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            lastSelectedMarker.setZIndex(1.0f);
        }

        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        marker.setZIndex(10.0f);
        lastSelectedMarker = marker;

        centerMapOnMarkerWithOffset(marker.getPosition());

        String hId = marker.getSnippet();
        if (hId != null && !hId.isEmpty()) {
            showBottomSheetDetail(hId);
        }
    }

    private void centerMapOnMarkerWithOffset(LatLng markerLatLng) {
        if (mMap == null || getContext() == null) return;

        float targetZoom = 18.5f;
        int height = getResources().getDisplayMetrics().heightPixels;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, targetZoom));

        Projection projection = mMap.getProjection();
        android.graphics.Point markerPoint = projection.toScreenLocation(markerLatLng);

        int targetY = height / 4;
        int dy = markerPoint.y - targetY;

        android.graphics.Point newCenterPoint = new android.graphics.Point(markerPoint.x, markerPoint.y + dy);
        LatLng newCenterLatLng = projection.fromScreenLocation(newCenterPoint);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newCenterLatLng, targetZoom));
    }

    private void loadData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null || mMap == null) return;

                mMap.clear();

                // ĐÃ SỬA: Đảm bảo sau khi mMap.clear(), lớp MyLocation vẫn được giữ nguyên không bị tắt
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }

                householdMap.clear();
                lastSelectedMarker = null;

                int tong = 0, xayMoi = 0, suaChua = 0;
                boolean coNgheo = false, coCanNgheo = false, coBaoTro = false, coChinhSach = false;
                boolean coDanToc = false, coKhoKhan = false, coKhongLaoDong = false, coCong = false;

                List<Marker> markerList = new ArrayList<>();

                for (DataSnapshot item : snapshot.getChildren()) {
                    Household h = item.getValue(Household.class);
                    if (h != null) {
                        String keyId = item.getKey();
                        h.setHouseholdId(keyId);

                        DoiTuong dt = h.getDoiTuong();
                        if (dt != null) {
                            if (dt.isHoNgheo()) coNgheo = true;
                            if (dt.isHoCanNgheo()) coCanNgheo = true;
                            if (dt.isHoBaoTroXaHoi()) coBaoTro = true;
                            if (dt.isGiaDinhChinhSach()) coChinhSach = true;
                            if (dt.isHoDanToc()) coDanToc = true;
                            if (dt.isHoKhoKhan()) coKhoKhan = true;
                            if (dt.isHoKhongKhaNangLaoDong()) coKhongLaoDong = true;
                            if (dt.isNguoiCoCong()) coCong = true;
                        }

                        if (!currentFilterKey.equals("all")) {
                            boolean isMatch = false;
                            if (dt != null) {
                                if (currentFilterKey.equals("hoNgheo") && dt.isHoNgheo()) isMatch = true;
                                else if (currentFilterKey.equals("hoCanNgheo") && dt.isHoCanNgheo()) isMatch = true;
                                else if (currentFilterKey.equals("hoBaoTroXaHoi") && dt.isHoBaoTroXaHoi()) isMatch = true;
                                else if (currentFilterKey.equals("giaDinhChinhSach") && dt.isGiaDinhChinhSach()) isMatch = true;
                                else if (currentFilterKey.equals("hoDanToc") && dt.isHoDanToc()) isMatch = true;
                                else if (currentFilterKey.equals("hoKhoKhan") && dt.isHoKhoKhan()) isMatch = true;
                                else if (currentFilterKey.equals("hoKhongKhaNangLaoDong") && dt.isHoKhongKhaNangLaoDong()) isMatch = true;
                                else if (currentFilterKey.equals("nguoiCoCong") && dt.isNguoiCoCong()) isMatch = true;
                            }
                            if (!isMatch) continue;
                        }

                        householdMap.put(keyId, h);
                        tong++;

                        if (dt != null) {
                            if (dt.isXayMoi()) xayMoi++;
                            else suaChua++;
                        }

                        Marker m = processHouseholdLocation(getContext(), keyId, h);
                        if (m != null) {
                            markerList.add(m);
                        }
                    }
                }

                tvTongSo.setText(String.valueOf(tong));
                tvXayMoi.setText(String.valueOf(xayMoi));
                tvSuaChua.setText(String.valueOf(suaChua));

                String selectedKeyBefore = currentFilterKey;
                filterOptions.clear();
                filterOptions.add("Tất cả đối tượng");

                if (coNgheo) filterOptions.add(filterMap.get("hoNgheo"));
                if (coCanNgheo) filterOptions.add(filterMap.get("hoCanNgheo"));
                if (coBaoTro) filterOptions.add(filterMap.get("hoBaoTroXaHoi"));
                if (coChinhSach) filterOptions.add(filterMap.get("giaDinhChinhSach"));
                if (coDanToc) filterOptions.add(filterMap.get("hoDanToc"));
                if (coKhoKhan) filterOptions.add(filterMap.get("hoKhoKhan"));
                if (coKhongLaoDong) filterOptions.add(filterMap.get("hoKhongKhaNangLaoDong"));
                if (coCong) filterOptions.add(filterMap.get("nguoiCoCong"));

                spinnerAdapter.notifyDataSetChanged();

                String displayName = filterMap.get(selectedKeyBefore);
                if (displayName != null) {
                    int idx = filterOptions.indexOf(displayName);
                    if (idx >= 0) spinnerFilter.setSelection(idx, false);
                }

                checkAndApplyRouting(markerList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ĐÃ TỐI ƯU: Đảm bảo giữ vững màu xanh dương bất tử của hộ gia đình được chọn
    private void checkAndApplyRouting(List<Marker> markerList) {
        extractRoutedId();

        boolean hasFoundTarget = false;

        if (permanentlySelectedId != null && !permanentlySelectedId.isEmpty()) {
            for (Marker m : markerList) {
                if (m.getSnippet() != null && m.getSnippet().equals(permanentlySelectedId)) {

                    // Nhuộm màu xanh dương chuẩn chỉ cho Marker mục tiêu
                    m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    m.setZIndex(10.0f);
                    lastSelectedMarker = m;

                    // Chỉ dịch chuyển Camera và bật BottomSheet lên ở lần đầu tiên từ màn hình chi tiết nhảy qua
                    if (routedHouseholdId != null && !routedHouseholdId.isEmpty()) {
                        centerMapOnMarkerWithOffset(m.getPosition());
                        showBottomSheetDetail(permanentlySelectedId);
                    }

                    hasFoundTarget = true;
                    isCameraRouted = true;
                    break;
                }
            }
        }

        // Xóa dấu vết Intent một lần sau khi sử dụng để tránh dính vòng lặp vô hạn
        if (routedHouseholdId != null && !routedHouseholdId.isEmpty()) {
            if (getArguments() != null) getArguments().remove("householdId");
            if (getActivity() != null && getActivity().getIntent() != null) {
                getActivity().getIntent().removeExtra("householdId");
            }
            routedHouseholdId = "";
        }

        if (!hasFoundTarget && !isCameraRouted) {
            moveCameraToDeviceLocation();
        }
    }

    private void moveCameraToDeviceLocation() {
        if (getContext() != null && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null && mMap != null) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                } else {
                    LatLng longHo = new LatLng(10.2167, 105.9667);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(longHo, 13f));
                }
            });
        } else {
            LatLng longHo = new LatLng(10.2167, 105.9667);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(longHo, 13f));
        }
        isCameraRouted = true;
    }

    private void showBottomSheetDetail(String hId) {
        Household h = householdMap.get(hId);
        if (h == null || getContext() == null) return;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.layout_bottom_sheet_detail, null);
        bottomSheetDialog.setContentView(sheetView);

        bottomSheetDialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                int peekHeightInPixels = (int) (220 * getResources().getDisplayMetrics().density);
                behavior.setPeekHeight(peekHeightInPixels);
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                behavior.setSkipCollapsed(false);
                behavior.setDraggable(true);
            }
        });

        bottomSheetDialog.setOnDismissListener(dialogInterface -> {
            if (lastSelectedMarker != null) {
                lastSelectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                lastSelectedMarker.setZIndex(1.0f);
                lastSelectedMarker = null;
            }
            permanentlySelectedId = ""; // Giải phóng ID khi người dùng vuốt đóng BottomSheet
        });

        TextView txtName = sheetView.findViewById(R.id.txtName);
        TextView txtAddress = sheetView.findViewById(R.id.txtAddress);
        TextView txtBirth = sheetView.findViewById(R.id.txtBirth);
        TextView txtCCCD = sheetView.findViewById(R.id.txtCCCD);
        TextView txtObject = sheetView.findViewById(R.id.txtObject);
        TextView txtMember = sheetView.findViewById(R.id.txtMember);
        TextView txtCost = sheetView.findViewById(R.id.txtCost);
        TextView txtSupport = sheetView.findViewById(R.id.txtSupport);
        Button btnNavigate = sheetView.findViewById(R.id.btnNavigate);

        if (btnNavigate != null) {
            btnNavigate.setText("Google Maps");
        }

        ViewPager2 viewPagerImages = sheetView.findViewById(R.id.viewPagerImages);
        TextView tvImageCounter = sheetView.findViewById(R.id.tvImageCounter);

        if (h.getChuHo() != null) {
            txtName.setText(h.getChuHo().getHoTen());
            txtBirth.setText("Năm sinh: " + h.getChuHo().getNamSinh());
            txtCCCD.setText("CCCD: " + h.getChuHo().getCccd());
        }
        if (h.getDiaChi() != null) {
            txtAddress.setText("Địa chỉ: Ấp " + h.getDiaChi().getAp() + ", " + h.getDiaChi().getXa() + ", " + h.getDiaChi().getTinh());
        }
        txtMember.setText("Số nhân khẩu: " + (h.getThanhVien() != null ? h.getThanhVien().size() : 0) + " người");

        String doiTuong = "Khác";
        if (h.getDoiTuong() != null) {
            if (h.getDoiTuong().isHoNgheo()) doiTuong = "Hộ nghèo";
            else if (h.getDoiTuong().isHoCanNgheo()) doiTuong = "Hộ cận nghèo";
            else if (h.getDoiTuong().isGiaDinhChinhSach()) doiTuong = "Gia đình chính sách";
        }
        txtObject.setText("Đối tượng: " + doiTuong);

        if (h.getHoTro() != null) {
            txtCost.setText("Kinh phí: " + String.format("%,d", h.getHoTro().getKinhPhiDeXuat()) + " đ");
            txtSupport.setText("Trạng thái: " + (h.getHoTro().getKinhPhiDeXuat() > 0 ? "Đã đề xuất" : "Chưa đề xuất"));
        }

        List<String> mListUrls = new ArrayList<>();
        databaseReference.child(hId).child("images").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String url = ds.getValue(String.class);
                        if (url != null) mListUrls.add(url);
                    }

                    ImageSliderAdapter adapter = new ImageSliderAdapter(getActivity(), mListUrls);
                    viewPagerImages.setAdapter(adapter);
                    tvImageCounter.setText("1/" + mListUrls.size());

                    viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                        @Override
                        public void onPageSelected(int position) {
                            super.onPageSelected(position);
                            tvImageCounter.setText((position + 1) + "/" + mListUrls.size());
                        }
                    });

                    adapter.setOnItemClickListener(position -> openFullScreenDialog(mListUrls, position));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        btnNavigate.setOnClickListener(v -> {
            if (h.getExtraFields() != null) {
                String latStr = h.getExtraFields().get("vido").toString();
                String lngStr = h.getExtraFields().get("kinhdo").toString();
                String uri = String.format(Locale.ENGLISH, "google.navigation:q=%s,%s", latStr, lngStr);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
    }

    private void openFullScreenDialog(List<String> urls, int startPosition) {
        if (getActivity() == null) return;
        final Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_screen_image);

        ViewPager2 viewPagerFullScreen = dialog.findViewById(R.id.viewPagerFullScreen);
        TextView tvCounterFull = dialog.findViewById(R.id.tvCounterFull);
        ImageButton btnCloseFull = dialog.findViewById(R.id.btnCloseFull);

        FullScreenImageAdapter fullScreenAdapter = new FullScreenImageAdapter(getActivity(), urls);
        viewPagerFullScreen.setAdapter(fullScreenAdapter);

        viewPagerFullScreen.setCurrentItem(startPosition, false);
        tvCounterFull.setText((startPosition + 1) + "/" + urls.size());

        viewPagerFullScreen.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tvCounterFull.setText((position + 1) + "/" + urls.size());
            }
        });

        btnCloseFull.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private Marker processHouseholdLocation(Context context, String keyId, Household household) {
        double vido = 0.0, kinhdo = 0.0;
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

        String nameTitle = household.getChuHo() != null ? household.getChuHo().getHoTen() : "Hộ gia đình";

        if (vido != 0.0 && kinhdo != 0.0) {
            LatLng location = new LatLng(vido, kinhdo);
            if (mMap != null) {
                return mMap.addMarker(new MarkerOptions().position(location).title(nameTitle).snippet(keyId));
            }
        }
        return null;
    }
}
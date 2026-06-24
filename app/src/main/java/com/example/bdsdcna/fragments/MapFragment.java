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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
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

    private GoogleMap mMap;
    private TextView tvTongSo, tvXayMoi, tvSuaChua;
    private Button btnHoNgheo, btnCanNgheo, btnChinhSach;
    private DatabaseReference databaseReference;

    private boolean isCameraRouted = false;
    private HashMap<String, Household> householdMap = new HashMap<>();
    private String routedHouseholdId = "";

    private Marker lastSelectedMarker = null;

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

        if (getArguments() != null) {
            routedHouseholdId = getArguments().getString("householdId", "");
        } else if (getActivity() != null && getActivity().getIntent() != null) {
            routedHouseholdId = getActivity().getIntent().getStringExtra("householdId");
        }

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

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMarkerClickListener(marker -> {
            if (lastSelectedMarker != null) {
                lastSelectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                lastSelectedMarker.setZIndex(1.0f);
            }

            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            marker.setZIndex(10.0f);
            lastSelectedMarker = marker;

            // Đẩy nhẹ tâm bản đồ xuống dưới để Marker lộ diện ở nửa trên màn hình
            centerMapOnMarkerWithOffset(marker.getPosition());

            String hId = marker.getSnippet();
            if (hId != null && !hId.isEmpty()) {
                showBottomSheetDetail(hId);
            }
            return true;
        });

        isCameraRouted = false;
        loadData();
    }

    /**
     * Hàm tính toán dịch chuyển vị trí camera map lên trên một chút để Marker lọt vào vùng trống
     * Đồng thời tự động zoom cận cảnh (độ zoom tương đương 18.5f) giúp dễ nhìn hơn.
     */
    /**
     * Hàm tính toán dịch chuyển vị trí camera map lên trên một chút để Marker lọt vào vùng trống
     * Đồng thời tự động zoom cận cảnh giúp dễ nhìn hơn và không bị lệch tâm.
     */
    private void centerMapOnMarkerWithOffset(LatLng markerLatLng) {
        if (mMap == null || getContext() == null) return;

        // Cấu hình mức zoom mong muốn (ví dụ: 18.5f cho cận cảnh)
        float targetZoom = 18.5f;
        int height = getResources().getDisplayMetrics().heightPixels;

        // BƯỚC 1: Đưa camera về vị trí Marker với mức zoom mục tiêu trước (không hiệu ứng)
        // để hệ thống cập nhật đúng ma trận tọa độ Pixel hiện tại.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, targetZoom));

        // BƯỚC 2: Tính toán khoảng cách cần đẩy (Offset Y) trên màn hình thực tế
        Projection projection = mMap.getProjection();
        android.graphics.Point markerPoint = projection.toScreenLocation(markerLatLng);

        // Muốn Marker nằm ở 1/4 màn hình từ trên xuống (nửa trên khoảng trống)
        int targetY = height / 4;
        int dy = markerPoint.y - targetY;

        // BƯỚC 3: Tạo điểm tâm mới cho Bản đồ (đẩy tâm thật xuống dưới để marker trồi lên trên)
        android.graphics.Point newCenterPoint = new android.graphics.Point(markerPoint.x, markerPoint.y + dy);
        LatLng newCenterLatLng = projection.fromScreenLocation(newCenterPoint);

        // BƯỚC 4: Thực hiện hiệu ứng lướt camera mượt mà đến vị trí chuẩn cuối cùng
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newCenterLatLng, targetZoom));
    }

    private void loadData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null || mMap == null) return;

                mMap.clear();
                householdMap.clear();
                lastSelectedMarker = null;

                int tong = 0, xayMoi = 0, suaChua = 0;
                boolean coHoNgheo = false, coCanNgheo = false, coChinhSach = false;
                List<Marker> markerList = new ArrayList<>();

                for (DataSnapshot item : snapshot.getChildren()) {
                    Household h = item.getValue(Household.class);
                    if (h != null) {
                        String keyId = item.getKey();
                        h.setHouseholdId(keyId);
                        householdMap.put(keyId, h);

                        tong++;
                        DoiTuong dt = h.getDoiTuong();
                        if (dt != null) {
                            if (dt.isXayMoi()) xayMoi++; else suaChua++;
                            if (dt.isHoNgheo()) coHoNgheo = true;
                            if (dt.isHoCanNgheo()) coCanNgheo = true;
                            if (dt.isGiaDinhChinhSach()) coChinhSach = true;
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

                btnHoNgheo.setVisibility(coHoNgheo ? View.VISIBLE : View.GONE);
                btnCanNgheo.setVisibility(coCanNgheo ? View.VISIBLE : View.GONE);
                btnChinhSach.setVisibility(coChinhSach ? View.VISIBLE : View.GONE);

                checkAndApplyRouting(markerList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkAndApplyRouting(List<Marker> markerList) {
        // ĐỌC THÔNG TIN TỪ TRANG KHÁC SANG
        if (getArguments() != null) {
            String targetId = getArguments().getString("householdId", "");
            if (targetId != null && !targetId.isEmpty()) {
                routedHouseholdId = targetId;
            }
        }

        // THAY ĐỔI QUAN TRỌNG: Nếu chuyển trang có chỉ định ID hộ gia đình
        if (routedHouseholdId != null && !routedHouseholdId.isEmpty()) {
            for (Marker m : markerList) {
                if (m.getSnippet() != null && m.getSnippet().equals(routedHouseholdId)) {
                    m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    m.setZIndex(10.0f);
                    lastSelectedMarker = m;

                    // SỬA LỖI: Tự động dịch chuyển tâm bản đồ và ZOOM TO lên điểm chọn khi từ trang khác sang
                    centerMapOnMarkerWithOffset(m.getPosition());
                    isCameraRouted = true; // Đánh dấu đã zoom theo luồng điều hướng thành công

                    showBottomSheetDetail(routedHouseholdId);

                    if (getArguments() != null) {
                        getArguments().remove("householdId");
                    }
                    routedHouseholdId = "";
                    break;
                }
            }
        }

        // Trường hợp không có luồng chuyển màn hình chỉ định (Mở bản đồ mặc định lên xem tổng quan)
        if (!isCameraRouted) {
            LatLng longHo = new LatLng(10.2167, 105.9667);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(longHo, 13f));
            isCameraRouted = true;
        }
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
                try { vido = Double.parseDouble(extra.get("vido").toString()); } catch (Exception ignored) {}
            }
            if (extra.containsKey("kinhdo") && extra.get("kinhdo") != null) {
                try { kinhdo = Double.parseDouble(extra.get("kinhdo").toString()); } catch (Exception ignored) {}
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
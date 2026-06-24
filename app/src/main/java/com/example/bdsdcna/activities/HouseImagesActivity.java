package com.example.bdsdcna.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.bdsdcna.R;
import com.example.bdsdcna.adapters.FullScreenImageAdapter; // Sử dụng lại Adapter phóng to
import com.example.bdsdcna.adapters.ImageAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog; // Thư viện Material sẵn có
import com.google.firebase.database.*;

import androidx.viewpager2.widget.ViewPager2;
import android.widget.ImageButton;

import java.util.*;

public class HouseImagesActivity extends AppCompatActivity {

    private RecyclerView rvImages;
    private ImageAdapter imageAdapter;
    private List<String> mListUrls;
    private String householdId;
    private DatabaseReference mDatabase;
    private ValueEventListener mDBListener;

    private Uri cameraImageUri;

    // Launcher xử lý chọn ảnh từ Thư viện
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) uploadImageToCloudinary(imageUri);
                }
            }
    );

    // Launcher xử lý chụp ảnh trực tiếp từ Camera
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (cameraImageUri != null) {
                        uploadImageToCloudinary(cameraImageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_images);

        rvImages = findViewById(R.id.rvImages);
        householdId = getIntent().getStringExtra("householdId");
        mDatabase = FirebaseDatabase.getInstance().getReference();

        rvImages.setLayoutManager(new GridLayoutManager(this, 2));

        rvImages.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int spacing = (int) (6 * parent.getContext().getResources().getDisplayMetrics().density); // ~6dp
                int position = parent.getChildAdapterPosition(view);

                if (position >= 0) {
                    int column = position % 2;

                    if (column == 0) {
                        outRect.left = spacing;
                        outRect.right = spacing / 2;
                    } else {
                        outRect.left = spacing / 2;
                        outRect.right = spacing;
                    }

                    if (position < 2) {
                        outRect.top = spacing;
                    }
                    outRect.bottom = spacing;
                }
            }
        });

        mListUrls = new ArrayList<>();
        imageAdapter = new ImageAdapter(this, mListUrls);
        rvImages.setAdapter(imageAdapter);

        imageAdapter.setOnItemClickListener(position -> {
            if (mListUrls != null && !mListUrls.isEmpty()) {
                openFullScreenDialog(position);
            }
        });

        try {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", "dydtzxe8w");
            config.put("api_key", "316213668843562");
            config.put("api_secret", "gWFhhEODS3OpllsC5pJuJunDPWY");
            MediaManager.init(this, config);
        } catch (Exception ignored) {}

        // SỬ DỤNG THANH CHỌN VUỐT DƯỚI LÊN (BOTTOM SHEET) ĐẸP HƠN
        findViewById(R.id.fabAddImage).setOnClickListener(v -> showBottomSheetImageSource());

        loadImages();
    }

    // --- TẠO GIAO DIỆN THANH CHỌN VUỐT DƯỚI LÊN (BOTTOM SHEET) HOÀN TOÀN BẰNG CODE ---
    private void showBottomSheetImageSource() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        // Tạo Layout tổng container màu trắng
        LinearLayout layoutMain = new LinearLayout(this);
        layoutMain.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        layoutMain.setPadding(padding, padding, padding, padding);
        layoutMain.setBackgroundColor(Color.WHITE);

        // 1. Tiêu đề của thanh chọn
        TextView tvTitle = new TextView(this);
        tvTitle.setText("Thêm hình ảnh hộ gia đình");
        tvTitle.setTextSize(18);
        tvTitle.setTextColor(Color.parseColor("#212121"));
        tvTitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, (int) (16 * getResources().getDisplayMetrics().density));
        tvTitle.setLayoutParams(titleParams);
        layoutMain.addView(tvTitle);

        // Cách định nghĩa style chung cho 2 nút bấm chọn
        int itemHeight = (int) (55 * getResources().getDisplayMetrics().density);

        // 2. Nút "Chụp ảnh mới"
        TextView btnCamera = new TextView(this);
        btnCamera.setText("📷   Chụp ảnh mới trực tiếp");
        btnCamera.setTextSize(16);
        btnCamera.setTextColor(Color.parseColor("#424242"));
        btnCamera.setGravity(Gravity.CENTER_VERTICAL);
        btnCamera.setPadding((int) (12 * getResources().getDisplayMetrics().density), 0, 0, 0);
        btnCamera.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight));

        // Tạo hiệu ứng gợn sóng khi click (Ripple effect) bản xứ của điện thoại
        int[] attrs = new int[]{android.R.attr.selectableItemBackground};
        android.content.res.TypedArray ta = obtainStyledAttributes(attrs);
        btnCamera.setBackground(ta.getDrawable(0));
        btnCamera.setClickable(true);
        btnCamera.setFocusable(true);

        btnCamera.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            openCamera();
        });
        layoutMain.addView(btnCamera);

        // 3. Nút "Chọn từ thư viện"
        TextView btnGallery = new TextView(this);
        btnGallery.setText("🖼️   Chọn ảnh từ Thư viện");
        btnGallery.setTextSize(16);
        btnGallery.setTextColor(Color.parseColor("#424242"));
        btnGallery.setGravity(Gravity.CENTER_VERTICAL);
        btnGallery.setPadding((int) (12 * getResources().getDisplayMetrics().density), 0, 0, 0);
        btnGallery.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight));
        btnGallery.setBackground(ta.getDrawable(0));
        btnGallery.setClickable(true);
        btnGallery.setFocusable(true);
        ta.recycle(); // Giải phóng tài nguyên hệ thống

        btnGallery.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
        layoutMain.addView(btnGallery);

        // Đưa toàn bộ giao diện Code vừa tạo vào BottomSheet và hiển thị lên
        bottomSheetDialog.setContentView(layoutMain);
        bottomSheetDialog.show();
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New House Photo");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");

        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        cameraLauncher.launch(intent);
    }

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

    private void loadImages() {
        if (householdId == null || householdId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin hộ gia đình!", Toast.LENGTH_SHORT).show();
            return;
        }

        mDBListener = mDatabase.child("households").child(householdId).child("images")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mListUrls.clear();

                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String url = dataSnapshot.getValue(String.class);
                                if (url != null) {
                                    mListUrls.add(url);
                                }
                            }
                        }
                        imageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HouseImagesActivity.this, "Lỗi tải ảnh: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        if (householdId == null || householdId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID hộ gia đình!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đang tải ảnh lên Cloudinary...", Toast.LENGTH_SHORT).show();

        MediaManager.get().upload(imageUri)
                .unsigned("ho_gia_dinh")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String downloadUrl = (String) resultData.get("secure_url");
                        if (downloadUrl != null) {
                            saveImageUrlToDatabase(downloadUrl);
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(HouseImagesActivity.this, "Tải ảnh thất bại: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void saveImageUrlToDatabase(String url) {
        String imageId = mDatabase.child("households").child(householdId).child("images").push().getKey();

        if (imageId != null) {
            mDatabase.child("households")
                    .child(householdId)
                    .child("images")
                    .child(imageId)
                    .setValue(url)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(HouseImagesActivity.this, "Đã thêm ảnh thành công!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(HouseImagesActivity.this, "Lưu link ảnh thất bại.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDatabase != null && mDBListener != null && householdId != null) {
            mDatabase.child("households").child(householdId).child("images").removeEventListener(mDBListener);
        }
    }
}
package com.example.bdsdcna.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import androidx.viewpager2.widget.ViewPager2;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.*;

public class HouseImagesActivity extends AppCompatActivity {

    private RecyclerView rvImages;
    private ImageAdapter imageAdapter;
    private List<String> mListUrls;
    private String householdId;
    private DatabaseReference mDatabase;
    private ValueEventListener mDBListener;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) uploadImageToCloudinary(imageUri);
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

        // 1. Cấu hình lưới 2 cột
        rvImages.setLayoutManager(new GridLayoutManager(this, 2));

        // 2. Tạo khoảng cách đều và đẹp mắt giữa các ảnh trong lưới 2 cột
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

        // --- TÍCH HỢP SỰ KIỆN CLICK VÀO ẢNH TRÊN LƯỚI ĐỂ PHÓNG TO ---
        imageAdapter.setOnItemClickListener(position -> {
            if (mListUrls != null && !mListUrls.isEmpty()) {
                openFullScreenDialog(position);
            }
        });

        // 3. Khởi tạo Cloudinary (chỉ thực hiện 1 lần)
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", "dydtzxe8w");
            config.put("api_key", "316213668843562");
            config.put("api_secret", "gWFhhEODS3OpllsC5pJuJunDPWY");
            MediaManager.init(this, config);
        } catch (Exception ignored) {}

        findViewById(R.id.fabAddImage).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        loadImages();
    }

    // --- HÀM TẠO VÀ HIỂN THỊ CỬA SỔ PHÓNG TO ẢNH (BẬT TẠI CHỖ CHUẨN ZALO) ---
    private void openFullScreenDialog(int startPosition) {
        final android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_screen_image); // Tái sử dụng lại layout XML cũ

        ViewPager2 viewPagerFullScreen = dialog.findViewById(R.id.viewPagerFullScreen);
        TextView tvCounterFull = dialog.findViewById(R.id.tvCounterFull);
        ImageButton btnCloseFull = dialog.findViewById(R.id.btnCloseFull);

        // Tái sử dụng FullScreenImageAdapter có chứa PhotoView để bóp tay phóng to thu nhỏ
        FullScreenImageAdapter fullScreenAdapter = new FullScreenImageAdapter(this, mListUrls);
        viewPagerFullScreen.setAdapter(fullScreenAdapter);

        // Di chuyển ViewPager đến vị trí ảnh vừa click trong Grid danh sách
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

    // HÀM ĐỌC DỮ LIỆU TỰ ĐỘNG
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
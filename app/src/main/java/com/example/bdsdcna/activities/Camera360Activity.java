package com.example.bdsdcna.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.bdsdcna.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Camera360Activity extends AppCompatActivity {

    private Toolbar toolbar;
    private WebView webView360;
    private DatabaseReference mDatabase;
    private LinearLayout layoutEmptyState;

    // Hai nút bấm chuyển ảnh qua lại
    private FloatingActionButton fabPrev;
    private FloatingActionButton fabNext;

    private String householdId;
    private List<String> mList360Urls;
    private int currentImageIndex = 0;

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
        setContentView(R.layout.activity_camera360);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Hình ảnh 360° Ngôi Nhà");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        householdId = getIntent().getStringExtra("householdId");
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mList360Urls = new ArrayList<>();

        try {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", "dydtzxe8w");
            config.put("api_key", "316213668843562");
            config.put("api_secret", "gWFhhEODS3OpllsC5pJuJunDPWY");
            MediaManager.init(this, config);
        } catch (Exception ignored) {}

        // Khởi tạo toàn bộ giao diện bằng Code Java (WebView -> Các nút điều hướng -> Nút Thêm)
        initWebViewByCode();
        initNavigationButtonsByCode();
        initUploadFabByCode();

        if (householdId != null && !householdId.isEmpty()) {
            loadAndRender360Image(householdId);
        } else {
            showEmptyState();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebViewByCode() {
        webView360 = new WebView(this);
        webView360.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        webView360.setBackgroundColor(Color.BLACK);
        webView360.setVisibility(View.GONE);

        WebSettings webSettings = webView360.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        webView360.setWebViewClient(new WebViewClient());

        ViewGroup rootView = findViewById(android.R.id.content);
        if (rootView != null && rootView.getChildCount() > 0) {
            ViewGroup mainLinearLayout = (ViewGroup) rootView.getChildAt(0);
            mainLinearLayout.addView(webView360);
        }
    }

    // --- THÊM 2 NÚT QUA TRÁI / QUA PHẢI ĐỂ CHUYỂN ẢNH DỄ DÀNG ---
    private void initNavigationButtonsByCode() {
        ViewGroup viewContent = findViewById(android.R.id.content);
        if (viewContent == null) return;

        int marginSide = (int) (16 * getResources().getDisplayMetrics().density);

        // 1. Nút quay lại ảnh trước (Xếp bên cạnh TRÁI màn hình)
        fabPrev = new FloatingActionButton(this);
        FrameLayout.LayoutParams paramsPrev = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsPrev.gravity = Gravity.CENTER_VERTICAL | Gravity.START; // Cạnh trái giữa màn hình
        paramsPrev.setMargins(marginSide, 0, 0, 0);
        fabPrev.setLayoutParams(paramsPrev);
        fabPrev.setSize(FloatingActionButton.SIZE_MINI); // Thiết lập kích thước nhỏ gọn để tránh che tầm nhìn
        fabPrev.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#80000000"))); // Nền đen mờ xuyên thấu
        fabPrev.setImageResource(android.R.drawable.ic_media_previous); // Mũi tên qua trái
        fabPrev.setColorFilter(Color.WHITE);
        fabPrev.setVisibility(View.GONE); // Mặc định ẩn, chỉ hiện khi có ảnh
        fabPrev.setOnClickListener(v -> navigateImage(-1));
        viewContent.addView(fabPrev);

        // 2. Nút chuyển ảnh tiếp theo (Xếp bên cạnh PHẢI màn hình)
        fabNext = new FloatingActionButton(this);
        FrameLayout.LayoutParams paramsNext = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsNext.gravity = Gravity.CENTER_VERTICAL | Gravity.END; // Cạnh phải giữa màn hình
        paramsNext.setMargins(0, 0, marginSide, 0);
        fabNext.setLayoutParams(paramsNext);
        fabNext.setSize(FloatingActionButton.SIZE_MINI);
        fabNext.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#80000000")));
        fabNext.setImageResource(android.R.drawable.ic_media_next); // Mũi tên qua phải
        fabNext.setColorFilter(Color.WHITE);
        fabNext.setVisibility(View.GONE);
        fabNext.setOnClickListener(v -> navigateImage(1));
        viewContent.addView(fabNext);
    }

    // --- NÚT DẤU CỘNG ĐỂ TẢI ẢNH LÊN ---
    private void initUploadFabByCode() {
        FloatingActionButton fabAdd = new FloatingActionButton(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.END;
        int margin = (int) (24 * getResources().getDisplayMetrics().density);
        params.setMargins(margin, margin, margin, margin);
        fabAdd.setLayoutParams(params);

        fabAdd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2E7D32")));
        fabAdd.setImageResource(android.R.drawable.ic_input_add);
        fabAdd.setColorFilter(Color.WHITE);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        ViewGroup viewContent = findViewById(android.R.id.content);
        if (viewContent != null) {
            viewContent.addView(fabAdd);
        }
    }

    // HÀM XỬ LÝ CHUYỂN ĐỔI CHỈ MỤC INDEX KHI BẤM NÚT MŨI TÊN
    private void navigateImage(int step) {
        int newIndex = currentImageIndex + step;
        if (newIndex >= 0 && newIndex < mList360Urls.size()) {
            currentImageIndex = newIndex;
            renderPannellumWebView(mList360Urls.get(currentImageIndex));
            updateArrowButtonsVisibility();
        }
    }

    // Ẩn hoặc Hiện nút mũi tên tuỳ thuộc vào vị trí ảnh (Ví dụ ảnh đầu thì ẩn nút Quay lại)
    private void updateArrowButtonsVisibility() {
        if (mList360Urls.size() <= 1) {
            fabPrev.setVisibility(View.GONE);
            fabNext.setVisibility(View.GONE);
            return;
        }
        fabPrev.setVisibility(currentImageIndex == 0 ? View.GONE : View.VISIBLE);
        fabNext.setVisibility(currentImageIndex == mList360Urls.size() - 1 ? View.GONE : View.VISIBLE);
    }

    private void loadAndRender360Image(String householdId) {
        mDatabase.child("households").child(householdId).child("images")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mList360Urls.clear();

                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String url = dataSnapshot.getValue(String.class);
                                if (url != null && url.toLowerCase().contains("_pano360_")) {
                                    mList360Urls.add(url);
                                }
                            }
                        }

                        if (!mList360Urls.isEmpty()) {
                            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
                            webView360.setVisibility(View.VISIBLE);

                            if (currentImageIndex >= mList360Urls.size()) {
                                currentImageIndex = 0;
                            }

                            renderPannellumWebView(mList360Urls.get(currentImageIndex));
                            updateArrowButtonsVisibility();
                        } else {
                            showEmptyState();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showEmptyState();
                    }
                });
    }

    private void renderPannellumWebView(String imageUrl) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Ảnh 360° (" + (currentImageIndex + 1) + "/" + mList360Urls.size() + ")");
        }

        // Đã gỡ bỏ toàn bộ Javascript nhận diện vuốt vuốt phức tạp, trả lại WebView xoay 360° nguyên bản mượt mà
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>360 Viewer</title>\n" +
                "    <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/pannellum@2.5.6/build/pannellum.css\"/>\n" +
                "    <script type=\"text/javascript\" src=\"https://cdn.jsdelivr.net/npm/pannellum@2.5.6/build/pannellum.js\"></script>\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; }\n" +
                "        html, body, #panorama { width: 100%; height: 100%; overflow: hidden; background: #000; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div id=\"panorama\"></div>\n" +
                "<script>\n" +
                "    pannellum.viewer('panorama', {\n" +
                "        \"type\": \"equirectangular\",\n" +
                "        \"panorama\": \"" + imageUrl + "\",\n" +
                "        \"autoLoad\": true,\n" +
                "        \"showControls\": false\n" +
                "    });\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>";

        webView360.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null);
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        Toast.makeText(this, "Đang xử lý tải ảnh không gian 360° lên...", Toast.LENGTH_SHORT).show();
        String customPublicId = "house_pano360_" + System.currentTimeMillis();

        MediaManager.get().upload(imageUri)
                .unsigned("ho_gia_dinh")
                .option("public_id", customPublicId)
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
                        Toast.makeText(Camera360Activity.this, "Tải ảnh thất bại: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void saveImageUrlToDatabase(String url) {
        String imageId = mDatabase.child("households").child(householdId).child("images").push().getKey();
        if (imageId != null) {
            mDatabase.child("households").child(householdId).child("images").child(imageId).setValue(url)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Camera360Activity.this, "Đã lưu ảnh 360° thành công!", Toast.LENGTH_SHORT).show();
                            currentImageIndex = mList360Urls.size();
                            loadAndRender360Image(householdId);
                        }
                    });
        }
    }

    private void showEmptyState() {
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
        if (webView360 != null) webView360.setVisibility(View.GONE);
        if (fabPrev != null) fabPrev.setVisibility(View.GONE);
        if (fabNext != null) fabNext.setVisibility(View.GONE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Hình ảnh 360°");
        }
    }

    @Override
    protected void onDestroy() {
        if (webView360 != null) webView360.destroy();
        super.onDestroy();
    }
}
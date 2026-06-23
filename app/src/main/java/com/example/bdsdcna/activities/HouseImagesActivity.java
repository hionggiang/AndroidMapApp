package com.example.bdsdcna.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdsdcna.R;

public class HouseImagesActivity extends AppCompatActivity {

    private RecyclerView rvImages;
    private String householdId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_images);

        rvImages = findViewById(R.id.rvImages);

        rvImages.setLayoutManager(new GridLayoutManager(this, 2));

        householdId = getIntent().getStringExtra("householdId");

        loadImages();
    }

    private void loadImages() {

        // Đọc dữ liệu Firebase tại:
        // households/{householdId}/images
        //
        // Sau khi bạn gửi model Images hoặc cấu trúc node images,
        // sẽ gắn Adapter hiển thị toàn bộ ảnh ở đây.

    }

}
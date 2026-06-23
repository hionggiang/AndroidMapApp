package com.example.bdsdcna.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bdsdcna.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final Context context;
    private final List<String> imageUrls;

    public ImageAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Nạp giao diện item_image.xml vào cho từng ô hiển thị trong danh sách lưới
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String url = imageUrls.get(position);

        // Sử dụng thư viện Glide để tự động tải link ảnh từ Cloudinary đưa vào ImageView
        Glide.with(context)
                .load(url)
                .placeholder(android.R.drawable.ic_menu_gallery) // Ảnh mặc định xuất hiện khi chờ tải
                .error(android.R.drawable.stat_notify_error)      // Ảnh xuất hiện nếu đường link bị lỗi
                .into(holder.imgHouse);
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHouse;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ thành phần ImageView từ tệp giao diện item_image.xml
            imgHouse = itemView.findViewById(R.id.imgHouse);
        }
    }
}
package com.example.bdsdcna.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import java.util.List;

public class FullScreenImageAdapter extends RecyclerView.Adapter<FullScreenImageAdapter.ViewHolder> {

    private final Context context;
    private final List<String> imageUrls;

    public FullScreenImageAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PhotoView photoView = new PhotoView(context);
        photoView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return new ViewHolder(photoView); // Truyền trực tiếp photoView vào
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = imageUrls.get(position);

        if (url != null) {
            Glide.with(context)
                    .load(url)
                    .into(holder.photoView);
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    // --- ĐOẠN SỬA ĐỂ HẾT LỖI NONNULL ---
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public PhotoView photoView;

        // Thay đổi tham số truyền vào thành PhotoView thay vì View thông thường
        public ViewHolder(@NonNull PhotoView itemView) {
            super(itemView);
            this.photoView = itemView; // Không cần ép kiểu (PhotoView) nữa
        }
    }
}
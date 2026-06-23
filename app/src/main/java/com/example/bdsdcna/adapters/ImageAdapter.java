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
    private final List<String> imageList;

    // --- 1. ĐỊNH NGHĨA INTERFACE CLICK ---
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ImageAdapter(Context context, List<String> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Hãy đảm bảo nạp đúng file layout item của bạn (Ví dụ: item_image.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String url = imageList.get(position);

        Glide.with(context)
                .load(url)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.imgHouse); // Thay bằng ID ImageView trong item của bạn

        // --- 2. BẮT SỰ KIỆN CLICK CHO ITEM ---
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList != null ? imageList.size() : 0;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHouse;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            // Thay R.id.imgHouse bằng đúng ID ImageView trong item_image.xml của bạn
            imgHouse = itemView.findViewById(R.id.imgHouse);
        }
    }
}
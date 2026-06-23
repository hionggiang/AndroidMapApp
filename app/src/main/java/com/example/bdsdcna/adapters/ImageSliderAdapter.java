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

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder> {

    private final Context context;
    private final List<String> imageList;

    // --- 1. ĐỊNH NGHĨA INTERFACE CLICK ---
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // --- 2. HÀM ĐỂ SET LISTENER TỪ ACTIVITY TRUYỀN VÀO ---
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ImageSliderAdapter(Context context, List<String> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        String url = imageList.get(position);
        Glide.with(context)
                .load(url)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.imgHouse);

        // --- 3. BẮT SỰ KIỆN CLICK CHO ITEM ---
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

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHouse;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHouse = itemView.findViewById(R.id.imgHouse);
        }
    }
}
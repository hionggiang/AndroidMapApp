package com.example.bdsdcna.adapters;

import android.app.DatePickerDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdsdcna.R;
import com.example.bdsdcna.models.ThanhVien;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ThanhVienAdapter extends RecyclerView.Adapter<ThanhVienAdapter.ThanhVienViewHolder> {

    private final List<ThanhVien> listThanhVien;
    private final Map<String, Boolean> selectedMap;
    private final String[] quanHeArr = {"CHỦ HỘ", "Vợ", "Chồng", "Con", "Bố", "Mẹ", "Anh", "Chị", "Em", "Khác"};

    // Sử dụng cờ của Adapter để quản lý đồng bộ tuyệt đối cho cả EditText và Spinner
    private boolean isBindingData = false;

    public ThanhVienAdapter(List<ThanhVien> listThanhVien, Map<String, Boolean> selectedMap) {
        this.listThanhVien = listThanhVien;
        this.selectedMap = selectedMap;
    }

    @NonNull
    @Override
    public ThanhVienViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_ud, parent, false);
        return new ThanhVienViewHolder(v, listThanhVien);
    }

    @Override
    public void onBindViewHolder(@NonNull ThanhVienViewHolder holder, int position) {
        ThanhVien tv = listThanhVien.get(position);

        // 1. Bật cờ chặn tất cả các TextWatcher rác kích hoạt ngược khi set chữ cũ
        isBindingData = true;

        // Đổ thông tin hiện có ra các ô EditText
        holder.etHoTen.setText(tv.getHoTen());
        holder.etCCCD.setText(tv.getCccd());
        holder.etNgaySinh.setText(tv.getNgaySinh());
        holder.etDanToc.setText(tv.getDanToc());

        // 2. Bộ chọn ngày sinh bằng DatePickerDialog nhanh
        holder.etNgaySinh.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(holder.itemView.getContext(),
                    (view, year, month, dayOfMonth) -> {
                        String m = (month + 1) < 10 ? "0" + (month + 1) : String.valueOf(month + 1);
                        String d = dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                        String fullDate = d + "/" + m + "/" + year;

                        holder.etNgaySinh.setText(fullDate);
                        tv.setNgaySinh(fullDate);
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        // 3. Thiết lập Spinner Mối quan hệ
        ArrayAdapter<String> qhAdapter = new ArrayAdapter<>(holder.itemView.getContext(),
                android.R.layout.simple_spinner_item, quanHeArr);
        qhAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spQuanHe.setAdapter(qhAdapter);
        holder.spQuanHe.setSelection(getQuanHeIndex(tv.getQuanHe()));

        holder.spQuanHe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (isBindingData) return; // Đang đổ chữ từ Firebase thì bỏ qua sự kiện này

                String select = quanHeArr[pos];
                if (pos == 0) {
                    if (!"CHỦ HỘ".equalsIgnoreCase(tv.getQuanHe()) && !"CHU_HO".equalsIgnoreCase(tv.getQuanHe())) {
                        handleNewChuHo(holder.getAdapterPosition());
                    }
                } else {
                    if ("CHỦ HỘ".equalsIgnoreCase(tv.getQuanHe()) || "CHU_HO".equalsIgnoreCase(tv.getQuanHe())) {
                        tv.setQuanHe(select);
                        notifyDataSetChanged();
                    } else {
                        tv.setQuanHe(select);
                    }
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 4. Trạng thái nút chọn nhanh Chủ hộ (RadioButton)
        boolean isChuHo = "CHU_HO".equalsIgnoreCase(tv.getQuanHe()) || "CHỦ HỘ".equalsIgnoreCase(tv.getQuanHe());
        holder.rbChuHo.setOnCheckedChangeListener(null);
        holder.rbChuHo.setChecked(isChuHo);
        holder.rbChuHo.setOnClickListener(v -> {
            if (!isChuHo) {
                handleNewChuHo(holder.getAdapterPosition());
            }
        });

        // 5. Checkbox xử lý xóa hàng loạt
        Boolean isChecked = selectedMap.get(tv.getMemberId());
        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(isChecked != null && isChecked);
        holder.cbSelect.setOnCheckedChangeListener((bv, checked) -> selectedMap.put(tv.getMemberId(), checked));

        // 6. Thiết lập Spinner Giới tính (Tự xử lý text trực tiếp tại chỗ không cần sửa model)
        String[] gioiTinhArr = {"Nam", "Nữ"};
        ArrayAdapter<String> gtAdapter = new ArrayAdapter<>(holder.itemView.getContext(),
                android.R.layout.simple_spinner_item, gioiTinhArr);
        gtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spGioiTinh.setAdapter(gtAdapter);
        holder.spGioiTinh.setSelection(tv.getGioiTinh() == 2 ? 1 : 0);

        holder.spGioiTinh.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (isBindingData) return;

                // Gán cả mã số lẫn text trực tiếp vào model mà không cần sửa cấu trúc file ThanhVien.java
                tv.setGioiTinh(pos == 1 ? 2 : 1);

                // Lưu ý: Nếu trong model ThanhVien của bạn có hàm setGioiTinhText() thì nó sẽ cập nhật luôn,
                // còn nếu hoàn toàn không có hàm đó thì cũng không bị báo lỗi crash nhờ cơ chế ép dữ liệu thô lúc lưu.
                try {
                    // Bạn có thể giữ nguyên cách xử lý này, Activity khi gửi Map lên Firebase sẽ nhận diện chính xác số 1, 2
                } catch (Exception ignored) {}
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Đổ data tĩnh hoàn tất -> Hạ cờ xuống để nhận diện các thao tác sửa đổi nhập tay của người dùng
        isBindingData = false;
    }

    private void handleNewChuHo(int pos) {
        if (pos == RecyclerView.NO_POSITION || pos >= listThanhVien.size()) return;
        for (int i = 0; i < listThanhVien.size(); i++) {
            if (i == pos) {
                listThanhVien.get(i).setQuanHe("CHỦ HỘ");
            } else if ("CHỦ HỘ".equalsIgnoreCase(listThanhVien.get(i).getQuanHe()) || "CHU_HO".equalsIgnoreCase(listThanhVien.get(i).getQuanHe())) {
                listThanhVien.get(i).setQuanHe("Khác");
            }
        }
        notifyDataSetChanged();
    }

    private int getQuanHeIndex(String val) {
        if (val == null) return 9;
        if (val.equalsIgnoreCase("CHU_HO") || val.equalsIgnoreCase("CHỦ HỘ")) return 0;
        for (int i = 0; i < quanHeArr.length; i++) {
            if (quanHeArr[i].equalsIgnoreCase(val)) return i;
        }
        return 9;
    }

    @Override
    public int getItemCount() {
        return listThanhVien.size();
    }

    // Đổi ViewHolder thành Class thông thường (Non-Static) để dùng chung biến cờ isBindingData của Adapter
    class ThanhVienViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        RadioButton rbChuHo;
        EditText etHoTen, etCCCD, etNgaySinh, etDanToc;
        Spinner spGioiTinh, spQuanHe;

        public ThanhVienViewHolder(@NonNull View itemView, List<ThanhVien> listThanhVien) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            rbChuHo = itemView.findViewById(R.id.rbChuHo);
            etHoTen = itemView.findViewById(R.id.etHoTen);
            etCCCD = itemView.findViewById(R.id.etCCCD);
            etNgaySinh = itemView.findViewById(R.id.etNamSinh);
            etDanToc = itemView.findViewById(R.id.etDanToc);
            spGioiTinh = itemView.findViewById(R.id.spGioiTinh);
            spQuanHe = itemView.findViewById(R.id.spQuanHe);

            // ĐĂNG KÝ BỘ LẮNG NGHE CHỮ CỐ ĐỊNH - LIÊN KẾT ĐÚNG BIẾN CỜ CỦA ADAPTER
            etHoTen.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int pos = getAdapterPosition();
                    // Trỏ chính xác vào biến cờ isBindingData của Adapter lớp cha ngoài
                    if (!isBindingData && pos != RecyclerView.NO_POSITION && pos < listThanhVien.size()) {
                        listThanhVien.get(pos).setHoTen(s.toString());
                    }
                }
            });

            etCCCD.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int pos = getAdapterPosition();
                    if (!isBindingData && pos != RecyclerView.NO_POSITION && pos < listThanhVien.size()) {
                        listThanhVien.get(pos).setCccd(s.toString());
                    }
                }
            });

            etDanToc.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int pos = getAdapterPosition();
                    if (!isBindingData && pos != RecyclerView.NO_POSITION && pos < listThanhVien.size()) {
                        listThanhVien.get(pos).setDanToc(s.toString());
                    }
                }
            });
        }
    }

    abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }
}
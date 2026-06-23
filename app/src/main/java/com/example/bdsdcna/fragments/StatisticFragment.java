package com.example.bdsdcna.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bdsdcna.R;
import com.example.bdsdcna.models.Household;
import com.example.bdsdcna.models.DoiTuong;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticFragment extends Fragment {

    private TextView txtTotalHouseholds, txtTotalMembers;
    private PieChart pieChartDoiTuong;
    private BarChart barChartCompareAreas;
    private Spinner spinnerCompareCriteria;
    private TableLayout tableLayoutStats;
    private TableRow currentlySelectedRow = null;
    private String currentlySelectedArea = null;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private DatabaseReference databaseReference;

    private final Map<String, String> columnLabels = new LinkedHashMap<>();
    private final List<Map.Entry<String, List<Household>>> sortedAreaList = new ArrayList<>();
    private final Map<String, Boolean> sortDirections = new HashMap<>();
    private final Map<String, Map<String, Long>> areaCountsMap = new HashMap<>();
    private final Map<String, Long> globalCounts = new HashMap<>();

    // QUY CHUẨN MÀU GỐC CỦA BẠN
    private final Map<String, Integer> colorMapping = new HashMap<>();
    private String currentSortCriteria = "tongHo";
    private final List<String> spinnerKeys = new ArrayList<>();

    public StatisticFragment() {
        columnLabels.put("tongHo", "Tổng Số Hộ");
        columnLabels.put("tongNhanKhau", "Tổng Nhân Khẩu");
        columnLabels.put("hoNgheo", "Hộ Nghèo");
        columnLabels.put("hoCanNgheo", "Hộ Cận Nghèo");
        columnLabels.put("hoKhongKhaNangLaoDong", "Không KN Lao Động");
        columnLabels.put("hoBaoTroXaHoi", "Bảo Trợ XH");
        columnLabels.put("giaDinhChinhSach", "Gia Đình CS");
        columnLabels.put("hoDanToc", "Hộ Dân Tộc");
        columnLabels.put("hoKhoKhan", "Hộ Khó Khăn");
        columnLabels.put("nguoiCoCong", "Người Có Công");
        columnLabels.put("mstb", "Mất Sức LĐ");

        sortDirections.put("diaBan", false);
        sortDirections.put("tongHo", true);
        sortDirections.put("tongNhanKhau", true);
        for (String key : columnLabels.keySet()) sortDirections.put(key, true);

        // Khởi tạo bảng màu quy chuẩn của bạn
        colorMapping.put("tongHo", Color.parseColor("#455A64"));
        colorMapping.put("tongNhanKhau", Color.parseColor("#37474F"));
        colorMapping.put("hoNgheo", Color.parseColor("#E53935"));
        colorMapping.put("hoCanNgheo", Color.parseColor("#FB8C00"));
        colorMapping.put("hoKhongKhaNangLaoDong", Color.parseColor("#FDD835"));
        colorMapping.put("hoBaoTroXaHoi", Color.parseColor("#43A047"));
        colorMapping.put("giaDinhChinhSach", Color.parseColor("#1E88E5"));
        colorMapping.put("hoDanToc", Color.parseColor("#8E24AA"));
        colorMapping.put("hoKhoKhan", Color.parseColor("#6D4C41"));
        colorMapping.put("nguoiCoCong", Color.parseColor("#00ACC1"));
        colorMapping.put("mstb", Color.parseColor("#546E7A"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        txtTotalHouseholds = view.findViewById(R.id.txtTotalHouseholds);
        txtTotalMembers = view.findViewById(R.id.txtTotalMembers);

        pieChartDoiTuong = view.findViewById(R.id.pieChartDoiTuong);
        barChartCompareAreas = view.findViewById(R.id.barChartCompareAreas);
        spinnerCompareCriteria = view.findViewById(R.id.spinnerCompareCriteria);
        tableLayoutStats = view.findViewById(R.id.tableLayoutStats);

        initSpinner();

        databaseReference = FirebaseDatabase.getInstance().getReference("households");
        loadDataFromFirebase();
        return view;
    }

    private void initSpinner() {
        List<String> spinnerLabels = new ArrayList<>();
        spinnerKeys.clear();
        for (Map.Entry<String, String> entry : columnLabels.entrySet()) {
            spinnerKeys.add(entry.getKey());
            spinnerLabels.add(entry.getValue());
        }

        if (getContext() != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinnerLabels);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCompareCriteria.setAdapter(adapter);

            spinnerCompareCriteria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (view instanceof TextView) {
                        ((TextView) view).setTextColor(Color.parseColor("#263238"));
                        ((TextView) view).setTextSize(15f);
                    }
                    updateBarChartComparingAreas(spinnerKeys.get(position));
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private void loadDataFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Household> tempDataList = new ArrayList<>();
                for (DataSnapshot item : snapshot.getChildren()) {
                    Household household = item.getValue(Household.class);
                    if (household != null) tempDataList.add(household);
                }
                processStatistics(tempDataList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void processStatistics(List<Household> data) {
        executorService.execute(() -> {
            int totalHouseholds = data.size();
            long totalMembers = 0;
            for (Household h : data) if (h.getThanhVien() != null) totalMembers += h.getThanhVien().size();

            globalCounts.clear();
            areaCountsMap.clear();
            for (String key : columnLabels.keySet()) globalCounts.put(key, 0L);

            globalCounts.put("tongHo", (long) totalHouseholds);
            globalCounts.put("tongNhanKhau", totalMembers);

            Map<String, List<Household>> geoGroupedData = new LinkedHashMap<>();
            for (Household h : data) {
                String areaName = "Chưa rõ địa bàn";
                if (h.getDiaChi() != null) {
                    String ap = h.getDiaChi().getAp() != null ? h.getDiaChi().getAp() : "";
                    String xa = h.getDiaChi().getXa() != null ? h.getDiaChi().getXa() : "";
                    areaName = ap.isEmpty() && xa.isEmpty() ? "Chưa rõ địa bàn" : ap + " - " + xa;
                }

                if (!geoGroupedData.containsKey(areaName)) {
                    geoGroupedData.put(areaName, new ArrayList<>());
                    Map<String, Long> areaMap = new HashMap<>();
                    for (String key : columnLabels.keySet()) areaMap.put(key, 0L);
                    areaCountsMap.put(areaName, areaMap);
                }
                geoGroupedData.get(areaName).add(h);

                Map<String, Long> areaMap = areaCountsMap.get(areaName);
                if (areaMap != null) {
                    areaMap.put("tongHo", areaMap.get("tongHo") + 1);
                    if (h.getThanhVien() != null) {
                        areaMap.put("tongNhanKhau", areaMap.get("tongNhanKhau") + h.getThanhVien().size());
                    }
                }

                DoiTuong dt = h.getDoiTuong();
                if (dt != null && areaMap != null) {
                    if (dt.isHoNgheo()) { globalCounts.put("hoNgheo", globalCounts.get("hoNgheo") + 1); areaMap.put("hoNgheo", areaMap.get("hoNgheo") + 1); }
                    if (dt.isHoCanNgheo()) { globalCounts.put("hoCanNgheo", globalCounts.get("hoCanNgheo") + 1); areaMap.put("hoCanNgheo", areaMap.get("hoCanNgheo") + 1); }
                    if (dt.isHoKhongKhaNangLaoDong()) { globalCounts.put("hoKhongKhaNangLaoDong", globalCounts.get("hoKhongKhaNangLaoDong") + 1); areaMap.put("hoKhongKhaNangLaoDong", areaMap.get("hoKhongKhaNangLaoDong") + 1); }
                    if (dt.isHoBaoTroXaHoi()) { globalCounts.put("hoBaoTroXaHoi", globalCounts.get("hoBaoTroXaHoi") + 1); areaMap.put("hoBaoTroXaHoi", areaMap.get("hoBaoTroXaHoi") + 1); }
                    if (dt.isGiaDinhChinhSach()) { globalCounts.put("giaDinhChinhSach", globalCounts.get("giaDinhChinhSach") + 1); areaMap.put("giaDinhChinhSach", areaMap.get("giaDinhChinhSach") + 1); }
                    if (dt.isHoDanToc()) { globalCounts.put("hoDanToc", globalCounts.get("hoDanToc") + 1); areaMap.put("hoDanToc", areaMap.get("hoDanToc") + 1); }
                    if (dt.isHoKhoKhan()) { globalCounts.put("hoKhoKhan", globalCounts.get("hoKhoKhan") + 1); areaMap.put("hoKhoKhan", areaMap.get("hoKhoKhan") + 1); }
                    if (dt.isNguoiCoCong()) { globalCounts.put("nguoiCoCong", globalCounts.get("nguoiCoCong") + 1); areaMap.put("nguoiCoCong", areaMap.get("nguoiCoCong") + 1); }
                    if (dt.isMstb()) { globalCounts.put("mstb", globalCounts.get("mstb") + 1); areaMap.put("mstb", areaMap.get("mstb") + 1); }
                }
            }

            synchronized (sortedAreaList) {
                sortedAreaList.clear();
                sortedAreaList.addAll(geoGroupedData.entrySet());
            }

            final long finalTotalMembers = totalMembers;

            mainHandler.post(() -> {
                if (isAdded() && getContext() != null) {
                    txtTotalHouseholds.setText(String.valueOf(totalHouseholds));
                    txtTotalMembers.setText(String.valueOf(finalTotalMembers));

                    if (currentlySelectedArea != null && areaCountsMap.containsKey(currentlySelectedArea)) {
                        setupPieChart(areaCountsMap.get(currentlySelectedArea), currentlySelectedArea);
                    } else {
                        setupPieChart(globalCounts, "Toàn Địa Bàn");
                    }

                    int selectedSpinnerPos = spinnerCompareCriteria.getSelectedItemPosition();
                    if (selectedSpinnerPos >= 0 && selectedSpinnerPos < spinnerKeys.size()) {
                        updateBarChartComparingAreas(spinnerKeys.get(selectedSpinnerPos));
                    }

                    refreshTableRows();
                }
            });
        });
    }

    // BIỂU ĐỒ TRÒN: ÁP DỤNG QUY CHUẨN MÀU CŨ - LỌC BỎ TỔNG NHÂN KHẨU THEO YÊU CẦU
    private void setupPieChart(Map<String, Long> counts, String areaTitle) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (counts != null) {
            for (Map.Entry<String, Long> entry : counts.entrySet()) {
                if (entry.getKey().equals("tongHo") || entry.getKey().equals("tongNhanKhau")) {
                    continue;
                }
                if (entry.getValue() > 0) {
                    String label = columnLabels.get(entry.getKey());
                    entries.add(new PieEntry(entry.getValue().floatValue(), label));
                    colors.add(colorMapping.getOrDefault(entry.getKey(), Color.GRAY));
                }
            }
        }

        if (entries.isEmpty()) {
            pieChartDoiTuong.clear();
            pieChartDoiTuong.setNoDataText("Không có dữ liệu đối tượng tại\n" + areaTitle);
            pieChartDoiTuong.setNoDataTextColor(Color.GRAY);
            pieChartDoiTuong.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) { return ((int) value) + ""; }
        });

        Legend legend = pieChartDoiTuong.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setYOffset(15f);
        legend.setTextSize(11f);
        legend.setTextColor(Color.parseColor("#424242"));

        pieChartDoiTuong.setData(new PieData(dataSet));
        pieChartDoiTuong.getDescription().setEnabled(false);
        pieChartDoiTuong.setHoleColor(Color.WHITE);
        pieChartDoiTuong.setCenterText(areaTitle + "\nCơ cấu đối tượng");
        pieChartDoiTuong.setCenterTextColor(Color.parseColor("#1A237E"));
        pieChartDoiTuong.setCenterTextSize(13f);
        pieChartDoiTuong.setCenterTextTypeface(Typeface.DEFAULT_BOLD);

        pieChartDoiTuong.setDrawEntryLabels(false);
        pieChartDoiTuong.setExtraBottomOffset(28f);
        pieChartDoiTuong.animateY(800);
        pieChartDoiTuong.invalidate();
    }

    // BIỂU ĐỒ CỘT: SỬA LỖI ĐÔNG ĐỊA BÀN BẰNG CÁCH THÊM ZOOM/CUỘN NGANG TỰ ĐỘNG
    private void updateBarChartComparingAreas(String criteriaKey) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        synchronized (sortedAreaList) {
            for (Map.Entry<String, List<Household>> areaEntry : sortedAreaList) {
                long val = 0;
                if (criteriaKey.equals("tongHo")) {
                    val = areaEntry.getValue().size();
                } else if (criteriaKey.equals("tongNhanKhau")) {
                    for (Household h : areaEntry.getValue()) if (h.getThanhVien() != null) val += h.getThanhVien().size();
                } else {
                    val = countDoiTuongInArea(areaEntry.getValue(), criteriaKey);
                }

                entries.add(new BarEntry(index, val));
                labels.add(areaEntry.getKey());
                index++;
            }
        }

        if (entries.isEmpty()) {
            barChartCompareAreas.clear();
            barChartCompareAreas.invalidate();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, columnLabels.get(criteriaKey));
        dataSet.setColor(colorMapping.getOrDefault(criteriaKey, Color.BLUE));
        dataSet.setValueTextSize(11f);
        dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
        dataSet.setValueTextColor(Color.parseColor("#37474F"));
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) { return String.valueOf((int) value); }
        });

        barChartCompareAreas.getDescription().setEnabled(false);
        barChartCompareAreas.getLegend().setEnabled(false);

        XAxis xAxis = barChartCompareAreas.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-15f); // Góc nghiêng nhẹ tinh tế khi cuộn ngang
        xAxis.setTextColor(Color.parseColor("#263238"));
        xAxis.setTextSize(10f);

        barChartCompareAreas.getAxisLeft().setDrawGridLines(true);
        barChartCompareAreas.getAxisLeft().setGridColor(Color.parseColor("#ECEFF1"));
        barChartCompareAreas.getAxisRight().setEnabled(false);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f);

        barChartCompareAreas.setData(barData);
        barChartCompareAreas.setFitBars(true);
        barChartCompareAreas.setExtraBottomOffset(45f);

        // TÍNH NĂNG NÂNG CẤP: BẬT CUỘN NGANG KHI ĐÔNG ẤP/XÃ
        barChartCompareAreas.setDragEnabled(true);       // Cho phép vuốt để kéo qua lại
        barChartCompareAreas.setScaleYEnabled(false);    // Khóa phóng to chiều dọc
        barChartCompareAreas.setScaleXEnabled(true);     // Cho phép phóng to chiều ngang

        barChartCompareAreas.getViewPortHandler().setMaximumScaleX(20f);
        barChartCompareAreas.fitScreen();

        // Nếu tổng số địa bàn vượt quá 4, biểu đồ tự động giãn cách rộng ra và tạo thanh cuộn ngang
        if (entries.size() > 4) {
            float xValueCountOnScreen = 4f; // Giới hạn chỉ hiển thị đúng 4 cột cùng lúc trên màn hình
            float scaleX = (float) entries.size() / xValueCountOnScreen;
            barChartCompareAreas.zoom(scaleX, 1f, 0, 0);
        }

        barChartCompareAreas.animateY(700);
        barChartCompareAreas.invalidate();
    }

    private void refreshTableRows() {
        tableLayoutStats.removeAllViews();
        if (getContext() == null) return;
        float density = getResources().getDisplayMetrics().density;
        int areaColWidth = (int) (220 * density);
        int colWidth = (int) (160 * density);

        TableRow headerRow = new TableRow(getContext());
        headerRow.setBackground(createRoundedShape(Color.parseColor("#F5F7FA"), 8f));
        headerRow.setPadding(8, 16, 8, 16);
        TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(-1, -2);
        rowParams.setMargins(0, 4, 0, 4);

        TextView thDiaBan = new TextView(getContext());
        thDiaBan.setText(createStyledHeader("Địa bàn (Ấp - Xã)", Boolean.TRUE.equals(sortDirections.get("diaBan")), currentSortCriteria.equals("diaBan")));
        thDiaBan.setTypeface(null, Typeface.BOLD);
        thDiaBan.setLayoutParams(new TableRow.LayoutParams(areaColWidth, -2));
        thDiaBan.setOnClickListener(v -> sortData("diaBan"));
        headerRow.addView(thDiaBan);

        TextView thTongHo = new TextView(getContext());
        thTongHo.setText(createStyledHeader("Tổng Hộ", Boolean.TRUE.equals(sortDirections.get("tongHo")), currentSortCriteria.equals("tongHo")));
        thTongHo.setTypeface(null, Typeface.BOLD);
        thTongHo.setGravity(Gravity.CENTER);
        thTongHo.setLayoutParams(new TableRow.LayoutParams(colWidth, -2));
        thTongHo.setOnClickListener(v -> sortData("tongHo"));
        headerRow.addView(thTongHo);

        TextView thTongNhanKhau = new TextView(getContext());
        thTongNhanKhau.setText(createStyledHeader("Tổng Nhân Khẩu", Boolean.TRUE.equals(sortDirections.get("tongNhanKhau")), currentSortCriteria.equals("tongNhanKhau")));
        thTongNhanKhau.setTypeface(null, Typeface.BOLD);
        thTongNhanKhau.setGravity(Gravity.CENTER);
        thTongNhanKhau.setLayoutParams(new TableRow.LayoutParams(colWidth, -2));
        thTongNhanKhau.setOnClickListener(v -> sortData("tongNhanKhau"));
        headerRow.addView(thTongNhanKhau);

        for (Map.Entry<String, String> entry : columnLabels.entrySet()) {
            if(entry.getKey().equals("tongHo") || entry.getKey().equals("tongNhanKhau")) continue;
            TextView thDoiTuong = new TextView(getContext());
            thDoiTuong.setText(createStyledHeader(entry.getValue(), Boolean.TRUE.equals(sortDirections.get(entry.getKey())), currentSortCriteria.equals(entry.getKey())));
            thDoiTuong.setTypeface(null, Typeface.BOLD);
            thDoiTuong.setGravity(Gravity.CENTER);
            thDoiTuong.setLayoutParams(new TableRow.LayoutParams(colWidth, -2));
            thDoiTuong.setOnClickListener(v -> sortData(entry.getKey()));
            headerRow.addView(thDoiTuong);
        }
        tableLayoutStats.addView(headerRow, rowParams);

        int rowIndex = 0;
        currentlySelectedRow = null;

        synchronized (sortedAreaList) {
            for (Map.Entry<String, List<Household>> areaEntry : sortedAreaList) {
                TableRow row = new TableRow(getContext());
                int defaultBg = (rowIndex % 2 == 0) ? Color.parseColor("#FFFFFF") : Color.parseColor("#F8FAFC");

                if (areaEntry.getKey().equals(currentlySelectedArea)) {
                    row.setBackground(createRoundedShape(Color.parseColor("#D2E3FC"), 12f));
                    currentlySelectedRow = row;
                } else {
                    row.setBackground(createRoundedShape(defaultBg, 12f));
                }

                row.setPadding(8, 14, 8, 14);
                row.setClickable(true);

                TextView tvDiaBan = new TextView(getContext());
                tvDiaBan.setText(areaEntry.getKey());
                tvDiaBan.setLayoutParams(new TableRow.LayoutParams(areaColWidth, -2));
                row.addView(tvDiaBan);

                TextView tvTongHo = new TextView(getContext());
                tvTongHo.setText(String.valueOf(areaEntry.getValue().size()));
                tvTongHo.setGravity(Gravity.CENTER);
                tvTongHo.setLayoutParams(new TableRow.LayoutParams(colWidth, -2));
                row.addView(tvTongHo);

                long areaMembersCount = 0;
                for (Household h : areaEntry.getValue()) {
                    if (h.getThanhVien() != null) areaMembersCount += h.getThanhVien().size();
                }
                TextView tvTongNhanKhau = new TextView(getContext());
                tvTongNhanKhau.setText(String.valueOf(areaMembersCount));
                tvTongNhanKhau.setGravity(Gravity.CENTER);
                tvTongNhanKhau.setLayoutParams(new TableRow.LayoutParams(colWidth, -2));
                row.addView(tvTongNhanKhau);

                for (String key : columnLabels.keySet()) {
                    if(key.equals("tongHo") || key.equals("tongNhanKhau")) continue;
                    long count = countDoiTuongInArea(areaEntry.getValue(), key);
                    TextView tvCount = new TextView(getContext());
                    tvCount.setText(String.valueOf(count));
                    tvCount.setGravity(Gravity.CENTER);
                    tvCount.setLayoutParams(new TableRow.LayoutParams(colWidth, -2));
                    row.addView(tvCount);
                }

                row.setOnClickListener(v -> {
                    if (currentlySelectedRow == row) {
                        row.setBackground(createRoundedShape(defaultBg, 12f));
                        currentlySelectedRow = null;
                        currentlySelectedArea = null;
                        setupPieChart(globalCounts, "Toàn Địa Bàn");
                    } else {
                        if (currentlySelectedRow != null) {
                            currentlySelectedRow.setBackground(createRoundedShape((Integer) currentlySelectedRow.getTag(), 12f));
                        }
                        row.setBackground(createRoundedShape(Color.parseColor("#D2E3FC"), 12f));
                        currentlySelectedRow = row;
                        currentlySelectedArea = areaEntry.getKey();
                        setupPieChart(areaCountsMap.get(areaEntry.getKey()), areaEntry.getKey());
                    }
                });
                row.setTag(defaultBg);
                tableLayoutStats.addView(row, rowParams);
                rowIndex++;
            }
        }
    }

    private SpannableString createStyledHeader(String title, boolean asc, boolean active) {
        String fullText = title + (asc ? "  ▲" : "  ▼");
        SpannableString ss = new SpannableString(fullText);
        ss.setSpan(new RelativeSizeSpan(0.7f), title.length(), fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(active ? Color.parseColor("#1E88E5") : Color.parseColor("#BDBDBD")), title.length(), fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    private GradientDrawable createRoundedShape(int color, float radiusDp) {
        float density = getResources().getDisplayMetrics().density;
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(radiusDp * density);
        shape.setColor(color);
        return shape;
    }

    private void sortData(String key) {
        currentSortCriteria = key;
        boolean asc = Boolean.TRUE.equals(sortDirections.get(key));
        sortDirections.put(key, !asc);
        executorService.execute(() -> {
            synchronized (sortedAreaList) {
                Collections.sort(sortedAreaList, (o1, o2) -> {
                    if (key.equals("diaBan")) return asc ? o1.getKey().compareTo(o2.getKey()) : o2.getKey().compareTo(o1.getKey());
                    if (key.equals("tongHo")) return asc ? Integer.compare(o2.getValue().size(), o1.getValue().size()) : Integer.compare(o1.getValue().size(), o2.getValue().size());

                    if (key.equals("tongNhanKhau")) {
                        long count1 = 0; for (Household h : o1.getValue()) if (h.getThanhVien() != null) count1 += h.getThanhVien().size();
                        long count2 = 0; for (Household h : o2.getValue()) if (h.getThanhVien() != null) count2 += h.getThanhVien().size();
                        return asc ? Long.compare(count2, count1) : Long.compare(count1, count2);
                    }

                    long c1 = countDoiTuongInArea(o1.getValue(), key);
                    long c2 = countDoiTuongInArea(o2.getValue(), key);
                    return asc ? Long.compare(c2, c1) : Long.compare(c1, c2);
                });
            }
            mainHandler.post(() -> {
                int keyIndex = spinnerKeys.indexOf(key);
                if (keyIndex != -1) {
                    spinnerCompareCriteria.setSelection(keyIndex);
                }
                refreshTableRows();
            });
        });
    }

    private long countDoiTuongInArea(List<Household> list, String key) {
        long count = 0;
        for (Household h : list) {
            DoiTuong dt = h.getDoiTuong();
            if (dt == null) continue;
            if (key.equals("hoNgheo") && dt.isHoNgheo()) count++;
            else if (key.equals("hoCanNgheo") && dt.isHoCanNgheo()) count++;
            else if (key.equals("hoKhongKhaNangLaoDong") && dt.isHoKhongKhaNangLaoDong()) count++;
            else if (key.equals("hoBaoTroXaHoi") && dt.isHoBaoTroXaHoi()) count++;
            else if (key.equals("giaDinhChinhSach") && dt.isGiaDinhChinhSach()) count++;
            else if (key.equals("hoDanToc") && dt.isHoDanToc()) count++;
            else if (key.equals("hoKhoKhan") && dt.isHoKhoKhan()) count++;
            else if (key.equals("nguoiCoCong") && dt.isNguoiCoCong()) count++;
            else if (key.equals("mstb") && dt.isMstb()) count++;
        }
        return count;
    }

    @Override public void onDestroy() { super.onDestroy(); executorService.shutdown(); }
}
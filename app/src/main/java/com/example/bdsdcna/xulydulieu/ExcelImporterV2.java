package com.example.bdsdcna.xulydulieu;

import android.content.Context;
import android.net.Uri;
import com.example.bdsdcna.models.*;
import org.apache.poi.ss.usermodel.*;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelImporterV2 {

    public static List<Household> importExcel(Context context, Uri fileUri, String loaiHo) throws Exception {
        try (InputStream is = context.getContentResolver().openInputStream(fileUri);
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();
            int indexRowIndex = -1;

            // 1. Định vị nhanh hàng chứa chỉ mục (1), (2), (3)...
            for (int i = 0; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String cell0 = getValue(row.getCell(0));
                if (cell0.contains("(1)") || cell0.equals("1")) {
                    indexRowIndex = i;
                    break;
                }
            }
            if (indexRowIndex == -1) indexRowIndex = 2;

            // 2. Tạo Map Dynamic Index chuẩn xác
            Map<String, Integer> colMap = new HashMap<>();
            Row indexRow = sheet.getRow(indexRowIndex);
            if (indexRow != null) {
                for (int c = 0; c < indexRow.getLastCellNum(); c++) {
                    String cleanIdx = getValue(indexRow.getCell(c)).replaceAll("[() ]", "");
                    if (!cleanIdx.isEmpty()) colMap.put("col_" + cleanIdx, c);
                }
            }

            Map<Integer, Household> householdMap = new LinkedHashMap<>();
            int currentSttHo = -1, fallbackSttHo = 1;

            // 3. Quét tuyến tính dòng dữ liệu
            for (int i = indexRowIndex + 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String sttHoStr = getValue(row.getCell(colMap.getOrDefault("col_2", 1)));
                if (!sttHoStr.isEmpty()) {
                    try {
                        currentSttHo = Integer.parseInt(sttHoStr);
                    } catch (Exception e) {
                        currentSttHo = fallbackSttHo;
                    }
                } else if (currentSttHo == -1) {
                    currentSttHo = fallbackSttHo;
                }

                // Nếu là hộ mới -> Tạo cấu trúc thông tin ban đầu
                if (!householdMap.containsKey(currentSttHo)) {
                    Household h = new Household();
                    h.setStt(currentSttHo);
                    h.setHouseholdId(String.format("HH%04d", currentSttHo));
                    h.setThanhVien(new ArrayList<>());

                    ChuHo chuHo = new ChuHo();
                    chuHo.setHoTen(getValue(row.getCell(colMap.getOrDefault("col_3", 2))));
                    chuHo.setNamSinh(getYear(getValue(row.getCell(colMap.getOrDefault("col_6", 5)))));
                    chuHo.setGioiTinh(parseGender(getValue(row.getCell(colMap.getOrDefault("col_7", 6)))));
                    chuHo.setCccd(getValue(row.getCell(colMap.getOrDefault("col_8", 7))));
                    chuHo.setDanToc(getValue(row.getCell(colMap.getOrDefault("col_9", 8))));
                    h.setChuHo(chuHo);

                    DiaChi diaChi = new DiaChi();
                    diaChi.setTinh(getValue(row.getCell(colMap.getOrDefault("col_10", 9))));
                    diaChi.setXa(getValue(row.getCell(colMap.getOrDefault("col_11", 10))));
                    diaChi.setAp(getValue(row.getCell(colMap.getOrDefault("col_12", 11))));
                    h.setDiaChi(diaChi);

                    DoiTuong doiTuong = new DoiTuong();
                    if (loaiHo != null) {
                        doiTuong.setHoNgheo(loaiHo.equals("Hộ nghèo"));
                        doiTuong.setHoCanNgheo(loaiHo.equals("Hộ cận nghèo"));
                        doiTuong.setHoKhoKhan(loaiHo.equals("Hộ khó khăn"));
                        doiTuong.setGiaDinhChinhSach(loaiHo.equals("Gia đình chính sách"));
                    }
                    doiTuong.setHoDanToc(checkChecked(getValue(row.getCell(colMap.getOrDefault("col_14", 13)))));
                    doiTuong.setHoKhongKhaNangLaoDong(checkChecked(getValue(row.getCell(colMap.getOrDefault("col_15", 14)))));
                    doiTuong.setHoBaoTroXaHoi(checkChecked(getValue(row.getCell(colMap.getOrDefault("col_16", 15)))));
                    doiTuong.setNguoiCoCong(checkChecked(getValue(row.getCell(colMap.getOrDefault("col_17", 16)))));
                    h.setDoiTuong(doiTuong);

                    householdMap.put(currentSttHo, h);
                    fallbackSttHo++;
                }

                // Thêm thành viên
                String memberName = getValue(row.getCell(colMap.getOrDefault("col_4", 3)));
                if (!memberName.isEmpty()) {
                    ThanhVien tv = new ThanhVien();
                    tv.setMemberId(UUID.randomUUID().toString());
                    tv.setHoTen(memberName);
                    tv.setQuanHe(convertRelation(getValue(row.getCell(colMap.getOrDefault("col_5", 4)))));
                    tv.setNgaySinh(getValue(row.getCell(colMap.getOrDefault("col_6", 5))));
                    tv.setGioiTinh(parseGender(getValue(row.getCell(colMap.getOrDefault("col_7", 6)))));
                    tv.setCccd(getValue(row.getCell(colMap.getOrDefault("col_8", 7))));
                    tv.setDanToc(getValue(row.getCell(colMap.getOrDefault("col_9", 8))));

                    try {
                        tv.setSttThanhVien(Integer.parseInt(getValue(row.getCell(colMap.getOrDefault("col_1", 0)))));
                    } catch (Exception ignored) {}

                    Household currentHousehold = householdMap.get(currentSttHo);
                    if (currentHousehold != null) currentHousehold.getThanhVien().add(tv);
                }
            }
            return new ArrayList<>(householdMap.values());
        }
    }

    private static boolean checkChecked(String val) {
        return val.equalsIgnoreCase("x") || val.equals("1") || val.equalsIgnoreCase("true");
    }

    private static int parseGender(String val) {
        val = val.toLowerCase();
        return (val.contains("nam") || val.equals("1")) ? 1 : ((val.contains("nữ") || val.contains("nu") || val.equals("2")) ? 2 : 0);
    }

    private static String convertRelation(String val) {
        val = val.toLowerCase();
        if (val.contains("chủ") || val.equals("1")) return "CHU_HO";
        if (val.contains("vợ") || val.contains("chồng") || val.equals("2")) return "VO_CHONG";
        if (val.contains("con") || val.equals("3")) return "CON";
        if (val.contains("bố") || val.contains("mẹ") || val.equals("4")) return "BO_ME";
        return "KHAC";
    }

    private static int getYear(String date) {
        try {
            date = date.trim();
            if (date.contains("/") || date.contains("-")) {
                String[] parts = date.split("[/\\-]");
                for (String p : parts) if (p.trim().length() == 4) return Integer.parseInt(p.trim());
            }
            return Integer.parseInt(date.substring(0, Math.min(date.length(), 4)));
        } catch (Exception e) { return 0; }
    }

    private static String getValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cell.getDateCellValue());
                }
                double num = cell.getNumericCellValue();
                return num == (long) num ? String.valueOf((long) num) : String.valueOf(num);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }
}
package com.example.bdsdcna.xulydulieu;

import android.content.Context;
import android.net.Uri;

import com.example.bdsdcna.models.ChuHo;
import com.example.bdsdcna.models.DiaChi;
import com.example.bdsdcna.models.HoTro;
import com.example.bdsdcna.models.Household;
import com.example.bdsdcna.models.ThanhVien;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ExcelImporter {

    public static List<Household> importExcel(
            Context context,
            Uri fileUri
    ) throws Exception {

        InputStream is =
                context.getContentResolver()
                        .openInputStream(fileUri);

        Workbook workbook =
                WorkbookFactory.create(is);

        Sheet sheet =
                workbook.getSheetAt(0);

        Map<Integer, Household> householdMap =
                new LinkedHashMap<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);

            if (row == null)
                continue;

            String sttHoStr =
                    getValue(row.getCell(0));

            if (sttHoStr.isEmpty())
                continue;

            int sttHo;

            try {

                sttHo = Integer.parseInt(
                        sttHoStr
                );

            } catch (Exception e) {

                // Bỏ qua dòng tiêu đề
                continue;
            }

            Household household;

            if (!householdMap.containsKey(sttHo)) {

                household =
                        createHousehold(
                                sttHo,
                                row
                        );

                householdMap.put(
                        sttHo,
                        household
                );

            } else {

                household =
                        householdMap.get(
                                sttHo
                        );
            }

            ThanhVien tv =
                    createMember(row);

            household.getThanhVien()
                    .add(tv);
        }

        workbook.close();
        is.close();

        List<Household> households =
                new ArrayList<>(
                        householdMap.values()
                );

        return households;
    }

    private static Household createHousehold(
            int sttHo,
            Row row
    ) {

        Household h =
                new Household();

        h.setHouseholdId(
                String.format(
                        "HH%04d",
                        sttHo
                )
        );

        h.setStt(sttHo);

        // CHỦ HỘ

        ChuHo chuHo =
                new ChuHo();

        chuHo.setHoTen(
                getValue(row.getCell(2))
        );

        chuHo.setNamSinh(
                getYear(
                        getValue(row.getCell(5))
                )
        );

        chuHo.setGioiTinh(
                parseInt(
                        getValue(row.getCell(6))
                )
        );

        chuHo.setCccd(
                getValue(row.getCell(7))
        );

        chuHo.setDanToc(
                getValue(row.getCell(8))
        );

        h.setChuHo(chuHo);

        // ĐỊA CHỈ

        DiaChi diaChi =
                new DiaChi();

        // sửa tên setter theo model của bạn

        try {
            diaChi.setAp(
                    getValue(row.getCell(9))
            );

            diaChi.setXa(
                    getValue(row.getCell(10))
            );

            diaChi.setHuyen(
                    getValue(row.getCell(11))
            );

            diaChi.setTinh(
                    getValue(row.getCell(12))
            );
        } catch (Exception ignored) {}

        h.setDiaChi(diaChi);

        // HỖ TRỢ

        HoTro hoTro =
                new HoTro();

        hoTro.setLoai("XAY_MOI");
        hoTro.setKinhPhiDeXuat(
                60000000
        );

        hoTro.setKinhPhiDaHoTro(
                0
        );

        h.setHoTro(hoTro);

        // THÀNH VIÊN

        h.setThanhVien(
                new ArrayList<>()
        );

        return h;
    }

    private static ThanhVien createMember(
            Row row
    ) {

        ThanhVien tv =
                new ThanhVien();

        tv.setMemberId(
                UUID.randomUUID()
                        .toString()
        );

        tv.setHoTen(
                getValue(row.getCell(3))
        );

        tv.setQuanHe(
                convertRelation(
                        getValue(row.getCell(4))
                )
        );

        tv.setNgaySinh(
                getValue(row.getCell(5))
        );

        tv.setGioiTinh(
                parseInt(
                        getValue(row.getCell(6))
                )
        );

        tv.setCccd(
                getValue(row.getCell(7))
        );

        tv.setDanToc(
                getValue(row.getCell(8))
        );

        return tv;
    }

    private static String convertRelation(
            String value
    ) {

        value =
                value.toLowerCase();

        if (value.contains("chủ"))
            return "CHU_HO";

        if (value.contains("vợ")
                || value.contains("chồng"))
            return "VO_CHONG";

        if (value.contains("con"))
            return "CON";

        if (value.contains("bố")
                || value.contains("mẹ"))
            return "BO_ME";

        return "KHAC";
    }

    private static int parseInt(
            String value
    ) {

        try {

            return Integer.parseInt(
                    value
            );

        } catch (Exception e) {

            return 0;
        }
    }

    private static int getYear(
            String date
    ) {

        try {

            return Integer.parseInt(
                    date.substring(0,4)
            );

        } catch (Exception e) {

            return 0;
        }
    }

    private static String getValue(
            Cell cell
    ) {

        if (cell == null)
            return "";

        switch (
                cell.getCellType()
        ) {

            case STRING:

                return cell
                        .getStringCellValue()
                        .trim();

            case NUMERIC:

                if (DateUtil
                        .isCellDateFormatted(
                                cell
                        )) {

                    return new SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                    ).format(
                            cell.getDateCellValue()
                    );
                }

                return String.valueOf(
                        (long) cell
                                .getNumericCellValue()
                );

            case BOOLEAN:

                return String.valueOf(
                        cell.getBooleanCellValue()
                );

            default:

                return "";
        }
    }
    public static void uploadToFirebase(
            List<Household> households,
            String loaiHo
    ) {

        String node;

        switch (loaiHo) {

            case "Hộ nghèo":
                node = "ho_ngheo";
                break;

            case "Hộ cận nghèo":
                node = "ho_can_ngheo";
                break;

            case "Hộ khó khăn":
                node = "ho_kho_khan";
                break;

            default:
                node = "gia_dinh_chinh_sach";
                break;
        }

        DatabaseReference ref =
                FirebaseDatabase.getInstance()
                        .getReference("households")
                        .child(node);

        for (Household household : households) {

            ref.child(
                    household.getHouseholdId()
            ).setValue(
                    household
            );
        }
    }
}
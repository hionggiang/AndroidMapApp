package com.example.bdsdcna.xulydulieu;

import android.content.Context;
import android.net.Uri;

import com.example.bdsdcna.models.*;

import org.apache.poi.ss.usermodel.*;

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

        List<Household> households =
                new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);

            if (row == null)
                continue;

            String sttStr =
                    getValue(row.getCell(0));

            if (sttStr.isEmpty())
                continue;

            int stt =
                    Integer.parseInt(sttStr);

            Household household =
                    createHousehold(
                            stt,
                            row
                    );

            households.add(
                    household
            );
        }

        workbook.close();
        is.close();

        return households;
    }
    private static Household createHousehold(
            int sttHo,
            Row row
    ) {

        Household h = new Household();

        h.setHouseholdId(
                String.format(
                        "HH%04d",
                        sttHo
                )
        );

        h.setStt(sttHo);

        // =====================
        // CHỦ HỘ
        // =====================

        ChuHo chuHo = new ChuHo();

        chuHo.setHoTen(
                safe(
                        getValue(row.getCell(1))
                )
        );

        chuHo.setNamSinh(
                parseInt(
                        getValue(row.getCell(2))
                )
        );

        chuHo.setGioiTinh(0);
        chuHo.setCccd("");
        chuHo.setDanToc("");
        chuHo.setSoDienThoai("");

        h.setChuHo(chuHo);

        // =====================
        // ĐỊA CHỈ
        // =====================

        DiaChi diaChi = new DiaChi();

        diaChi.setAp("");
        diaChi.setXa("");
        diaChi.setHuyen("");
        diaChi.setTinh("");

        diaChi.setDiaChiDayDu(
                safe(
                        getValue(row.getCell(3))
                )
        );

        h.setDiaChi(diaChi);

        // =====================
        // HOÀN CẢNH
        // =====================

        HoanCanh hoanCanh =
                new HoanCanh();

        hoanCanh.setMoTa(
                safe(
                        getValue(row.getCell(4))
                )
        );

        hoanCanh.setHienTrangNha("");
        hoanCanh.setDienTichNha(null);
        hoanCanh.setCoQSDD(false);
        hoanCanh.setGhiChu("");

        h.setHoanCanh(
                hoanCanh
        );

        // =====================
        // ĐỐI TƯỢNG
        // =====================

        DoiTuong doiTuong =
                new DoiTuong();

        doiTuong.setGiaDinhChinhSach(false);
        doiTuong.setHoNgheo(false);
        doiTuong.setHoCanNgheo(false);
        doiTuong.setHoKhoKhan(false);
        doiTuong.setMstb(false);

        h.setDoiTuong(
                doiTuong
        );

        // =====================
        // HỖ TRỢ
        // =====================

        HoTro hoTro =
                new HoTro();

        hoTro.setLoai(
                "XAY_MOI"
        );

        hoTro.setKinhPhiDeXuat(
                60000000
        );

        hoTro.setKinhPhiDaHoTro(
                0
        );

        h.setHoTro(
                hoTro
        );

        // =====================
        // GPS
        // =====================

        GPS gps =
                new GPS();

        gps.setLatitude(0.0);
        gps.setLongitude(0.0);
        gps.setAccuracy(0.0);
        gps.setNgayCapNhat("");

        h.setGPS(gps);

        // =====================
        // CAMERA360
        // =====================

        Camera360 camera360 =
                new Camera360();

        camera360.setUrl("");
        camera360.setThumbnail("");
        camera360.setCapturedDate("");

        h.setCamera360(
                camera360
        );

        // =====================
        // IMAGES
        // =====================

        Images images =
                new Images();

        images.setBefore(
                new ArrayList<>()
        );

        images.setDuring(
                new ArrayList<>()
        );

        images.setAfter(
                new ArrayList<>()
        );

        h.setImages(images);

        // =====================
        // THÀNH VIÊN
        // =====================

        List<ThanhVien> ds =
                new ArrayList<>();

        ThanhVien tv =
                new ThanhVien();

        tv.setMemberId(
                UUID.randomUUID()
                        .toString()
        );

        tv.setHoTen(
                chuHo.getHoTen()
        );

        tv.setQuanHe(
                "CHU_HO"
        );

        tv.setNgaySinh(
                String.valueOf(
                        chuHo.getNamSinh()
                )
        );

        tv.setGioiTinh(0);
        tv.setCccd("");
        tv.setDanToc("");

        ds.add(tv);

        h.setThanhVien(ds);

        // =====================
        // THỐNG KÊ
        // =====================

        ThongKe thongKe =
                new ThongKe();

        thongKe.setTongNhanKhau(1);
        thongKe.setSoLaoDong(0);
        thongKe.setSoNguoiPhuThuoc(1);

        h.setThongKe(
                thongKe
        );

        // =====================
        // TIẾN ĐỘ
        // =====================

        TienDo tienDo =
                new TienDo();

        tienDo.setTrangThai(
                "CHO_KHAO_SAT"
        );

        tienDo.setPhanTramHoanThanh(0);

        tienDo.setNgayKhoiCong("");
        tienDo.setNgayHoanThanh("");
        tienDo.setGhiChu("");

        h.setTienDo(
                tienDo
        );

        // =====================
        // TÀI TRỢ
        // =====================

        h.setTaiTro(
                new ArrayList<>()
        );

        return h;
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
    private static String safe(
            String value
    ) {
        return value == null
                ? ""
                : value.trim();
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
}
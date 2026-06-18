package com.example.bdsdcna.xulydulieu;

import android.content.Context;
import android.net.Uri;

import com.example.bdsdcna.models.ChuHo;
import com.example.bdsdcna.models.DiaChi;
import com.example.bdsdcna.models.DoiTuong;
import com.example.bdsdcna.models.Household;
import com.example.bdsdcna.models.ThanhVien;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelImporterV2 {

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

        Map<Integer,String> headers =
                detectHeaders(sheet);

        Map<Integer,Household> households =
                new LinkedHashMap<>();

        int currentSttHo = 0;

        for(int r=1;r<=sheet.getLastRowNum();r++){

            Row row = sheet.getRow(r);

            if(row == null)
                continue;

            String sttHoValue =
                    getByName(
                            row,
                            headers,
                            "stt hộ"
                    );

            if(!sttHoValue.isEmpty()){

                try{

                    currentSttHo =
                            Integer.parseInt(
                                    sttHoValue
                            );

                }catch (Exception ignored){}
            }

            if(currentSttHo == 0)
                continue;

            Household household;

            if(!households.containsKey(
                    currentSttHo
            )){

                household =
                        createHousehold(
                                currentSttHo,
                                row,
                                headers
                        );

                households.put(
                        currentSttHo,
                        household
                );

            }else{

                household =
                        households.get(
                                currentSttHo
                        );
            }

            ThanhVien member =
                    createMember(
                            row,
                            headers
                    );

            if(member != null){

                household
                        .getThanhVien()
                        .add(member);
            }
        }

        workbook.close();
        is.close();

        return new ArrayList<>(
                households.values()
        );
    }

    private static Household createHousehold(
            int sttHo,
            Row row,
            Map<Integer,String> headers
    ){

        Household h =
                new Household();

        // Tùy chỉnh setSttHo nếu trong model của bạn đặt tên khác (ví dụ: h.setStt(sttHo))
        h.setStt(sttHo);

        h.setHouseholdId(
                String.format(
                        "HH%05d",
                        sttHo
                )
        );

        // ================= CHỦ HỘ =================
        ChuHo chuHo =
                new ChuHo();

        chuHo.setHoTen(
                getByName(
                        row,
                        headers,
                        "họ và tên chủ hộ"
                )
        );

        chuHo.setCccd(
                getByName(
                        row,
                        headers,
                        "số cccd"
                )
        );

        chuHo.setDanToc(
                getByName(
                        row,
                        headers,
                        "dân tộc"
                )
        );

        chuHo.setGioiTinh(
                parseGender(
                        getByName(
                                row,
                                headers,
                                "giới tính"
                        )
                )
        );

        // Bạn có thể bổ sung chuHo.setNgaySinh(...) nếu model ChuHo của bạn có trường này

        h.setChuHo(chuHo);

        // ================= ĐỊA CHỈ =================
        DiaChi diaChi =
                new DiaChi();

        diaChi.setTinh(
                getByName(
                        row,
                        headers,
                        "tỉnh"
                )
        );

        diaChi.setXa(
                getByName(
                        row,
                        headers,
                        "xã"
                )
        );

        diaChi.setAp(
                getByName(
                        row,
                        headers,
                        "ấp"
                )
        );

        String soNha =
                getByName(
                        row,
                        headers,
                        "số nhà"
                );

        diaChi.setDiaChiDayDu(
                soNha
                        + ", "
                        + diaChi.getAp()
                        + ", "
                        + diaChi.getXa()
                        + ", "
                        + diaChi.getTinh()
        );

        h.setDiaChi(
                diaChi
        );

        // Khởi tạo đối tượng tượng rỗng tránh NullPointerException khi mapStatusField
        if (h.getDoiTuong() == null) {
            h.setDoiTuong(new DoiTuong());
        }

        processExtraFields(
                h,
                row,
                headers
        );

        return h;
    }

    private static ThanhVien createMember(
            Row row,
            Map<Integer,String> headers
    ){

        String name =
                getByName(
                        row,
                        headers,
                        "họ và tên thành viên"
                );

        if(name.isEmpty())
            return null;

        ThanhVien tv =
                new ThanhVien();

        tv.setMemberId(
                UUID.randomUUID()
                        .toString()
        );

        tv.setHoTen(
                name
        );

        tv.setQuanHe(
                parseRelation(
                        getByName(
                                row,
                                headers,
                                "mối quan hệ"
                        )
                )
        );

        tv.setNgaySinh(
                normalizeDate(
                        getByName(
                                row,
                                headers,
                                "ngày, tháng, năm sinh"
                        )
                )
        );

        tv.setGioiTinh(
                parseGender(
                        getByName(
                                row,
                                headers,
                                "giới tính"
                        )
                )
        );

        tv.setCccd(
                getByName(
                        row,
                        headers,
                        "số cccd"
                )
        );

        tv.setDanToc(
                getByName(
                        row,
                        headers,
                        "dân tộc"
                )
        );

        return tv;
    }

    private static void processExtraFields(
            Household h,
            Row row,
            Map<Integer,String> headers
    ){

        Set<String> standard =
                new HashSet<>(
                        Arrays.asList(
                                "stt",
                                "stt hộ",
                                "họ và tên chủ hộ",
                                "họ và tên thành viên",
                                "mối quan hệ",
                                "ngày, tháng, năm sinh",
                                "giới tính",
                                "số cccd",
                                "dân tộc",
                                "tỉnh",
                                "xã",
                                "ấp",
                                "số nhà"
                        )
                );

        for(Integer col : headers.keySet()){

            String header =
                    headers.get(col);

            if(standard.contains(header))
                continue;

            String value =
                    getValue(
                            row.getCell(col)
                    );

            if(value.isEmpty())
                continue;

            String key =
                    toKey(header);

            if(isStatusField(value)){

                mapStatusField(
                        h,
                        key,
                        true
                );

            }else {

                h.getExtraFields()
                        .put(
                                key,
                                value
                        );
            }
        }
    }

    private static void mapStatusField(
            Household h,
            String key,
            boolean value
    ){

        DoiTuong dt =
                h.getDoiTuong();

        try {

            switch (key){

                case "ho_ngheo":

                    dt.setHoNgheo(value);
                    break;

                case "ho_can_ngheo":

                    dt.setHoCanNgheo(value);
                    break;

                case "ho_kho_khan":

                    dt.setHoKhoKhan(value);
                    break;

                case "gia_dinh_chinh_sach":

                    dt.setGiaDinhChinhSach(value);
                    break;

                default:

                    h.getExtraFields()
                            .put(
                                    key,
                                    value
                            );
                    break;
            }

        }catch (Exception e){

            h.getExtraFields()
                    .put(
                            key,
                            value
                    );
        }
    }

    private static boolean isStatusField(
            String value
    ){

        String v =
                value.trim()
                        .toLowerCase();

        return v.equals("x")
                || v.equals("✓")
                || v.equals("1")
                || v.equals("có")
                || v.equals("yes");
    }

    private static String parseRelation(
            String value
    ){

        value =
                value.toLowerCase();

        if(value.equals("1")
                || value.contains("chủ"))
            return "CHU_HO";

        if(value.equals("2")
                || value.contains("vợ")
                || value.contains("chồng"))
            return "VO_CHONG";

        if(value.equals("3")
                || value.contains("con"))
            return "CON";

        if(value.equals("4")
                || value.contains("bố")
                || value.contains("mẹ"))
            return "BO_ME";

        return "KHAC";
    }

    private static int parseGender(
            String value
    ){

        value =
                value.trim()
                        .toLowerCase();

        if(value.equals("nam"))
            return 1;

        if(value.equals("nữ")
                || value.equals("nu"))
            return 2;

        return 0;
    }

    private static String normalizeDate(
            String value
    ){

        if(value.matches("\\d{4}")){

            return "01/01/" + value;
        }

        return value;
    }

    private static String getByName(
            Row row,
            Map<Integer,String> headers,
            String keyword
    ){

        keyword =
                keyword.toLowerCase();

        for(Integer col : headers.keySet()){

            String h =
                    headers.get(col);

            if(h.contains(keyword)){

                return getValue(
                        row.getCell(col)
                );
            }
        }

        return "";
    }

    private static Map<Integer,String>
    detectHeaders(Sheet sheet){

        Map<Integer,String> map =
                new LinkedHashMap<>();

        Row row =
                sheet.getRow(0);

        if(row == null)
            return map;

        for(int c=0;c<row.getLastCellNum();c++){

            String value =
                    getValue(
                            row.getCell(c)
                    );

            map.put(
                    c,
                    normalizeHeader(value)
            );
        }

        return map;
    }

    private static String normalizeHeader(
            String value
    ){

        return value
                .trim()
                .toLowerCase()
                .replace("\n"," ")
                .replace("  "," ");
    }

    private static String toKey(
            String text
    ){

        text =
                text.replaceAll(
                        "[^a-zA-Z0-9À-ỹ ]",
                        ""
                );

        text =
                text.trim()
                        .replace(" ","_");

        return text;
    }

    private static String getValue(
            Cell cell
    ){

        if(cell == null)
            return "";

        switch(cell.getCellType()){

            case STRING:

                return cell
                        .getStringCellValue()
                        .trim();

            case NUMERIC:

                if(DateUtil
                        .isCellDateFormatted(
                                cell
                        )){

                    return new SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                    ).format(
                            cell.getDateCellValue()
                    );
                }

                return String.valueOf(
                        (long)
                                cell.getNumericCellValue()
                );

            case BOOLEAN:

                return String.valueOf(
                        cell.getBooleanCellValue()
                );

            default:

                return "";
        }
    }
}
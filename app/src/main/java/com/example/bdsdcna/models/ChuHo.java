package com.example.bdsdcna.models;

public class ChuHo {

    private String hoTen;

    // Mới: lưu đầy đủ ngày sinh
    private String ngaySinh;

    // Giữ lại để tương thích dữ liệu cũ
    private int namSinh;

    // 1 = Nam, 2 = Nữ
    private int gioiTinh;

    private String cccd;

    private String danToc;

    private String soDienThoai;

    public ChuHo() {
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(String ngaySinh) {
        this.ngaySinh = ngaySinh;

        // tự cập nhật năm sinh
        try {

            if (ngaySinh != null && ngaySinh.length() >= 4) {

                String[] parts = ngaySinh.split("/");

                if (parts.length == 3) {

                    this.namSinh =
                            Integer.parseInt(
                                    parts[2]
                            );
                }
            }

        } catch (Exception ignored) {
        }
    }

    public int getNamSinh() {
        return namSinh;
    }

    public void setNamSinh(int namSinh) {
        this.namSinh = namSinh;
    }

    public int getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(int gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getDanToc() {
        return danToc;
    }

    public void setDanToc(String danToc) {
        this.danToc = danToc;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getGioiTinhText() {

        switch (gioiTinh) {

            case 1:
                return "Nam";

            case 2:
                return "Nữ";

            default:
                return "";
        }
    }

    @Override
    public String toString() {

        return "ChuHo{" +
                "hoTen='" + hoTen + '\'' +
                ", ngaySinh='" + ngaySinh + '\'' +
                ", namSinh=" + namSinh +
                ", gioiTinh=" + gioiTinh +
                ", cccd='" + cccd + '\'' +
                ", danToc='" + danToc + '\'' +
                ", soDienThoai='" + soDienThoai + '\'' +
                '}';
    }
}
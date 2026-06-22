package com.example.bdsdcna.models;

public class ThanhVien {

    private String memberId;

    private String hoTen;

    /*
     * CHU_HO
     * VO_CHONG
     * CON
     * BO_ME
     * KHAC
     */
    private String quanHe;

    private String ngaySinh;

    /*
     * 1 = Nam
     * 2 = Nữ
     */
    private int gioiTinh;

    private String cccd;

    private String danToc;

    private String doiTuong;

    public ThanhVien() {
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getQuanHe() {
        return quanHe;
    }

    public void setQuanHe(String quanHe) {
        this.quanHe = quanHe;
    }

    public String getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(String ngaySinh) {
        this.ngaySinh = ngaySinh;
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

    public String getDoiTuong() {
        return doiTuong;
    }

    public void setDoiTuong(String doiTuong) {
        this.doiTuong = doiTuong;
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

    public boolean isChuHo() {
        return "CHU_HO".equals(quanHe);
    }

    @Override
    public String toString() {

        return "ThanhVien{" +
                "memberId='" + memberId + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", quanHe='" + quanHe + '\'' +
                ", ngaySinh='" + ngaySinh + '\'' +
                ", gioiTinh=" + gioiTinh +
                ", cccd='" + cccd + '\'' +
                ", danToc='" + danToc + '\'' +
                ", doiTuong='" + doiTuong + '\'' +
                '}';
    }

    public void setSttThanhVien(int i) {
    }
}
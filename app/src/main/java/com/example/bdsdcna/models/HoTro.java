package com.example.bdsdcna.models;

public class HoTro {

    /*
     * XAY_MOI
     * SUA_CHUA
     * KHAC
     */
    private String loai;

    /*
     * Kinh phí đề xuất
     */
    private long kinhPhiDeXuat;

    /*
     * Kinh phí đã hỗ trợ
     */
    private long kinhPhiDaHoTro;

    public HoTro() {
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public long getKinhPhiDeXuat() {
        return kinhPhiDeXuat;
    }

    public void setKinhPhiDeXuat(long kinhPhiDeXuat) {
        this.kinhPhiDeXuat = kinhPhiDeXuat;
    }

    public long getKinhPhiDaHoTro() {
        return kinhPhiDaHoTro;
    }

    public void setKinhPhiDaHoTro(long kinhPhiDaHoTro) {
        this.kinhPhiDaHoTro = kinhPhiDaHoTro;
    }

    public long getKinhPhiConThieu() {
        return kinhPhiDeXuat - kinhPhiDaHoTro;
    }

    public boolean daHoTroDu() {
        return kinhPhiDaHoTro >= kinhPhiDeXuat;
    }

    @Override
    public String toString() {
        return "HoTro{" +
                "loai='" + loai + '\'' +
                ", kinhPhiDeXuat=" + kinhPhiDeXuat +
                ", kinhPhiDaHoTro=" + kinhPhiDaHoTro +
                '}';
    }
}
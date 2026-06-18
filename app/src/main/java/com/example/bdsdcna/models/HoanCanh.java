package com.example.bdsdcna.models;

public class HoanCanh {

    private String moTa;

    private String hienTrangNha;

    private Double dienTichNha;

    private boolean coQSDD;

    private String ghiChu;

    public HoanCanh() {
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getHienTrangNha() {
        return hienTrangNha;
    }

    public void setHienTrangNha(String hienTrangNha) {
        this.hienTrangNha = hienTrangNha;
    }

    public Double getDienTichNha() {
        return dienTichNha;
    }

    public void setDienTichNha(Double dienTichNha) {
        this.dienTichNha = dienTichNha;
    }

    public boolean isCoQSDD() {
        return coQSDD;
    }

    public void setCoQSDD(boolean coQSDD) {
        this.coQSDD = coQSDD;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    @Override
    public String toString() {
        return "HoanCanh{" +
                "moTa='" + moTa + '\'' +
                ", hienTrangNha='" + hienTrangNha + '\'' +
                ", dienTichNha=" + dienTichNha +
                ", coQSDD=" + coQSDD +
                ", ghiChu='" + ghiChu + '\'' +
                '}';
    }
}
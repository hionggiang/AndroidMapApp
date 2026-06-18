package com.example.bdsdcna.models;

import java.util.ArrayList;
import java.util.List;

public class Household {

    private String householdId;
    private int stt;

    private ChuHo chuHo;
    private DiaChi diaChi;
    private HoanCanh hoanCanh;
    private DoiTuong doiTuong;
    private HoTro hoTro;

    private List<ThanhVien> thanhVien;

    public Household() {
        thanhVien = new ArrayList<>();
    }

    public String getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(String householdId) {
        this.householdId = householdId;
    }

    public int getStt() {
        return stt;
    }

    public void setStt(int stt) {
        this.stt = stt;
    }

    public ChuHo getChuHo() {
        return chuHo;
    }

    public void setChuHo(ChuHo chuHo) {
        this.chuHo = chuHo;
    }

    public DiaChi getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(DiaChi diaChi) {
        this.diaChi = diaChi;
    }

    public HoanCanh getHoanCanh() {
        return hoanCanh;
    }

    public void setHoanCanh(HoanCanh hoanCanh) {
        this.hoanCanh = hoanCanh;
    }

    public DoiTuong getDoiTuong() {
        return doiTuong;
    }

    public void setDoiTuong(DoiTuong doiTuong) {
        this.doiTuong = doiTuong;
    }

    public HoTro getHoTro() {
        return hoTro;
    }

    public void setHoTro(HoTro hoTro) {
        this.hoTro = hoTro;
    }

    public List<ThanhVien> getThanhVien() {
        return thanhVien;
    }

    public void setThanhVien(List<ThanhVien> thanhVien) {
        this.thanhVien = thanhVien;
    }
}
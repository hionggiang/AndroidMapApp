package com.example.bdsdcna.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Household {

    private String householdId;

    private Integer stt;

    private ChuHo chuHo;

    private DiaChi diaChi;

    private HoanCanh hoanCanh;

    private DoiTuong doiTuong;

    private HoTro hoTro;

    private List<ThanhVien> thanhVien;

    // Các cột phát sinh ngoài biểu mẫu
    private Map<String,Object> extraFields;

    public Household() {

        chuHo = new ChuHo();

        diaChi = new DiaChi();

        hoanCanh = new HoanCanh();

        doiTuong = new DoiTuong();

        hoTro = new HoTro();

        thanhVien = new ArrayList<>();

        extraFields = new HashMap<>();
    }

    public String getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(String householdId) {
        this.householdId = householdId;
    }

    public Integer getStt() {
        return stt;
    }

    public void setStt(Integer stt) {
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

    public Map<String, Object> getExtraFields() {
        return extraFields;
    }

    public void setExtraFields(
            Map<String, Object> extraFields
    ) {
        this.extraFields = extraFields;
    }

    /**
     * Tiện ích thêm field phát sinh
     */
    public void addExtraField(
            String key,
            Object value
    ) {

        if(key == null || key.trim().isEmpty())
            return;

        extraFields.put(
                key,
                value
        );
    }

    /**
     * Tiện ích thêm thành viên
     */
    public void addThanhVien(
            ThanhVien thanhVien
    ) {

        if(thanhVien != null){

            this.thanhVien.add(
                    thanhVien
            );
        }
    }

    @Override
    public String toString() {

        return "Household{" +
                "householdId='" + householdId + '\'' +
                ", stt=" + stt +
                ", chuHo=" + chuHo +
                ", diaChi=" + diaChi +
                ", hoanCanh=" + hoanCanh +
                ", doiTuong=" + doiTuong +
                ", hoTro=" + hoTro +
                ", thanhVien=" + thanhVien +
                ", extraFields=" + extraFields +
                '}';
    }
}
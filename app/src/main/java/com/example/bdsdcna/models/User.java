package com.example.bdsdcna.models;

public class User {

    private String uid;
    private String fullName;
    private String phone;
    private String email;

    private String chucVu;
    private String donViCongTac;

    private String role;
    private String status;

    private String avatarUrl;

    public User() {
        // Firebase cần constructor rỗng
    }

    public User(String uid,
                String fullName,
                String phone,
                String email,
                String chucVu,
                String donViCongTac,
                String role,
                String status,
                String avatarUrl) {

        this.uid = uid;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.chucVu = chucVu;
        this.donViCongTac = donViCongTac;
        this.role = role;
        this.status = status;
        this.avatarUrl = avatarUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public String getDonViCongTac() {
        return donViCongTac;
    }

    public void setDonViCongTac(String donViCongTac) {
        this.donViCongTac = donViCongTac;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
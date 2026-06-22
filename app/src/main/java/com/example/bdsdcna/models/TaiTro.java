package com.example.bdsdcna.models;
public class TaiTro {

    private String donationId;
    private String donorName;
    private long amount;
    private String date;

    public TaiTro() {
    }

    public String getDonorName() {
        return donorName;
    }

    public String getDonationId() {
        return donationId;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
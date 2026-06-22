package com.example.bdsdcna.models;

public class DoiTuong {

    private boolean giaDinhChinhSach;
    private boolean hoNgheo;
    private boolean hoCanNgheo;
    private boolean hoKhoKhan;
    private boolean mstb;

    public DoiTuong() {
    }

    public boolean isHoNgheo() {
        return hoNgheo;
    }

    public boolean isHoCanNgheo() {
        return hoCanNgheo;
    }

    public boolean isGiaDinhChinhSach() {
        return giaDinhChinhSach;
    }

    public boolean isHoKhoKhan() {
        return hoKhoKhan;
    }

    public void setGiaDinhChinhSach(boolean giaDinhChinhSach) {
        this.giaDinhChinhSach = giaDinhChinhSach;
    }

    public void setHoNgheo(boolean hoNgheo) {
        this.hoNgheo = hoNgheo;
    }

    public void setHoCanNgheo(boolean hoCanNgheo) {
        this.hoCanNgheo = hoCanNgheo;
    }

    public void setHoKhoKhan(boolean hoKhoKhan) {
        this.hoKhoKhan = hoKhoKhan;
    }

    public boolean isMstb() {
        return mstb;
    }

    public void setMstb(boolean mstb) {
        this.mstb = mstb;
    }
}
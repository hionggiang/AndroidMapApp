package com.example.bdsdcna.models;

public class DoiTuong {

    /*
     * NHÓM PHÂN LOẠI HỘ
     */

    private boolean giaDinhChinhSach;

    private boolean hoNgheo;

    private boolean hoCanNgheo;

    private boolean hoKhoKhan;

    /*
     * Đối tượng mới theo biểu mẫu điều tra
     */

    private boolean hoDanToc;

    private boolean hoKhongKhaNangLaoDong;

    private boolean hoBaoTroXaHoi;

    private boolean nguoiCoCong;
    private boolean isXayMoi;

    /*
     * Mã số trợ giúp xã hội
     */

    private boolean mstb;

    public DoiTuong() {
    }
    public boolean isXayMoi() {
        return false;
    }

    public boolean isGiaDinhChinhSach() {
        return giaDinhChinhSach;
    }

    public void setGiaDinhChinhSach(
            boolean giaDinhChinhSach
    ) {
        this.giaDinhChinhSach =
                giaDinhChinhSach;
    }

    public boolean isHoNgheo() {
        return hoNgheo;
    }

    public void setHoNgheo(
            boolean hoNgheo
    ) {
        this.hoNgheo = hoNgheo;
    }

    public boolean isHoCanNgheo() {
        return hoCanNgheo;
    }

    public void setHoCanNgheo(
            boolean hoCanNgheo
    ) {
        this.hoCanNgheo = hoCanNgheo;
    }

    public boolean isHoKhoKhan() {
        return hoKhoKhan;
    }

    public void setHoKhoKhan(
            boolean hoKhoKhan
    ) {
        this.hoKhoKhan = hoKhoKhan;
    }

    public boolean isHoDanToc() {
        return hoDanToc;
    }

    public void setHoDanToc(
            boolean hoDanToc
    ) {
        this.hoDanToc = hoDanToc;
    }

    public boolean isHoKhongKhaNangLaoDong() {
        return hoKhongKhaNangLaoDong;
    }

    public void setHoKhongKhaNangLaoDong(
            boolean value
    ) {
        this.hoKhongKhaNangLaoDong =
                value;
    }

    public boolean isHoBaoTroXaHoi() {
        return hoBaoTroXaHoi;
    }

    public void setHoBaoTroXaHoi(
            boolean value
    ) {
        this.hoBaoTroXaHoi =
                value;
    }

    public boolean isNguoiCoCong() {
        return nguoiCoCong;
    }

    public void setNguoiCoCong(
            boolean value
    ) {
        this.nguoiCoCong =
                value;
    }

    public boolean isMstb() {
        return mstb;
    }

    public void setMstb(
            boolean mstb
    ) {
        this.mstb = mstb;
    }

    @Override
    public String toString() {

        return "DoiTuong{" +
                "giaDinhChinhSach=" +
                giaDinhChinhSach +

                ", hoNgheo=" +
                hoNgheo +

                ", hoCanNgheo=" +
                hoCanNgheo +

                ", hoKhoKhan=" +
                hoKhoKhan +

                ", hoDanToc=" +
                hoDanToc +

                ", hoKhongKhaNangLaoDong=" +
                hoKhongKhaNangLaoDong +

                ", hoBaoTroXaHoi=" +
                hoBaoTroXaHoi +

                ", nguoiCoCong=" +
                nguoiCoCong +

                ", mstb=" +
                mstb +

                '}';
    }
}
package com.example.bdsdcna.models;

public class DiaChi {

    private String tinh;

    private String xa;

    private String ap;

    // mới
    private String soNha;

    // giữ lại để tương thích dữ liệu cũ
    private String huyen;

    private String diaChiDayDu;

    public DiaChi() {
    }

    public String getTinh() {
        return tinh;
    }

    public void setTinh(String tinh) {
        this.tinh = tinh;
    }

    public String getXa() {
        return xa;
    }

    public void setXa(String xa) {
        this.xa = xa;
    }

    public String getAp() {
        return ap;
    }

    public void setAp(String ap) {
        this.ap = ap;
    }

    public String getSoNha() {
        return soNha;
    }

    public void setSoNha(String soNha) {
        this.soNha = soNha;
    }

    public String getHuyen() {
        return huyen;
    }

    public void setHuyen(String huyen) {
        this.huyen = huyen;
    }

    public String getDiaChiDayDu() {
        return diaChiDayDu;
    }

    public void setDiaChiDayDu(String diaChiDayDu) {
        this.diaChiDayDu = diaChiDayDu;
    }

    public void buildDiaChiDayDu() {

        StringBuilder sb =
                new StringBuilder();

        if(soNha != null && !soNha.isEmpty())
            sb.append(soNha);

        if(ap != null && !ap.isEmpty()) {

            if(sb.length() > 0)
                sb.append(", ");

            sb.append(ap);
        }

        if(xa != null && !xa.isEmpty()) {

            if(sb.length() > 0)
                sb.append(", ");

            sb.append(xa);
        }

        if(huyen != null && !huyen.isEmpty()) {

            if(sb.length() > 0)
                sb.append(", ");

            sb.append(huyen);
        }

        if(tinh != null && !tinh.isEmpty()) {

            if(sb.length() > 0)
                sb.append(", ");

            sb.append(tinh);
        }

        diaChiDayDu =
                sb.toString();
    }
}
package hbie2.HAFPIS2.Entity;

import java.util.Objects;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/11
 * 最后修改时间:2018/4/11
 */
public class HafpisHplpKey {
    private String latentid;
    private String devname;
    private String enrolldate;

    public String getLatentid() {
        return latentid;
    }

    public void setLatentid(String latentid) {
        this.latentid = latentid;
    }

    public String getDevname() {
        return devname;
    }

    public void setDevname(String devname) {
        this.devname = devname;
    }

    public String getEnrolldate() {
        return enrolldate;
    }

    public void setEnrolldate(String enrolldate) {
        this.enrolldate = enrolldate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HafpisHplpKey that = (HafpisHplpKey) o;
        return Objects.equals(latentid, that.latentid) &&
                Objects.equals(devname, that.devname) &&
                Objects.equals(enrolldate, that.enrolldate);
    }

    @Override
    public int hashCode() {

        return Objects.hash(latentid, devname, enrolldate);
    }
}

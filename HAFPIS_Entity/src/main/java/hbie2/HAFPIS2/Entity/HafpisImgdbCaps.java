package hbie2.HAFPIS2.Entity;

import java.util.Objects;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/11
 * 最后修改时间:2018/4/11
 */
public class HafpisImgdbCaps {
    private String devname;
    private Short caporder;
    private Short devtype;
    private String devpath;
    private Short devsize;
    private Short cursize;

    public String getDevname() {
        return devname;
    }

    public void setDevname(String devname) {
        this.devname = devname;
    }

    public Short getCaporder() {
        return caporder;
    }

    public void setCaporder(Short caporder) {
        this.caporder = caporder;
    }

    public Short getDevtype() {
        return devtype;
    }

    public void setDevtype(Short devtype) {
        this.devtype = devtype;
    }

    public String getDevpath() {
        return devpath;
    }

    public void setDevpath(String devpath) {
        this.devpath = devpath;
    }

    public Short getDevsize() {
        return devsize;
    }

    public void setDevsize(Short devsize) {
        this.devsize = devsize;
    }

    public Short getCursize() {
        return cursize;
    }

    public void setCursize(Short cursize) {
        this.cursize = cursize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HafpisImgdbCaps that = (HafpisImgdbCaps) o;
        return Objects.equals(devname, that.devname) &&
                Objects.equals(caporder, that.caporder) &&
                Objects.equals(devtype, that.devtype) &&
                Objects.equals(devpath, that.devpath) &&
                Objects.equals(devsize, that.devsize) &&
                Objects.equals(cursize, that.cursize);
    }

    @Override
    public int hashCode() {

        return Objects.hash(devname, caporder, devtype, devpath, devsize, cursize);
    }
}

package hbie2.HAFPIS2.Entity;

import java.util.Objects;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/11
 * 最后修改时间:2018/4/11
 */
public class HafpisHtppKey {
    private String personid;
    private String devname;
    private String enrolldate;

    public String getPersonid() {
        return personid;
    }

    public void setPersonid(String personid) {
        this.personid = personid;
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
        HafpisHtppKey that = (HafpisHtppKey) o;
        return Objects.equals(personid, that.personid) &&
                Objects.equals(devname, that.devname) &&
                Objects.equals(enrolldate, that.enrolldate);
    }

    @Override
    public int hashCode() {

        return Objects.hash(personid, devname, enrolldate);
    }
}

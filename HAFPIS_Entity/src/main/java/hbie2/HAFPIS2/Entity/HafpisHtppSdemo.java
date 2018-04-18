package hbie2.HAFPIS2.Entity;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/28
 * 最后修改时间:2018/3/28
 */
public class HafpisHtppSdemo {
    private String personid;
    private Integer dataver;
    private Integer dbid;
    private Integer tpcarddup;
    private String enrolldate;
    private String imgmask;
    private String mainno;
    private String updatedate;

    public String getPersonid() {
        return personid;
    }

    public void setPersonid(String personid) {
        this.personid = personid;
    }

    public Integer getDataver() {
        return dataver;
    }

    public void setDataver(Integer dataver) {
        this.dataver = dataver;
    }

    public Integer getDbid() {
        return dbid;
    }

    public void setDbid(Integer dbid) {
        this.dbid = dbid;
    }

    public Integer getTpcarddup() {
        return tpcarddup;
    }

    public void setTpcarddup(Integer tpcarddup) {
        this.tpcarddup = tpcarddup;
    }

    public String getEnrolldate() {
        return enrolldate;
    }

    public void setEnrolldate(String enrolldate) {
        this.enrolldate = enrolldate;
    }

    public String getImgmask() {
        return imgmask;
    }

    public void setImgmask(String imgmask) {
        this.imgmask = imgmask;
    }

    public String getMainno() {
        return mainno;
    }

    public void setMainno(String mainno) {
        this.mainno = mainno;
    }

    public String getUpdatedate() {
        return updatedate;
    }

    public void setUpdatedate(String updatedate) {
        this.updatedate = updatedate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HafpisHtppSdemo htppSdemo = (HafpisHtppSdemo) o;

        if (personid != null ? !personid.equals(htppSdemo.personid) : htppSdemo.personid != null) return false;
        if (dataver != null ? !dataver.equals(htppSdemo.dataver) : htppSdemo.dataver != null) return false;
        if (dbid != null ? !dbid.equals(htppSdemo.dbid) : htppSdemo.dbid != null) return false;
        if (tpcarddup != null ? !tpcarddup.equals(htppSdemo.tpcarddup) : htppSdemo.tpcarddup != null) return false;
        if (enrolldate != null ? !enrolldate.equals(htppSdemo.enrolldate) : htppSdemo.enrolldate != null) return false;
        if (imgmask != null ? !imgmask.equals(htppSdemo.imgmask) : htppSdemo.imgmask != null) return false;
        if (mainno != null ? !mainno.equals(htppSdemo.mainno) : htppSdemo.mainno != null) return false;
        if (updatedate != null ? !updatedate.equals(htppSdemo.updatedate) : htppSdemo.updatedate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = personid != null ? personid.hashCode() : 0;
        result = 31 * result + (dataver != null ? dataver.hashCode() : 0);
        result = 31 * result + (dbid != null ? dbid.hashCode() : 0);
        result = 31 * result + (tpcarddup != null ? tpcarddup.hashCode() : 0);
        result = 31 * result + (enrolldate != null ? enrolldate.hashCode() : 0);
        result = 31 * result + (imgmask != null ? imgmask.hashCode() : 0);
        result = 31 * result + (mainno != null ? mainno.hashCode() : 0);
        result = 31 * result + (updatedate != null ? updatedate.hashCode() : 0);
        return result;
    }
}

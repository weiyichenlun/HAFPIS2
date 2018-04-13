package hbie2.HAFPIS2.Entity;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/28
 * 最后修改时间:2018/3/28
 */
public class HafpisHlppSdemo {
    private String latentid;
    private String caseid;
    private Integer dataver;
    private Integer dbid;
    private Integer solveattr;
    private String enrolldate;

    public String getLatentid() {
        return latentid;
    }

    public void setLatentid(String latentid) {
        this.latentid = latentid;
    }

    public String getCaseid() {
        return caseid;
    }

    public void setCaseid(String caseid) {
        this.caseid = caseid;
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

    public Integer getSolveattr() {
        return solveattr;
    }

    public void setSolveattr(Integer solveattr) {
        this.solveattr = solveattr;
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

        HafpisHlppSdemo that = (HafpisHlppSdemo) o;

        if (latentid != null ? !latentid.equals(that.latentid) : that.latentid != null) return false;
        if (caseid != null ? !caseid.equals(that.caseid) : that.caseid != null) return false;
        if (dataver != null ? !dataver.equals(that.dataver) : that.dataver != null) return false;
        if (dbid != null ? !dbid.equals(that.dbid) : that.dbid != null) return false;
        if (solveattr != null ? !solveattr.equals(that.solveattr) : that.solveattr != null) return false;
        if (enrolldate != null ? !enrolldate.equals(that.enrolldate) : that.enrolldate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = latentid != null ? latentid.hashCode() : 0;
        result = 31 * result + (caseid != null ? caseid.hashCode() : 0);
        result = 31 * result + (dataver != null ? dataver.hashCode() : 0);
        result = 31 * result + (dbid != null ? dbid.hashCode() : 0);
        result = 31 * result + (solveattr != null ? solveattr.hashCode() : 0);
        result = 31 * result + (enrolldate != null ? enrolldate.hashCode() : 0);
        return result;
    }
}

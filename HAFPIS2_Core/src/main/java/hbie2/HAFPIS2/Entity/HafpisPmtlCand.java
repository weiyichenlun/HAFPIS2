package hbie2.HAFPIS2.Entity;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/28
 * 最后修改时间:2018/3/28
 */
public class HafpisPmtlCand {
    private OtherCompositeKeys keys;
    private String transno;
    private String probeid;
    private Integer dbid;
    private Integer candrank;
    private Integer score;

    public OtherCompositeKeys getKeys() {
        return keys;
    }

    public void setKeys(OtherCompositeKeys keys) {
        this.keys = keys;
    }

    public String getTransno() {
        return transno;
    }

    public void setTransno(String transno) {
        this.transno = transno;
    }

    public String getProbeid() {
        return probeid;
    }

    public void setProbeid(String probeid) {
        this.probeid = probeid;
    }

    public Integer getDbid() {
        return dbid;
    }

    public void setDbid(Integer dbid) {
        this.dbid = dbid;
    }

    public Integer getCandrank() {
        return candrank;
    }

    public void setCandrank(Integer candrank) {
        this.candrank = candrank;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HafpisPmtlCand that = (HafpisPmtlCand) o;

        if (keys != null ? !keys.equals(that.keys) : that.keys != null) return false;
        if (transno != null ? !transno.equals(that.transno) : that.transno != null) return false;
        if (probeid != null ? !probeid.equals(that.probeid) : that.probeid != null) return false;
        if (dbid != null ? !dbid.equals(that.dbid) : that.dbid != null) return false;
        if (candrank != null ? !candrank.equals(that.candrank) : that.candrank != null) return false;
        if (score != null ? !score.equals(that.score) : that.score != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = keys != null ? keys.hashCode() : 0;
        result = 31 * result + (transno != null ? transno.hashCode() : 0);
        result = 31 * result + (probeid != null ? probeid.hashCode() : 0);
        result = 31 * result + (dbid != null ? dbid.hashCode() : 0);
        result = 31 * result + (candrank != null ? candrank.hashCode() : 0);
        result = 31 * result + (score != null ? score.hashCode() : 0);
        return result;
    }
}

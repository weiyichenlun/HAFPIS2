package hbie2.HAFPIS2.Entity;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/28
 * 最后修改时间:2018/3/28
 */
public class HafpisIrisCand {
    private TTCompositeKeys keys;
    private String transno;
    private String probeid;
    private Integer dbid;
    private Integer candrank;
    private Integer score;
    private Integer score01;
    private Integer score02;

    public TTCompositeKeys getKeys() {
        return keys;
    }

    public void setKeys(TTCompositeKeys keys) {
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

    public Integer getScore01() {
        return score01;
    }

    public void setScore01(Integer score01) {
        this.score01 = score01;
    }

    public Integer getScore02() {
        return score02;
    }

    public void setScore02(Integer score02) {
        this.score02 = score02;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HafpisIrisCand that = (HafpisIrisCand) o;

        if (keys != null ? !keys.equals(that.keys) : that.keys != null) return false;
        if (transno != null ? !transno.equals(that.transno) : that.transno != null) return false;
        if (probeid != null ? !probeid.equals(that.probeid) : that.probeid != null) return false;
        if (dbid != null ? !dbid.equals(that.dbid) : that.dbid != null) return false;
        if (candrank != null ? !candrank.equals(that.candrank) : that.candrank != null) return false;
        if (score != null ? !score.equals(that.score) : that.score != null) return false;
        if (score01 != null ? !score01.equals(that.score01) : that.score01 != null) return false;
        if (score02 != null ? !score02.equals(that.score02) : that.score02 != null) return false;

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
        result = 31 * result + (score01 != null ? score01.hashCode() : 0);
        result = 31 * result + (score02 != null ? score02.hashCode() : 0);
        return result;
    }
}

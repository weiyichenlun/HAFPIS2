package hbie2.HAFPIS2.Entity;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/28
 * 最后修改时间:2018/3/28
 */
public class HafpisPmttCand extends AbstractBean<HafpisPmttCand> {
    private TTCompositeKeys keys;
    private String transno;
    private String probeid;
    private Integer dbid;
    private Integer candrank;
    private Integer score;
    private Integer score01;
    private Integer score06;
    private Integer score05;
    private Integer score10;

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

    public Integer getScore06() {
        return score06;
    }

    public void setScore06(Integer score06) {
        this.score06 = score06;
    }

    public Integer getScore05() {
        return score05;
    }

    public void setScore05(Integer score05) {
        this.score05 = score05;
    }

    public Integer getScore10() {
        return score10;
    }

    public void setScore10(Integer score10) {
        this.score10 = score10;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HafpisPmttCand that = (HafpisPmttCand) o;

        if (keys != null ? !keys.equals(that.keys) : that.keys != null) return false;
        if (transno != null ? !transno.equals(that.transno) : that.transno != null) return false;
        if (probeid != null ? !probeid.equals(that.probeid) : that.probeid != null) return false;
        if (dbid != null ? !dbid.equals(that.dbid) : that.dbid != null) return false;
        if (candrank != null ? !candrank.equals(that.candrank) : that.candrank != null) return false;
        if (score != null ? !score.equals(that.score) : that.score != null) return false;
        if (score01 != null ? !score01.equals(that.score01) : that.score01 != null) return false;
        if (score06 != null ? !score06.equals(that.score06) : that.score06 != null) return false;
        if (score05 != null ? !score05.equals(that.score05) : that.score05 != null) return false;
        if (score10 != null ? !score10.equals(that.score10) : that.score10 != null) return false;

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
        result = 31 * result + (score06 != null ? score06.hashCode() : 0);
        result = 31 * result + (score05 != null ? score05.hashCode() : 0);
        result = 31 * result + (score10 != null ? score10.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(HafpisPmttCand o) {
        return this.score > o.score ? -1 : (this.score < o.score ? 1 : 0);
    }
}

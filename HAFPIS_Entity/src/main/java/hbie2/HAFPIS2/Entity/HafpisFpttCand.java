package hbie2.HAFPIS2.Entity;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/28
 * 最后修改时间:2018/3/28
 */
public class HafpisFpttCand extends AbstractBean<HafpisFpttCand>{
    private TTCompositeKeys keys;
    private String transno;
    private String probeid;
    private Integer dbid;
    private Integer candrank;
    private Integer score;
    private Integer score01;
    private Integer score02;
    private Integer score03;
    private Integer score04;
    private Integer score05;
    private Integer score06;
    private Integer score07;
    private Integer score08;
    private Integer score09;
    private Integer score10;
    private Integer score11;
    private Integer score12;
    private Integer score13;
    private Integer score14;
    private Integer score15;
    private Integer score16;
    private Integer score17;
    private Integer score18;
    private Integer score19;
    private Integer score20;

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

    public Integer getScore03() {
        return score03;
    }

    public void setScore03(Integer score03) {
        this.score03 = score03;
    }

    public Integer getScore04() {
        return score04;
    }

    public void setScore04(Integer score04) {
        this.score04 = score04;
    }

    public Integer getScore05() {
        return score05;
    }

    public void setScore05(Integer score05) {
        this.score05 = score05;
    }

    public Integer getScore06() {
        return score06;
    }

    public void setScore06(Integer score06) {
        this.score06 = score06;
    }

    public Integer getScore07() {
        return score07;
    }

    public void setScore07(Integer score07) {
        this.score07 = score07;
    }

    public Integer getScore08() {
        return score08;
    }

    public void setScore08(Integer score08) {
        this.score08 = score08;
    }

    public Integer getScore09() {
        return score09;
    }

    public void setScore09(Integer score09) {
        this.score09 = score09;
    }

    public Integer getScore10() {
        return score10;
    }

    public void setScore10(Integer score10) {
        this.score10 = score10;
    }

    public Integer getScore11() {
        return score11;
    }

    public void setScore11(Integer score11) {
        this.score11 = score11;
    }

    public Integer getScore12() {
        return score12;
    }

    public void setScore12(Integer score12) {
        this.score12 = score12;
    }

    public Integer getScore13() {
        return score13;
    }

    public void setScore13(Integer score13) {
        this.score13 = score13;
    }

    public Integer getScore14() {
        return score14;
    }

    public void setScore14(Integer score14) {
        this.score14 = score14;
    }

    public Integer getScore15() {
        return score15;
    }

    public void setScore15(Integer score15) {
        this.score15 = score15;
    }

    public Integer getScore16() {
        return score16;
    }

    public void setScore16(Integer score16) {
        this.score16 = score16;
    }

    public Integer getScore17() {
        return score17;
    }

    public void setScore17(Integer score17) {
        this.score17 = score17;
    }

    public Integer getScore18() {
        return score18;
    }

    public void setScore18(Integer score18) {
        this.score18 = score18;
    }

    public Integer getScore19() {
        return score19;
    }

    public void setScore19(Integer score19) {
        this.score19 = score19;
    }

    public Integer getScore20() {
        return score20;
    }

    public void setScore20(Integer score20) {
        this.score20 = score20;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HafpisFpttCand that = (HafpisFpttCand) o;

        if (keys != null ? !keys.equals(that.keys) : that.keys != null) return false;
        if (transno != null ? !transno.equals(that.transno) : that.transno != null) return false;
        if (probeid != null ? !probeid.equals(that.probeid) : that.probeid != null) return false;
        if (dbid != null ? !dbid.equals(that.dbid) : that.dbid != null) return false;
        if (candrank != null ? !candrank.equals(that.candrank) : that.candrank != null) return false;
        if (score != null ? !score.equals(that.score) : that.score != null) return false;
        if (score01 != null ? !score01.equals(that.score01) : that.score01 != null) return false;
        if (score02 != null ? !score02.equals(that.score02) : that.score02 != null) return false;
        if (score03 != null ? !score03.equals(that.score03) : that.score03 != null) return false;
        if (score04 != null ? !score04.equals(that.score04) : that.score04 != null) return false;
        if (score05 != null ? !score05.equals(that.score05) : that.score05 != null) return false;
        if (score06 != null ? !score06.equals(that.score06) : that.score06 != null) return false;
        if (score07 != null ? !score07.equals(that.score07) : that.score07 != null) return false;
        if (score08 != null ? !score08.equals(that.score08) : that.score08 != null) return false;
        if (score09 != null ? !score09.equals(that.score09) : that.score09 != null) return false;
        if (score10 != null ? !score10.equals(that.score10) : that.score10 != null) return false;
        if (score11 != null ? !score11.equals(that.score11) : that.score11 != null) return false;
        if (score12 != null ? !score12.equals(that.score12) : that.score12 != null) return false;
        if (score13 != null ? !score13.equals(that.score13) : that.score13 != null) return false;
        if (score14 != null ? !score14.equals(that.score14) : that.score14 != null) return false;
        if (score15 != null ? !score15.equals(that.score15) : that.score15 != null) return false;
        if (score16 != null ? !score16.equals(that.score16) : that.score16 != null) return false;
        if (score17 != null ? !score17.equals(that.score17) : that.score17 != null) return false;
        if (score18 != null ? !score18.equals(that.score18) : that.score18 != null) return false;
        if (score19 != null ? !score19.equals(that.score19) : that.score19 != null) return false;
        if (score20 != null ? !score20.equals(that.score20) : that.score20 != null) return false;

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
        result = 31 * result + (score03 != null ? score03.hashCode() : 0);
        result = 31 * result + (score04 != null ? score04.hashCode() : 0);
        result = 31 * result + (score05 != null ? score05.hashCode() : 0);
        result = 31 * result + (score06 != null ? score06.hashCode() : 0);
        result = 31 * result + (score07 != null ? score07.hashCode() : 0);
        result = 31 * result + (score08 != null ? score08.hashCode() : 0);
        result = 31 * result + (score09 != null ? score09.hashCode() : 0);
        result = 31 * result + (score10 != null ? score10.hashCode() : 0);
        result = 31 * result + (score11 != null ? score11.hashCode() : 0);
        result = 31 * result + (score12 != null ? score12.hashCode() : 0);
        result = 31 * result + (score13 != null ? score13.hashCode() : 0);
        result = 31 * result + (score14 != null ? score14.hashCode() : 0);
        result = 31 * result + (score15 != null ? score15.hashCode() : 0);
        result = 31 * result + (score16 != null ? score16.hashCode() : 0);
        result = 31 * result + (score17 != null ? score17.hashCode() : 0);
        result = 31 * result + (score18 != null ? score18.hashCode() : 0);
        result = 31 * result + (score19 != null ? score19.hashCode() : 0);
        result = 31 * result + (score20 != null ? score20.hashCode() : 0);
        return result;
    }
}

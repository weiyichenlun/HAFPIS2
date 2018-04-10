package hbie2.HAFPIS2.Entity;

import java.util.Arrays;
import java.util.List;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/28
 * 最后修改时间:2018/3/28
 */
public class HafpisSrchTask {
    private String taskidd;
    private String transno;
    private String probeid;
    private Integer datatype;
    private Integer tasktype;
    private Integer status;
    private Integer priority;
    private Integer numofcand;
    private Integer averagecand;
    private Integer patternfilter;
    private Integer solveordup;
    private String srchposmask;
    private String srchdbsmask;
    private String demofilter;
    private byte[] srchdata;
    private String begtime;
    private String endtime;
    private String exptmsg;
    private List<SrchDataBean> srchDataBeans;

    public List<SrchDataBean> getSrchDataBeans() {
        return srchDataBeans;
    }

    public void setSrchDataBeans(List<SrchDataBean> srchDataBeans) {
        this.srchDataBeans = srchDataBeans;
    }

    public String getTaskidd() {
        return taskidd;
    }

    public void setTaskidd(String taskidd) {
        this.taskidd = taskidd;
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

    public Integer getDatatype() {
        return datatype;
    }

    public void setDatatype(Integer datatype) {
        this.datatype = datatype;
    }

    public Integer getTasktype() {
        return tasktype;
    }

    public void setTasktype(Integer tasktype) {
        this.tasktype = tasktype;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getNumofcand() {
        return numofcand;
    }

    public void setNumofcand(Integer numofcand) {
        this.numofcand = numofcand;
    }

    public Integer getAveragecand() {
        return averagecand;
    }

    public void setAveragecand(Integer averagecand) {
        this.averagecand = averagecand;
    }

    public Integer getPatternfilter() {
        return patternfilter;
    }

    public void setPatternfilter(Integer patternfilter) {
        this.patternfilter = patternfilter;
    }

    public Integer getSolveordup() {
        return solveordup;
    }

    public void setSolveordup(Integer solveordup) {
        this.solveordup = solveordup;
    }

    public String getSrchposmask() {
        return srchposmask;
    }

    public void setSrchposmask(String srchposmask) {
        this.srchposmask = srchposmask;
    }

    public String getSrchdbsmask() {
        return srchdbsmask;
    }

    public void setSrchdbsmask(String srchdbsmask) {
        this.srchdbsmask = srchdbsmask;
    }

    public String getDemofilter() {
        return demofilter;
    }

    public void setDemofilter(String demofilter) {
        this.demofilter = demofilter;
    }

    public byte[] getSrchdata() {
        return srchdata;
    }

    public void setSrchdata(byte[] srchdata) {
        this.srchdata = srchdata;
    }

    public String getBegtime() {
        return begtime;
    }

    public void setBegtime(String begtime) {
        this.begtime = begtime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public String getExptmsg() {
        return exptmsg;
    }

    public void setExptmsg(String exptmsg) {
        this.exptmsg = exptmsg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HafpisSrchTask that = (HafpisSrchTask) o;

        if (taskidd != null ? !taskidd.equals(that.taskidd) : that.taskidd != null) return false;
        if (transno != null ? !transno.equals(that.transno) : that.transno != null) return false;
        if (probeid != null ? !probeid.equals(that.probeid) : that.probeid != null) return false;
        if (datatype != null ? !datatype.equals(that.datatype) : that.datatype != null) return false;
        if (tasktype != null ? !tasktype.equals(that.tasktype) : that.tasktype != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (priority != null ? !priority.equals(that.priority) : that.priority != null) return false;
        if (numofcand != null ? !numofcand.equals(that.numofcand) : that.numofcand != null) return false;
        if (averagecand != null ? !averagecand.equals(that.averagecand) : that.averagecand != null) return false;
        if (patternfilter != null ? !patternfilter.equals(that.patternfilter) : that.patternfilter != null)
            return false;
        if (solveordup != null ? !solveordup.equals(that.solveordup) : that.solveordup != null) return false;
        if (srchposmask != null ? !srchposmask.equals(that.srchposmask) : that.srchposmask != null) return false;
        if (srchdbsmask != null ? !srchdbsmask.equals(that.srchdbsmask) : that.srchdbsmask != null) return false;
        if (demofilter != null ? !demofilter.equals(that.demofilter) : that.demofilter != null) return false;
        if (!Arrays.equals(srchdata, that.srchdata)) return false;
        if (begtime != null ? !begtime.equals(that.begtime) : that.begtime != null) return false;
        if (endtime != null ? !endtime.equals(that.endtime) : that.endtime != null) return false;
        if (exptmsg != null ? !exptmsg.equals(that.exptmsg) : that.exptmsg != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = taskidd != null ? taskidd.hashCode() : 0;
        result = 31 * result + (transno != null ? transno.hashCode() : 0);
        result = 31 * result + (probeid != null ? probeid.hashCode() : 0);
        result = 31 * result + (datatype != null ? datatype.hashCode() : 0);
        result = 31 * result + (tasktype != null ? tasktype.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        result = 31 * result + (numofcand != null ? numofcand.hashCode() : 0);
        result = 31 * result + (averagecand != null ? averagecand.hashCode() : 0);
        result = 31 * result + (patternfilter != null ? patternfilter.hashCode() : 0);
        result = 31 * result + (solveordup != null ? solveordup.hashCode() : 0);
        result = 31 * result + (srchposmask != null ? srchposmask.hashCode() : 0);
        result = 31 * result + (srchdbsmask != null ? srchdbsmask.hashCode() : 0);
        result = 31 * result + (demofilter != null ? demofilter.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(srchdata);
        result = 31 * result + (begtime != null ? begtime.hashCode() : 0);
        result = 31 * result + (endtime != null ? endtime.hashCode() : 0);
        result = 31 * result + (exptmsg != null ? exptmsg.hashCode() : 0);
        return result;
    }
}

package hbie2.HAFPIS2.Entity;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/28
 * 最后修改时间:2018/3/28
 */
public class HafpisDbopTask {
    private String taskidd;
    private String transno;
    private String probeid;
    private Integer datatype;
    private Integer tasktype;
    private Integer status;
    private Integer priority;
    private Integer dbid;
    private String begtime;
    private String endtime;
    private String exptmsg;

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

    public Integer getDbid() {
        return dbid;
    }

    public void setDbid(Integer dbid) {
        this.dbid = dbid;
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

        HafpisDbopTask that = (HafpisDbopTask) o;

        if (taskidd != null ? !taskidd.equals(that.taskidd) : that.taskidd != null) return false;
        if (transno != null ? !transno.equals(that.transno) : that.transno != null) return false;
        if (probeid != null ? !probeid.equals(that.probeid) : that.probeid != null) return false;
        if (datatype != null ? !datatype.equals(that.datatype) : that.datatype != null) return false;
        if (tasktype != null ? !tasktype.equals(that.tasktype) : that.tasktype != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (priority != null ? !priority.equals(that.priority) : that.priority != null) return false;
        if (dbid != null ? !dbid.equals(that.dbid) : that.dbid != null) return false;
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
        result = 31 * result + (dbid != null ? dbid.hashCode() : 0);
        result = 31 * result + (begtime != null ? begtime.hashCode() : 0);
        result = 31 * result + (endtime != null ? endtime.hashCode() : 0);
        result = 31 * result + (exptmsg != null ? exptmsg.hashCode() : 0);
        return result;
    }
}

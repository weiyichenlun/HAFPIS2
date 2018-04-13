package hbie2.HAFPIS2.Entity;

import java.io.Serializable;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/28
 * 最后修改时间:2018/3/28
 */
public class OtherCompositeKeys implements Serializable {
    private String taskidd;
    private String candid;
    private Integer position;

    public String getTaskidd() {
        return taskidd;
    }

    public void setTaskidd(String taskidd) {
        this.taskidd = taskidd;
    }

    public String getCandid() {
        return candid;
    }

    public void setCandid(String candid) {
        this.candid = candid;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    @Override
    public int hashCode() {
        int result = taskidd != null ? taskidd.hashCode() : 0;
        result += 31 * result + (candid != null ? candid.hashCode() : 0);
        result += 31 * result + (position != null ? position.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OtherCompositeKeys that = (OtherCompositeKeys) obj;
        if (taskidd != null ? !taskidd.equals(that.candid) : that.taskidd != null) return false;
        if (candid != null ? !candid.equals(that.candid) : that.candid != null ) return false;
        if (position != null ? !position.equals(that.position) : that.position != null) return false;
        return true;
    }
}

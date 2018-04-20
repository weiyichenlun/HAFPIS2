package hbie2.HAFPIS2.Entity;

import java.io.Serializable;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/13
 * 最后修改时间:2018/4/13
 */
public class RecordStatusKey implements Serializable {
    private String probeid;
    private Integer datatype;

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

    @Override
    public int hashCode() {
        int result = probeid != null ? probeid.hashCode() : 0;
        result += 31 * result + (datatype != null ? datatype.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RecordStatusKey that = (RecordStatusKey) obj;
        if (probeid != null ? !probeid.equals(that.probeid) : that.probeid != null) return false;
        if (datatype != null ? !datatype.equals(that.datatype) : that.datatype != null) return false;
        return true;
    }
}

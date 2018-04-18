package hbie2.HAFPIS2.Entity;

import java.util.Objects;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/12
 * 最后修改时间:2018/4/12
 */
public class HafpisMatcherTask {
    private MatcherTaskKey key;
    private String status;
    private String nistpath;
    private String createtime;
    private String magic;

    public MatcherTaskKey getKey() {
        return key;
    }

    public void setKey(MatcherTaskKey key) {
        this.key = key;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNistpath() {
        return nistpath;
    }

    public void setNistpath(String nistpath) {
        this.nistpath = nistpath;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getMagic() {
        return magic;
    }

    public void setMagic(String magic) {
        this.magic = magic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HafpisMatcherTask that = (HafpisMatcherTask) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(status, that.status) &&
                Objects.equals(nistpath, that.nistpath) &&
                Objects.equals(createtime, that.createtime) &&
                Objects.equals(magic, that.magic);
    }

    @Override
    public int hashCode() {

        return Objects.hash(key, status, nistpath, createtime, magic);
    }
}

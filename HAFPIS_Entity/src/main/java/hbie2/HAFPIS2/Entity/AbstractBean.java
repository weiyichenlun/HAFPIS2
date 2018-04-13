package hbie2.HAFPIS2.Entity;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/3
 * 最后修改时间:2018/4/3
 */
public abstract class AbstractBean<T extends AbstractBean> implements Comparable<T> {
    public int score;

    public int compareTo(T o) {
        return Integer.compare(o.score, this.score);
    }
}

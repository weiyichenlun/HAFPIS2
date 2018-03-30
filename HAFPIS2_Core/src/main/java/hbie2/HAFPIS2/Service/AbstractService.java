package hbie2.HAFPIS2.Service;

import java.util.List;
import java.util.Properties;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/28
 * 最后修改时间:2018/3/28
 */
public abstract class AbstractService implements Runnable {
    public int type;
    public int interval;
    public int querynum;
    public int status;
    public String tablename;
    public int[] tasktypes = new int[2];
    public int[] datatypes = new int[2];

    public abstract void init(Properties cfg);

    public abstract <T> void doWork(List<T> list);
}

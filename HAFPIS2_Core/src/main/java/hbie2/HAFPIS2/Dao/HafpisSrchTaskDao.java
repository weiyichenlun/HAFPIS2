package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisSrchTask;
import hbie2.HAFPIS2.Utils.CONSTANTS;
import hbie2.HAFPIS2.Utils.DateUtil;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import hbie2.HAFPIS2.Utils.StringUtil;
import org.hibernate.Session;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/29
 * 最后修改时间:2018/3/29
 */
public class HafpisSrchTaskDao {
    private Session session;

    public HafpisSrchTaskDao() {
        this.session = HibernateSessionFactoryUtil.getSession();
    }

    public List<HafpisSrchTask> getSrchTasks(int status, int datatype, int tasktype, int querynum) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<HafpisSrchTask> query = builder.createQuery(HafpisSrchTask.class);
        Root<HafpisSrchTask> srchTaskRoot = query.from(HafpisSrchTask.class);
        query.select(srchTaskRoot)
                .where(builder.and(builder.equal(srchTaskRoot.get("status"), status),
                        builder.equal(srchTaskRoot.get("datatype"), datatype),
                        builder.equal(srchTaskRoot.get("tasktype"), tasktype)))
                .orderBy(builder.desc(srchTaskRoot.get("priority")),
                        builder.asc(srchTaskRoot.get("begtime")));
        return session.createQuery(query).setFirstResult(0).setMaxResults(querynum).getResultList();
    }

    public boolean update(String taskidd, int status) {
        session.beginTransaction();
        Query query = session.createQuery("update HafpisSrchTask srch set srch.status=" + StringUtil.addQuotes(status)
                + ", srch.endtime=" + StringUtil.addQuotes(DateUtil.getFormatDate(System.currentTimeMillis()))
                + " where srch.taskidd=" + StringUtil.addQuotes(taskidd));
        int cnt = query.executeUpdate();
        session.getTransaction().commit();
        return cnt == 1;
    }

    public void update(String taskidd, int status, String exptmsg) {
        session.beginTransaction();
        Query query = session.createQuery("update HafpisSrchTask srch set srch.status=" + StringUtil.addQuotes(status)
                + ", srch.endtime=" + StringUtil.addQuotes(DateUtil.getFormatDate(System.currentTimeMillis()))
                + ", srch.exptmsg=" + StringUtil.addQuotes(exptmsg)
                + " where srch.taskidd=" + StringUtil.addQuotes(taskidd));
        query.executeUpdate();
        session.getTransaction().commit();
    }

    public void updateStatus(int datatype, int tasktype) {
        session.beginTransaction();
        Query query = session.createQuery("update HafpisSrchTask srch set status=" + StringUtil.addQuotes(CONSTANTS.WAIT_STATUS)
                + " where srch.status=" + StringUtil.addQuotes(CONSTANTS.PROCESSING_STATUS)
                + " and srch.datatype=" + StringUtil.addQuotes(datatype)
                + " and srch.tasktype=" + StringUtil.addQuotes(tasktype));
        query.executeUpdate();
        session.getTransaction().commit();
    }
}

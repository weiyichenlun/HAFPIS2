package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisSrchTask;
import hbie2.HAFPIS2.Utils.CONSTANTS;
import hbie2.HAFPIS2.Utils.DateUtil;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import hbie2.HAFPIS2.Utils.StringUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Logger log = LoggerFactory.getLogger(HafpisSrchTaskDao.class);

    public List<HafpisSrchTask> getSrchTasks(int status, int datatype, int tasktype, int querynum) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.getTransaction().begin();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<HafpisSrchTask> query = builder.createQuery(HafpisSrchTask.class);
        Root<HafpisSrchTask> srchTaskRoot = query.from(HafpisSrchTask.class);
        query.select(srchTaskRoot)
                .where(builder.and(builder.equal(srchTaskRoot.get("status"), status),
                        builder.equal(srchTaskRoot.get("datatype"), datatype),
                        builder.equal(srchTaskRoot.get("tasktype"), tasktype)))
                .orderBy(builder.desc(srchTaskRoot.get("priority")),
                        builder.asc(srchTaskRoot.get("begtime")));
        List<HafpisSrchTask> result = session.createQuery(query).setFirstResult(0).setMaxResults(querynum).getResultList();
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
        return result;
    }

    public boolean update(String taskidd, int status) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        Query query = session.createQuery("update HafpisSrchTask srch set srch.status=" + StringUtil.addQuotes(status)
                + ", srch.endtime=" + StringUtil.addQuotes(DateUtil.getFormatDate(System.currentTimeMillis()))
                + " where srch.taskidd=" + StringUtil.addQuotes(taskidd));
        int cnt = query.executeUpdate();
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
        return cnt == 1;
    }

    public void update(String taskidd, int status, String exptmsg) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        Query query = session.createQuery("update HafpisSrchTask srch set srch.status=" + StringUtil.addQuotes(status)
                + ", srch.endtime=" + StringUtil.addQuotes(DateUtil.getFormatDate(System.currentTimeMillis()))
                + ", srch.exptmsg=" + StringUtil.addQuotes(exptmsg)
                + " where srch.taskidd=" + StringUtil.addQuotes(taskidd));
        query.executeUpdate();
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
    }

    public void update(HafpisSrchTask srchTask) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        String hql = "update HafpisSrchTask srch set srch.status=:status where srch.taskidd=:taskidd";
        int updateCnt = session.createQuery(hql).setParameter("taskidd", srchTask.getTaskidd())
                .setParameter("status", srchTask.getStatus()).executeUpdate();
        log.info("update {} record with status {} for taskidd {}",updateCnt, srchTask.getStatus(), srchTask.getTaskidd());
//        if (session.getTransaction().getStatus().equals(TransactionStatus.ACTIVE)) {
//            session.getTransaction().commit();
//        }
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
    }

    public void updateStatus(int datatype, int tasktype) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        String hql = "update HafpisSrchTask  srch set srch.status=:newStatus where srch.status=:originalStatus " +
                "and srch.datatype=:datatype and srch.tasktype=:tasktype";
        int updateCnt = session.createQuery(hql).setParameter("newStatus", CONSTANTS.WAIT_STATUS)
                .setParameter("originalStatus", CONSTANTS.PROCESSING_STATUS).setParameter("datatype", datatype)
                .setParameter("tasktype", tasktype).executeUpdate();
//        Query query = session.createQuery("update HafpisSrchTask srch set status=" + StringUtil.addQuotes(CONSTANTS.WAIT_STATUS)
//                + " where srch.status=" + StringUtil.addQuotes(CONSTANTS.PROCESSING_STATUS)
//                + " and srch.datatype=" + StringUtil.addQuotes(datatype)
//                + " and srch.tasktype=" + StringUtil.addQuotes(tasktype));
//        query.executeUpdate();
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
    }
}

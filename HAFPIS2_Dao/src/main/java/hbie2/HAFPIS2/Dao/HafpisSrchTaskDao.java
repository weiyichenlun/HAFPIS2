package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisSrchTask;
import hbie2.HAFPIS2.Utils.CONSTANTS;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        HibernateSessionFactoryUtil.closeSession();
        return result;
    }

    public List<HafpisSrchTask> getSrchTasks(int status, int datatype, int tasktype, int querynum, Session session) {
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
        HibernateSessionFactoryUtil.closeSession();;
        return result;
    }

    public void update(HafpisSrchTask srchTask) {
        Transaction tx = null;
        try (Session session = HibernateSessionFactoryUtil.getSession()) {
            tx = session.getTransaction();
            tx.begin();
            String hql = "update HafpisSrchTask srch set srch.status=:status where srch.taskidd=:taskidd";
            int updateCnt = session.createQuery(hql).setParameter("taskidd", srchTask.getTaskidd())
                    .setParameter("status", srchTask.getStatus()).executeUpdate();
            log.info("update {} record with status {} for taskidd {}", updateCnt, srchTask.getStatus(), srchTask.getTaskidd());
            tx.commit();
        } catch (Exception e) {
            log.error("update srchTask error. ", e);
            if (tx != null) {
                tx.rollback();
            }
        }
    }

    public void update(HafpisSrchTask srchTask, Session session) {
//        String hql = "update HafpisSrchTask srch set srch.status=:status where srch.taskidd=:taskidd";
//        int updateCnt = session.createQuery(hql).setParameter("taskidd", srchTask.getTaskidd())
//                .setParameter("status", srchTask.getStatus()).executeUpdate();
        session.update(srchTask);
        log.info("update record with status {} for taskidd {}",srchTask.getStatus(), srchTask.getTaskidd());
    }

    public void updateStatus(int datatype, int tasktype) {
        Transaction tx = null;
        Session session = HibernateSessionFactoryUtil.getSession();
        try{
            tx = session.getTransaction();
            tx.begin();
            String hql = "update HafpisSrchTask  srch set srch.status=:newStatus where srch.status=:originalStatus " +
                    "and srch.datatype=:datatype and srch.tasktype=:tasktype";
            int updateCnt = session.createQuery(hql).setParameter("newStatus", CONSTANTS.WAIT_STATUS)
                    .setParameter("originalStatus", CONSTANTS.PROCESSING_STATUS).setParameter("datatype", datatype)
                    .setParameter("tasktype", tasktype).executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error("update status error. ", e);
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            HibernateSessionFactoryUtil.closeSession();;
        }
    }
}

package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisMatcherTask;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/12
 * 最后修改时间:2018/4/12
 */
public class HafpisMatcherTaskDao {
    private Logger log = LoggerFactory.getLogger(HafpisMatcherTask.class);

    public HafpisMatcherTask select(String probeid) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        HafpisMatcherTask matcherTask = session.get(HafpisMatcherTask.class, probeid);
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
        return matcherTask;
    }

    public boolean insert(HafpisMatcherTask matcherTask) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        session.save(matcherTask);
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
        return true;
    }

    public void resetStatus(String newStatus, String oriStatus) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        String hql = "update HafpisMatcherTask matc set matc.status=:newStatus where matc.status=:oriStatus";
        int resetCnt = session.createQuery(hql).setParameter("newStatus", newStatus).setParameter("oriStatus", oriStatus).executeUpdate();
        log.info("Reset status finish. Total {}", resetCnt);
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
    }

    public void resetStatus(String newStatus, String oriStatus, int datatype, int tasktype) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        String hql = "update HafpisMatcherTask matc set matc.status=:newStatus where matc.status=:oriStatus and " +
                "matc.datatype=:datatype and matc.tasktype=:tasktype";
        int resetCnt = session.createQuery(hql).setParameter("newStatus", newStatus)
                .setParameter("oriStatus", oriStatus).setParameter("datatype", datatype)
                .setParameter("tasktype", tasktype).executeUpdate();
        log.info("Reset status finish: Total/Datatype/Tasktype: {} / {] / {}", resetCnt, datatype, tasktype);
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
    }

    public boolean update(HafpisMatcherTask task) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        String hql = "update HafpisMatcherTask matc set matc.status=:status, matc.nistpath=:nistpath where matc.probeid=:probeid";
        int updateCnt = session.createQuery(hql).setParameter("status", task.getStatus())
                .setParameter("nistpath", task.getNistpath())
                .setParameter("probeid", task.getProbeid())
                .executeUpdate();
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
        return updateCnt == 1;
    }
}

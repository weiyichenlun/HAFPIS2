package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisRecordStatus;
import hbie2.HAFPIS2.Entity.RecordStatusKey;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/12
 * 最后修改时间:2018/4/12
 */
public class HafpisRecordStatusDao {
    private Logger log = LoggerFactory.getLogger(HafpisRecordStatus.class);

    public HafpisRecordStatus select(String probeid, int datatype) {
        Session session = HibernateSessionFactoryUtil.getSession();
        RecordStatusKey key = new RecordStatusKey();
        key.setProbeid(probeid);
        key.setDatatype(datatype);
        HafpisRecordStatus recordStatus = session.get(HafpisRecordStatus.class, key);
        HibernateSessionFactoryUtil.closeSession();
        return recordStatus;
    }

    public boolean insert(HafpisRecordStatus recordStatus) {
        Session session = HibernateSessionFactoryUtil.getSession();
        Transaction tx = session.getTransaction();
        try {
            tx.begin();
            session.save(recordStatus);
            tx.commit();
            return true;
        } catch (Exception e) {
            log.error("Error: ", e);
            tx.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    public void resetStatus(String newStatus, String oriStatus) {
        Session session = HibernateSessionFactoryUtil.getSession();
        Transaction tx = session.getTransaction();
        try {
            tx.begin();
            String hql = "update HafpisRecordStatus matc set matc.status=:newStatus where matc.status=:oriStatus";
            int resetCnt = session.createQuery(hql).setParameter("newStatus", newStatus).setParameter("oriStatus", oriStatus).executeUpdate();
            log.debug("Reset status finish. Total {}", resetCnt);
            tx.commit();
        } catch (Exception e) {
            log.error("Error: ", e);
            tx.rollback();
        } finally {
            session.close();
        }
    }

    public void resetStatus(String newStatus, String oriStatus, int datatype, int tasktype) {
        Session session = HibernateSessionFactoryUtil.getSession();
        Transaction tx = session.getTransaction();
        try {
            tx.begin();
            String hql = "update HafpisRecordStatus matc set matc.status=:newStatus where matc.status=:oriStatus and " +
                    "matc.datatype=:datatype and matc.tasktype=:tasktype";
            int resetCnt = session.createQuery(hql).setParameter("newStatus", newStatus)
                    .setParameter("oriStatus", oriStatus).setParameter("datatype", datatype)
                    .setParameter("tasktype", tasktype).executeUpdate();
            log.debug("Reset status finish: Total/Datatype/Tasktype: {} / {] / {}", resetCnt, datatype, tasktype);
            tx.commit();
        } catch (Exception e) {
            log.error("Error: ", e);
            tx.rollback();
        } finally {
            session.close();
        }
    }

    public boolean update(HafpisRecordStatus task) {
        Session session = HibernateSessionFactoryUtil.getSession();
        Transaction tx = session.getTransaction();
        try {
            tx.begin();
            String hql = "update HafpisRecordStatus matc set matc.status=:status, matc.nistpath=:nistpath where matc.probeid=:probeid";
            int updateCnt = session.createQuery(hql).setParameter("status", task.getStatus())
                    .setParameter("nistpath", task.getNistpath())
                    .setParameter("probeid", task.getKey().getProbeid())
                    .executeUpdate();
            tx.commit();
            return updateCnt == 1;
        } catch (Exception e) {
            log.error("Error: ", e);
            tx.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    public void delete(HafpisRecordStatus task) {
        Session session = HibernateSessionFactoryUtil.getSession();
        Transaction tx = session.getTransaction();
        try {
            tx.begin();
            session.delete(task);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            log.error("Error: ", e);
        } finally {
            session.close();
        }
    }
}

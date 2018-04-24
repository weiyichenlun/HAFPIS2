package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisFpttCand;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/29
 * 最后修改时间:2018/3/29
 */
public class HafpisFpttCandDao {
    private Logger log = LoggerFactory.getLogger(HafpisFpttCandDao.class);

    public void insert(List<HafpisFpttCand> result) {
        Session session = HibernateSessionFactoryUtil.getSession();
        Transaction tx = session.getTransaction();
        try {
            tx.begin();
            for (int i = 0; i < result.size(); i++) {
                log.debug("FPTT: the rank {} and tha candid {}/{}", i, result.get(i).getKeys().getTaskidd(), result.get(i).getKeys().getCandid());
                session.save(result.get(i));
                if (i % 20 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            tx.commit();
        } catch (Exception e) {
            log.error("insert cand error. ", e);
            tx.rollback();
        } finally {
            HibernateSessionFactoryUtil.closeSession();;
        }
    }

    public void insert(List<HafpisFpttCand> result, Session session) {
        for (int i = 0; i < result.size(); i++) {
            session.saveOrUpdate(result.get(i));
            if (i % 20 == 0) {
                session.flush();
                session.clear();
            }
        }
    }

    public void delete(String taskidd) {
        Session session = HibernateSessionFactoryUtil.getSession();
        Transaction tx = session.getTransaction();
        try {
            tx.begin();
            String hql = "delete from HafpisFpttCand fptt where fptt.keys.taskidd=:taskidd";
            int deleteCnt = session.createQuery(hql).setParameter("taskidd", taskidd).executeUpdate();
            log.info("FPTT: delete {} records with taskidd {}", deleteCnt, taskidd);
            tx.commit();
        } catch (Exception e) {
            log.error("delete cand error. ", e);
            tx.rollback();
        } finally {
            HibernateSessionFactoryUtil.closeSession();;
        }
    }
}

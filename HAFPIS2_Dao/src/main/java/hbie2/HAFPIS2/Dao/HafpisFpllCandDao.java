package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisFpllCand;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/23
 * 最后修改时间:2018/4/23
 */
public class HafpisFpllCandDao {
    private Logger log = LoggerFactory.getLogger(HafpisFpllCandDao.class);

    public void insert(List<HafpisFpllCand> result) {
        Session session = HibernateSessionFactoryUtil.getSession();
        Transaction tx = session.getTransaction();
        try {
            tx.begin();
            String taskidd = result.get(0).getKeys().getTaskidd();
            for (int i = 0; i < result.size(); i++) {
                log.debug("FPLT: the rank {} and tha taskidd/candid/position {}/{}", i,
                        result.get(i).getKeys().getTaskidd(), result.get(i).getKeys().getCandid());
                session.save(result.get(i));
                if (i % 20 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            tx.commit();
        } catch (Exception e) {
            log.error("insert cands error. ", e);
            tx.rollback();
        } finally {
            HibernateSessionFactoryUtil.closeSession();;
        }
    }

    public void insert(List<HafpisFpllCand> result, Session session) {
        for (int i = 0; i < result.size(); i++) {
            session.saveOrUpdate(result.get(0));
            if (i % 20 == 0 && i > 0) {
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
            String hql = "delete from HafpisFpllCand fpll where fpll.keys.taskidd=:taskidd";
            int deleteCnt = session.createQuery(hql).setParameter("taskidd", taskidd).executeUpdate();
            log.info("FPLT: delete {} records with taskidd {}", deleteCnt, taskidd);
            tx.commit();
        } catch (Exception e) {
            log.error("delete cands error.", e);
            tx.rollback();
        } finally {
            HibernateSessionFactoryUtil.closeSession();;
        }
    }
}

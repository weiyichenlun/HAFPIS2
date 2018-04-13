package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisFpltCand;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/8
 * 最后修改时间:2018/4/8
 */
public class HafpisFpltCandDao {
    private Logger log = LoggerFactory.getLogger(HafpisFpltCandDao.class);

    public void insert(List<HafpisFpltCand> result) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        String taskidd = result.get(0).getKeys().getTaskidd();
        for (int i = 0; i < result.size(); i++) {
            log.info("FPLT: the rank {} and tha taskidd/candid/position {}/{}/{}", i,
                    result.get(i).getKeys().getTaskidd(), result.get(i).getKeys().getCandid(), result.get(i).getKeys().getPosition());
            session.save(result.get(i));
            if (i % 20 == 0) {
                session.flush();
                session.clear();
            }
        }
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
    }

    public void delete(String taskidd) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        String hql = "delete HafpisFpltCand fplt where fplt.keys.taskidd=:taskidd";
        int deleteCnt = session.createQuery(hql).setParameter("taskidd", taskidd).executeUpdate();
        log.info("FPLT: delete {} records with taskidd {}", deleteCnt, taskidd);
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
    }
}

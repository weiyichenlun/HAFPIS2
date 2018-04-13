package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisFaceCand;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/28
 * 最后修改时间:2018/3/28
 */
public class HafpisFaceCandDao {
    private Logger log = LoggerFactory.getLogger(HafpisFaceCandDao.class);
    private Session session = HibernateSessionFactoryUtil.getSession();

    public void save(HafpisFaceCand faceCand) {
        session.beginTransaction();
        String id = String.valueOf(session.save(faceCand));
        System.out.println(id);
        session.getTransaction().commit();
    }
}

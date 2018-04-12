package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisImgdbCaps;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/11
 * 最后修改时间:2018/4/11
 */
public class HafpisImgdbCapsDao {
    private Session session;

    public HafpisImgdbCaps get(String devname) {
        session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        HafpisImgdbCaps imgdbCaps = session.get(HafpisImgdbCaps.class, devname);
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
        return imgdbCaps;
    }
}

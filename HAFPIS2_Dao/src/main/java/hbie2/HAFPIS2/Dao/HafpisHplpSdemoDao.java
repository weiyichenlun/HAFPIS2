package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisHplpSdemo;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/20
 * 最后修改时间:2018/4/20
 */
public class HafpisHplpSdemoDao {
    public Integer getDbId(String latentid) {
        Session session = HibernateSessionFactoryUtil.getSession();
        HafpisHplpSdemo hplpSdemo = session.get(HafpisHplpSdemo.class, latentid);
        HibernateSessionFactoryUtil.closeSession();;
        return hplpSdemo == null ? 0 : hplpSdemo.getDbid();
    }

    public HafpisHplpSdemo select(String latentid) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        HafpisHplpSdemo hplpSdemo = session.get(HafpisHplpSdemo.class, latentid);
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
        return hplpSdemo;
    }
}

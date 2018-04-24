package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisHlppSdemo;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/20
 * 最后修改时间:2018/4/20
 */
public class HafpisHlppSdemoDao {

    public Integer getDbId(String latentid) {
        Session session = HibernateSessionFactoryUtil.getSession();
        HafpisHlppSdemo hlppSdemo = session.get(HafpisHlppSdemo.class, latentid);
        HibernateSessionFactoryUtil.closeSession();;
        return hlppSdemo == null ? 0 : hlppSdemo.getDbid();
    }

    public HafpisHlppSdemo select(String latentid) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        HafpisHlppSdemo hlppSdemo = session.get(HafpisHlppSdemo.class, latentid);
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
        return hlppSdemo;
    }
}

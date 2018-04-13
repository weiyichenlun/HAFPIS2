package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisHtppSdemo;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/28
 * 最后修改时间:2018/3/28
 */
public class HafpisHtppSdemoDao {
    private Session session;

    public String getImgMask(String personid) {
        session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        HafpisHtppSdemo htppSdemo = session.get(HafpisHtppSdemo.class, personid);
        session.getTransaction().commit();
        return htppSdemo == null ? null : htppSdemo.getImgmask();
    }

    public Integer getDbId(String personid) {
        session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        HafpisHtppSdemo htppSdemo = session.get(HafpisHtppSdemo.class, personid);
        session.getTransaction().commit();
        return htppSdemo == null ? 0 : htppSdemo.getDbid();
    }


}

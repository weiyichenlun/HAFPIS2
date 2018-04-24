package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisHlppKey;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/19
 * 最后修改时间:2018/4/19
 */
public class HafpisHlppKeyDao {

    public HafpisHlppKey get(String latentid) {
        Session session = HibernateSessionFactoryUtil.getSession();
        HafpisHlppKey hlppKey = session.get(HafpisHlppKey.class, latentid);
        HibernateSessionFactoryUtil.closeSession();;
        return hlppKey;
    }
}

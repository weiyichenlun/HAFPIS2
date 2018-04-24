package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisHtppKey;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/11
 * 最后修改时间:2018/4/11
 */
public class HafpisHtppKeyDao {

    public HafpisHtppKey get(String personid) {
        Session session = HibernateSessionFactoryUtil.getSession();
        HafpisHtppKey htppKey = session.get(HafpisHtppKey.class, personid);
        HibernateSessionFactoryUtil.closeSession();;
        return htppKey;
    }
}

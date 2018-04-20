package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisHplpKey;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/20
 * 最后修改时间:2018/4/20
 */
public class HafpisHplpKeyDao {
    public HafpisHplpKey get(String latentid) {
        Session session = HibernateSessionFactoryUtil.getSession();
        HafpisHplpKey hplpKey = session.get(HafpisHplpKey.class, latentid);
        session.close();
        return hplpKey;
    }
}

package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/29
 * 最后修改时间:2018/3/29
 */
public class HafpisFpttCandDao {
    private Session session;

    public HafpisFpttCandDao() {
        this.session = HibernateSessionFactoryUtil.getSession();
    }


}

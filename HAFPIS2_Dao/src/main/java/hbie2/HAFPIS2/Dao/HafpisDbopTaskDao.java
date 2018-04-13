package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisDbopTask;
import hbie2.HAFPIS2.Utils.DateUtil;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import hbie2.HAFPIS2.Utils.StringUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/27
 * 最后修改时间:2018/3/27
 */
public class HafpisDbopTaskDao {
    private Session session;

    public List<HafpisDbopTask> getDbopTasks(int status, int datatype, int querynum) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<HafpisDbopTask> criteria = builder.createQuery(HafpisDbopTask.class);
        Root<HafpisDbopTask> dbopTaskRoot = criteria.from(HafpisDbopTask.class);
        criteria.select(dbopTaskRoot)
                .where(builder.and(builder.equal(dbopTaskRoot.get("status"), status),
                        builder.equal(dbopTaskRoot.get("datatype"), datatype)))
                .orderBy(builder.desc(dbopTaskRoot.get("priority")),
                        builder.asc(dbopTaskRoot.get("begtime")));
        return session.createQuery(criteria).setFirstResult(0).setMaxResults(querynum).getResultList();
    }

    public boolean update(String taskidd, int status) {
        session.beginTransaction();
        Query query = session.createQuery("update HafpisDbopTask dbop set dbop.status=" + StringUtil.addQuotes(status)
                + ", dbop.endtime=\'" + DateUtil.getFormatDate(System.currentTimeMillis())
                + "\' where dbop.taskidd=" + StringUtil.addQuotes(taskidd));
        int num = query.executeUpdate();
        session.getTransaction().commit();
        return num == 1;
    }

    public void update(HafpisDbopTask dbopTask) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        String hql = "update HafpisDbopTask dbop set dbop.status=:status where dbop.taskidd=:taskidd";
        int updateHql = session.createQuery(hql).setParameter("status", dbopTask.getStatus())
                .setParameter("taskidd", dbopTask.getTaskidd()).executeUpdate();
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
    }

    public boolean update(String taskidd, int status, String exptmsg) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        String hql = "update HafpisDbopTask dbop set dbop.status=:status" + (exptmsg==null?" ":", dbop.exptmsg=:exptmsg") + " where dbop.taskidd=:taskidd";
        Query query = session.createQuery(hql).setParameter("status", status);
        if (exptmsg != null) {
            query.setParameter("exptmsg", exptmsg);
        }
        query.setParameter("taskidd", taskidd);
        int updateCnt = query.executeUpdate();
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
        return updateCnt == 1;
    }

    public void updateStatus(int datatype) {
        session.beginTransaction();
        Query query = session.createQuery("update HafpisDbopTask dbop set dbop.status=\'3\' where dbop.status="
                + StringUtil.addQuotes(4) + " and dbop.datatype=" + StringUtil.addQuotes(datatype));
        query.executeUpdate();
        session.getTransaction().commit();
    }
}
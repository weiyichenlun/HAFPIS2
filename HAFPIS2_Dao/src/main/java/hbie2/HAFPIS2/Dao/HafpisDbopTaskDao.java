package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisDbopTask;
import hbie2.HAFPIS2.Utils.CONSTANTS;
import hbie2.HAFPIS2.Utils.DateUtil;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
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

    public List<HafpisDbopTask> getDbopTasks(int status, int datatype, int querynum) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<HafpisDbopTask> criteria = builder.createQuery(HafpisDbopTask.class);
        Root<HafpisDbopTask> dbopTaskRoot = criteria.from(HafpisDbopTask.class);
        criteria.select(dbopTaskRoot)
                .where(builder.and(builder.equal(dbopTaskRoot.get("status"), status),
                        builder.equal(dbopTaskRoot.get("datatype"), datatype)))
                .orderBy(builder.desc(dbopTaskRoot.get("priority")),
                        builder.asc(dbopTaskRoot.get("begtime")));
        List<HafpisDbopTask> list = session.createQuery(criteria).setFirstResult(0).setMaxResults(querynum).getResultList();
        if (list != null) {
            System.out.println("list size is " + list.size());
        } else {
            System.out.println("list size is null");
        }
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
        return list;
    }

    public boolean update(String taskidd, int status) {
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        String hql = "update HafpisDbopTask dbop set dbop.status=:status, dbop.endtime=:endtime where dbop.taskidd=:taskidd";
        Query query = session.createQuery(hql);
        int num = query.setParameter("status", status).setParameter("endtime", DateUtil.getFormatDate(System.currentTimeMillis()))
                .setParameter("taskidd", taskidd).executeUpdate();
//        Query query = session.createQuery("update HafpisDbopTask dbop set dbop.status=" + StringUtil.addQuotes(status)
//                + ", dbop.endtime=\'" + DateUtil.getFormatDate(System.currentTimeMillis())
//                + "\' where dbop.taskidd=" + StringUtil.addQuotes(taskidd));
//        int num = query.executeUpdate();
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
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

    public void update(HafpisDbopTask dbopTask, Session session) {
        session.update(dbopTask);
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
        Session session = HibernateSessionFactoryUtil.getSession();
        session.beginTransaction();
        String hql = "update HafpisDbopTask dbop set dbop.status=:newstatus where dbop.status=:oldstatus and dbop.datatype=:datatype";
        Query query = session.createQuery(hql);
        query.setParameter("newstatus", CONSTANTS.WAIT_STATUS).setParameter("oldstatus", CONSTANTS.PROCESSING_STATUS)
                .setParameter("datatype", datatype);
//        Query query = session.createQuery("update HafpisDbopTask dbop set dbop.status=\'3\' where dbop.status="
//                + StringUtil.addQuotes(4) + " and dbop.datatype=" + StringUtil.addQuotes(datatype));
        query.executeUpdate();
        session.getTransaction().commit();
        HibernateSessionFactoryUtil.closeSession();
    }
}

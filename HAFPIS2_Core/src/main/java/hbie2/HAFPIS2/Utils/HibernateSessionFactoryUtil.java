package hbie2.HAFPIS2.Utils;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/27
 * 最后修改时间:2018/3/27
 */
public class HibernateSessionFactoryUtil {
    private static final Logger log = LoggerFactory.getLogger(HibernateSessionFactoryUtil.class);
    private static final Configuration cfg = new Configuration();
    private static SessionFactory sessionFactory;
    private static final ThreadLocal<Session> threadLocal = new ThreadLocal<>();

    public static Session getSession() {
        Session session = threadLocal.get();
        if (session == null) {
            if (sessionFactory == null) {
                try {
                    cfg.configure(new File("hibernate.cfg.xml"));
                    sessionFactory = cfg.buildSessionFactory();
                } catch (HibernateException e) {
                    log.error("Error creating SessionFactory. ", e);
                    System.exit(-1);
                }
            }
            session = sessionFactory.openSession();
            threadLocal.set(session);
        }
        return session;
    }

    public static void closeSession() {
        Session session = threadLocal.get();
        threadLocal.set(null);
        if (session != null) {
            session.close();
        }
    }
}

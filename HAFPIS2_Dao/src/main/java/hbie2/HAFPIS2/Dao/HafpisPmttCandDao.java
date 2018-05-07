package hbie2.HAFPIS2.Dao;

import hbie2.HAFPIS2.Entity.HafpisPmttCand;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/5/2
 * 最后修改时间:2018/5/2
 */
public class HafpisPmttCandDao {
    private Logger log = LoggerFactory.getLogger(HafpisPmttCandDao.class);

    public void insert(List<HafpisPmttCand> list, Session session) {
        for (int i = 0; i < list.size(); i++) {
            session.saveOrUpdate(list.get(i));
            if (i % 20 == 0 && i > 0) {
                session.flush();
                session.clear();
            }
        }
    }
}

package hbie2.HAFPIS2.Service.Impl;

import hbie2.Candidate;
import hbie2.HAFPIS2.Dao.HafpisPmllCandDao;
import hbie2.HAFPIS2.Dao.HafpisSrchTaskDao;
import hbie2.HAFPIS2.Entity.HafpisPmllCand;
import hbie2.HAFPIS2.Entity.HafpisSrchTask;
import hbie2.HAFPIS2.Entity.SrchDataBean;
import hbie2.HAFPIS2.Service.AbstractService;
import hbie2.HAFPIS2.Utils.CONSTANTS;
import hbie2.HAFPIS2.Utils.CommonUtils;
import hbie2.HAFPIS2.Utils.ConfigUtils;
import hbie2.HAFPIS2.Utils.HbieUtils;
import hbie2.HAFPIS2.Utils.HibernateSessionFactoryUtil;
import hbie2.TaskSearch;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/5/2
 * 最后修改时间:2018/5/2
 */
public class HafpisPMLLService extends AbstractService implements Runnable {
    private Logger log = LoggerFactory.getLogger(HafpisPMLLService.class);
    private HafpisSrchTaskDao srchTaskDao;
    private HafpisPmllCandDao pmllDao;
    private ArrayBlockingQueue<HafpisSrchTask> srchTaskQueue;
    private int PMLL_Threshold;

    @Override
    public void init(Properties cfg) {
        try {
            this.status = Integer.parseInt(cfg.getProperty("status", "3"));
        } catch (NumberFormatException e) {
            log.error("status: {} config error, must be a number. Use default status: 3 ", cfg.getProperty("status"), e);
            this.status = 3;
        }
        try {
            this.querynum = Integer.parseInt(cfg.getProperty("querynum", "10"));

        } catch (NumberFormatException e) {
            log.error("querynum: {} config error, must be a number. Use default querynum: 10", cfg.getProperty("querynum"), e);
            this.querynum = 10;
        }
        try {
            this.interval = Integer.parseInt(cfg.getProperty("interval", "1"));

        } catch (NumberFormatException e) {
            log.error("interval: {} config error, must be a number. Use default interval: 1", cfg.getProperty("interval"), e);
            this.interval = 1;
        }
        try {
            this.PMLL_Threshold = Integer.parseInt(cfg.getProperty("PMLL_Threshold", "0"));
        } catch (NumberFormatException e) {
            log.error("PMLL_Threshold: {} config error, must be an Integer. Use default value: 0", cfg.getProperty("PMLL_Threshold"));
            this.PMLL_Threshold = 0;
        }

        try {
            this.thread_num = Integer.parseInt(ConfigUtils.getConfigOrDefault("latpalm_thread_num", "1"));
        } catch (NumberFormatException e) {
            log.error("threadnum: {} config error, must be an Integer. Use default value: 1", ConfigUtils.getConfig("latpalm_thread_num"));
            this.thread_num = 1;
        }

        srchTaskDao = new HafpisSrchTaskDao();
        pmllDao = new HafpisPmllCandDao();
        srchTaskQueue = new ArrayBlockingQueue<>(CONSTANTS.PPLL_LIMIT);
    }

    @Override
    public <T> void doWork(List<T> list) {
        HafpisSrchTask srchTask = (HafpisSrchTask) list.get(0);
        SrchDataBean srchDataBean = srchTask.getSrchDataBeans().get(0);
        String taskidd = srchTask.getTaskidd();
        byte[] feature = srchDataBean.latpalmmnt;
        if (feature == null || feature.length == 0) {
            log.error("latpalmmnt is null. taskidd: {}", taskidd);
            srchTask.setStatus(CONSTANTS.ERROR_STATUS);
            srchTask.setExptmsg("latpalmmnt is null");
            updateSrchOnly(srchTask);
        } else {
            if (HbieUtils.getInstance().hbie_PLP != null) {
                try {
                    TaskSearch taskSearch = new TaskSearch();
                    Map<String, HafpisPmllCand> pmllCandMap = new HashMap<>();
                    int numOfCand = srchTask.getNumofcand();
                    numOfCand = numOfCand > 0 ? (int) (numOfCand * 1.5) : CONSTANTS.MAXCANDS;
                    taskSearch.setId(taskidd);
                    taskSearch.setType("L2L");
                    taskSearch.setScoreThreshold(PMLL_Threshold);

                    //set filters
                    String dbsFilter = CommonUtils.getDbsFilter(srchTask.getSrchdbsmask());
                    String sovleOrDupFilter = CommonUtils.getSolveOrDupFilter(CONSTANTS.DBOP_PLP, srchTask.getSolveordup());
                    String demoFilter = CommonUtils.getDemoFilter(srchTask.getDemofilter());
                    String filter = CommonUtils.mergeFilters(dbsFilter, sovleOrDupFilter, demoFilter);
                    taskSearch.setFilter(filter);
                    log.debug("Total filter is {}", filter);
                    taskSearch.setFeature(feature);

                    // search
                    String uid = HbieUtils.getInstance().hbie_PLP.submitSearch(taskSearch);
                    while (true) {
                        TaskSearch task = HbieUtils.getInstance().hbie_PLP.querySearch(uid);
                        if (task == null) {
                            log.warn("PMLL: Impossible. taskidd/uid: {}/{}", taskidd, uid);
                            srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                            updateSrchOnly(srchTask);
                            break;
                        } else if (task.getStatus() == TaskSearch.Status.Error) {
                            log.error("PMLL search error. taskidd/uid: {}/{}", taskidd, uid);
                            srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                            srchTask.setExptmsg(task.getMsg());
                            updateSrchOnly(srchTask);
                            break;
                        } else if (task.getStatus() != TaskSearch.Status.Done) {
                            CommonUtils.sleep(10);
                            continue;
                        }
                        List<Candidate> candidates = task.getCandidates();
                        for (int i = 0; i < candidates.size(); i++) {
                            Candidate candidate = candidates.get(i);
                            String candid = candidate.getId();
                            HafpisPmllCand pmllCand = pmllCandMap.get(candid);
                            if (pmllCand == null) {
                                pmllCand = new HafpisPmllCand();
                                pmllCand.getKeys().setTaskidd(taskidd);
                                pmllCand.getKeys().setCandid(candid);
                                pmllCand.getKeys().setPosition(1);
                                pmllCand.setDbid((Integer) candidate.getFields().get("dbid"));
                                pmllCand.setScore(candidate.getScore());
                                pmllCand.setTransno(srchTask.getTransno());
                                pmllCand.setProbeid(srchTask.getProbeid());
                            } else {
                                if (candidate.getScore() > pmllCand.getScore()) {
                                    pmllCand.setScore(candidate.getScore());
                                }
                            }
                            pmllCandMap.put(candid, pmllCand);
                        }
                        break;
                    }
                    //insert into db
                    log.debug("begin to insert into table");
                    if (pmllCandMap.size() == 0) {
                        log.info("PMLL search finish. No results for taskidd {}", taskidd);
                        srchTask.setStatus(CONSTANTS.FINISH_NOMATCH_STATUS);
                        srchTask.setExptmsg("No results");
                        updateSrchOnly(srchTask);
                    } else {
                        List<HafpisPmllCand> pmllCands = new ArrayList<>();
                        pmllCands.addAll(pmllCandMap.values());
                        if (pmllCands.size() >= numOfCand) {
                            pmllCands = CommonUtils.getLimitedList(pmllCands, numOfCand);
                        } else {
                            Collections.sort(pmllCands);
                        }

                        //rank
                        for (int i = 0; i < pmllCands.size(); i++) {
                            pmllCands.get(i).setCandrank(i + 1);
                        }
                        log.debug("Inserting...");
                        srchTask.setStatus(CONSTANTS.FINISH_STATUS);
                        updateSrchAndPMLL(srchTask, pmllCands);
                        log.info("srchtask {} finish", taskidd);
                    }
                } catch (Exception e) {
                    log.error("Impossible.", e);
                    srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                    updateSrchOnly(srchTask);
                }
            } else {
                log.warn("Get HBIE client null. Suspending until HBIE is started.");
                log.info("waiting PMLL client...");
                srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                updateSrchOnly(srchTask);
                CommonUtils.sleep(interval * 1000);
            }
        }
    }

    @Override
    public void run() {
        //add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(()-> {
            srchTaskDao.updateStatus(CONSTANTS.SRCH_DATATYPE_PLP, CONSTANTS.SRCH_TASKTYPE_LL);
            log.info("PMLL is shutting down.");
        }));

        log.info("PMLL service start. Update status first...");
        srchTaskDao.updateStatus(CONSTANTS.SRCH_DATATYPE_PLP, CONSTANTS.SRCH_TASKTYPE_LL);

        //Take SrchTask from db
        new Thread(()-> {
            log.info("PMLL_SRCHTASKQUEUE_THREAD start...");
            while (true) {
                List<HafpisSrchTask> list = srchTaskDao.getSrchTasks(CONSTANTS.URGENT_STATUS, CONSTANTS.SRCH_DATATYPE_PLP,
                        CONSTANTS.SRCH_TASKTYPE_LL, querynum);
                if (null == list || list.size() == 0) {
                    list = srchTaskDao.getSrchTasks(CONSTANTS.WAIT_STATUS, CONSTANTS.SRCH_DATATYPE_PLP, CONSTANTS.SRCH_TASKTYPE_LL,
                            querynum);
                    if (null == list || list.size() == 0) {
                        CommonUtils.sleep(interval * 1000);
                    } else {
                        for (HafpisSrchTask srchTask : list) {
                            try {
                                srchTaskQueue.put(srchTask);
                                srchTask.setStatus(CONSTANTS.PROCESSING_STATUS);
                                updateSrchOnly(srchTask);
                            } catch (InterruptedException e) {
                                log.error("PMLL: put {} into srchtask queue error.", srchTask.getTaskidd());
                            }
                        }
                    }
                } else {
                    for (HafpisSrchTask srchTask : list) {
                        try {
                            srchTaskQueue.put(srchTask);
                            srchTask.setStatus(CONSTANTS.PROCESSING_STATUS);
                            updateSrchOnly(srchTask);
                        } catch (InterruptedException e) {
                            log.error("PMLL: put urgent {} into srchtask queue error.", e);
                        }
                    }
                }
            }
        }, "PMLL_SRCHTASKQUEUE_THREAD").start();

        for (int i = 0; i < this.thread_num; i++) {
            new Thread(this::PMLL, "PMLL_SEARCH_THREAD").start();
        }
    }

    private void PMLL() {
        log.info("PMLL_SEARCH_THREAD start...");
        while (true) {
            HafpisSrchTask srchTask = null;
            try {
                srchTask = srchTaskQueue.take();
                if (srchTask.getSrchdata() == null || srchTask.getSrchdata().length == 0) {
                    log.error("SrchTask {} srchdata is null", srchTask.getTaskidd());
                    srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                    srchTask.setExptmsg("srchdata is null");
                    updateSrchOnly(srchTask);
                } else {
                    log.info("get srchtask: {}", srchTask.getTaskidd());
                    CommonUtils.convert(srchTask);
                    if (CommonUtils.check(srchTask)) {
                        log.error("Convert srchdata error. taskidd: {}", srchTask.getTaskidd());
                        srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                        srchTask.setExptmsg("convert srchdata error");
                        updateSrchOnly(srchTask);
                    } else {
                        List<HafpisSrchTask> list = new ArrayList<>();
                        list.add(srchTask);
                        doWork(list);
                    }
                }
            } catch (InterruptedException e) {
                log.error("Take srchtask from queue error. ", e);
                CommonUtils.sleep(100);
            }
        }
    }

    private void updateSrchOnly(HafpisSrchTask srchTask) {
        Transaction tx = null;
        Session session = HibernateSessionFactoryUtil.getSession();
        try {
            tx = session.getTransaction();
            tx.begin();
            srchTaskDao.update(srchTask, session);
            tx.commit();
        } catch (Exception e) {
            log.error("Update srchtask error. ", e);
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            HibernateSessionFactoryUtil.closeSession();
        }
    }

    private void updateSrchAndPMLL(HafpisSrchTask srchTask, List<HafpisPmllCand> pmllCands) {
        Transaction tx = null;
        Session session = HibernateSessionFactoryUtil.getSession();
        try {
            tx = session.getTransaction();
            tx.begin();
            srchTaskDao.update(srchTask, session);
            pmllDao.insert(pmllCands, session);
            tx.commit();
        } catch (Exception e) {
            log.error("Update srchtask and pmll cand error.", e);
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            HibernateSessionFactoryUtil.closeSession();
        }
    }
}

package hbie2.HAFPIS2.Service.Impl;

import hbie2.Candidate;
import hbie2.HAFPIS2.Dao.HafpisPmttCandDao;
import hbie2.HAFPIS2.Dao.HafpisSrchTaskDao;
import hbie2.HAFPIS2.Entity.HafpisPmttCand;
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
public class HafpisPMTTService extends AbstractService implements Runnable {
    private Logger log = LoggerFactory.getLogger(HafpisPMTTService.class);
    private HafpisSrchTaskDao srchTaskDao;
    private HafpisPmttCandDao pmttDao;
    private ArrayBlockingQueue<HafpisSrchTask> srchTaskQueue;
    private int PMTT_Threshold;

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
            this.PMTT_Threshold = Integer.parseInt(cfg.getProperty("PMTT_Threshold", "0"));
        } catch (NumberFormatException e) {
            log.error("PMTT_Threshold: {} config error, must be an Integer. Use default value: 0", cfg.getProperty("PMTT_Threshold"));
            this.PMTT_Threshold = 0;
        }

        try {
            this.thread_num = Integer.parseInt(ConfigUtils.getConfigOrDefault("fourpalm_thread_num", "1"));
        } catch (NumberFormatException e) {
            log.error("threadnum: {} config error, must be an Integer. Use default value: 1", ConfigUtils.getConfig("fourpalm_thread_num"));
            this.thread_num = 1;
        }
        srchTaskDao = new HafpisSrchTaskDao();
        pmttDao = new HafpisPmttCandDao();
        srchTaskQueue = new ArrayBlockingQueue<>(CONSTANTS.PPTT_LIMIT);
    }

    @Override
    public <T> void doWork(List<T> list) {
        HafpisSrchTask srchTask = (HafpisSrchTask) list.get(0);
        SrchDataBean srchDataBean = srchTask.getSrchDataBeans().get(0);
        if (srchDataBean.palmmntnum == 0) {
            log.error("palm mnt is null. taskidd: {}", srchTask.getTaskidd());
            srchTask.setStatus(CONSTANTS.ERROR_STATUS);
            srchTask.setExptmsg("palm mnt is null");
//            srchTaskDao.update(srchTask);
            updateSrchOnly(srchTask);
        } else {
            if (HbieUtils.getInstance().hbie_PP != null) {
                try {
                    Map<String, HafpisPmttCand> pmttCandMap = new HashMap<>();
                    TaskSearch taskSearch = new TaskSearch();
                    int numOfCand = srchTask.getNumofcand();
                    numOfCand = numOfCand > 0 ? (int) (numOfCand * 1.5) : CONSTANTS.MAXCANDS;
                    String taskidd = srchTask.getTaskidd();
                    taskSearch.setId(srchTask.getProbeid());
                    taskSearch.setType("P2P");
                    taskSearch.setMaxCandidateNum(numOfCand);
                    taskSearch.setScoreThreshold(PMTT_Threshold);

                    //set filters
                    String dbsFilter = CommonUtils.getDbsFilter(srchTask.getSrchdbsmask());
                    String solveOrDupFilter = CommonUtils.getSolveOrDupFilter(CONSTANTS.DBOP_TPP, srchTask.getSolveordup());
                    String demoFilter = CommonUtils.getDemoFilter(srchTask.getDemofilter());
                    taskSearch.setFilter(CommonUtils.mergeFilters(dbsFilter, solveOrDupFilter, demoFilter));
                    log.debug("Total filter is {}", taskSearch.getFilter());

                    String uid;

                    taskSearch.setFeatures(srchDataBean.rpmnt);
                    if (HbieUtils.getInstance().hbie_PP != null) {
                        uid = HbieUtils.getInstance().hbie_PP.submitSearch(taskSearch);
                        while (true) {
                            TaskSearch task = HbieUtils.getInstance().hbie_PP.querySearch(uid);
                            if (task == null) {
                                log.error("PMTT: Impossible. taskidd: {}, uid: {}", taskidd, uid);
                                srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                                updateSrchOnly(srchTask);
                                break;
                            } else if (task.getStatus() == TaskSearch.Status.Error) {
                                log.error("PMTT rpmnt search error. taskidd:{}, uid:{}", taskidd, uid);
                                srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                                srchTask.setExptmsg(task.getMsg());
                                updateSrchOnly(srchTask);
                                break;
                            } else if (task.getStatus() != TaskSearch.Status.Done) {
                                CommonUtils.sleep(100);
                                continue;
                            }
                            List<Candidate> candidates = task.getCandidates();
                            for (int i = 0; i < candidates.size(); i++) {
                                HafpisPmttCand pmttCand = new HafpisPmttCand();
                                Candidate cand = candidates.get(i);
                                pmttCand.getKeys().setTaskidd(taskidd);
                                pmttCand.getKeys().setCandid(cand.getId());
                                pmttCand.setTransno(srchTask.getTransno());
                                pmttCand.setProbeid(srchTask.getProbeid());
                                pmttCand.setDbid((Integer) cand.getFields().get("dbid"));
                                pmttCand.setScore(cand.getScore());
                                int[] subScores = cand.getSubScores();
                                if (subScores != null) {
                                    pmttCand.setScore01(subScores[0]);
                                    pmttCand.setScore05(subScores[2]);
                                    pmttCand.setScore06(subScores[1]);
                                    pmttCand.setScore10(subScores[3]);
                                }
                                pmttCandMap.put(cand.getId(), pmttCand);
                            }
                            break;
                        }
                    } else {
                        log.error("Get HBIE client null. Suspenging until HBIE is started..");
                        log.info("waiting PMTT client...");
                        srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                        updateSrchOnly(srchTask);
                        CommonUtils.sleep(interval * 1000);
                    }


                    //insert result
                    log.debug("begin insert into result table");
                    if (pmttCandMap.size() == 0) {
                        log.info("PMTT search finish. No results for taskidd: {}", taskidd);
                        srchTask.setStatus(CONSTANTS.FINISH_NOMATCH_STATUS);
                        srchTask.setExptmsg("No result");
                        updateSrchOnly(srchTask);
                    } else {
                        List<HafpisPmttCand> pmttCands = new ArrayList<>();
                        pmttCands.addAll(pmttCandMap.values());
                        if (pmttCands.size() > numOfCand) {
                            pmttCands = CommonUtils.getLimitedList(pmttCands, numOfCand);
                        } else {
                            Collections.sort(pmttCands);
                        }
                        // rank
                        for (int i = 0; i < pmttCands.size(); i++) {
                            pmttCands.get(i).setCandrank(i + 1);
                        }
                        log.info("Inserting....");
                        srchTask.setStatus(CONSTANTS.FINISH_STATUS);
                        updateSrchAndPMTT(srchTask, pmttCands);
                        log.info("srchtask {} srch finish", taskidd);
                    }
                } catch (Exception e) {
                    log.error("Impossiable", e);
                }
            } else {
                log.error("Get HBIE client null. Suspending until HBIE is started.");
                log.info("waiting PMTT client..");
                srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                updateSrchOnly(srchTask);
                CommonUtils.sleep(interval * 1000);
            }
        }
    }

    @Override
    public void run() {
        //add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            srchTaskDao.updateStatus(CONSTANTS.SRCH_DATATYPE_PP, CONSTANTS.SRCH_TASKTYPE_TT);
        }));

        log.info("PMTT service start. Update status first...");
        srchTaskDao.updateStatus(CONSTANTS.SRCH_DATATYPE_PP, CONSTANTS.SRCH_TASKTYPE_TT);

        //Take SrchTask from db
        new Thread(() -> {
            log.info("PMTT_SRCHTASKQUEUE_THREAD start...");
            while (true) {
                List<HafpisSrchTask> list = srchTaskDao.getSrchTasks(CONSTANTS.URGENT_STATUS, CONSTANTS.SRCH_DATATYPE_PP,
                        CONSTANTS.SRCH_TASKTYPE_TT, querynum);
                if (null == list || list.size() == 0) {
                    list = srchTaskDao.getSrchTasks(CONSTANTS.WAIT_STATUS, CONSTANTS.SRCH_DATATYPE_PP,
                            CONSTANTS.SRCH_TASKTYPE_TT, querynum);
                    if (null == list || list.size() == 0) {
                        CommonUtils.sleep(interval * 1000);
                    } else {
                        for (HafpisSrchTask srchTask : list) {
                            try {
                                srchTaskQueue.put(srchTask);
                                srchTask.setStatus(CONSTANTS.PROCESSING_STATUS);
                                updateSrchOnly(srchTask);
                            } catch (InterruptedException e) {
                                log.error("PMTT: put {] into srchtask queue error.", srchTask.getTaskidd());
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
                            log.error("PMTT: Put urgent {} into srchtask queue error.",srchTask.getTaskidd(), e);
                        }
                    }
                }
            }
        }, "PMTT_SRCHTASKQUEUE_THREAD").start();

        for (int i=0; i<this.thread_num; i++) {
            new Thread(this::PMTT, "PMTT_SEARCH_THREAD").start();
        }
    }

    private void PMTT() {
        log.info("PMTT_SEARCH_THREAD start ...");
        while (true) {
            HafpisSrchTask srchTask = null;
            try {
                srchTask = srchTaskQueue.take();
                log.debug("take one srchtask");
                if (srchTask.getSrchdata() == null || srchTask.getSrchdata().length == 0) {
                    log.error("SrchTask {} srchdata is null.");
                    srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                    srchTask.setExptmsg("Srchdata is null");
                    updateSrchOnly(srchTask);
                } else {
                    log.info("get srchtask: {}", srchTask.getTaskidd());
                    CommonUtils.convert(srchTask);
                    if (CommonUtils.check(srchTask)) {
                        log.error("Convert srchdata error. taskidd: {}", srchTask.getTaskidd());
                        srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                        srchTask.setExptmsg("Convert srchdata error");
                        updateSrchOnly(srchTask);
                    } else {
                        List<HafpisSrchTask> list = new ArrayList<>();
                        list.add(srchTask);
                        doWork(list);
                    }
                }
            } catch (InterruptedException e) {
                log.error("Take srchtask from queue error.", e);
            }
        }
    }

    private void updateSrchOnly(HafpisSrchTask srchTask) {
        Session session = HibernateSessionFactoryUtil.getSession();
        Transaction tx = null;
        try {
            tx = session.getTransaction();
            tx.begin();
            srchTaskDao.update(srchTask, session);
            tx.commit();
        } catch (Exception e) {
            log.error("update srchtask error.", e);
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            HibernateSessionFactoryUtil.closeSession();
        }
    }

    private void updateSrchAndPMTT(HafpisSrchTask srchTask, List<HafpisPmttCand> pmttCands) {
        Transaction tx = null;
        Session session = HibernateSessionFactoryUtil.getSession();
        try {
            tx = session.getTransaction();
            tx.begin();
            srchTaskDao.update(srchTask, session);
            pmttDao.insert(pmttCands, session);
            tx.commit();
        } catch (Exception e) {
            log.error("update srch and pmtt error.", e);
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            HibernateSessionFactoryUtil.closeSession();;
        }
    }
}

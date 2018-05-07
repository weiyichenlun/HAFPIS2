package hbie2.HAFPIS2.Service.Impl;

import hbie2.Candidate;
import hbie2.HAFPIS2.Dao.HafpisPmtlCandDao;
import hbie2.HAFPIS2.Dao.HafpisSrchTaskDao;
import hbie2.HAFPIS2.Entity.HafpisPmtlCand;
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
public class HafpisPMTLService extends AbstractService implements Runnable {
    private Logger log = LoggerFactory.getLogger(HafpisPMTLService.class);
    private HafpisSrchTaskDao srchTaskDao;
    private HafpisPmtlCandDao pmtlDao;
    private ArrayBlockingQueue<HafpisSrchTask> srchTaskQueue;
    private int PMTL_Threshold;

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
            this.PMTL_Threshold = Integer.parseInt(cfg.getProperty("PMTL_Threshold", "0"));
        } catch (NumberFormatException e) {
            log.error("PMTL_Threshold: {} config error, must be an Integer. Use default value: 0", cfg.getProperty("PMTL_Threshold"));
            this.PMTL_Threshold = 0;
        }

        try {
            this.thread_num = Integer.parseInt(ConfigUtils.getConfigOrDefault("latpalm_thread_num", "1"));
        } catch (NumberFormatException e) {
            log.error("threadnum: {} config error, must be an Integer. Use default value: 1", ConfigUtils.getConfig("latpalm_thread_num"));
            this.thread_num = 1;
        }

        srchTaskDao = new HafpisSrchTaskDao();
        pmtlDao = new HafpisPmtlCandDao();
        srchTaskQueue = new ArrayBlockingQueue<>(CONSTANTS.PPTL_LIMIT);
    }

    @Override
    public <T> void doWork(List<T> list) {
        HafpisSrchTask srchTask = (HafpisSrchTask) list.get(interval);
        SrchDataBean srchDataBean = srchTask.getSrchDataBeans().get(0);
        String srchPosMask = CommonUtils.checkSrchPosMask(CONSTANTS.SRCH_DATATYPE_PP, srchTask.getSrchposmask());
        String taskidd = srchTask.getTaskidd();
        int[] ppPosMask = new int[4];
        int numOf1 = 0;
        int avgCand = srchTask.getAveragecand();

        if (srchDataBean.palmmntnum == 0) {
            log.error("palm mnt is null. taskidd {}", taskidd);
            srchTask.setStatus(CONSTANTS.ERROR_STATUS);
            srchTask.setExptmsg("palm mnt is null");
            updateSrchOnly(srchTask);
        } else {
            for (int i = 0; i < 4; i++) {
                if (srchPosMask.charAt(CONSTANTS.srchOrder[i]) == '1' && srchDataBean.PalmMntLen[i] != 0) {
                    ppPosMask[CONSTANTS.feaOrder[i]] = 1;
                    numOf1 ++;
                }
            }

            if (HbieUtils.getInstance().hbie_PLP != null) {
                try {
                    Map<String, HafpisPmtlCand> pmtlCandMap = new HashMap<>();
                    List<HafpisPmtlCand> rest = new ArrayList<>();
                    TaskSearch taskSearch = new TaskSearch();
                    int numOfCand = srchTask.getNumofcand();
                    numOfCand = numOfCand > 0 ? (int) (numOfCand * 1.5) : CONSTANTS.MAXCANDS;
                    taskSearch.setId(taskidd);
                    taskSearch.setType("P2L");
                    taskSearch.setScoreThreshold(PMTL_Threshold);
                    // calculate the average number of candidates on condition of avgcand = 1
                    if (numOf1 == 0) {
                        numOf1 = 1;
                    }
                    int avgCandNum = numOfCand / numOf1;
                    if ((numOfCand % numOf1) > (avgCandNum / 2)) {
                        avgCandNum += 1;
                    }

                    //set filter
                    String dbsFilter = CommonUtils.getDbsFilter(srchTask.getSrchdbsmask());
                    String solveOrDupFilter = CommonUtils.getSolveOrDupFilter(CONSTANTS.DBOP_PLP, srchTask.getSolveordup());
                    String demoFilter = CommonUtils.getDemoFilter(srchTask.getDemofilter());
                    String filter = CommonUtils.mergeFilters(dbsFilter, solveOrDupFilter, demoFilter);
                    taskSearch.setFilter(filter);
                    log.debug("Total filter is {}", filter);

                    if (avgCand == 1) {
                        for (int i = 0; i < 4; i++) {
                            if (ppPosMask[i] == 1) {
                                taskSearch.setFeature(srchDataBean.palmmnt[i]);

                                String uid = HbieUtils.getInstance().hbie_PLP.submitSearch(taskSearch);
                                while (true) {
                                    TaskSearch task = HbieUtils.getInstance().hbie_PLP.querySearch(uid);
                                    if (task == null) {
                                        log.warn("PMTL: Impossible. taskidd {}, uid {}", taskidd, uid);
                                        srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                                        updateSrchOnly(srchTask);
                                        break;
                                    } else if (task.getStatus() == TaskSearch.Status.Error) {
                                        log.error("PMTL search error. taskidd {} uid {}", taskidd, uid);
                                        srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                                        srchTask.setExptmsg(task.getMsg());
                                        updateSrchOnly(srchTask);
                                        break;
                                    } else if (task.getStatus() != TaskSearch.Status.Done) {
                                        CommonUtils.sleep(10);
                                        continue;
                                    }
                                    List<Candidate> candidates = task.getCandidates();
                                    for (int j = 0; j < candidates.size(); j++) {
                                        Candidate candidate = candidates.get(i);
                                        HafpisPmtlCand pmtlCand = new HafpisPmtlCand();
                                        pmtlCand.getKeys().setTaskidd(taskidd);
                                        pmtlCand.getKeys().setCandid(candidate.getId());
                                        pmtlCand.getKeys().setPosition(CONSTANTS.srchOrder[i] + 1);
                                        pmtlCand.setTransno(srchTask.getTransno());
                                        pmtlCand.setProbeid(srchTask.getProbeid());
                                        pmtlCand.setDbid((Integer) candidate.getFields().get("dbid"));
                                        pmtlCand.setScore(candidate.getScore());
                                        if (j < avgCandNum) {
                                            String key = candidate.getId() + String.valueOf(CONSTANTS.srchOrder[i] + 1);
                                            pmtlCandMap.put(key, pmtlCand);
                                        } else {
                                            rest.add(pmtlCand);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    } else {
                        taskSearch.setFeatures(srchDataBean.palmmnt);

                        String uid = HbieUtils.getInstance().hbie_PLP.submitSearch(taskSearch);
                        while (true) {
                            TaskSearch task = HbieUtils.getInstance().hbie_PLP.querySearch(uid);
                            if (task == null) {
                                log.warn("PMTL: Impossible. taskidd {} uid {}", taskidd, uid);
                                srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                                updateSrchOnly(srchTask);
                                break;
                            } else if (task.getStatus() == TaskSearch.Status.Error) {
                                log.error("PMTL search error. taskidd {], uid {}", taskidd, uid);
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
                                HafpisPmtlCand pmtlCand = new HafpisPmtlCand();
                                pmtlCand.getKeys().setTaskidd(taskidd);
                                pmtlCand.getKeys().setCandid(candidate.getId());
                                pmtlCand.getKeys().setPosition(1);
                                pmtlCand.setTransno(srchTask.getTransno());
                                pmtlCand.setProbeid(srchTask.getProbeid());
                                pmtlCand.setDbid((Integer) candidate.getFields().get("dbid"));
                                pmtlCand.setScore(candidate.getScore());

                                pmtlCandMap.put(candidate.getId(), pmtlCand);
                            }
                            break;
                        }
                    }
                    //insert into db
                    log.debug("begin to insert into table");
                    if (pmtlCandMap.size() == 0) {
                        log.info("PMTL search finish. No results for taskidd {}", taskidd);
                        srchTask.setStatus(CONSTANTS.FINISH_NOMATCH_STATUS);
                        srchTask.setExptmsg("No results");
                        updateSrchOnly(srchTask);
                    } else {
                        List<HafpisPmtlCand> pmtlCands = new ArrayList<>();
                        List<HafpisPmtlCand> pmtlCandsRest = new ArrayList<>();
                        pmtlCands.addAll(pmtlCandMap.values());
                        if (pmtlCands.size() >= numOfCand) {
                            pmtlCands = CommonUtils.getLimitedList(pmtlCands, numOfCand);
                        } else {
                            pmtlCandsRest = CommonUtils.getLimitedList(rest, numOfCand - pmtlCands.size());
                            pmtlCands.addAll(pmtlCandsRest);
                            Collections.sort(pmtlCands);
                        }

                        //rank
                        for (int i = 0; i < pmtlCands.size(); i++) {
                            pmtlCands.get(i).setCandrank(i + 1);
                        }
                        log.debug("Inserting...");
                        srchTask.setStatus(CONSTANTS.FINISH_STATUS);
                        updateSrchAndPMTL(srchTask, pmtlCands);
                        log.info("srchtask {} finish", taskidd);
                    }
                } catch (Exception e) {
                    log.error("Impossible. ", e);
                    srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                    updateSrchOnly(srchTask);
                }
            } else {
                log.error("Get HBIE client null. Suspending until HBIE is started.");
                log.info("waiting PMTL client..");
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
            srchTaskDao.updateStatus(CONSTANTS.SRCH_DATATYPE_PP, CONSTANTS.SRCH_TASKTYPE_TL);
            log.error("PMTL is shutting down");
        }));

        log.info("PMTL service start. Update status first..");
        srchTaskDao.updateStatus(CONSTANTS.SRCH_DATATYPE_TP, CONSTANTS.SRCH_TASKTYPE_TL);

        //Take SrchTask from db
        new Thread(() ->{
            log.info("PMTL_SRCHTASKQUEUE_THREAD start...");
            while (true) {
                List<HafpisSrchTask> list = srchTaskDao.getSrchTasks(CONSTANTS.URGENT_STATUS, CONSTANTS.SRCH_DATATYPE_TP,
                        CONSTANTS.SRCH_TASKTYPE_TL, querynum);
                if (null == list || list.size() == 0) {
                    list = srchTaskDao.getSrchTasks(CONSTANTS.WAIT_STATUS, CONSTANTS.SRCH_DATATYPE_TP,
                            CONSTANTS.SRCH_TASKTYPE_TL, querynum);
                    if (null == list || list.size() == 0) {
                        CommonUtils.sleep(interval * 1000);
                    } else {
                        for (HafpisSrchTask srchTask : list) {
                            try {
                                srchTaskQueue.put(srchTask);
                                srchTask.setStatus(CONSTANTS.PROCESSING_STATUS);
                                updateSrchOnly(srchTask);
                            } catch (InterruptedException e) {
                                log.error("PMTL: put {} into srchtask queue error.", srchTask.getTaskidd());
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
                            log.error("PMTL: put urgent {} into srchtask queue error.", srchTask.getTaskidd());
                        }
                    }
                }
            }
        }, "PMTL_SRCHTASKQUEUE_THREAD").start();

        for (int i = 0; i < this.thread_num; i++) {
            new Thread(this::PMTL, "PMTL_SEARCH_THREAD").start();
        } 
    }

    private void PMTL() {
        log.debug("PMTL_SEARCH_THREAD start...");
        while (true) {
            HafpisSrchTask srchTask = null;
            try {
                srchTask = srchTaskQueue.take();
                if (srchTask.getSrchdata() == null || srchTask.getSrchdata().length == 0) {
                    log.error("Srchtask {} srchdata is null", srchTask.getTaskidd());
                    srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                    srchTask.setExptmsg("srchdata is null");
                    updateSrchOnly(srchTask);
                } else {
                    log.info("get srchtask {}", srchTask.getTaskidd());
                    CommonUtils.convert(srchTask);
                    if (CommonUtils.check(srchTask)) {
                        log.error("Convert srchdata error. taskidd {}", srchTask.getTaskidd());
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
                log.error("Take srchtask from queue error.", e);
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

    private void updateSrchAndPMTL(HafpisSrchTask srchTask, List<HafpisPmtlCand> pmtlCands) {
        Transaction tx = null;
        Session session = HibernateSessionFactoryUtil.getSession();
        try {
            tx = session.getTransaction();
            tx.begin();
            srchTaskDao.update(srchTask, session);
            pmtlDao.insert(pmtlCands, session);
            tx.commit();
        } catch (Exception e) {
            log.error("Update srchtask and pmtl cand error.", e);
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            HibernateSessionFactoryUtil.closeSession();
        }
    }
}

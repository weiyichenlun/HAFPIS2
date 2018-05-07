package hbie2.HAFPIS2.Service.Impl;

import hbie2.Candidate;
import hbie2.HAFPIS2.Dao.HafpisPmltCandDao;
import hbie2.HAFPIS2.Dao.HafpisSrchTaskDao;
import hbie2.HAFPIS2.Entity.HafpisPmltCand;
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
public class HafpisPMLTService extends AbstractService implements Runnable {
    private Logger log = LoggerFactory.getLogger(HafpisPMLTService.class);
    private HafpisSrchTaskDao srchTaskDao;
    private HafpisPmltCandDao pmltDao;
    private ArrayBlockingQueue<HafpisSrchTask> srchTaskQueue;
    private int PMLT_Threshold;

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
            this.PMLT_Threshold = Integer.parseInt(cfg.getProperty("PMLT_Threshold", "0"));
        } catch (NumberFormatException e) {
            log.error("PMLT_Threshold: {} config error, must be an Integer. Use default value: 0", cfg.getProperty("PMLT_Threshold"));
            this.PMLT_Threshold = 0;
        }

        try {
            this.thread_num = Integer.parseInt(ConfigUtils.getConfigOrDefault("fourpalm_thread_num", "1"));
        } catch (NumberFormatException e) {
            log.error("threadnum: {} config error, must be an Integer. Use default value: 1", ConfigUtils.getConfig("fourpalm_thread_num"));
            this.thread_num = 1;
        }

        srchTaskDao = new HafpisSrchTaskDao();
        pmltDao = new HafpisPmltCandDao();
        srchTaskQueue = new ArrayBlockingQueue<>(CONSTANTS.PPLT_LIMIT);
    }

    @Override
    public <T> void doWork(List<T> list) {
        HafpisSrchTask srchTask = (HafpisSrchTask) list.get(0);
        SrchDataBean srchDataBean = srchTask.getSrchDataBeans().get(0);
        String srchPosMask = CommonUtils.checkSrchPosMask(CONSTANTS.SRCH_DATATYPE_PP, srchTask.getSrchposmask());
        int[] ppPosMask = new int[4];
        int numOf1 = 0;
        for (int i = 0; i < 4; i++) {
            if (srchPosMask.charAt(CONSTANTS.srchOrder[i]) == '1') {
                ppPosMask[CONSTANTS.feaOrder[i]] = 1;
                numOf1 ++;
            }
        }
        int avgCand = srchTask.getAveragecand();
        if (srchDataBean.latpalmmnt == null || srchDataBean.latpalmmnt.length == 0) {
            log.error("latpalmmnt is null. taskidd: {}", srchTask.getTaskidd());
            srchTask.setStatus(CONSTANTS.ERROR_STATUS);
            srchTask.setExptmsg("latpalm mnt is null");
            updateSrchOnly(srchTask);
        } else {
            if (HbieUtils.getInstance().hbie_PP != null) {
                try {
                    Map<String, HafpisPmltCand> pmltCandMap = new HashMap<>();
                    List<HafpisPmltCand> rest = new ArrayList<>();
                    TaskSearch taskSearch = new TaskSearch();
                    int numOfCand = srchTask.getNumofcand();
                    numOfCand = numOfCand > 0 ? (int) (numOfCand * 1.5) : CONSTANTS.MAXCANDS;
                    String taskidd = srchTask.getTaskidd();
                    taskSearch.setId(taskidd);
                    taskSearch.setType("L2P");
                    taskSearch.setFeature(srchDataBean.latpalmmnt);
                    taskSearch.setScoreThreshold(PMLT_Threshold);

                    int avgCandNum = numOfCand / numOf1; //numOf1 can nerver be 0 cause CommonUtils.srchPosMask method
                    if ((numOfCand % numOf1) > (avgCandNum / 2)) {
                        avgCandNum += 1;
                    }

                    //set filters
                    String dbsFilter = CommonUtils.getDbsFilter(srchTask.getSrchdbsmask());
                    String solveOrDupFilter = CommonUtils.getSolveOrDupFilter(CONSTANTS.DBOP_PLP, srchTask.getSolveordup());
                    String demoFilter = CommonUtils.getDemoFilter(srchTask.getDemofilter());
                    String filter = CommonUtils.mergeFilters(dbsFilter, solveOrDupFilter, demoFilter);
                    taskSearch.setFilter(filter);
                    log.debug("total filter is {}", filter);

                    //init pp_mask
                    String ppMask = "pp_mask=0000";
                    if (avgCand == 1) {
                        for (int i = 0; i < 4; i++) {
                            if (ppPosMask[i] == 1) {
                                String ppMaskNew = getPpMask(ppMask, i);
                                log.debug("new pp_mask is {}", ppMaskNew);
                                taskSearch.setCfg(ppMaskNew);

                                String uid = HbieUtils.getInstance().hbie_PP.submitSearch(taskSearch);
                                while (true) {
                                    TaskSearch task = HbieUtils.getInstance().hbie_PP.querySearch(uid);
                                    if (task == null) {
                                        log.error("PPLT: Impossible. taskidd: {}, uid: {}", taskidd, uid);
                                        srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                                        updateSrchOnly(srchTask);
                                        break;
                                    } else if (task.getStatus() == TaskSearch.Status.Error) {
                                        log.error("PPLT search error. taskidd: {}, uid: {}", taskidd, uid);
                                        srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                                        srchTask.setExptmsg(task.getMsg());
                                        updateSrchOnly(srchTask);
                                        break;
                                    } else if (task.getStatus() != TaskSearch.Status.Done) {
                                        CommonUtils.sleep(100);
                                        continue;
                                    }
                                    List<Candidate> candidatess = task.getCandidates();
                                    for (int j = 0; j < candidatess.size(); j++) {
                                        Candidate candidate = candidatess.get(i);
                                        HafpisPmltCand pmltCand = new HafpisPmltCand();
                                        pmltCand.getKeys().setTaskidd(taskidd);
                                        pmltCand.getKeys().setCandid(candidate.getId());
                                        pmltCand.getKeys().setPosition(CONSTANTS.oriOrder[i] + 1);
                                        pmltCand.setTransno(srchTask.getTransno());
                                        pmltCand.setProbeid(srchTask.getProbeid());
                                        pmltCand.setDbid((Integer) candidate.getFields().get("dbid"));
                                        pmltCand.setScore(candidate.getScore());
                                        if (j < avgCandNum) {
                                            pmltCandMap.put(candidate.getId(), pmltCand);
                                        } else {
                                            rest.add(pmltCand);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    } else {
                        String ppMaskNew = getPpMask(ppMask, ppPosMask);
                        log.debug("ppMaskNew is {}", ppMaskNew);
                        taskSearch.setCfg(ppMaskNew);

                        String uid = HbieUtils.getInstance().hbie_PP.submitSearch(taskSearch);
                        while (true) {
                            TaskSearch task = HbieUtils.getInstance().hbie_PP.querySearch(uid);
                            if (task == null) {
                                log.error("PmLT: Impossible. taskidd: {}, uid: {}", taskidd, uid);
                                srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                                updateSrchOnly(srchTask);
                                break;
                            } else if (task.getStatus() == TaskSearch.Status.Error) {
                                log.error("PPLM search error. taskidd: {}, uid: {}", taskidd, uid);
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
                                Candidate candidate = candidates.get(i);
                                HafpisPmltCand pmltCand = new HafpisPmltCand();
                                pmltCand.getKeys().setTaskidd(taskidd);
                                pmltCand.getKeys().setCandid(candidate.getId());
                                pmltCand.getKeys().setPosition(candidate.getFp()); //TODO 掌位待确定
                                pmltCand.setTransno(srchTask.getTransno());
                                pmltCand.setProbeid(srchTask.getProbeid());
                                pmltCand.setDbid((Integer) candidate.getFields().get("dbid"));
                                pmltCand.setScore(candidate.getScore());

                                String key = candidate.getId() + candidate.getFp();
                                pmltCandMap.put(key, pmltCand);
                            }
                            break;
                        }
                    }
                    //insert into db
                    log.debug("begin to insert into table");
                    if (pmltCandMap.size() == 0) {
                        log.info("PMLT search finish: Noresults for taskidd {}", taskidd);
                        srchTask.setStatus(CONSTANTS.FINISH_NOMATCH_STATUS);
                        srchTask.setExptmsg("No results");
                        updateSrchOnly(srchTask);
                    } else {
                        List<HafpisPmltCand> pmltCands = new ArrayList<>();
                        List<HafpisPmltCand> pmltCandsRest = new ArrayList<>();
                        pmltCands.addAll(pmltCandMap.values());
                        if (pmltCands.size() >= numOfCand) {
                            pmltCands = CommonUtils.getLimitedList(pmltCands, numOfCand);
                        } else {
                            pmltCandsRest = CommonUtils.getLimitedList(rest, numOfCand - pmltCands.size());
                            pmltCands.addAll(pmltCandsRest);
                            Collections.sort(pmltCands);
                        }

                        //rank
                        for (int i = 0; i < pmltCands.size(); i++) {
                            pmltCands.get(i).setCandrank(i + 1);
                        }
                        log.debug("Inserting...");
                        srchTask.setStatus(CONSTANTS.FINISH_STATUS);
                        updateSrchAndPMLT(srchTask, pmltCands);
                        log.info("srchtask {} finish", taskidd);
                    }
                } catch (Exception e) {
                    log.error("Impossible ", e);
                    srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                    updateSrchOnly(srchTask);
                }
            } else {
                log.error("Get HBIE client null. Suspending until HBIE is started.");
                log.info("waiting PMLT client...");
                srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                updateSrchOnly(srchTask);
                CommonUtils.sleep(interval * 1999);
            }
        }
    }

    @Override
    public void run() {
        //add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            srchTaskDao.updateStatus(CONSTANTS.SRCH_DATATYPE_PLP, CONSTANTS.SRCH_TASKTYPE_LT);
            log.info("PMLT is shutting down");
        }));

        log.info("PMLT service start. Update status first...");
        srchTaskDao.updateStatus(CONSTANTS.SRCH_DATATYPE_PLP, CONSTANTS.SRCH_TASKTYPE_LT);

        //Take SrchTask from db
        new Thread(()->{
            log.info("PMLT_SRCHTASKQUEUE_THREAD start...");
            while (true) {
                List<HafpisSrchTask> list = srchTaskDao.getSrchTasks(CONSTANTS.URGENT_STATUS, CONSTANTS.SRCH_DATATYPE_PLP,
                        CONSTANTS.SRCH_TASKTYPE_LT, querynum);
                if (null == list || list.size() == 0) {
                    list = srchTaskDao.getSrchTasks(CONSTANTS.WAIT_STATUS, CONSTANTS.SRCH_DATATYPE_PLP, CONSTANTS.SRCH_TASKTYPE_LT, querynum);
                    if (null == list || list.size() == 0) {
                        CommonUtils.sleep(interval * 1000);
                    } else {
                        for (HafpisSrchTask srchTask : list) {
                            try {
                                srchTaskQueue.put(srchTask);
                                srchTask.setStatus(CONSTANTS.PROCESSING_STATUS);
                                updateSrchOnly(srchTask);
                            } catch (InterruptedException e) {
                                log.error("PMLT: put {} into srchtask queue error.", srchTask.getTaskidd());
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
                            log.error("PMLT: put urgent {} into srchtask queue error.", srchTask.getTaskidd());
                        }
                    }
                }
            }
        }, "PMLT_SRCHTASKQUEUE_THREAD").start();

        for (int i = 0; i < this.thread_num; i++) {
            new Thread(this::PMLT, "PMLT_SRCH_THREAD").start();
        }
    }

    private void PMLT() {
        log.debug("PMLT_SEARCH_THREAD start...");
        while (true) {
            HafpisSrchTask srchTask = null;
            try {
                srchTask = srchTaskQueue.take();
                log.debug("take one srchtask");
                if (srchTask.getSrchdata() == null || srchTask.getSrchdata().length == 0) {
                    log.error("SrchTask {} srchdata is null.", srchTask.getTaskidd());
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
                log.error("Take srchtask from queue error. ", e);
            }
        }
    }

    private String getPpMask(String oriPpMask, int idx) {
        if (idx < 0 || idx > 3) {
            log.error("Pp_mask idx-{} error, shoube in range [0, 3]", idx);
            return oriPpMask;
        } else {
            char[] ppMaskChars = oriPpMask.toCharArray();
            ppMaskChars[8 + idx] = '1'; //the first 8 chars = pp_mask=
            return new String(ppMaskChars);
        }
    }

    private String getPpMask(String oriPpMask, int[] flags) {
        if (flags == null || flags.length == 0) {
            return oriPpMask;
        }
        char[] ppMaskChars = oriPpMask.toCharArray();
        for (int i = 0; i < flags.length; i++) {
            if (flags[i] == 1) {
                ppMaskChars[8 + i] = '1';//the first 8 chars = pp_mask=
            }
        }
        return new String(ppMaskChars);
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

    private void updateSrchAndPMLT(HafpisSrchTask srchTask, List<HafpisPmltCand> pmltCnads) {
        Transaction tx = null;
        Session session = HibernateSessionFactoryUtil.getSession();
        try {
            tx = session.getTransaction();
            tx.begin();
            srchTaskDao.update(srchTask, session);
            pmltDao.insert(pmltCnads, session);
            tx.commit();
        } catch (Exception e) {
            log.error("update srch and pmlt error.", e);
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            HibernateSessionFactoryUtil.closeSession();
        }
    }
}

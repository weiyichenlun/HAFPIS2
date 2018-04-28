package hbie2.HAFPIS2.Service.Impl;

import hbie2.Candidate;
import hbie2.HAFPIS2.Dao.HafpisFpltCandDao;
import hbie2.HAFPIS2.Dao.HafpisSrchTaskDao;
import hbie2.HAFPIS2.Entity.HafpisFpltCand;
import hbie2.HAFPIS2.Entity.HafpisSrchTask;
import hbie2.HAFPIS2.Entity.OtherCompositeKeys;
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
 * 创建时间:2018/4/8
 * 最后修改时间:2018/4/8
 */
public class HafpisFPLTService extends AbstractService implements Runnable {
    private Logger log = LoggerFactory.getLogger(HafpisFPLTService.class);
    private HafpisSrchTaskDao srchTaskDao;
    private HafpisFpltCandDao fpltDao;
    private ArrayBlockingQueue<HafpisSrchTask> srchTaskQueue;
    private int FPLT_Threshold;

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
            this.FPLT_Threshold = Integer.parseInt(cfg.getProperty("FPLT_Threshold", "0"));
        } catch (NumberFormatException e) {
            log.error("FPLT_Threshold: {} config error, must be an Integer. Use default value: 0", cfg.getProperty("FPLT_Threshold"));
            this.FPLT_Threshold = 0;
        }

        try {
            this.thread_num = Integer.parseInt(ConfigUtils.getConfigOrDefault("tenfp_thread_num", "1"));
        } catch (NumberFormatException e) {
            log.error("threadnum: {} config error, must be an Integer. Use default value: 1", cfg.getProperty("threadnum"));
            this.thread_num = 1;
        }

        srchTaskDao = new HafpisSrchTaskDao();
        fpltDao = new HafpisFpltCandDao();
        srchTaskQueue = new ArrayBlockingQueue<>(CONSTANTS.FPLT_LIMIT);
    }

    @Override
    public <T> void doWork(List<T> list) {
        HafpisSrchTask srchTask = (HafpisSrchTask) list.get(0);
        SrchDataBean srchDataBean = srchTask.getSrchDataBeans().get(0);
        String srchPosMask = CommonUtils.checkSrchPosMask(CONSTANTS.SRCH_DATATYPE_TP, srchTask.getSrchposmask());
        int[] rpPosMask = new int[10];
        int[] fpPosMask = new int[10];
        int numOf1 = 0;
        for (int i = 0; i < 10; i++) {
            if (srchPosMask.charAt(i) == '1') {
                rpPosMask[i] = 1;
                numOf1++;
            }
            if (srchPosMask.charAt(10 + i) == '1') {
                fpPosMask[i] = 1;
                numOf1++;
            }
        }
        int avgCand = srchTask.getAveragecand();
        if (srchDataBean.latfpmnt == null || srchDataBean.latfpmnt.length == 0) {
            log.error("latfpmnt is null. taskidd: {}", srchTask.getTaskidd());
            srchTask.setStatus(CONSTANTS.ERROR_STATUS);
            srchTask.setExptmsg("LatFp mnt is null");
            updateSrchOnly(srchTask);
//            srchTaskDao.update(srchTask);
        } else {
            if (HbieUtils.getInstance().hbie_FP != null) {
                try {
                    Map<String, HafpisFpltCand> fpltCandMap = new HashMap<>();
                    List<HafpisFpltCand> rest = new ArrayList<>();
                    TaskSearch taskSearch = new TaskSearch();
                    int numOfCand = srchTask.getNumofcand();
                    numOfCand = numOfCand > 0 ? (int) (numOfCand * 1.5) : CONSTANTS.MAXCANDS;
                    String taskidd = srchTask.getTaskidd();
                    taskSearch.setId(taskidd);
                    taskSearch.setType("LT");
                    taskSearch.setFeature(srchDataBean.latfpmnt);
                    taskSearch.setScoreThreshold(FPLT_Threshold);
                    // calculate the average number of candidates on condition of avgCand == 1
                    int avgCandNum = numOfCand / numOf1;
                    if ((numOfCand % numOf1) > (avgCandNum / 2)) {
                        avgCandNum += 1;
                    }

                    //set filters
                    String dbsFilter = CommonUtils.getDbsFilter(srchTask.getSrchdbsmask());
                    String solveOrDupFilter = CommonUtils.getSolveOrDupFilter(CONSTANTS.DBOP_TPP, srchTask.getSolveordup());
                    String demoFilter = CommonUtils.getDemoFilter(srchTask.getDemofilter());

                    // init fp_mask
                    String fpMask = "fpmask=0000000000";
                    if (avgCand == 1) {
                        // rp match
                        for (int i = 0; i < 10; i++) {
                            String filter = CommonUtils.mergeFilters("flag=={0}", dbsFilter, solveOrDupFilter, demoFilter);
                            log.debug("Total filter is {}", filter);
                            if (rpPosMask[i] == 1) {
                                String fpMaskNew = getFpMask(fpMask, i);
                                log.debug("new fp_mask is {}", fpMaskNew);
                                taskSearch.setCfg(fpMaskNew);
                                taskSearch.setFilter(filter);

                                String uid = HbieUtils.getInstance().hbie_FP.submitSearch(taskSearch);
                                while (true) {
                                    TaskSearch task = HbieUtils.getInstance().hbie_FP.querySearch(uid);
                                    if (task == null) {
                                        log.error("FPLT: Impossible. taskidd: {}, uid: {}", taskidd, uid);
                                        srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                                        updateSrchOnly(srchTask);
//                                        srchTaskDao.update(srchTask);
                                        break;
                                    } else if (task.getStatus() == TaskSearch.Status.Error) {
                                        log.error("FPLT search error. taskidd: {}, uid: {}", taskidd, uid);
                                        srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                                        srchTask.setExptmsg(task.getMsg());
                                        updateSrchOnly(srchTask);
//                                        srchTaskDao.update(srchTask);
                                        break;
                                    } else if (task.getStatus() != TaskSearch.Status.Done) {
                                        CommonUtils.sleep(100);
                                        continue;
                                    }
                                    List<Candidate> candidates = task.getCandidates();
                                    for (int j = 0; j < candidates.size(); j++) {
                                        Candidate candidate = candidates.get(j);
                                        HafpisFpltCand fpltCand = new HafpisFpltCand();
                                        OtherCompositeKeys keys = new OtherCompositeKeys();
                                        keys.setTaskidd(taskidd);
                                        keys.setCandid(candidate.getId());
                                        keys.setPosition(candidate.getFp() + 1);
                                        fpltCand.setKeys(keys);
                                        fpltCand.setTransno(srchTask.getTransno());
                                        fpltCand.setProbeid(srchTask.getProbeid());
                                        fpltCand.setDbid((Integer) candidate.getFields().get("dbid"));
                                        fpltCand.setScore(candidate.getScore());
                                        if (j < avgCandNum) {
                                            fpltCandMap.put(candidate.getId(), fpltCand);
                                        } else {
                                            rest.add(fpltCand);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        // fp match
                        for (int i = 0; i < 10; i++) {
                            String filter = CommonUtils.mergeFilters("flag=={1}", dbsFilter, solveOrDupFilter, demoFilter);
                            log.debug("Total filter is {}", filter);
                            if (fpPosMask[i] == 1) {
                                String fpMaskNew = getFpMask(fpMask, i);
                                log.debug("new fp_mask is {}", fpMaskNew);
                                taskSearch.setCfg(fpMaskNew);
                                taskSearch.setFilter(filter);

                                String uid = HbieUtils.getInstance().hbie_FP.submitSearch(taskSearch);
                                while (true) {
                                    TaskSearch task = HbieUtils.getInstance().hbie_FP.querySearch(uid);
                                    if (task == null) {
                                        log.error("FPLT: Impossible. taskidd: {}, uid: {}", taskidd, uid);
                                        srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                                        updateSrchOnly(srchTask);
//                                        srchTaskDao.update(srchTask);
                                        break;
                                    } else if (task.getStatus() == TaskSearch.Status.Error) {
                                        log.error("FPLT search error. taskidd: {}, uid: {}", taskidd, uid);
                                        srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                                        srchTask.setExptmsg(task.getMsg());
                                        updateSrchOnly(srchTask);
//                                        srchTaskDao.update(srchTask);
                                        break;
                                    } else if (task.getStatus() != TaskSearch.Status.Done) {
                                        CommonUtils.sleep(100);
                                        continue;
                                    }
                                    List<Candidate> candidates = task.getCandidates();
                                    for (int j = 0; j < candidates.size(); j++) {
                                        Candidate candidate = candidates.get(j);
                                        String candid = candidate.getId().substring(0, candidate.getId().length() - 1);
                                        HafpisFpltCand fpltCand = fpltCandMap.get(candid);
                                        if (fpltCand == null) {
                                            fpltCand = new HafpisFpltCand();
                                            OtherCompositeKeys keys = new OtherCompositeKeys();
                                            keys.setTaskidd(taskidd);
                                            keys.setCandid(candidate.getId());
                                            keys.setPosition(candidate.getFp() + 11);
                                            fpltCand.setKeys(keys);
                                            fpltCand.setTransno(srchTask.getTransno());
                                            fpltCand.setProbeid(srchTask.getProbeid());
                                            fpltCand.setDbid((Integer) candidate.getFields().get("dbid"));
                                            fpltCand.setScore(candidate.getScore());
                                        } else {
                                            if (candidate.getScore() > fpltCand.getScore()) {
                                                fpltCand.setScore(candidate.getScore());
                                                fpltCand.getKeys().setPosition(candidate.getFp() + 11);
                                            }
                                        }
                                        if (j < avgCandNum) {
                                            fpltCandMap.put(candid, fpltCand);
                                        } else {
                                            rest.add(fpltCand);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    } else {
                        String fpMaskRp = getFpMask(fpMask, rpPosMask);
                        log.debug("fpMaskRp is {}", fpMaskRp);
                        taskSearch.setCfg(fpMaskRp);
                        String filter = CommonUtils.mergeFilters("flag=={0}", dbsFilter, solveOrDupFilter, demoFilter);
                        taskSearch.setFilter(filter);
                        String uid = HbieUtils.getInstance().hbie_FP.submitSearch(taskSearch);
                        while (true) {
                            TaskSearch task = HbieUtils.getInstance().hbie_FP.querySearch(uid);
                            if (task == null) {
                                log.error("FPLT: Impossible. taskidd: {}, uid: {}", taskidd, uid);
                                srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                                updateSrchOnly(srchTask);
//                                srchTaskDao.update(srchTask);
                                break;
                            } else if (task.getStatus() == TaskSearch.Status.Error) {
                                log.error("FPLT search error. taskidd: {}, uid: {}", taskidd, uid);
                                srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                                srchTask.setExptmsg(task.getMsg());
                                updateSrchOnly(srchTask);
//                                srchTaskDao.update(srchTask);
                                break;
                            } else if (task.getStatus() != TaskSearch.Status.Done) {
                                CommonUtils.sleep(100);
                                continue;
                            }
                            List<Candidate> candidates = task.getCandidates();
                            for (int j = 0; j < candidates.size(); j++) {
                                Candidate candidate = candidates.get(j);
                                HafpisFpltCand fpltCand = new HafpisFpltCand();
                                OtherCompositeKeys keys = new OtherCompositeKeys();
                                keys.setTaskidd(taskidd);
                                keys.setCandid(candidate.getId());
                                keys.setPosition(candidate.getFp() + 1);
                                fpltCand.setKeys(keys);
                                fpltCand.setTransno(srchTask.getTransno());
                                fpltCand.setProbeid(srchTask.getProbeid());
                                fpltCand.setDbid((Integer) candidate.getFields().get("dbid"));
                                fpltCand.setScore(candidate.getScore());

                                fpltCandMap.put(candidate.getId(), fpltCand);
                            }
                            break;
                        }

                        String fpMaskFp = getFpMask(fpMask, fpPosMask);
                        log.debug("fpMaskFp is {}", fpMaskFp);
                        taskSearch.setCfg(fpMaskFp);
                        filter = CommonUtils.mergeFilters("flag=={1}", dbsFilter, solveOrDupFilter, demoFilter);
                        taskSearch.setFilter(filter);
                        uid = HbieUtils.getInstance().hbie_FP.submitSearch(taskSearch);
                        while (true) {
                            TaskSearch task = HbieUtils.getInstance().hbie_FP.querySearch(uid);
                            if (task == null) {
                                log.error("FPLT: Impossible. taskidd: {}, uid: {}", taskidd, uid);
                                srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                                updateSrchOnly(srchTask);
//                                srchTaskDao.update(srchTask);
                                break;
                            } else if (task.getStatus() == TaskSearch.Status.Error) {
                                log.error("FPLT search error. taskidd: {}, uid: {}", taskidd, uid);
                                srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                                srchTask.setExptmsg(task.getMsg());
                                updateSrchOnly(srchTask);
//                                srchTaskDao.update(srchTask);
                                break;
                            } else if (task.getStatus() != TaskSearch.Status.Done) {
                                CommonUtils.sleep(100);
                                continue;
                            }
                            List<Candidate> candidates = task.getCandidates();
                            for (int j = 0; j < candidates.size(); j++) {
                                Candidate candidate = candidates.get(j);
                                String candid = candidate.getId().substring(0, candidate.getId().length() - 1);
                                HafpisFpltCand fpltCand = fpltCandMap.get(candid);
                                if (fpltCand == null) {
                                    fpltCand = new HafpisFpltCand();
                                    OtherCompositeKeys keys = new OtherCompositeKeys();
                                    keys.setTaskidd(taskidd);
                                    keys.setCandid(candidate.getId());
                                    keys.setPosition(candidate.getFp() + 11);
                                    fpltCand.setKeys(keys);
                                    fpltCand.setTransno(srchTask.getTransno());
                                    fpltCand.setProbeid(srchTask.getProbeid());
                                    fpltCand.setDbid((Integer) candidate.getFields().get("dbid"));
                                    fpltCand.setScore(candidate.getScore());
                                } else {
                                    if (candidate.getScore() > fpltCand.getScore()) {
                                        fpltCand.setScore(candidate.getScore());
                                        fpltCand.getKeys().setPosition(candidate.getFp() + 11);
                                    }
                                }
                                fpltCandMap.put(candid, fpltCand);
                            }
                            break;
                        }

                    }
                    // insert into db
                    log.debug("begin to insert into table");
                    if (fpltCandMap.size() == 0) {
                        log.info("FPLT search finish. No results for taskidd: {}", taskidd);
                        srchTask.setStatus(CONSTANTS.FINISH_NOMATCH_STATUS);
                        srchTask.setExptmsg("No result");
                        updateSrchOnly(srchTask);
//                        srchTaskDao.update(srchTask);
                    } else {
                        List<HafpisFpltCand> fpltCands = new ArrayList<>();
                        List<HafpisFpltCand> fpltCandsRest = new ArrayList<>();
                        fpltCands.addAll(fpltCandMap.values());
                        if (fpltCands.size() >= numOfCand) {
                            fpltCands = CommonUtils.getLimitedList(fpltCands, numOfCand);
                        } else {
                            fpltCandsRest = CommonUtils.getLimitedList(rest, numOfCand - fpltCands.size());
                            fpltCands.addAll(fpltCandsRest);
                            Collections.sort(fpltCands);
                        }
                        // rank
                        for (int i = 0; i < fpltCands.size(); i++) {
                            fpltCands.get(i).setCandrank(i + 1);
                        }
                        log.debug("Inserting....");
                        srchTask.setStatus(CONSTANTS.FINISH_STATUS);
                        updateSrchAndFPTT(srchTask, fpltCands);
                        log.info("srchtask finish");
//                        fpltDao.delete(taskidd);
//                        fpltDao.insert(fpltCands);
//                        srchTaskDao.update(srchTask);
                    }
                } catch (Exception e) {
                    log.error("Impossiable", e);
                    srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                    updateSrchOnly(srchTask);
//                    srchTaskDao.update(srchTask);
                }
            } else {
                log.error("Get HBIE client null. Suspenging until HBIE is started..");
                log.info("waiting FPLT client...");
                srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                updateSrchOnly(srchTask);
//                srchTaskDao.update(srchTask);
                CommonUtils.sleep(interval * 1000);
            }

        }
    }

    @Override
    public void run() {
        //add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            srchTaskDao.updateStatus(CONSTANTS.SRCH_DATATYPE_TP, CONSTANTS.SRCH_TASKTYPE_LT);
            log.info("FPLT is shutting down");
        }));

        log.info("FPLT service start. Update status first...");
        srchTaskDao.updateStatus(CONSTANTS.SRCH_DATATYPE_TP, CONSTANTS.SRCH_TASKTYPE_LT);

        //Take SrchTask from db
        new Thread(() -> {
            log.info("FPLT_SRCHTASKQUEUE_THREAD start...");
            while (true) {
                List<HafpisSrchTask> list = srchTaskDao.getSrchTasks(CONSTANTS.URGENT_STATUS, CONSTANTS.SRCH_DATATYPE_LPP,
                        CONSTANTS.SRCH_TASKTYPE_LT, querynum);
                if (null == list || list.size() == 0) {
                    list = srchTaskDao.getSrchTasks(CONSTANTS.WAIT_STATUS, CONSTANTS.RECORD_DATATYPE_LPP,
                            CONSTANTS.SRCH_TASKTYPE_LT, querynum);
                    if (null == list || list.size() == 0) {
                        CommonUtils.sleep(interval * 1000);
                    } else {
                        for (HafpisSrchTask srchTask : list) {
                            try {
                                srchTaskQueue.put(srchTask);
                                srchTask.setStatus(CONSTANTS.PROCESSING_STATUS);
                                updateSrchOnly(srchTask);
//                                srchTaskDao.update(srchTask);
                            } catch (InterruptedException e) {
                                log.error("FPLT: Put {} into srchtask queue error.",srchTask.getTaskidd(), e);
                            }
                        }
                    }
                } else {
                    for (HafpisSrchTask srchTask : list) {
                        try {
                            srchTaskQueue.put(srchTask);
                            srchTask.setStatus(CONSTANTS.PROCESSING_STATUS);
                            updateSrchOnly(srchTask);
//                            srchTaskDao.update(srchTask);
                        } catch (InterruptedException e) {
                            log.error("FPLT: put urgent{} into srchtask queue error.",srchTask.getTaskidd(), e);
                        }
                    }
                }
            }
        }, "FPLT_SRCHTASKQUEUE_THREAD").start();

        for (int i = 0; i < this.thread_num; i++) {
            new Thread(this::FPLT, "FPLT_SEARCH_THREAD").start();
        }
    }

    private void FPLT() {
        log.debug("FPLT_SRARCH_THREAD start...");
        while (true) {
            HafpisSrchTask srchTask = null;
            try {
                srchTask = srchTaskQueue.take();
                log.info("take one srchtask");
                if (srchTask.getSrchdata() == null || srchTask.getSrchdata().length == 0) {
                    log.error("SrchTask {} srchdata is null.", srchTask.getTaskidd());
                    srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                    srchTask.setExptmsg("Srchdata is null");
                    updateSrchOnly(srchTask);
//                    srchTaskDao.update(srchTask);
                } else {
                    log.info("get srchtask: {}", srchTask.getTaskidd());
                    CommonUtils.convert(srchTask);
                    if (CommonUtils.check(srchTask)) {
                        log.error("Convert srchdata error. taskidd: {}", srchTask.getTaskidd());
                        srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                        srchTask.setExptmsg("Convert srchdata error");
                        updateSrchOnly(srchTask);
//                        srchTaskDao.update(srchTask);
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

    private String getFpMask(String oriFpMask, int idx) {
        if (idx < 0 || idx > 10) {
            log.error("Fp_mask idx-{} error, shoube in range [0, 9]", idx);
            return oriFpMask;
        } else {
            char[] fpMaskChars = oriFpMask.toCharArray();
            fpMaskChars[7 + idx] = '1'; //the first 7 bits = fpmask=
            return new String(fpMaskChars);
        }
    }

    private String getFpMask(String oriFpMask, int[] flags) {
        if (flags == null || flags.length == 0) {
            return oriFpMask;
        }
        char[] fpMaskChars = oriFpMask.toCharArray();
        for (int i = 0; i < flags.length; i++) {
            if (flags[i] == 1) {
                fpMaskChars[7 + i] = '1';//the first 7 bits = fpmask=
            }
        }
        return new String(fpMaskChars);
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

    private void updateSrchAndFPTT(HafpisSrchTask srchTask, List<HafpisFpltCand> fpltCands) {
        Transaction tx = null;
        Session session = HibernateSessionFactoryUtil.getSession();
        try {
            tx = session.getTransaction();
            tx.begin();
            srchTaskDao.update(srchTask, session);
            fpltDao.insert(fpltCands, session);
            tx.commit();
        } catch (Exception e) {
            log.error("update srch and fptt error.", e);
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            HibernateSessionFactoryUtil.closeSession();;
        }
    }
}

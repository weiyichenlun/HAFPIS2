package hbie2.HAFPIS2.Service.Impl;

import hbie2.Candidate;
import hbie2.HAFPIS2.Dao.HafpisFptlCandDao;
import hbie2.HAFPIS2.Dao.HafpisSrchTaskDao;
import hbie2.HAFPIS2.Entity.HafpisFptlCand;
import hbie2.HAFPIS2.Entity.HafpisSrchTask;
import hbie2.HAFPIS2.Entity.SrchDataBean;
import hbie2.HAFPIS2.Service.AbstractService;
import hbie2.HAFPIS2.Utils.CONSTANTS;
import hbie2.HAFPIS2.Utils.CommonUtils;
import hbie2.HAFPIS2.Utils.HbieUtils;
import hbie2.TaskSearch;
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
 * 创建时间:2018/4/23
 * 最后修改时间:2018/4/23
 */
public class HafpisFPTLService extends AbstractService implements Runnable {
    private Logger log = LoggerFactory.getLogger(HafpisFPTLService.class);
    private HafpisSrchTaskDao srchTaskDao;
    private HafpisFptlCandDao fptlDao;
    private ArrayBlockingQueue<HafpisSrchTask> srchTaskQueue;
    private int FPTL_Threshold;

    @Override
    public void init(Properties cfg) {
        try {
            this.status = Integer.parseInt(cfg.getProperty("status"));
        } catch (NumberFormatException e) {
            log.error("status: {} config error, must be a number. Use default status: 3 ", cfg.getProperty("status"), e);
            this.status = 3;
        }
        try {
            this.querynum = Integer.parseInt(cfg.getProperty("querynum"));

        } catch (NumberFormatException e) {
            log.error("querynum: {} config error, must be a number. Use default querynum: 10", cfg.getProperty("querynum"), e);
            this.querynum = 10;
        }
        try {
            this.interval = Integer.parseInt(cfg.getProperty("interval"));

        } catch (NumberFormatException e) {
            log.error("interval: {} config error, must be a number. Use default interval: 1", cfg.getProperty("interval"), e);
            this.interval = 1;
        }
        try {
            this.FPTL_Threshold = Integer.parseInt(cfg.getProperty("FPTL_Threshold"));
        } catch (NumberFormatException e) {
            log.error("FPTL_Threshold: {} config error, must be an Integer. Use default value: 0", cfg.getProperty("FPTL_Threshold"));
            this.FPTL_Threshold = 0;
        }
        srchTaskDao = new HafpisSrchTaskDao();
        fptlDao = new HafpisFptlCandDao();
        srchTaskQueue = new ArrayBlockingQueue<>(CONSTANTS.FPTL_LIMIT);
    }

    @Override
    public <T> void doWork(List<T> list) {
        HafpisSrchTask srchTask = (HafpisSrchTask) list.get(0);
        SrchDataBean srchDataBean = srchTask.getSrchDataBeans().get(0);
        String srchPosMask = CommonUtils.checkSrchPosMask(CONSTANTS.SRCH_DATATYPE_LPP, srchTask.getSrchposmask());
        String taskidd = srchTask.getTaskidd();
        int[] rpPosMask = new int[10];
        int[] fpPosMask = new int[10];
        int numOf1 = 0;

        int avgCand = srchTask.getAveragecand();
        if (srchDataBean.rpmntnum == 0 || srchDataBean.fpmntnum == 0) {
            log.error("both rpmnt and fpmnt are null. taskidd: {}", srchTask.getTaskidd());
            srchTask.setStatus(CONSTANTS.ERROR_STATUS);
            srchTask.setExptmsg("rpmnt and fpmnt are both null");
            srchTaskDao.update(srchTask);
        } else {
            for (int i = 0; i < 10; i++) {
                if (srchPosMask.charAt(i) == '1' && srchDataBean.RpMntLen[i] != 0) {
                    rpPosMask[i] = 1;
                    numOf1++;
                }
                if (srchPosMask.charAt(10 + i) == '1' && srchDataBean.FpMntLen[i] != 0) {
                    fpPosMask[i] = 1;
                    numOf1++;
                }
            }
            byte[][] rpmnt = srchDataBean.rpmnt;
            byte[][] fpmnt = srchDataBean.fpmnt;

            if (HbieUtils.getInstance().hbie_LPP != null) {
                try {
                    Map<String, HafpisFptlCand> fptlCandMap = new HashMap<>();
                    List<HafpisFptlCand> rest = new ArrayList<>();
                    TaskSearch taskSearch = new TaskSearch();
                    int numOfCand = srchTask.getNumofcand();
                    numOfCand = numOfCand > 0 ? (int) (numOfCand * 1.5) : CONSTANTS.MAXCANDS;
                    taskSearch.setId(taskidd);
                    taskSearch.setType("TL");
                    taskSearch.setScoreThreshold(FPTL_Threshold);
                    // calculate the average number of candidates on condition of avgcand = 1
                    int avgCandNum = numOfCand / numOf1;
                    if ((numOfCand % numOf1) > (avgCandNum / 2)) {
                        avgCandNum += 1;
                    }

                    // set filters
                    String dbsFilter = CommonUtils.getDbsFilter(srchTask.getSrchdbsmask());
                    String solveOrDupFilter = CommonUtils.getSolveOrDupFilter(CONSTANTS.DBOP_LPP, srchTask.getSolveordup());
                    String demoFilter = CommonUtils.getDemoFilter(srchTask.getDemofilter());
                    String filter = CommonUtils.mergeFilters(dbsFilter, solveOrDupFilter, demoFilter);
                    taskSearch.setFilter(filter);
                    log.debug("Total filter is {}", filter);

                    if (avgCand == 1) {
                        for (int i = 0; i < 10; i++) {
                            if (rpPosMask[i] == 1) {
                                taskSearch.setFeature(rpmnt[i]);

                                String uid = HbieUtils.getInstance().hbie_LPP.submitSearch(taskSearch);
                                while (true) {
                                    TaskSearch task = HbieUtils.getInstance().hbie_LPP.querySearch(uid);
                                    if (task == null) {
                                        log.warn("FPTL: Impossible. taskidd: {}, uid: {}", taskidd, uid);
                                        srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                                        srchTaskDao.update(srchTask);
                                        break;
                                    } else if (task.getStatus() == TaskSearch.Status.Error) {
                                        log.error("FPTL search error. taskidd: {}, uid: {}", taskidd, uid);
                                        srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                                        srchTask.setExptmsg(task.getMsg());
                                        srchTaskDao.update(srchTask);
                                        break;
                                    } else if (task.getStatus() != TaskSearch.Status.Done) {
                                        CommonUtils.sleep(10);
                                        continue;
                                    }
                                    List<Candidate> candidates = task.getCandidates();
                                    for (int j = 0; j < candidates.size(); j++) {
                                        Candidate candidate = candidates.get(j);
                                        HafpisFptlCand fptlCand = new HafpisFptlCand();
                                        fptlCand.getKeys().setTaskidd(taskidd);
                                        fptlCand.getKeys().setCandid(candidate.getId());
                                        fptlCand.getKeys().setPosition(i + 1);
                                        fptlCand.setDbid((Integer) candidate.getFields().get("dbid"));
                                        fptlCand.setScore(candidate.getScore());
                                        fptlCand.setTransno(srchTask.getTransno());
                                        fptlCand.setProbeid(srchTask.getProbeid());
                                        if (j < avgCandNum) {
                                            fptlCandMap.put(candidate.getId(), fptlCand);
                                        } else {
                                            rest.add(fptlCand);
                                        }
                                    }
                                    break;
                                }
                            }
                        }

                        for (int i = 0; i < 10; i++) {
                            if (fpPosMask[i] == 1) {
                                taskSearch.setFeature(fpmnt[i]);

                                String uid = HbieUtils.getInstance().hbie_LPP.submitSearch(taskSearch);
                                while (true) {
                                    TaskSearch task = HbieUtils.getInstance().hbie_LPP.querySearch(uid);
                                    if (task == null) {
                                        log.warn("FPTL: Impossible. taskidd: {}, uid: {}", taskidd, uid);
                                        srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                                        srchTaskDao.update(srchTask);
                                        break;
                                    } else if (task.getStatus() == TaskSearch.Status.Error) {
                                        log.error("FPTL search error. taskidd: {}, uid: {}", taskidd, uid);
                                        srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                                        srchTask.setExptmsg(task.getMsg());
                                        srchTaskDao.update(srchTask);
                                        break;
                                    } else if (task.getStatus() != TaskSearch.Status.Done) {
                                        CommonUtils.sleep(10);
                                        continue;
                                    }
                                    List<Candidate> candidates = task.getCandidates();
                                    for (int j = 0; j < candidates.size(); j++) {
                                        Candidate candidate = candidates.get(j);
                                        String candid = candidate.getId();
                                        HafpisFptlCand fptlCand = fptlCandMap.get(candid);

                                        if (fptlCand == null) {
                                            fptlCand = new HafpisFptlCand();
                                            fptlCand.getKeys().setTaskidd(taskidd);
                                            fptlCand.getKeys().setCandid(candidate.getId());
                                            fptlCand.getKeys().setPosition(i + 11);
                                            fptlCand.setDbid((Integer) candidate.getFields().get("dbid"));
                                            fptlCand.setScore(candidate.getScore());
                                            fptlCand.setTransno(srchTask.getTransno());
                                            fptlCand.setProbeid(srchTask.getProbeid());
                                        } else {
                                            if (candidate.getScore() > fptlCand.getScore()) {
                                                fptlCand.setScore(candidate.getScore());
                                                fptlCand.getKeys().setPosition(i + 11);
                                            }
                                        }

                                        if (j < avgCandNum) {
                                            fptlCandMap.put(candid, fptlCand);
                                        } else {
                                            rest.add(fptlCand);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    } else {
                        for (int i = 0; i < 10; i++) {
                            if (rpPosMask[i] == 1) {
                                taskSearch.setFeature(rpmnt[i]);

                                String uid = HbieUtils.getInstance().hbie_LPP.submitSearch(taskSearch);
                                while (true) {
                                    TaskSearch task = HbieUtils.getInstance().hbie_LPP.querySearch(uid);
                                    if (task == null) {
                                        log.warn("FPTL: Impossible. taskidd: {}, uid: {}", taskidd, uid);
                                        srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                                        srchTaskDao.update(srchTask);
                                        break;
                                    } else if (task.getStatus() == TaskSearch.Status.Error) {
                                        log.error("FPTL search error. taskidd: {}, uid: {}", taskidd, uid);
                                        srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                                        srchTask.setExptmsg(task.getMsg());
                                        srchTaskDao.update(srchTask);
                                        break;
                                    } else if (task.getStatus() != TaskSearch.Status.Done) {
                                        CommonUtils.sleep(10);
                                        continue;
                                    }
                                    List<Candidate> candidates = task.getCandidates();
                                    for (int j = 0; j < candidates.size(); j++) {
                                        Candidate candidate = candidates.get(j);
                                        HafpisFptlCand fptlCand = new HafpisFptlCand();
                                        fptlCand.getKeys().setTaskidd(taskidd);
                                        fptlCand.getKeys().setCandid(candidate.getId());
                                        fptlCand.getKeys().setPosition(i + 1);
                                        fptlCand.setDbid((Integer) candidate.getFields().get("dbid"));
                                        fptlCand.setScore(candidate.getScore());
                                        fptlCand.setTransno(srchTask.getTransno());
                                        fptlCand.setProbeid(srchTask.getProbeid());
                                        fptlCandMap.put(candidate.getId(), fptlCand);
                                    }
                                    break;
                                }
                            }

                            if (fpPosMask[i] == 1) {
                                taskSearch.setFeature(fpmnt[i]);

                                String uid = HbieUtils.getInstance().hbie_LPP.submitSearch(taskSearch);
                                while (true) {
                                    TaskSearch task = HbieUtils.getInstance().hbie_LPP.querySearch(uid);
                                    if (task == null) {
                                        log.warn("FPTL: Impossible. taskidd: {}, uid: {}", taskidd, uid);
                                        srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                                        srchTaskDao.update(srchTask);
                                        break;
                                    } else if (task.getStatus() == TaskSearch.Status.Error) {
                                        log.error("FPTL search error. taskidd: {}, uid: {}", taskidd, uid);
                                        srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                                        srchTask.setExptmsg(task.getMsg());
                                        srchTaskDao.update(srchTask);
                                        break;
                                    } else if (task.getStatus() != TaskSearch.Status.Done) {
                                        CommonUtils.sleep(10);
                                        continue;
                                    }
                                    List<Candidate> candidates = task.getCandidates();
                                    for (int j = 0; j < candidates.size(); j++) {
                                        Candidate candidate = candidates.get(j);
                                        String candid = candidate.getId();
                                        HafpisFptlCand fptlCand = fptlCandMap.get(candid);

                                        if (fptlCand == null) {
                                            fptlCand = new HafpisFptlCand();
                                            fptlCand.getKeys().setTaskidd(taskidd);
                                            fptlCand.getKeys().setCandid(candidate.getId());
                                            fptlCand.getKeys().setPosition(i + 11);
                                            fptlCand.setDbid((Integer) candidate.getFields().get("dbid"));
                                            fptlCand.setScore(candidate.getScore());
                                            fptlCand.setTransno(srchTask.getTransno());
                                            fptlCand.setProbeid(srchTask.getProbeid());
                                        } else {
                                            if (candidate.getScore() > fptlCand.getScore()) {
                                                fptlCand.setScore(candidate.getScore());
                                                fptlCand.getKeys().setPosition(i + 11);
                                            }
                                        }
                                        fptlCandMap.put(candid, fptlCand);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    //insert into db
                    log.debug("begin to insert into table");
                    if (fptlCandMap.size() == 0) {
                        log.info("FPTL search finish. No results for taskidd: {}", taskidd);
                        srchTask.setStatus(CONSTANTS.FINISH_NOMATCH_STATUS);
                        srchTask.setExptmsg("No results");
                        srchTaskDao.update(srchTask);
                    } else {
                        List<HafpisFptlCand> fptlCands = new ArrayList<>();
                        List<HafpisFptlCand> fptlCandsRest = new ArrayList<>();
                        fptlCands.addAll(fptlCandMap.values());
                        if (fptlCands.size() >= numOfCand) {
                            fptlCands = CommonUtils.getLimitedList(fptlCands, numOfCand);
                        } else {
                            fptlCandsRest = CommonUtils.getLimitedList(rest, numOfCand - fptlCands.size());
                            fptlCands.addAll(fptlCandsRest);
                            Collections.sort(fptlCands);
                        }

                        //rank
                        for (int i = 0; i < fptlCands.size(); i++) {
                            fptlCands.get(i).setCandrank(i + 1);
                        }
                        log.debug("Inserting...");
                        fptlDao.delete(taskidd);
                        fptlDao.insert(fptlCands);
                        srchTask.setStatus(CONSTANTS.FINISH_STATUS);
                        srchTaskDao.update(srchTask);
                        log.info("srchtask {} finish", taskidd);
                    }
                } catch (Exception e) {
                    log.error("Impossible. ", e);
                    srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                    srchTaskDao.update(srchTask);
                }
            } else {
                log.error("Get HBIE client null. Suspenging until HBIE is started..");
                log.info("waiting FPTL client...");
                srchTask.setStatus(CONSTANTS.WAIT_STATUS);
                srchTaskDao.update(srchTask);
                CommonUtils.sleep(interval * 1000);
            }
        }
    }

    @Override
    public void run() {
        //add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            srchTaskDao.updateStatus(CONSTANTS.SRCH_DATATYPE_LPP, CONSTANTS.SRCH_TASKTYPE_TL);
            log.info("FPTL is shuting down");
        }));

        log.info("FPTL service start. Update status first...");
        srchTaskDao.updateStatus(CONSTANTS.SRCH_DATATYPE_LPP, CONSTANTS.SRCH_TASKTYPE_TL);

        //Take SrchTask from db
        new Thread(() -> {
            log.info("FPTL_SRCHTASKQUEUE_THREAD start...");
            while (true) {
                List<HafpisSrchTask> list = srchTaskDao.getSrchTasks(CONSTANTS.URGENT_STATUS, CONSTANTS.SRCH_DATATYPE_LPP,
                        CONSTANTS.SRCH_TASKTYPE_TL, querynum);
                if (null == list || list.size() == 0) {
                    list = srchTaskDao.getSrchTasks(CONSTANTS.WAIT_STATUS, CONSTANTS.SRCH_DATATYPE_LPP, CONSTANTS.SRCH_TASKTYPE_TL, querynum);
                    if (null == list || list.size() == 0) {
                        CommonUtils.sleep(interval * 1000);
                    } else {
                        for (HafpisSrchTask srchTask : list) {
                            try {
                                srchTaskQueue.put(srchTask);
                                srchTask.setStatus(CONSTANTS.PROCESSING_STATUS);
                                srchTaskDao.update(srchTask);
                            } catch (InterruptedException e) {
                                log.error("FPTL: put {} into srchtask queue error.", srchTask.getTaskidd(), e);
                            }
                        }
                    }
                } else {
                    for (HafpisSrchTask srchTask : list) {
                        try {
                            srchTaskQueue.put(srchTask);
                            srchTask.setStatus(CONSTANTS.PROCESSING_STATUS);
                            srchTaskDao.update(srchTask);
                        } catch (InterruptedException e) {
                            log.error("FPTL: put urgent {} into srchtask queue error.", srchTask.getTaskidd(), e);
                        }
                    }
                }
            }
        }, "FPTL_SRCHTASKQUEUE_THREAD").start();

        new Thread(this::FPTL, "FPTL_SEARCH_THREAD").start();
    }

    private void FPTL() {
        log.debug("FPTL_SEARCH_THREAD start...");
        while (true) {
            HafpisSrchTask srchTask = null;
            try {
                srchTask = srchTaskQueue.take();
                if (srchTask.getSrchdata() == null || srchTask.getSrchdata().length == 0) {
                    log.error("SrchTask {} srchdata is null", srchTask.getTaskidd());
                    srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                    srchTask.setExptmsg("srchdata is null");
                    srchTaskDao.update(srchTask);
                } else {
                    log.info("get srchtask: [}", srchTask.getTaskidd());
                    CommonUtils.convert(srchTask);
                    if (CommonUtils.check(srchTask)) {
                        log.error("Convert srchdata error. taskidd: {}", srchTask.getTaskidd());
                        srchTask.setStatus(CONSTANTS.ERROR_STATUS);
                        srchTask.setExptmsg("conver srchdata error");
                        srchTaskDao.update(srchTask);
                    } else {
                        List<HafpisSrchTask> list = new ArrayList<>();
                        list.add(srchTask);
                        doWork(list);
                    }
                }
            } catch (InterruptedException e) {
                log.info("Take srchtask from queue error. ", e);
            }
        }
    }
}

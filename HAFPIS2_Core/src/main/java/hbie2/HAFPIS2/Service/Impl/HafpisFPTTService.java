package hbie2.HAFPIS2.Service.Impl;

import hbie2.Candidate;
import hbie2.HAFPIS2.Dao.HafpisFpttCandDao;
import hbie2.HAFPIS2.Dao.HafpisSrchTaskDao;
import hbie2.HAFPIS2.Entity.HafpisFpttCand;
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
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/29
 * 最后修改时间:2018/3/29
 */
public class HafpisFPTTService extends AbstractService implements Runnable {
    private Logger log = LoggerFactory.getLogger(HafpisFPTTService.class);
    private HafpisSrchTaskDao srchTaskDao;
    private HafpisFpttCandDao fpttDao;
    private ArrayBlockingQueue<HafpisSrchTask> srchTaskQueue;
    private ArrayBlockingQueue<TaskSearch> taskSearchQueue;
    private int FPTT_Threshold;

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
            this.FPTT_Threshold = Integer.parseInt(cfg.getProperty("FPTT_Threshold"));
        } catch (NumberFormatException e) {
            log.error("FPTT_Threshold: {} config error, must be an Integer. Use default value: 0", cfg.getProperty("FPTT_Threshold"));
            this.FPTT_Threshold = 0;
        }
        srchTaskDao = new HafpisSrchTaskDao();
        fpttDao = new HafpisFpttCandDao();
        srchTaskQueue = new ArrayBlockingQueue<>(CONSTANTS.FPTT_LIMIT);
        taskSearchQueue = new ArrayBlockingQueue<TaskSearch>(CONSTANTS.FPTT_LIMIT);
    }

    @Override
    public <T> void doWork(List<T> list) {
        HafpisSrchTask srchTask = (HafpisSrchTask) list.get(0);
        SrchDataBean srchDataBean = srchTask.getSrchDataBeans().get(0);
        if (srchDataBean.rpmntnum == 0 && srchDataBean.fpmntnum == 0) {
            log.error("Rpmnt and fpmnt are both null. taskidd: {}", srchTask.getTaskidd());
            srchTaskDao.update(srchTask.getTaskidd(), CONSTANTS.ERROR_STATUS, "Rpmnt and fpmnt are both null");
        } else {
            try {
                List<HafpisFpttCand> fpttCands = new ArrayList<>();
                TaskSearch taskSearch = new TaskSearch();
                int numOfCand = srchTask.getNumofcand();
                numOfCand = numOfCand > 0 ? (int) (numOfCand * 1.5) : CONSTANTS.MAXCANDS;
                String taskidd = srchTask.getTaskidd();
                taskSearch.setId(taskidd);
                taskSearch.setType("TT");
                taskSearch.setMaxCandidateNum(numOfCand);
                taskSearch.setScoreThreshold(FPTT_Threshold);

                //set filters
                String dbsFilter = CommonUtils.getDbsFilter(srchTask.getSrchdbsmask());
                String solveOrDupFilter = CommonUtils.getSolveOrDupFilter(CONSTANTS.DBOP_TPP, srchTask.getSolveordup());
                String demoFilter = CommonUtils.getDemoFilter(srchTask.getDemofilter());
                taskSearch.setFilter(CommonUtils.mergeFilters("flag=={0}", dbsFilter, solveOrDupFilter, demoFilter));
                log.debug("Total filter is {}", taskSearch.getFilter());

                String uid;
                //rpmnt
                if (srchDataBean.rpmntnum > 0) {
                    taskSearch.setFeatures(srchDataBean.rpmnt);
                    if (HbieUtils.getInstance().hbie_FP != null) {
                        uid = HbieUtils.getInstance().hbie_FP.submitSearch(taskSearch);
                        while (true) {
                            TaskSearch task = HbieUtils.getInstance().hbie_FP.querySearch(uid);
                            if (task == null) {
                                log.error("Impossible. taskidd: {}", taskidd);
                                srchTaskDao.update(taskidd, CONSTANTS.WAIT_STATUS);
                                break;
                            } else if (task.getStatus() == TaskSearch.Status.Error) {
                                log.error("FPTT search error. taskidd: {}", taskidd);
                                srchTaskDao.update(taskidd, CONSTANTS.ERROR_STATUS, task.getMsg());
                                break;
                            } else if (task.getStatus() != TaskSearch.Status.Done) {
                                CommonUtils.sleep(100);
                                continue;
                            }
                            List<Candidate> candidates = task.getCandidates();
                            for (int i = 0; i < candidates.size(); i++) {
                                HafpisFpttCand fpttCand = new HafpisFpttCand();
                                Candidate cand = candidates.get(i);
                                fpttCand.getKeys().setTaskidd(taskidd);
                                fpttCand.getKeys().setCandid(cand.getId());
                                fpttCand.setTransno(srchTask.getTransno());
                                fpttCand.setProbeid(srchTask.getProbeid());
                                fpttCand.setDbid((Integer) cand.getFields().get("dbid"));
                                fpttCand.setScore(cand.getScore());

                            }
                        }
                    } else {
                        log.error("Get HBIE client error. Suspenging until HBIE is started..");
                        log.info("waiting FPTT client...");
                        srchTaskDao.update(srchTask.getTaskidd(), CONSTANTS.WAIT_STATUS);
                        CommonUtils.sleep(interval);
                    }
                }

            } catch (Exception e) {

            }
        }
    }

    @Override
    public void run() {
        //add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            srchTaskDao.updateStatus(CONSTANTS.SRCH_DATATYPE_TP, CONSTANTS.SRCH_TASKTYPE_TT);
            log.info("Fp executorservice is shutting down");
        }));

        log.info("FPTT service start. Update status first...");
        srchTaskDao.updateStatus(CONSTANTS.SRCH_DATATYPE_TP, CONSTANTS.SRCH_DATATYPE_TP);

        //Take SrchTask from db
        new Thread(() -> {
            while (true) {
                List<HafpisSrchTask> list = srchTaskDao.getSrchTasks(CONSTANTS.WAIT_STATUS, CONSTANTS.SRCH_DATATYPE_TP,
                        CONSTANTS.SRCH_TASKTYPE_TT, querynum);
                if (null == list || list.size() == 0) {
                    CommonUtils.sleep(interval);
                } else {
                    for (HafpisSrchTask srchTask : list) {
                        try {
                            srchTaskQueue.put(srchTask);
                        } catch (InterruptedException e) {
                            log.error("Put {} into srchtask queue error.",srchTask.getTaskidd(), e);
                        }
                    }
                }
            }
        }, "FPTT_SRCHTASKQUEUE_THREAD").start();

        new Thread(this::FPTT, "FPTT_SEARCH_THREAD").start();
    }

    private void FPTT() {
        while (true) {
            HafpisSrchTask srchTask = null;
            try {
                srchTask = srchTaskQueue.take();
                if (srchTask.getSrchdata() == null || srchTask.getSrchdata().length == 0) {
                    log.error("SrchTask {} srchdata is null.");
                    srchTaskDao.update(srchTask.getTaskidd(), CONSTANTS.ERROR_STATUS, "Srchdata is null");
                } else {
                    srchTaskDao.update(srchTask.getTaskidd(), CONSTANTS.PROCESSING_STATUS);
                    CommonUtils.convert(srchTask);
                    if (CommonUtils.check(srchTask)) {
                        log.error("Convert srchdata error. taskidd: {}", srchTask.getTaskidd());
                        srchTaskDao.update(srchTask.getTaskidd(), CONSTANTS.ERROR_STATUS, "Conver srchdata error");
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

}

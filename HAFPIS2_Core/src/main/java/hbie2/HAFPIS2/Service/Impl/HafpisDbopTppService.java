package hbie2.HAFPIS2.Service.Impl;

import hbie2.HAFPIS2.Dao.HafpisDbopTaskDao;
import hbie2.HAFPIS2.Dao.HafpisHtppSdemoDao;
import hbie2.HAFPIS2.Entity.HafpisDbopTask;
import hbie2.HAFPIS2.Service.AbstractService;
import hbie2.HAFPIS2.Utils.CONSTANTS;
import hbie2.HAFPIS2.Utils.CommonUtils;
import hbie2.HAFPIS2.Utils.HbieUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/28
 * 最后修改时间:2018/3/28
 */
public class HafpisDbopTppService extends AbstractService implements Runnable {
    private Logger log = LoggerFactory.getLogger(HafpisDbopTppService.class);
    private HafpisHtppSdemoDao htppSdemoDao;
    private HafpisDbopTaskDao dbopTaskDao;
    private ExecutorService executorService;


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
        htppSdemoDao = new HafpisHtppSdemoDao();
        dbopTaskDao = new HafpisDbopTaskDao();
        executorService = Executors.newFixedThreadPool(CONSTANTS.NCORES > 8 ? 8 : CONSTANTS.NCORES);
    }

    @Override
    public <T> void doWork(List<T> list) {
        List<Future<String>> futureList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            HafpisDbopTask dbopTask = (HafpisDbopTask) list.get(i);
            dbopTaskDao.update(dbopTask.getTaskidd(), 4);
            Future<String> future = executorService.submit(() -> {
                try {
                    int dbopTasktype = dbopTask.getTasktype();
                    String id = dbopTask.getProbeid();
                    switch (dbopTasktype) {
                        case 6: //delete
                            if (HbieUtils.getInstance().hbie_FP != null) {
                                HbieUtils.getInstance().hbie_FP.syncMatcherRecord(id, CONSTANTS.DELETE);
                                HbieUtils.getInstance().hbie_FP.syncMatcherRecord(id + "$", CONSTANTS.DELETE);
                            }
                            if (HbieUtils.getInstance().hbie_PP != null) {
                                HbieUtils.getInstance().hbie_PP.syncMatcherRecord(id, CONSTANTS.DELETE);
                            }
                            if (HbieUtils.getInstance().hbie_FACE != null) {
                                HbieUtils.getInstance().hbie_FACE.syncMatcherRecord(id, CONSTANTS.DELETE);
                            }
                            if (HbieUtils.getInstance().hbie_IRIS != null) {
                                HbieUtils.getInstance().hbie_IRIS.syncMatcherRecord(id, CONSTANTS.DELETE);
                            }
                            break;
                        case 5: // insert
                            String imgMask = htppSdemoDao.getImgMask(id);
                            boolean oldVersion = imgMask.length() == 43;
                            String rfp = imgMask.substring(0, 10);
                            if (!"0000000000".equals(rfp) && HbieUtils.getInstance().hbie_FP != null)
                                HbieUtils.getInstance().hbie_FP.syncMatcherRecord(id, CONSTANTS.UPDATE);
                            String ffp = imgMask.substring(10, 20);
                            if (!"0000000000".equals(ffp) && HbieUtils.getInstance().hbie_FP != null) {
                                HbieUtils.getInstance().hbie_FP.syncMatcherRecord(id + "$", CONSTANTS.UPDATE);
                            }
                            String pm = imgMask.substring(20, 30);
                            if (!"0000000000".equals(pm) && HbieUtils.getInstance().hbie_PP != null) {
                                HbieUtils.getInstance().hbie_PP.syncMatcherRecord(id, CONSTANTS.UPDATE);
                            }
                            String face = oldVersion ? imgMask.substring(30, 33) : imgMask.substring(40, 50);
                            if (face.charAt(0) == '1' && HbieUtils.getInstance().hbie_FACE != null) {
                                HbieUtils.getInstance().hbie_FACE.syncMatcherRecord(id, CONSTANTS.UPDATE);
                            }
                            String iris = oldVersion ? imgMask.substring(33, 35) : imgMask.substring(50, 55);
                            if (!"00".equals(iris.substring(0, 2)) && HbieUtils.getInstance().hbie_IRIS != null) {
                                HbieUtils.getInstance().hbie_IRIS.syncMatcherRecord(id, CONSTANTS.UPDATE);
                            }
                            break;
                        case 7: //delete and save
                            if (HbieUtils.getInstance().hbie_FP != null) {
                                HbieUtils.getInstance().hbie_FP.syncMatcherRecord(id, CONSTANTS.UPDATE);
                                HbieUtils.getInstance().hbie_FP.syncMatcherRecord(id + "$", CONSTANTS.UPDATE);
                            }
                            if (HbieUtils.getInstance().hbie_PP != null) {
                                HbieUtils.getInstance().hbie_PP.syncMatcherRecord(id, CONSTANTS.UPDATE);
                            }
                            if (HbieUtils.getInstance().hbie_FACE != null) {
                                HbieUtils.getInstance().hbie_FACE.syncMatcherRecord(id, CONSTANTS.UPDATE);
                            }
                            if (HbieUtils.getInstance().hbie_IRIS != null) {
                                HbieUtils.getInstance().hbie_IRIS.syncMatcherRecord(id, CONSTANTS.UPDATE);
                            }
                            break;
                        default:
                            log.error("dbopTasktype: {} error", dbopTasktype);
                            break;
                    }
                } catch (Exception e) {
                    log.error("matcher error.", e);
                    String expt = "matcher error " + e.toString();
                    dbopTaskDao.update(dbopTask.getTaskidd(), dbopTask.getStatus(), expt.length() > 128 ? expt.substring(0, 128) : expt);
                }
            }, dbopTask.getTaskidd());
            futureList.add(future);
        }
        for (Future<String> future : futureList) {
            String taskid = null;
            while (true) {
                try {
                    taskid = future.get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("get future result error. ", e);
                }
                if (taskid != null) {
                    boolean is = dbopTaskDao.update(taskid, 5);
                    if (is) {
                        log.info("Dbop-TPP taskid:{} finish.", taskid);
                    } else {
                        log.warn("Dbop-TPP taskid:{} update table error.", taskid);
                        dbopTaskDao.update(taskid, -1, "update " + taskid + " error");
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void run() {
        // add shut-down hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                executorService.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
            executorService.shutdown();
            dbopTaskDao.updateStatus(CONSTANTS.DBOP_TPP);
            log.info("DbopTpp executorservice is shutting down");
        }));

        while (true) {
            List<HafpisDbopTask> dbopTasks = dbopTaskDao.getDbopTasks(CONSTANTS.URGENT_STATUS, CONSTANTS.DBOP_TPP, querynum);
            if (null == dbopTasks || dbopTasks.size() == 0) {
                dbopTasks = dbopTaskDao.getDbopTasks(status, CONSTANTS.DBOP_TPP, querynum);
                if (null == dbopTasks || dbopTasks.size() == 0) {
                    CommonUtils.sleep(interval);
                } else {
                    doWork(dbopTasks);
                }
            } else {
                //增加处理加急作业流程
                log.info("Get urgent DbopTpp task...");
                doWork(dbopTasks);
            }
        }
    }

}

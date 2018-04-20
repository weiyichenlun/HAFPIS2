package hbie2.HAFPIS2.Service.Impl;

import hbie2.HAFPIS2.Dao.HafpisDbopTaskDao;
import hbie2.HAFPIS2.Dao.HafpisHlppKeyDao;
import hbie2.HAFPIS2.Dao.HafpisHlppSdemoDao;
import hbie2.HAFPIS2.Dao.HafpisImgdbCapsDao;
import hbie2.HAFPIS2.Dao.HafpisRecordStatusDao;
import hbie2.HAFPIS2.Entity.HafpisDbopTask;
import hbie2.HAFPIS2.Entity.HafpisHlppKey;
import hbie2.HAFPIS2.Entity.HafpisHlppSdemo;
import hbie2.HAFPIS2.Entity.HafpisImgdbCaps;
import hbie2.HAFPIS2.Entity.HafpisRecordStatus;
import hbie2.HAFPIS2.Service.AbstractService;
import hbie2.HAFPIS2.Utils.CONSTANTS;
import hbie2.HAFPIS2.Utils.CommonUtils;
import hbie2.HAFPIS2.Utils.HbieUtils;
import hbie2.Record;
import hbie2.ftp.FTPClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/29
 * 最后修改时间:2018/3/29
 */
public class HafpisDbopLppService extends AbstractService implements Runnable {
    private Logger log = LoggerFactory.getLogger(HafpisDbopLppService.class);
    private HafpisDbopTaskDao dbopTaskDao;
    private HafpisHlppSdemoDao hlppSdemoDao;
    private HafpisHlppKeyDao hlppKeyDao;
    private HafpisRecordStatusDao recordStatusDao;
    private HafpisImgdbCapsDao imgdbCapsDao;
    private ExecutorService executorService;
    private ArrayBlockingQueue<HafpisDbopTask> dbopTaskQueue;
    private FTPClientUtil ftpClient;
    private final String suffix = ".nist";

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
        hlppSdemoDao = new HafpisHlppSdemoDao();
        dbopTaskDao = new HafpisDbopTaskDao();
        hlppKeyDao = new HafpisHlppKeyDao();
        imgdbCapsDao = new HafpisImgdbCapsDao();
        recordStatusDao = new HafpisRecordStatusDao();
        dbopTaskQueue = new ArrayBlockingQueue<>(CONSTANTS.DBOP_LPP_LIMIT);
        executorService = Executors.newFixedThreadPool(CONSTANTS.NCORES > 8 ? 8 : CONSTANTS.NCORES);    }

    @Override
    public <T> void doWork(List<T> list) {
        HafpisDbopTask dbopTask = (HafpisDbopTask) list.get(0);
        int dbopTaskType = dbopTask.getTasktype();
        String id = dbopTask.getProbeid();
        HafpisHlppSdemo hlppSdemo = hlppSdemoDao.select(id);
        String createtime = hlppSdemo.getUpdatedate();
        switch (dbopTaskType) {
            case 6: //delete
                if (HbieUtils.getInstance().hbie_LPP != null) {
                    HbieUtils.getInstance().hbie_LPP.deleteRecord(id);
                    HafpisRecordStatus recordStatus = new HafpisRecordStatus();
                    recordStatus.getKey().setProbeid(id);
                    recordStatus.getKey().setDatatype(CONSTANTS.RECORD_DATATYPE_LPP);
                    recordStatusDao.delete(recordStatus);
                }
                dbopTask.setStatus(CONSTANTS.FINISH_STATUS);
                break;
            case 5: // insert
                String path = findPathByDb(id);
                if (path == null) {
                    log.error("Can not find nist path for taskidd: {}", dbopTask.getTaskidd());
                    dbopTask.setStatus(CONSTANTS.ERROR_STATUS);
                    dbopTask.setExptmsg("Can not find nish path");
                    break;
                }
                HafpisRecordStatus recordStatus = new HafpisRecordStatus();
                recordStatus.getKey().setProbeid(id);
                recordStatus.getKey().setDatatype(CONSTANTS.RECORD_DATATYPE_LPP);
                recordStatus.setStatus(Record.Status.Pending.name());
                recordStatus.setNistpath(path);
                recordStatus.setCreatetime(createtime);
                recordStatusDao.insert(recordStatus);
                log.debug("Insert into HAFPIS_RECORD_STATUS success. pid {}", id);

                dbopTask.setStatus(CONSTANTS.FINISH_STATUS);
                break;
            case 7 : // update
                HafpisRecordStatus task = recordStatusDao.select(id, CONSTANTS.RECORD_DATATYPE_LPP);
                String nistpath = findPathByDb(id);
                if (nistpath == null) {
                    log.error("Can not find nist path for taskidd {}", dbopTask.getTaskidd());
                    dbopTask.setStatus(CONSTANTS.ERROR_STATUS);
                    dbopTask.setExptmsg("Can not find nist path");
                    break;
                }

                if (task == null) {
                    log.debug("Get RecordStatus null for pid: {}", id);
                    task = new HafpisRecordStatus();
                    task.getKey().setProbeid(id);
                    task.getKey().setDatatype(CONSTANTS.RECORD_DATATYPE_LPP);
                    task.setStatus(Record.Status.Pending.name());
                    task.setNistpath(nistpath);
                    task.setCreatetime(createtime);
                    recordStatusDao.insert(task);
                    log.debug("Insert recordStatus {} success", id);
                } else {
                    log.debug("Get RecordStatus {}", id);
                    task.getKey().setDatatype(CONSTANTS.RECORD_DATATYPE_LPP);
                    task.setStatus(Record.Status.Pending.name());
                    task.setNistpath(nistpath);
                    task.setCreatetime(createtime);
                    recordStatusDao.update(task);
                    log.debug("Update recordStatus {} success", id);
                }
                dbopTask.setStatus(CONSTANTS.FINISH_STATUS);
                break;
            default:
                log.error("dbopTask type {} error", dbopTaskType);
                break;
        }
        dbopTaskDao.update(dbopTask.getTaskidd(), dbopTask.getStatus(), dbopTask.getExptmsg());
        log.info("Dbop_LPP taskid {} finish. Status {}", dbopTask.getTaskidd(), dbopTask.getStatus());


//        List<Future<String>> futureList = new ArrayList<>();
//        for (int i = 0; i < list.size(); i++) {
//            HafpisDbopTask dbopTask = (HafpisDbopTask) list.get(i);
//            dbopTaskDao.update(dbopTask.getTaskidd(), 4);
//            Future<String> future = executorService.submit(() -> {
//                try {
//                    int dbopTaskType = dbopTask.getTasktype();
//                    String pid = dbopTask.getProbeid();
//                    switch (dbopTaskType) {
//                        case 6:
//                            if (HbieUtils.getInstance().hbie_LPP != null) {
//                                HbieUtils.getInstance().hbie_LPP.syncMatcherRecord(pid, CONSTANTS.DELETE);
//                            }
//                            break;
//                        case 5:
//                        case 7:
//                            if (HbieUtils.getInstance().hbie_LPP != null) {
//                                HbieUtils.getInstance().hbie_LPP.syncMatcherRecord(pid, CONSTANTS.UPDATE);
//                            }
//                            break;
//                        default:
//                            log.error("dbopTaskType error {}.", dbopTaskType);
//                            break;
//                    }
//                }catch (Exception e) {
//                    log.warn("matcher error: ", e);
//                    String expt = "matcher error" + e.toString();
//                    dbopTaskDao.update(dbopTask.getTaskidd(), 3, expt.length() > 128 ? expt.substring(0, 128) : expt);
//                }
//            }, dbopTask.getTaskidd());
//            futureList.add(future);
//        }
//
//        for (Future<String> future : futureList) {
//            String taskid = null;
//            while (true) {
//                try {
//                    taskid = future.get();
//                } catch (InterruptedException | ExecutionException e) {
//                    log.error("get future result error. ", e);
//                }
//                if (taskid != null) {
//                    boolean is = dbopTaskDao.update(taskid, 5);
//                    if (is) {
//                        log.info("Dbop-LPP taskid:{} finish.", taskid);
//                    } else {
//                        log.warn("Dbop-LPP taskid:{} update table error.", taskid);
//                        dbopTaskDao.update(taskid, -1, "update " + taskid + " error");
//                    }
//                    break;
//                }
//            }
//        }
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
            dbopTaskDao.updateStatus(CONSTANTS.DBOP_LPP);
            log.info("DbopLpp executorservice is shutting down");
        }));

        log.info("DBOP_LPP service start. Update status first...");
        dbopTaskDao.updateStatus(CONSTANTS.DBOP_LPP);

        //Take dbop task from db
        new Thread(() -> {
            log.info("DBOP_LPP_DBOPTASKQUEUE_THREAD start...");
            while (true) {
                List<HafpisDbopTask> dbopTasks = dbopTaskDao.getDbopTasks(CONSTANTS.URGENT_STATUS, CONSTANTS.DBOP_LPP, querynum);
                if (null == dbopTasks || dbopTasks.size() == 0) {
                    dbopTasks = dbopTaskDao.getDbopTasks(status, CONSTANTS.DBOP_LPP, querynum);
                    if (null == dbopTasks || dbopTasks.size() == 0) {
                        CommonUtils.sleep(interval * 1000);
                    } else {
                        for (HafpisDbopTask dbopTask : dbopTasks) {
                            try {
                                dbopTaskQueue.put(dbopTask);
                                dbopTask.setStatus(CONSTANTS.PROCESSING_STATUS);
                                dbopTaskDao.update(dbopTask);
                            } catch (InterruptedException e) {
                                log.error("DBOP_LPP put {} into dboptask queue error.", dbopTask.getTaskidd());
                                dbopTask.setStatus(CONSTANTS.WAIT_STATUS);
                                dbopTaskDao.update(dbopTask);
                            }
                        }
                    }
                }
            }
        }, "DBOP_LPP_DBOPTASKQUEUE_THREAD").start();

        new Thread(this::DBOP, "DBOP_LPP_THREAD").start();
    }

    private void DBOP() {
        log.debug("DBOP_LPP_THREAD start...");
        while (true) {
            HafpisDbopTask dbopTask = null;
            try {
                dbopTask = dbopTaskQueue.take();
                log.debug("Take one dbopTask lpp");
                List<HafpisDbopTask> list = new ArrayList<>();
                list.add(dbopTask);
                doWork(list);
            } catch (InterruptedException e) {
                log.error("Take dbopTask from queue error.", e);
                CommonUtils.sleep(100);
            }
        }
    }

    private String findPathByDb(String personid) {
        HafpisHlppKey hlppKey = hlppKeyDao.get(personid);
        if (hlppKey != null) {
            String devname = hlppKey.getDevname();
            HafpisImgdbCaps imgdbCaps = imgdbCapsDao.get(devname);
            if (imgdbCaps != null) {
                return imgdbCaps.getDevpath();
            } else {
                log.error("Can't find ImgdbCaps for devname: {}", devname);
                return null;
            }
        } else {
            log.error("Can't find HtppKey for taskidd: {}", personid);
            return null;
        }
    }
}

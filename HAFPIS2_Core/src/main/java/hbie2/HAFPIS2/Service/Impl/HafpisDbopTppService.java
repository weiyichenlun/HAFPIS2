package hbie2.HAFPIS2.Service.Impl;

import hbie2.HAFPIS2.Dao.HafpisDbopTaskDao;
import hbie2.HAFPIS2.Dao.HafpisHtppKeyDao;
import hbie2.HAFPIS2.Dao.HafpisHtppSdemoDao;
import hbie2.HAFPIS2.Dao.HafpisImgdbCapsDao;
import hbie2.HAFPIS2.Dao.HafpisRecordStatusDao;
import hbie2.HAFPIS2.Entity.HafpisDbopTask;
import hbie2.HAFPIS2.Entity.HafpisHtppKey;
import hbie2.HAFPIS2.Entity.HafpisHtppSdemo;
import hbie2.HAFPIS2.Entity.HafpisImgdbCaps;
import hbie2.HAFPIS2.Entity.HafpisRecordStatus;
import hbie2.HAFPIS2.Entity.RecordStatusKey;
import hbie2.HAFPIS2.Service.AbstractService;
import hbie2.HAFPIS2.Utils.CONSTANTS;
import hbie2.HAFPIS2.Utils.CommonUtils;
import hbie2.HAFPIS2.Utils.ConfigUtils;
import hbie2.HAFPIS2.Utils.HbieUtils;
import hbie2.Record;
import hbie2.ftp.FTPClientException;
import hbie2.ftp.FTPClientUtil;
import hbie2.nist.NistDecoder;
import hbie2.nist.nistType.NistImg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private HafpisHtppKeyDao htppKeyDao;
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
        htppSdemoDao = new HafpisHtppSdemoDao();
        dbopTaskDao = new HafpisDbopTaskDao();
        htppKeyDao = new HafpisHtppKeyDao();
        imgdbCapsDao = new HafpisImgdbCapsDao();
        recordStatusDao = new HafpisRecordStatusDao();
        executorService = Executors.newFixedThreadPool(CONSTANTS.NCORES > 8 ? 8 : CONSTANTS.NCORES);
        dbopTaskQueue = new ArrayBlockingQueue<>(CONSTANTS.DBOP_TPP_LIMIT);
    }
    @Override
    public <T> void doWork(List<T> list) {
        HafpisDbopTask dbopTask = (HafpisDbopTask) list.get(0);
        int dbopTaskType = dbopTask.getTasktype();
        String id = dbopTask.getProbeid();
        HafpisHtppSdemo htppSdemo = htppSdemoDao.select(id);
        String imgmask = htppSdemo.getImgmask();
        String createtime = htppSdemo.getUpdatedate();
        switch (dbopTaskType) {
            case 6: //delete:
                if (HbieUtils.getInstance().hbie_FP != null) {
                    HbieUtils.getInstance().hbie_FP.deleteRecord(id);
                    HafpisRecordStatus recordStatus = new HafpisRecordStatus();
                    recordStatus.getKey().setProbeid(id);
                    recordStatus.getKey().setDatatype(CONSTANTS.RECORD_DATATYPE_TP);
                    recordStatusDao.delete(recordStatus);
                    HbieUtils.getInstance().hbie_FP.deleteRecord(id + "$");
                    recordStatus.getKey().setProbeid(id + "$");
                    recordStatus.getKey().setDatatype(CONSTANTS.RECORD_DATATYPE_TP);
                    recordStatusDao.delete(recordStatus);
                }
                if (HbieUtils.getInstance().hbie_PP != null) {
                    HbieUtils.getInstance().hbie_PP.deleteRecord(id);
                    HafpisRecordStatus recordStatus = new HafpisRecordStatus();
                    recordStatus.getKey().setProbeid(id);
                    recordStatus.getKey().setDatatype(CONSTANTS.RECORD_DATATYPE_PP);
                    recordStatusDao.delete(recordStatus);
                }
                if (HbieUtils.getInstance().hbie_FACE != null) {
                    HbieUtils.getInstance().hbie_FACE.deleteRecord(id);
                    HafpisRecordStatus recordStatus = new HafpisRecordStatus();
                    recordStatus.getKey().setProbeid(id);
                    recordStatus.getKey().setDatatype(CONSTANTS.RECORD_DATATYPE_FACE);
                    recordStatusDao.delete(recordStatus);
                }
                if (HbieUtils.getInstance().hbie_IRIS != null) {
                    HbieUtils.getInstance().hbie_IRIS.deleteRecord(id);
                    HafpisRecordStatus recordStatus = new HafpisRecordStatus();
                    recordStatus.getKey().setProbeid(id);
                    recordStatus.getKey().setDatatype(CONSTANTS.RECORD_DATATYPE_IRIS);
                    recordStatusDao.delete(recordStatus);
                }
                dbopTask.setStatus(CONSTANTS.FINISH_STATUS);
                break;
            case 5: //insert
                String path = dbopTask.getNistpath();
                if (path == null) {
                    path = findPathByDb(dbopTask.getProbeid());
                    if (path == null) {
                        log.error("Can't find nist path for taskidd: {}", dbopTask.getTaskidd());
                        dbopTask.setStatus(CONSTANTS.ERROR_STATUS);
                        dbopTask.setExptmsg("Can not find nist path");
                        break;
                    }
                }

                if (!imgmask.substring(0, 10).equals("0000000000")) {
                    HafpisRecordStatus recordStatus = new HafpisRecordStatus();
                    RecordStatusKey key = new RecordStatusKey();
                    key.setProbeid(id);
                    key.setDatatype(CONSTANTS.RECORD_DATATYPE_TP);
                    recordStatus.setKey(key);
                    recordStatus.setStatus(Record.Status.Pending.name());
                    recordStatus.setNistpath(path);;
                    recordStatus.setCreatetime(createtime);
                    recordStatusDao.insert(recordStatus);
                    log.debug("Insert into HAFPIS_RECORD_STATUS success. Probeid: {}", id);
                }
                if (!imgmask.substring(10, 20).equals("0000000000")) {
                    HafpisRecordStatus recordStatus = new HafpisRecordStatus();
                    RecordStatusKey key = new RecordStatusKey();
                    key.setProbeid(id + "$");
                    key.setDatatype(CONSTANTS.RECORD_DATATYPE_TP);
                    recordStatus.setKey(key);
                    recordStatus.setStatus(Record.Status.Pending.name());
                    recordStatus.setNistpath(path);
                    recordStatus.setCreatetime(createtime);
                    recordStatusDao.insert(recordStatus);
                    log.debug("Insert into HAFPIS_RECORD_STATUS success. Probeid: {}", id + "$");
                }
                dbopTask.setStatus(CONSTANTS.FINISH_STATUS);
                break;
            case 7: //update
                HafpisRecordStatus task = recordStatusDao.select(id, CONSTANTS.RECORD_DATATYPE_TP);
                String nistPath = dbopTask.getNistpath();
                if (nistPath == null) {
                    nistPath = findPathByDb(id);
                    if (nistPath == null) {
                        log.error("Can't find nist path for taskidd: {}", dbopTask.getTaskidd());
                        dbopTask.setStatus(CONSTANTS.ERROR_STATUS);
                        dbopTask.setExptmsg("Can not find nist path");
                        break;
                    }
                }
                // rp matcher task
                if (task == null) {
                    log.debug("Get RecordStatus null for probeid: {}", id);
                    if (!imgmask.substring(0, 10).equals("0000000000")) {
                        task = new HafpisRecordStatus();
                        RecordStatusKey key = new RecordStatusKey();
                        key.setProbeid(id);
                        key.setDatatype(CONSTANTS.RECORD_DATATYPE_TP);
                        task.setKey(key);
                        task.setStatus(Record.Status.Pending.name());
                        task.setNistpath(nistPath);
                        task.setCreatetime(createtime);
                        recordStatusDao.insert(task);
                        log.debug("Insert Task {} success", id);
                    }
                } else {
                    //TODO what if imgmask == "0000000000"
                    log.debug("Get RecordStatus {} ", id);
                    task.getKey().setDatatype(CONSTANTS.RECORD_DATATYPE_TP);
                    task.setStatus(Record.Status.Pending.name());
                    task.setNistpath(nistPath);
                    task.setCreatetime(createtime);
                    recordStatusDao.update(task);
                    log.debug("Update Task {} success", id);
                }

                HafpisRecordStatus task1 = recordStatusDao.select(id + "$", CONSTANTS.RECORD_DATATYPE_TP);
                if (task1 == null) {
                    log.debug("Get RecordStatus null for probeid: {}", id + "$");

                    if (!imgmask.substring(10, 20).equals("0000000000")) {
                        task1 = new HafpisRecordStatus();
                        RecordStatusKey key = new RecordStatusKey();
                        key.setProbeid(id + "$");
                        key.setDatatype(CONSTANTS.RECORD_DATATYPE_TP);
                        task1.setKey(key);
                        task1.setStatus(Record.Status.Pending.name());
                        task1.setNistpath(nistPath);
                        task1.setCreatetime(createtime);
                        recordStatusDao.insert(task1);
                        log.debug("Insert Task {} success", id + "$");
                    }
                } else {
                    log.debug("Get RecordStatus {} ", id + "$");
                    task1.getKey().setDatatype(CONSTANTS.RECORD_DATATYPE_TP);
                    task1.setStatus(Record.Status.Pending.name());
                    task1.setNistpath(nistPath);
                    task1.setCreatetime(createtime);
                    recordStatusDao.update(task1);
                    log.debug("Insert Task {} success", id + "$");
                }
                dbopTask.setStatus(CONSTANTS.FINISH_STATUS);
                break;
            default:
                log.error("dbopTasktype: {} error", dbopTaskType);
                break;
        }
        dbopTaskDao.update(dbopTask.getTaskidd(), dbopTask.getStatus(), dbopTask.getExptmsg());
        log.info("Dbop-TPP taskid:{} finish. Status: {}", dbopTask.getTaskidd(), dbopTask.getStatus());
    }

//    @Override
//    public <T> void doWork(List<T> list) {
//        List<Future<String>> futureList = new ArrayList<>();
//        for (int i = 0; i < list.size(); i++) {
//            HafpisDbopTask dbopTask = (HafpisDbopTask) list.get(i);
//            dbopTaskDao.update(dbopTask.getTaskidd(), 4);
//            Future<String> future = executorService.submit(() -> {
//                try {
//                    int dbopTasktype = dbopTask.getTasktype();
//                    String id = dbopTask.getProbeid();
//                    switch (dbopTasktype) {
//                        case 6: //delete
//                            if (HbieUtils.getInstance().hbie_FP != null) {
//                                HbieUtils.getInstance().hbie_FP.syncMatcherRecord(id, CONSTANTS.DELETE);
//                                HbieUtils.getInstance().hbie_FP.syncMatcherRecord(id + "$", CONSTANTS.DELETE);
//                            }
//                            if (HbieUtils.getInstance().hbie_PP != null) {
//                                HbieUtils.getInstance().hbie_PP.syncMatcherRecord(id, CONSTANTS.DELETE);
//                            }
//                            if (HbieUtils.getInstance().hbie_FACE != null) {
//                                HbieUtils.getInstance().hbie_FACE.syncMatcherRecord(id, CONSTANTS.DELETE);
//                            }
//                            if (HbieUtils.getInstance().hbie_IRIS != null) {
//                                HbieUtils.getInstance().hbie_IRIS.syncMatcherRecord(id, CONSTANTS.DELETE);
//                            }
//                            break;
//                        case 5: // insert
//                            String imgMask = htppSdemoDao.getImgMask(id);
//                            boolean oldVersion = imgMask.length() == 43;
//                            String rfp = imgMask.substring(0, 10);
//                            if (!"0000000000".equals(rfp) && HbieUtils.getInstance().hbie_FP != null)
//                                HbieUtils.getInstance().hbie_FP.syncMatcherRecord(id, CONSTANTS.UPDATE);
//                            String ffp = imgMask.substring(10, 20);
//                            if (!"0000000000".equals(ffp) && HbieUtils.getInstance().hbie_FP != null) {
//                                HbieUtils.getInstance().hbie_FP.syncMatcherRecord(id + "$", CONSTANTS.UPDATE);
//                            }
//                            String pm = imgMask.substring(20, 30);
//                            if (!"0000000000".equals(pm) && HbieUtils.getInstance().hbie_PP != null) {
//                                HbieUtils.getInstance().hbie_PP.syncMatcherRecord(id, CONSTANTS.UPDATE);
//                            }
//                            String face = oldVersion ? imgMask.substring(30, 33) : imgMask.substring(40, 50);
//                            if (face.charAt(0) == '1' && HbieUtils.getInstance().hbie_FACE != null) {
//                                HbieUtils.getInstance().hbie_FACE.syncMatcherRecord(id, CONSTANTS.UPDATE);
//                            }
//                            String iris = oldVersion ? imgMask.substring(33, 35) : imgMask.substring(50, 55);
//                            if (!"00".equals(iris.substring(0, 2)) && HbieUtils.getInstance().hbie_IRIS != null) {
//                                HbieUtils.getInstance().hbie_IRIS.syncMatcherRecord(id, CONSTANTS.UPDATE);
//                            }
//                            break;
//                        case 7: //delete and save
//                            if (HbieUtils.getInstance().hbie_FP != null) {
//                                HbieUtils.getInstance().hbie_FP.syncMatcherRecord(id, CONSTANTS.UPDATE);
//                                HbieUtils.getInstance().hbie_FP.syncMatcherRecord(id + "$", CONSTANTS.UPDATE);
//                            }
//                            if (HbieUtils.getInstance().hbie_PP != null) {
//                                HbieUtils.getInstance().hbie_PP.syncMatcherRecord(id, CONSTANTS.UPDATE);
//                            }
//                            if (HbieUtils.getInstance().hbie_FACE != null) {
//                                HbieUtils.getInstance().hbie_FACE.syncMatcherRecord(id, CONSTANTS.UPDATE);
//                            }
//                            if (HbieUtils.getInstance().hbie_IRIS != null) {
//                                HbieUtils.getInstance().hbie_IRIS.syncMatcherRecord(id, CONSTANTS.UPDATE);
//                            }
//                            break;
//                        default:
//                            log.error("dbopTasktype: {} error", dbopTasktype);
//                            break;
//                    }
//                } catch (Exception e) {
//                    log.error("matcher error.", e);
//                    String expt = "matcher error " + e.toString();
//                    dbopTaskDao.update(dbopTask.getTaskidd(), dbopTask.getStatus(), expt.length() > 128 ? expt.substring(0, 128) : expt);
//                }
//            }, dbopTask.getTaskidd());
//            futureList.add(future);
//        }
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
//                        log.info("Dbop-TPP taskid:{} finish.", taskid);
//                    } else {
//                        log.warn("Dbop-TPP taskid:{} update table error.", taskid);
//                        dbopTaskDao.update(taskid, -1, "update " + taskid + " error");
//                    }
//                    break;
//                }
//            }
//        }
//    }

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

        log.info("DBOP_TPP service start. Update status first...");
        dbopTaskDao.updateStatus(CONSTANTS.DBOP_TPP);

        //Take dbop task from db
        new Thread(() -> {
            log.info("DBOP_TPP_DBOPTASKQUEUE_THREAD start ...");
            while (true) {
                List<HafpisDbopTask> dbopTasks = dbopTaskDao.getDbopTasks(CONSTANTS.URGENT_STATUS, CONSTANTS.DBOP_TPP, querynum);
                if (null == dbopTasks || dbopTasks.size() == 0) {
                    dbopTasks = dbopTaskDao.getDbopTasks(status, CONSTANTS.DBOP_TPP, querynum);
                    if (null == dbopTasks || dbopTasks.size() == 0) {
                        CommonUtils.sleep(interval * 1000);
                    } else {
                        for (HafpisDbopTask dbopTask : dbopTasks) {
                            try {
                                dbopTaskQueue.put(dbopTask);
                                dbopTask.setStatus(CONSTANTS.PROCESSING_STATUS);
                                dbopTaskDao.update(dbopTask);
                            } catch (InterruptedException e) {
                                log.error("DBOP_TPP Put {} into dboptask queue error", dbopTask.getTaskidd(), e);
                            }
                        }
                    }
                } else {
                    for (HafpisDbopTask dbopTask : dbopTasks) {
                        try {
                            dbopTaskQueue.put(dbopTask);
                            dbopTask.setStatus(CONSTANTS.PROCESSING_STATUS);
                            dbopTaskDao.update(dbopTask);
                        } catch (InterruptedException e) {
                            log.error("DBOP_TPP put urgent {} into dboptask queue error", dbopTask.getTaskidd(), e);
                        }
                    }
                }
            }
        }, "DBOP_TPP_DBOPTASKQUEUE_THREAD").start();

        new Thread(this::DBOP, "DBOP_TPP_THREAD").start();
    }

    private void DBOP() {
        log.debug("DBOP_TPP_THREAD start ...");
        while (true) {
            HafpisDbopTask dbopTask = null;
            try {
                dbopTask = dbopTaskQueue.take();
                log.debug("Take one dbopTask tpp");
                List<HafpisDbopTask> list = new ArrayList<>();
                list.add(dbopTask);
                doWork(list);
            } catch (InterruptedException e) {
                log.error("Take dbopTask from queue error.", e);
                CommonUtils.sleep(100);
            }
        }
    }

    private Map<Integer, List<NistImg>> initFtpAndLoadNist(HafpisDbopTask dbopTask) {
        Map<Integer, List<NistImg>> result = new HashMap<>();
        ftpClient = new FTPClientUtil();
        String host = ConfigUtils.getConfig("ftp_host");
        if (host != null) {
            ftpClient.setHost(host);
            String portStr = ConfigUtils.getConfig("ftp_port");
            if (portStr == null || portStr.equals("0")) {
                log.info("USe default port 0");
            } else {
                int port = Integer.parseInt(portStr);
                ftpClient.setPort(port);
            }
            String ftp_usr = ConfigUtils.getConfig("ftp_usr");
            if (ftp_usr != null) {
                ftpClient.setUsername(ftp_usr);
                String ftp_pwd = ConfigUtils.getConfig("ftp_pwd");
                if (ftp_pwd != null) {
                    ftpClient.setPassword(ftp_pwd);
                    //TODO add BinaryTransfer/ClientTimeOut/Encoding/PassiveMode configuration
                    String taskidd = dbopTask.getTaskidd();
                    String name = dbopTask.getProbeid();
                    String path = dbopTask.getNistpath();
                    if (path == null) {
                        path = findPathByDb(name);
                        if (path == null) {
                            log.error("Can't find nist path for taskidd: {}", dbopTask.getTaskidd());
                            return null;
                        }
                    }
                    String remoteFilePath = path + "\\" + name + suffix;
                    String localFilePath = ".\\" + name;
                    try (OutputStream outputStream = new FileOutputStream(new File(localFilePath));){
                        ftpClient.get(remoteFilePath, outputStream);
                        result = NistDecoder.decode(localFilePath);
                    } catch (IOException e) {
                        log.error("Can't create local temp file for taskidd:{}", taskidd);
                        return null;
                    } catch (FTPClientException e) {
                        log.error("Can't get nist file from ftp server for taskidd: {}", taskidd);
                        return null;
                    } finally {
                        File file = new File(localFilePath);
                        if (file.exists()) {
                            try {
                                file.delete();
                            } catch (Exception e) {
                                log.warn("Can't delete the temp file {}", localFilePath);
                            }
                        }
                    }
                } else {
                    log.error("Can't find ftp password config");
                    System.exit(-1);
                }
            } else {
                log.error("Can't find ftp user config");
                System.exit(-1);
            }
        } else {
            log.error("Can't find ftp host config");
            System.exit(-1);
        }
        return result;
    }

    private String findPathByDb(String personid) {
        HafpisHtppKey htppKey = htppKeyDao.get(personid);
        if (htppKey != null) {
            String devname = htppKey.getDevname();
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

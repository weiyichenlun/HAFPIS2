package hbie2.HAFPIS2.Service.Impl;

import hbie2.HAFPIS2.Dao.HafpisDbopTaskDao;
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
 * 创建时间:2018/3/29
 * 最后修改时间:2018/3/29
 */
public class HafpisDbopPlpService extends AbstractService implements Runnable {
    private Logger log = LoggerFactory.getLogger(HafpisDbopPlpService.class);
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
        dbopTaskDao = new HafpisDbopTaskDao();
        executorService = Executors.newFixedThreadPool(CONSTANTS.NCORES > 8 ? 8 : CONSTANTS.NCORES);    }

    @Override
    public <T> void doWork(List<T> dbopTasks) {
        List<Future<String>> futureList = new ArrayList<>();
        for (int i = 0; i < dbopTasks.size(); i++) {
            HafpisDbopTask dbopTask = (HafpisDbopTask) dbopTasks.get(i);
            dbopTaskDao.update(dbopTask.getTaskidd(), 4);
            Future<String> future = executorService.submit(() -> {
                try {
                    int dbopTaskType = dbopTask.getTasktype();
                    String pid = dbopTask.getProbeid();
                    switch (dbopTaskType) {
                        case 6:
                            if (HbieUtils.getInstance().hbie_PLP != null) {
                                HbieUtils.getInstance().hbie_PLP.syncMatcherRecord(pid, CONSTANTS.DELETE);
                            }
                            break;
                        case 5:
                        case 7:
                            if (HbieUtils.getInstance().hbie_PLP != null) {
                                HbieUtils.getInstance().hbie_PLP.syncMatcherRecord(pid, CONSTANTS.UPDATE);
                            }
                            break;
                        default:
                            log.error("dbopTaskType error {}.", dbopTaskType);
                            break;
                    }
                }catch (Exception e) {
                    log.warn("matcher error: ", e);
                    String expt = "matcher error" + e.toString();
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
                        log.info("Dbop-PLP taskid:{} finish.", taskid);
                    } else {
                        log.warn("Dbop-PLP taskid:{} update table error.", taskid);
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
            dbopTaskDao.updateStatus(CONSTANTS.DBOP_PLP);
            log.info("DbopPlp executorservice is shutting down");
        }));

        while (true) {
            List<HafpisDbopTask> dbopTasks = dbopTaskDao.getDbopTasks(CONSTANTS.URGENT_STATUS, CONSTANTS.DBOP_PLP, querynum);
            if (dbopTasks == null || dbopTasks.size() == 0) {
                dbopTasks = dbopTaskDao.getDbopTasks(status, CONSTANTS.DBOP_PLP, querynum);
                if (dbopTasks == null || dbopTasks.size() == 0) {
                    CommonUtils.sleep(interval * 1000);
                } else {
                    doWork(dbopTasks);
                }
            } else {
                //增加处理加急作业流程
                log.info("Get urgent DbopPlp task...");
                doWork(dbopTasks);
            }
        }
    }
}

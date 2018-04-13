package hbie2.dao.jdbc;

import hbie2.HAFPIS2.Entity.HafpisMatcherTask;
import hbie2.HAFPIS2.Utils.CONSTANTS;
import hbie2.HAFPIS2.Utils.CommonUtils;
import hbie2.HbieConfig;
import hbie2.InfoCol;
import hbie2.Record;
import hbie2.TaskSearch;
import hbie2.TaskVerify;
import hbie2.dao.MatcherDAO;
import hbie2.dao.mongodb.MatcherDAOMongoDB;
import hbie2.nist.nistType.NistImg;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/15
 * 最后修改时间:2018/3/15
 */
public class TenFpMatcherDaoJDBC implements MatcherDAO{
    private Logger log = LoggerFactory.getLogger(TenFpMatcherDaoJDBC.class);
    private String jdbc_driver;
    private String jdbc_url;
    private String jdbc_usr;
    private String jdbc_pwd;
    private String jdbc_table;
    private String task_table;
    private int ftp_enable;
    private String ftp_host;
    private int ftp_port;
    private String ftp_usr;
    private String ftp_pwd;
    private MatcherDAOMongoDB dao;
    private Connection conn = null;
    private QueryRunner queryRunner = null;
    private static final String[] RPTABLES = {"HAFPIS_RPMNT_01", "HAFPIS_RPMNT_02",
            "HAFPIS_RPMNT_03", "HAFPIS_RPMNT_04", "HAFPIS_RPMNT_05", "HAFPIS_RPMNT_06",
            "HAFPIS_RPMNT_07", "HAFPIS_RPMNT_08", "HAFPIS_RPMNT_09", "HAFPIS_RPMNT_10"};
    private static final String[] FPTABLES = {"HAFPIS_FPMNT_01", "HAFPIS_FPMNT_02",
            "HAFPIS_FPMNT_03", "HAFPIS_FPMNT_04", "HAFPIS_FPMNT_05", "HAFPIS_FPMNT_06",
            "HAFPIS_FPMNT_07", "HAFPIS_FPMNT_08", "HAFPIS_FPMNT_09", "HAFPIS_FPMNT_10"};


    @Override
    public void init(Properties prop) {
        this.jdbc_driver = prop.getProperty("jdbc_driver", "oracle.jdbc.OracleDriver");
        if (null == this.jdbc_driver) {
            log.warn("No jdbc_driver config. Use default: oracle.jdbc.OracleDriver");
        } else {
            this.jdbc_url = prop.getProperty("jdbc_uri");
            if (null == this.jdbc_url) {
                throw new IllegalArgumentException("No jdbc_url config.");
            } else {
                this.jdbc_usr = prop.getProperty("jdbc_usr");
                if (null == this.jdbc_usr) {
                    throw new IllegalArgumentException("No jdbc_usr config.");
                } else {
                    this.jdbc_pwd = prop.getProperty("jdbc_pwd");
                    if (null == this.jdbc_pwd) {
                        throw new IllegalArgumentException("No jdbc_pwd config");
                    } else {
                        this.jdbc_table = prop.getProperty("jdbc_table");
                        if (null == this.jdbc_table) {
                            throw new IllegalArgumentException("No jdbc_table config");
                        } else {
                            BasicDataSource dataSource;
                            try {
                                Class.forName(this.jdbc_driver);
                            } catch (ClassNotFoundException e) {
                                log.error("driver class: {} not found: ",this.jdbc_driver);
                                System.exit(-1);
                            }
                            dataSource = new BasicDataSource();
                            dataSource.setDriverClassName(this.jdbc_driver);
                            dataSource.setUrl(this.jdbc_url);
                            dataSource.setUsername(this.jdbc_usr);
                            dataSource.setPassword(this.jdbc_pwd);
                            try {
                                this.conn = dataSource.getConnection();
                                this.queryRunner = new QueryRunner(dataSource);
                                this.dao = new MatcherDAOMongoDB();
                                this.dao.init(prop);
                            } catch (SQLException e) {
                                log.error("connect to db error. ", e);
                                System.exit(-1);
                            }
//
//                            this.task_table = prop.getProperty("matcher_task_table");
//                            if (this.task_table == null) {
//                                log.error("no matcher_task_table config");
//                                System.exit(-1);
//                            }
                            String ftp_enable_str = prop.getProperty("ftp_enable");
                            if (null == ftp_enable_str) {
                                log.warn("Ftp_enable is not config.");
                                log.warn("Use default value: 0");
                                ftp_enable = 0;
                            } else {
                                try {
                                    ftp_enable = Integer.parseInt(ftp_enable_str);
                                } catch (NumberFormatException e) {
                                    log.error("Ftp_enable config error. ");
                                    ftp_enable = 0;
                                }
                            }

                            if (ftp_enable == 1) {
                                this.ftp_host = prop.getProperty("ftp_host");
                                if (null == ftp_host) {
                                    throw new IllegalArgumentException("no ftp_host config");
                                } else {
                                    String ftp_port_str = prop.getProperty("ftp_port");
                                    if (ftp_port_str == null) {
                                        log.warn("No ftp_port config. Use default port: 0");
                                        this.ftp_port = 0;
                                    } else {
                                        try {
                                            this.ftp_port = Integer.parseInt(ftp_port_str);
                                        } catch (NumberFormatException e) {
                                            log.error("ftp_port is not a valid number. Use default port: 0");
                                            this.ftp_port = 0;
                                        }
                                        this.ftp_usr = prop.getProperty("ftp_usr");
                                        if (null == ftp_usr) {
                                            throw new IllegalArgumentException("no ftp_usr config");
                                        } else {
                                            this.ftp_pwd = prop.getProperty("ftp_pwd");
                                            if (null == ftp_pwd) {
                                                throw new IllegalArgumentException("no fpt_pwd config");
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

    }


    @Nullable
    @Override
    public Record fetchRecordToProcess(String magic) {
        while (true) {
            //find pengding record from HAFPIS_MATCHER_TASK
            //对于平面指纹pid+$怎么处理
//            String sql = "select * from (select t1.probeid, t1.status, t1.nistpath, t2.enrolldate from " +
//                    this.task_table + " t1, " + this.jdbc_table + " t2 where t1.probeid=t2.personid and t1.status=? " +
//                    "and t1.datatype=? order by t2.enrolldate) where rownum <= 1 for update";
            String sql = "select * from (select * from HAFPIS_MATCHER_TASK where status=? and datatype=? order by createtime) " +
                    "where rownum <=1 for update";
            try {
                HafpisMatcherTask matcherTask = this.queryRunner.query(sql,
                        new BeanHandler<>(HafpisMatcherTask.class), Record.Status.Pending.name(), CONSTANTS.MATCHER_DATATYPE_TP);
                String update_sql = "update HAFPIS_MATCHER_TASK set status=? where probeid=? and datatype=?";
                String probeid = matcherTask.getKey().getProbeid();
                boolean is_fp = probeid.endsWith("$");
                this.queryRunner.update(update_sql, Record.Status.Processing.name(), probeid, CONSTANTS.MATCHER_DATATYPE_TP);
                Record record = new Record();
                record.setId(probeid);
                record.setCreateTime(new Date()); //TODO createtime should be comfirmes
                String path = matcherTask.getNistpath();
                String filename = is_fp ? probeid.substring(0, probeid.length() - 1) : probeid;
                log.debug("Path: {} filename: {}", path, filename);
                Map<Integer, List<NistImg>> nistImgMap = Utils.initFtpAndLoadNist(this.ftp_host, this.ftp_port,
                        this.ftp_usr, this.ftp_pwd, path, filename);
                if (is_fp) {
                    List<NistImg> fp_list = nistImgMap.get(16); //type 16: flat image
                    if (fp_list == null || fp_list.size() == 0) {
                        //TODO alse need to concern the four-fingers in type 14 list
                        log.warn("probeid {}: no flat finger image in nist file", probeid);
                    } else {
                        byte[][] imgs = new byte[10][];
                        fp_list.forEach(nistImg -> {
                            imgs[nistImg.idc - 1] = nistImg.imgData;
                        });
                        record.setImages(imgs);
                        return record;
                    }
                } else {
                    List<NistImg> rp_list = nistImgMap.get(14); //type 14: roll image
                    if (rp_list == null || rp_list.size() == 0) {
                        //TODO alse need to concern the four-fingers in type 14 list
                        log.warn("probeid {}: no roll finger image in nist file", probeid);
                    } else {
                        byte[][] imgs = new byte[10][];
                        rp_list.forEach(nistImg -> {
                            if (nistImg.idc >= 1 && nistImg.idc <= 10) {
                                imgs[nistImg.idc - 1] = nistImg.imgData;
                            }
                        });
                        record.setImages(imgs);
                        return record;
                    }
                }
            } catch (SQLException e) {
                log.error("Select error from HAFPIS_MATCHER_TASK", e);
                CommonUtils.sleep(100);
            }
        }
    }

    @Nullable
    @Override
    public HbieConfig selectHbieConfig() {
        return dao.selectHbieConfig();
    }

    @NotNull
    @Override
    public Date register(String s, String s1, int i, int i1) {
        return dao.register(s, s1, i, i1);
    }

    @Override
    public Date ping(String s, String s1) {
        return dao.ping(s, s1);
    }

    @NotNull
    @Override
    public List<Record> selectRecords(Collection<String> ids) {
        InfoCol[] info_cols = dao.cfg.getInfoCols();
        List<Record> res = new ArrayList<>();
        ids.forEach(id -> {
            Record rec = new Record();
            String pid = id;
            boolean change = false;
            if (id.endsWith("$")) {
                pid = id.substring(0, id.length() - 1);
                change = true;
            }
            rec.setId(id);
            rec.getFields().put("id", id);
            rec.getFields().put("flag", change ? (byte) 1 : (byte) 0);
            StringBuilder sb = new StringBuilder("select enrolldate");
            //info_cols[0]: id:String, info_cols[1]: flag:byte
            for (int i = 2; i < info_cols.length; i++) {
                sb.append(", ").append(info_cols[i].getName());
            }

            sb.append(" from ").append(this.jdbc_table).append(" where personid=?");
            try {
                Map<String, Object> map = this.queryRunner.query(sb.toString(), new MapHandler(), pid);
                rec.setCreateTime(Utils.getDateFromStr((String) map.get("ENROLLDATE")));
                for (int i = 2; i < info_cols.length; i++) {
                    String temp = info_cols[i].getName();
                    rec.getFields().put(temp, (Serializable) map.get(temp));
                }
            } catch (SQLException e) {
                log.error("select error. perosnid: {}/table: {}", id, this.jdbc_table, e);
            }
            byte[][] features = new byte[10][];
            for (int i = 0; i < 10; i++) {
                StringBuilder fea_sql = new StringBuilder();
                fea_sql.append("select mntdata from ");
                fea_sql.append(change ? FPTABLES[i] : RPTABLES[i]);
                fea_sql.append(" where personid=?");
                try {
                    int finalI = i;
                    this.queryRunner.query(fea_sql.toString(), rs -> {
                        if (rs.next()) {
                            byte[] fea = rs.getBytes("MNTDATA");
                            if (fea != null && fea.length > 0) {
                                features[finalI] = new byte[6144];
                                System.arraycopy(fea, 0, features[finalI], 0, fea.length);
                                System.arraycopy(fea, 0, features[finalI], 3072, fea.length);
                            } /*else {
                                log.debug("id: {}, index: {}", id, finalI);
                            }*/
                        }
                        return null;
                    }, pid);
                } catch (SQLException e) {
                    log.error("select error. perosnid: {}/feature: {} ", id, (i + 1), e);
                }
            }
            if (checkNull(features)) {
                log.warn("get null features for personid: {}", id);
            } else {
                rec.setFeatures(features);
                res.add(rec);
            }
        });
        return res;
    }

    private boolean checkNull(byte[][] bytes) {
        int cnt = 0;
        if (bytes == null) return true;
        for (byte[] aByte : bytes) {
            if (aByte == null || aByte.length == 0) cnt++;
        }
        return cnt == 10;
    }

    @Nullable
    @Override
    public TaskVerify fetchVerifyToProcess(String s) {
        return dao.fetchVerifyToProcess(s);
    }

    @Override
    public void finishVerify(TaskVerify taskVerify) {
        dao.finishVerify(taskVerify);
    }

    @Nullable
    @Override
    public Record fetchRecordToTrain(String magic) {
        String sql = "select * from (select * from HAFPIS_MATCHER_TASK where status=? and datatype=? order by " +
                "create_time) where rownum <= 1";
        try {
            HafpisMatcherTask matcherTask = this.queryRunner.query(sql, new BeanHandler<>(HafpisMatcherTask.class),
                    Record.Status.Processed.name(), CONSTANTS.MATCHER_DATATYPE_TP);
            if (matcherTask != null) {
                String pid = matcherTask.getKey().getProbeid();
                boolean is_fp = pid.endsWith("$");
                String name = is_fp ? pid.substring(0, pid.length() - 1) : pid;
                Record record = new Record();
                record.setId(matcherTask.getKey().getProbeid());
                // get features
                byte[][] features = new byte[10][];
                for (int i = 0; i < 10; i++) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("select * from (select mntdata from ");
                    sb.append(is_fp ? FPTABLES[i] : RPTABLES[i]);
                    sb.append(" where personid=?");
                    features[i] = this.queryRunner.query(sb.toString(), (ResultSetHandler<byte[]>) rs -> {
                        if (rs.next()) {
                            return rs.getBytes("mntdata");
                        }
                        return new byte[0];
                    }, name);
                }
                record.setFeatures(features);
                return record;
            }
            return null;
        } catch (SQLException e) {
            log.error("select record to train error.", e);
            return null;
        }
    }

    @Override
    public void finishRecordProcessed(Record record) {
        if (record.getStatus().compareTo(Record.Status.Processed) == 0 ||
                record.getStatus().compareTo(Record.Status.Trained) == 0) {
            String pid = record.getId();
            boolean is_fp = pid.endsWith("$");
            byte[][] features = record.getFeatures();
            String sql = "update HAFPIS_MATCHER_TASK set status=? where probeid=? and datatype=?";
            try {
                this.queryRunner.update(sql, Record.Status.Processed.name(), record.getId(), CONSTANTS.MATCHER_DATATYPE_TP);

                // update features in oracle tables
                for (int i = 0; i < features.length; i++) {
                    String updateSql;
                    if (is_fp) {
                        pid = pid.substring(0, pid.length() - 1);
                        updateSql = "merge into " + FPTABLES[i] + " using (select ? as pid, ? as mnt, ? as ver " +
                                "from dual) t on (probeid=t.pid) when not matched then insert (probeid, dataver, " +
                                "mntdata) values(t.pid, t.ver, t.mnt) when matched then update set mntdata=t.mnt";
                    } else {
                        updateSql = "merge into " + RPTABLES[i] + " using (select ? as pid, ? as mnt, ? as ver " +
                                "from dual) t on (probeid=t.pid) when not matched then insert (probeid, dataver, " +
                                "mntdata) values(t.pid, t.ver, t.mnt) when matched then update set mntdata=t.mnt";
                    }
                    log.debug("updateSql is {}", updateSql);
                    this.queryRunner.update(updateSql, pid, 0, features[i]);
                }
            } catch (SQLException e) {
                log.error("update record status error. {}", record.getId());
            }
        } else {
            throw new IllegalStateException("Wrong record status when finish");
        }
    }

    @Override
    public void finishRecordTrained(Record record) {
        dao.finishRecordTrained(record);
    }

    @Nullable
    @Override
    public TaskSearch fetchSearchToProcess(String s) {
        return dao.fetchSearchToProcess(s);
    }

    @Override
    public void prepareSearchToSearch(TaskSearch taskSearch, int i) {
        dao.prepareSearchToSearch(taskSearch, i);
    }

    @Nullable
    @Override
    public TaskSearch fetchSearchToSearch(String s, int i, int i1) {
        return dao.fetchSearchToSearch(s, i, i1);
    }

    @Override
    public boolean finishSearch(TaskSearch taskSearch, String s, int i, int i1) {
        return dao.finishSearch(taskSearch, s, i, i1);
    }

    @Override
    public void setRecordError(String s, Date date, String s1) {
        dao.setRecordError(s, date, s1);
    }

    @Override
    public void setVerifyError(String s, String s1) {
        dao.setVerifyError(s, s1);
    }

    @Override
    public void setSearchError(String s, String s1) {
        dao.setSearchError(s, s1);
    }

    @Nullable
    @Override
    public byte[] getRecordImage(String s, int i) {
        return dao.getRecordImage(s, i);
    }


}

package hbie2.dao.jdbc;

import hbie2.HAFPIS2.Entity.HafpisRecordStatus;
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
import org.apache.commons.dbutils.handlers.MapHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
 * 创建时间:2018/5/8
 * 最后修改时间:2018/5/8
 */
public class FourPalmMatcherDaoJDBC implements MatcherDAO {
    private Logger log = LoggerFactory.getLogger(FourPalmMatcherDaoJDBC.class);
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
    private static final String[] PMTABLES = {"HAFPIS_PMMNT_01","HAFPIS_PMMNT_05", "HAFPIS_PMMNT_06", "HAFPIS_PMMNT_10"};
    @Nullable
    @Override
    public Record fetchRecordToProcess(String magic) {
        while (true) {
            //find pending record from HAFPIS_RECORD_STATUS
            PreparedStatement ps = null;
            try (Connection conn = this.queryRunner.getDataSource().getConnection()){
                conn.setAutoCommit(false);
                String sql = "select * from (select * from HAFPIS_RECORD_STATUS where status=?, and datatype=? order by " +
                        "createtime asc) where rownum<=1";
                ps = conn.prepareStatement(sql);
                ps.setString(1, Record.Status.Pending.name());
                ps.setInt(2, CONSTANTS.RECORD_DATATYPE_PP);
                ResultSet rs = ps.executeQuery();
                HafpisRecordStatus recordStatus = Utils.convert(rs);

                if (recordStatus == null) {
                    CommonUtils.sleep(100);
                } else {
                    String updateSql = "update HAFPIS_RECORD_STATUS set status=?, magic=? where PID=? and datatype=?";
                    String probeid = recordStatus.getKey().getProbeid();
                    ps = conn.prepareStatement(updateSql);
                    ps.setString(1, Record.Status.Processing.name());
                    ps.setString(2, magic);
                    ps.setString(3, probeid);
                    ps.setInt(4, CONSTANTS.RECORD_DATATYPE_PP);
                    ps.executeUpdate();
                    conn.commit();

                    Record record = new Record();
                    record.setId(probeid);
                    record.setCreateTime(Utils.getDateFromStr(recordStatus.getCreatetime()));
                    String path = recordStatus.getNistpath();
                    String filepath = null;
                    if (path.startsWith("/")) {
                        filepath = path + "/";
                    } else {
                        filepath = "./";
                    }
                    log.debug("filepath is {}", filepath);
                    log.debug("Path: {}, filename: {}", path, probeid);
                    Map<Integer, List<NistImg>> nistImgMap = Utils.initFtpAndLoadNistByURL(this.ftp_host, this.ftp_port,
                            this.ftp_usr, this.ftp_pwd, filepath + probeid + ".nist");

                    List<NistImg> pp_list = nistImgMap.get(15); //type 15: four palm 右正右侧左正左侧
                    if (pp_list == null || pp_list.size() == 0) {
                        log.warn("probeid {}: no palm image in nist file", probeid);
                        return null;
                    } else {
                        byte[][] imgs = new byte[4][];
                        for (int i = 0; i < pp_list.size(); i++) {
                            NistImg nistImg = pp_list.get(i);
                            imgs[CONSTANTS.ppNistOrder[nistImg.idc - 31]] = nistImg.imgData;
                        }
                        record.setImages(imgs);
                        return record;
                    }
                }

            } catch (SQLException e) {
                log.error("fetch record to process error. magic {}", magic, e);
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                }
                return null;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                }
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                    }
                }
            }
        }
    }

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

    @Nullable
    @Override
    public HbieConfig selectHbieConfig() {
        return null;
    }

    @NotNull
    @Override
    public Date register(String s, String s1, int i, int i1) {
        return null;
    }

    @Nullable
    @Override
    public Date ping(String s, String s1) {
        return null;
    }

    @NotNull
    @Override
    public List<Record> selectRecords(Collection<String> ids) {
        InfoCol[] info_cols = dao.cfg.getInfoCols();
        List<Record> res = new ArrayList<>();
        ids.forEach(id -> {
            Record rec = new Record();
            rec.setId(id);
            rec.getFields().put("id", id);
            StringBuilder sb = new StringBuilder("select enrolldate");
            //info_cols[0]: id:String
            for (int i = 1; i < info_cols.length; i++) {
                sb.append(", ").append(info_cols[i].getName());
            }
            sb.append(" from ").append(this.jdbc_table).append(" where personid=?");
            try {
                Map<String, Object> map = this.queryRunner.query(sb.toString(), new MapHandler(), id);
                rec.setCreateTime(Utils.getDateFromStr((String) map.get("ENROLLDATE")));
                for (int i = 1; i < info_cols.length; i++) {
                    String temp = info_cols[i].getName();
                    rec.getFields().put(temp, (Serializable) map.get(temp));
                }
            } catch (SQLException e) {
                log.error("select error. personid {}, table {}", id, this.jdbc_table, e);
            }
            byte[][] features = new byte[4][];
            for (int i = 0; i < 4; i++) {
                StringBuilder fea_sql = new StringBuilder();
                fea_sql.append("select mntdata from ");
                fea_sql.append(PMTABLES[i]);
                fea_sql.append(" where personid=?");
                try {
                    int finalI = i;
                    this.queryRunner.query(fea_sql.toString(), rs -> {
                        if (rs.next()) {
                            byte[] fea = rs.getBytes("MNTDATA");
                            if (fea != null && fea.length > 0) {
                                features[CONSTANTS.feaOrder[finalI]] = fea;
                            } else {
                                log.debug("id {}, index {}", id, finalI);
                            }
                        }
                        return null;
                    }, id);
                } catch (SQLException e) {
                    log.error("select error. personid/feature_idx: {}/{}", id, (i+1), e);
                }
            }
            if (checkNull(features)) {
                log.warn("get null features for personid {}", id);
            } else {
                rec.setFeatures(features);
                res.add(rec);
            }
        });
        return res;
    }

    private boolean checkNull(byte[][] feas) {
        int cnt = 0;
        if (feas == null) return true;
        for (byte[] fea : feas) {
            if (fea == null || fea.length == 0) cnt++;
        }
        return cnt == 4;
    }

    @Nullable
    @Override
    public TaskVerify fetchVerifyToProcess(String s) {
        return null;
    }

    @Override
    public void finishVerify(TaskVerify taskVerify) {

    }

    @Nullable
    @Override
    public Record fetchRecordToTrain(String magic) {
        PreparedStatement ps = null;
        try (Connection conn = this.queryRunner.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            String sql = "select * from (select * from HAFPIS_RECORD_STATUS where status=? and datatype=? order by " +
                    "create_time) where rownum<=1";
            ps = conn.prepareStatement(sql);
            ps.setString(1, Record.Status.Processed.name());
            ps.setInt(2, CONSTANTS.RECORD_DATATYPE_PP);
            ResultSet rs = ps.executeQuery();
            HafpisRecordStatus recordStatus = Utils.convert(rs);

            if (recordStatus == null) {
                return null;
            } else {
                String updateSql = "update HAFPIS_RECORD_STATUS set status=?, magic=? where PID=? and datatype=?";
                String pid = recordStatus.getKey().getProbeid();
                ps = conn.prepareStatement(updateSql);
                ps.setString(1, Record.Status.Training.name());
                ps.setString(2, magic);
                ps.setString(3, pid);
                ps.setInt(4, CONSTANTS.RECORD_DATATYPE_PP);
                ps.executeUpdate();

                Record record = new Record();
                record.setId(pid);
                byte[][] features = new byte[4][];
                for (int i = 0; i < 4; i++) {
                    StringBuilder sb = new StringBuilder("select mntdata from ");
                    sb.append(PMTABLES[i]).append(" where personid=?");
                    ps = conn.prepareStatement(sb.toString());
                    ps.setString(1, pid);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        features[CONSTANTS.feaOrder[i]] = rs.getBytes("mntdata");
                    }
                }

                conn.commit();
                record.setFeatures(features);
                record.setCreateTime(Utils.getDateFromStr(recordStatus.getCreatetime()));
                return record;
            }
        } catch (SQLException e) {
            log.error("select record to train error. ", e);
            try {
                conn.rollback();
            } catch (SQLException e1) {
            }
            return null;
        }finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {

                }
            }
        }
    }

    @Override
    public void finishRecordProcessed(Record record) {
        if (record.getStatus().compareTo(Record.Status.Processed) == 0 ||
                record.getStatus().compareTo(Record.Status.Trained) == 0) {
            PreparedStatement ps = null;
            String pid = record.getId();
            byte[][] features = record.getFeatures();
            try (Connection conn = this.queryRunner.getDataSource().getConnection()) {
                conn.setAutoCommit(false);
                // update matcher_task
                String sql = "update HAFPIS_RECORD_STATUS set status=? where PID=? and datatype=? and status=?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, record.getStatus().name());
                ps.setString(2, pid);
                ps.setInt(3, CONSTANTS.RECORD_DATATYPE_PP);
                ps.setString(4, Record.Status.Processing.name());
                ps.executeUpdate();

                //update mnt table
                for (int i = 0; i < 4; i++) {
                    String updateSql = "update " + PMTABLES[i] + " set mntdata=? where personid=?";
                    ps = conn.prepareStatement(updateSql);
                    ps.setBytes(1, features[CONSTANTS.feaOrder[i]]);
                    ps.setString(2, pid);
                    ps.executeUpdate();
                    log.debug("updateSql is {}", updateSql);
                }
                conn.commit();
            } catch (SQLException e) {
                log.error("Update HAFPIS_RECORD_STATUS and related mnt table error. {}", pid, e);
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                }
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                }
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {

                    }
                }
            }
        } else {
            throw new IllegalStateException("Wrong record status wher finish");
        }
    }

    @Override
    public void finishRecordTrained(Record record) {
        PreparedStatement ps = null;
        String pid = record.getId();
        byte[][] features = record.getFeatures();
        try (Connection conn = this.queryRunner.getDataSource().getConnection()) {
            conn.setAutoCommit(false);

            //update matcher_task
            String sql = "update HAFPIS_RECORD_STATUS set status=? where PID=? and datatype=? and status=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, Record.Status.Trained.name());
            ps.setString(2, pid);
            ps.setInt(3, CONSTANTS.RECORD_DATATYPE_PP);
            ps.setString(4, Record.Status.Training.name());
            ps.executeUpdate();

            for (int i = 0; i < 4; i++) {
                String updateSql = "update " + PMTABLES[i] + " set mntdata=? where personid=?";
                ps = conn.prepareStatement(updateSql);
                ps.setBytes(1, features[CONSTANTS.feaOrder[i]]);
                ps.setString(2, pid);
                ps.executeUpdate();
                log.debug("updatesql is {}", updateSql);
            }

            conn.commit();
        } catch (SQLException e) {
            log.error("Update HAFPIS_RECORD_STATUS and related mnt table error. {}", pid, e);
            try {
                conn.rollback();
            } catch (SQLException e1) {

            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {

            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    @Nullable
    @Override
    public TaskSearch fetchSearchToProcess(String s) {
        return null;
    }

    @Override
    public void prepareSearchToSearch(TaskSearch taskSearch, int i) {

    }

    @Nullable
    @Override
    public TaskSearch fetchSearchToSearch(String s, int i, int i1) {
        return null;
    }

    @Override
    public boolean finishSearch(TaskSearch taskSearch, String s, int i, int i1) {
        return false;
    }

    @Override
    public void setRecordError(String s, Date date, String s1) {

    }

    @Override
    public void setVerifyError(String s, String s1) {

    }

    @Override
    public void setSearchError(String s, String s1) {

    }

    @Nullable
    @Override
    public byte[] getRecordImage(String s, int i) {
        return new byte[0];
    }
}

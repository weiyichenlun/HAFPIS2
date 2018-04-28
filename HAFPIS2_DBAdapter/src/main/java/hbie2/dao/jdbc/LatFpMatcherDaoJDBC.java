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
 * 创建时间:2018/3/15
 * 最后修改时间:2018/3/15
 */
public class LatFpMatcherDaoJDBC implements MatcherDAO{
    private Logger log = LoggerFactory.getLogger(LatFpMatcherDaoJDBC.class);
    private String jdbc_driver;
    private String jdbc_url;
    private String jdbc_usr;
    private String jdbc_pwd;
    private String jdbc_table;
    private int ftp_enable;
    private String ftp_host;
    private int ftp_port;
    private String ftp_usr;
    private String ftp_pwd;
    private MatcherDAOMongoDB dao;
    private Connection conn = null;
    private QueryRunner queryRunner = null;
    private static final String LPPTABLE = "HAFPIS_LPMNT";

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
    public Record fetchRecordToProcess(String magic) {
        while (true) {
            PreparedStatement ps = null;
            try (Connection conn = this.queryRunner.getDataSource().getConnection()) {
                conn.setAutoCommit(false);
                String sql = "select * from (select * from HAFPIS_RECORD_STATUS where status=? and datatype=? order by " +
                        "createtime asc) where rownum<=1";
                ps = conn.prepareStatement(sql);
                ps.setString(1, Record.Status.Pending.name());
                ps.setInt(2, CONSTANTS.RECORD_DATATYPE_LPP);
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
                    ps.setInt(4, CONSTANTS.RECORD_DATATYPE_LPP);
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
                    log.debug("filepath is ", filepath);
                    log.debug("Path: {} filename: {}", path, probeid);
//                    Map<Integer, List<NistImg>> nistImgMap = Utils.initFtpAndLoadNist(this.ftp_host, this.ftp_port,
//                            this.ftp_usr, this.ftp_pwd, path, filename);
                    Map<Integer, List<NistImg>> nistImgMap = Utils.initFtpAndLoadNistByURL(this.ftp_host, this.ftp_port,
                            this.ftp_usr, this.ftp_pwd, filepath + probeid + ".nist");

                    List<NistImg> latfp_list = nistImgMap.get(13); //type 13: latfp
                    if (latfp_list == null || latfp_list.size() == 0) {
                        log.warn("probeid {}: no roll finger image in nist file", probeid);
                        return null;
                    } else {
                        byte[] img = latfp_list.get(0).imgData;
                        record.setImage(img);
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
    //TODO 使用临时表加快查询速度
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
            sb.append(" from ").append(this.jdbc_table).append(" where latentid=?");
            try {
                Map<String, Object> map = this.queryRunner.query(sb.toString(), new MapHandler(), id);
                rec.setCreateTime(Utils.getDateFromStr((String) map.get("ENROLLDATE")));
                for (int i = 1; i < info_cols.length; i++) {
                    String temp = info_cols[i].getName();
                    rec.getFields().put(temp, (Serializable) map.get(temp));
                }
            } catch (SQLException e) {
                log.error("select error. latentid: {}/table: {}", id, this.jdbc_table, e);
            }
            String fea_sql = "select mntdata from " + LPPTABLE + " where latentid = ?";
            try {
                this.queryRunner.query(fea_sql, rs -> {
                    byte[] feature;
                    if (rs.next()) {
                        byte[] fea = rs.getBytes("MNTDATA");
                        if (fea == null) {
                            log.warn("Get null feature for latentid: {}", id);
                        } else if (fea.length == 3072) {
                            feature = new byte[3072 * 3];
                            System.arraycopy(fea, 0, feature, 0, 3072);
                            System.arraycopy(fea, 0, feature, 3072, 3072);
                            System.arraycopy(fea, 0, feature, 3072 * 2, 3072);
                            rec.setFeature(feature);
                            res.add(rec);
                        } else if (fea.length == 6304) {
                            feature = new byte[3072 * 3];
                            System.arraycopy(fea, 160, feature, 3072 * 2, 3072);
                            System.arraycopy(fea, 160 + 3072, feature, 0, 3072);
                            System.arraycopy(fea, 160 + 3072, feature, 3072, 3072);
                            rec.setFeature(feature);
                            res.add(rec);
                        }
                    }
                    return null;
                }, id);
            } catch (SQLException e) {
                log.error("select error. latentid: {}", id, e);
            }
        });
        return res;
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
        PreparedStatement ps = null;
        try (Connection conn = this.queryRunner.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            String sql = "select * from (select * from HAFPIS_RECORD_STATUS where status=? and datatype=? order by " +
                    "createtime) where rownum <= 1";
            ps = conn.prepareStatement(sql);
            ps.setString(1, Record.Status.Processed.name());
            ps.setInt(2, CONSTANTS.RECORD_DATATYPE_LPP);
            ResultSet rs = ps.executeQuery();
            HafpisRecordStatus recordStatus = Utils.convert(rs);

            if (recordStatus == null) {
                CommonUtils.sleep(100);
            } else {
                String updateSql = "update HAFPIS.HAFPIS_RECORD_STATUS set status=?, magic=? where PID=? and datatype=?";
                String probeid = recordStatus.getKey().getProbeid();
                ps = conn.prepareStatement(updateSql);
                ps.setString(1, Record.Status.Training.name());
                ps.setString(2, magic);
                ps.setString(3, probeid);
                ps.setInt(4, CONSTANTS.RECORD_DATATYPE_LPP);
                ps.executeUpdate();

                String mntSql = "select mntdata from " + LPPTABLE + " where latentid=?";
                ps = conn.prepareStatement(mntSql);
                ps.setString(1, probeid);
                rs = ps.executeQuery();
                byte[] feature = new byte[0];
                if (rs.next()) {
                    feature = rs.getBytes("mntdata");
                }
                conn.commit();
                Record record = new Record();
                record.setId(probeid);
                record.setFeature(feature);
                record.setCreateTime(Utils.getDateFromStr(recordStatus.getCreatetime()));
                return record;
            }
            return null;
        } catch (SQLException e) {
            log.error("select record to train error.", e);
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

    @Override
    public void finishRecordProcessed(Record record) {
        if (record.getStatus().compareTo(Record.Status.Processed) == 0 ||
                record.getStatus().compareTo(Record.Status.Trained) == 0) {
            PreparedStatement ps = null;
            String pid = record.getId();
            byte[] feature = record.getFeature();
            try (Connection conn = this.queryRunner.getDataSource().getConnection()){
                conn.setAutoCommit(false);
                // update matcher_task
                String sql = "update HAFPIS_RECORD_STATUS set status=? where PID=? and datatype=? and status=?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, record.getStatus().name());
                ps.setString(2, pid);
                ps.setInt(3, CONSTANTS.RECORD_DATATYPE_LPP);
                ps.setString(4, Record.Status.Processing.name());
                ps.executeUpdate();

                //update mnt table
                String updateSql = "update " + LPPTABLE + " set mntdata=? where latentid=?";
                ps = conn.prepareStatement(updateSql);
                ps.setBytes(1, feature);
                ps.setString(2, pid);
                ps.executeUpdate();
                log.debug("updateSql is {}", updateSql);

                conn.commit();
            } catch (SQLException e) {
                log.error("Update HAFPIS_RECORD_STATUS and related MNT table error. latentid: {}", pid);
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
            throw new IllegalStateException("Wrong record status when finish");
        }
    }

    @Override
    public void finishRecordTrained(Record record) {
        PreparedStatement ps = null;
        String pid = record.getId();
        byte[] feature = record.getFeature();
        try (Connection conn = this.queryRunner.getDataSource().getConnection()) {
            conn.setAutoCommit(false);

            //update matcher_task
            String sql = "update HAFPIS_RECORD_STATUS set status=? where PID=? and datatype=? and status=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, Record.Status.Trained.name());
            ps.setString(2, pid);
            ps.setInt(3, CONSTANTS.RECORD_DATATYPE_LPP);
            ps.setString(4, Record.Status.Training.name());
            ps.executeUpdate();

            String updateSql = "update " + LPPTABLE + " set mntdata=? where latentid=?";
            ps = conn.prepareStatement(updateSql);
            ps.setBytes(1, feature);
            ps.setString(2, pid);
            ps.executeUpdate();
            log.debug("updateSql is {}", updateSql);

            conn.commit();
        } catch (SQLException e) {
            log.error("Update HAFPIS_RECORD_STATUS and related MNT table error. latentid: {}", pid);
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

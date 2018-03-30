package hbie2.dao.jdbc;

import hbie2.HbieConfig;
import hbie2.InfoCol;
import hbie2.Record;
import hbie2.TaskSearch;
import hbie2.TaskVerify;
import hbie2.dao.MatcherDAO;
import hbie2.dao.mongodb.MatcherDAOMongoDB;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
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
public class LatFpMatcherDaoJDBC implements MatcherDAO{
    private Logger log = LoggerFactory.getLogger(LatFpMatcherDaoJDBC.class);
    private String jdbc_driver;
    private String jdbc_url;
    private String jdbc_usr;
    private String jdbc_pwd;
    private String jdbc_table;
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
                        }
                    }
                }
            }
        }

    }


    @Nullable
    @Override
    public Record fetchRecordToProcess(String s) {
        return dao.fetchRecordToProcess(s);
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
    public Record fetchRecordToTrain(String s) {
        return dao.fetchRecordToTrain(s);
    }

    @Override
    public void finishRecordProcessed(Record record) {
        dao.finishRecordProcessed(record);
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

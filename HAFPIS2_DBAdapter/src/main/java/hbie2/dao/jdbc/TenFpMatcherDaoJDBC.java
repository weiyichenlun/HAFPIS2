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
public class TenFpMatcherDaoJDBC implements MatcherDAO{
    private Logger log = LoggerFactory.getLogger(TenFpMatcherDaoJDBC.class);
    private String jdbc_driver;
    private String jdbc_url;
    private String jdbc_usr;
    private String jdbc_pwd;
    private String jdbc_table;
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

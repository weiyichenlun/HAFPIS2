package hbie2.dao.jdbc;

import hbie2.DataCheckStatus;
import hbie2.HbieConfig;
import hbie2.MasterInfo;
import hbie2.MatcherInfo;
import hbie2.Record;
import hbie2.TaskSearch;
import hbie2.TaskVerify;
import hbie2.dao.MasterDAO;
import hbie2.dao.mongodb.MasterDAOMongoDB;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/15
 * 最后修改时间:2018/3/15
 */
public class LatFpMasterDaoJDBC implements MasterDAO, Serializable {
    private Logger log = LoggerFactory.getLogger(LatFpMasterDaoJDBC.class);
    private String jdbc_driver;
    private String jdbc_url;
    private String jdbc_usr;
    private String jdbc_pwd;
    private String jdbc_table;
    private MasterDAOMongoDB dao;
    private Connection conn = null;
    private QueryRunner queryRunner = null;


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
                                this.dao = new MasterDAOMongoDB();
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
    public HbieConfig selectHbieConfig() {
        return dao.selectHbieConfig();
    }

    @Override
    public void saveHbieConfig(HbieConfig hbieConfig) {
        dao.saveHbieConfig(hbieConfig);
    }

    @NotNull
    @Override
    public Date register(String address, String magic) {
        return dao.register(address, magic);
    }

    @Nullable
    @Override
    public Date ping(String address, String magic) {
        return dao.ping(address, magic);
    }

    @Nullable
    @Override
    public DataCheckStatus fetchDataCheck(String magic) {
        return dao.fetchDataCheck(magic);
    }

    @Override
    public void finishDataCheck(String magic) {
        dao.finishDataCheck(magic);
    }

    @Override
    public void pendDataCheck(DataCheckStatus.Type type, Date skip, boolean force) {
        dao.pendDataCheck(type, skip, force);
    }

    @NotNull
    @Override
    public List<MatcherInfo> getMatcherList() {
        return dao.getMatcherList();
    }

    @NotNull
    @Override
    public List<MasterInfo> getMasterList() {
        return dao.getMasterList();
    }

    @Override
    public void disableMatcher(String address, String magic) {
        dao.disableMatcher(address, magic);
    }

    @Override
    public void disableMaster(String address, String magic) {
        dao.disableMaster(address, magic);
    }

    @Override
    public boolean saveRecord(Record record) {
        return dao.saveRecord(record);
    }

    @Override
    public boolean deleteRecord(String id) {
        return dao.deleteRecord(id);
    }

    @Nullable
    @Override
    public Record selectRecord(String id) {
        return dao.selectRecord(id);
    }

    @NotNull
    @Override
    public String submitVerify(TaskVerify taskVerify) {
        return dao.submitVerify(taskVerify);
    }

    @Nullable
    @Override
    public TaskVerify queryVerify(String id) {
        return dao.queryVerify(id);
    }

    @Override
    public boolean deleteVerify(String id) {
        return dao.deleteVerify(id);
    }

    @NotNull
    @Override
    public String submitSearch(TaskSearch taskSearch) {
        return dao.submitSearch(taskSearch);
    }

    @Nullable
    @Override
    public TaskSearch querySearch(String id) {
        return dao.querySearch(id);
    }

    @Override
    public boolean deleteSearch(String id) {
        return dao.deleteSearch(id);
    }

    @Nullable
    @Override
    public String fetchRecordToPublish(String magic) {
        return dao.fetchRecordToPublish(magic);
    }

    @Override
    public void finishRecord(String id) {
        dao.finishRecord(id);
    }

    @Override
    public void resetRecordProcessing(String id) {
        dao.resetRecordProcessing(id);
    }

    @Override
    public void resetRecordPublishing(String id) {
        dao.resetRecordPublishing(id);
    }

    @Nullable
    @Override
    public TaskSearch fetchSearchToFuse(String magic) {
        return dao.fetchSearchToFuse(magic);
    }

    @Override
    public void finishSearch(TaskSearch taskSearch) {
        dao.finishSearch(taskSearch);
    }

    @Override
    public void resetSearchFusing(String magic) {
        dao.resetSearchFusing(magic);
    }

    @Override
    public void resetSearchProcessing(String magic) {
        dao.resetSearchProcessing(magic);
    }

    @Override
    public void resetSearchSearching(String magic, int shardIdx, int shardNum) {
        dao.resetSearchSearching(magic, shardIdx, shardNum);
    }

    @Override
    public void resetDataChecking(String magic) {
        dao.resetDataChecking(magic);
    }

    @Override
    public void listId(Function2<? super String, ? super Date, Boolean> block) {
        StringBuilder sb = new StringBuilder();
        List<Object[]> res;
        sb.append("select latentid, enrolldate from ").append(this.jdbc_table).append(" order by latentid asc, " +
                "enrolldate asc");
        try {
            res = this.queryRunner.query(sb.toString(), new ArrayListHandler());
            res.forEach(objects -> {
                String pid = (String) objects[0];
                Date date = Utils.getDateFromStr((String) objects[1]);
                if(!block.invoke(pid, date)) return;
            });
        } catch (SQLException e) {
            log.error("Select id/enrolldate error. ", e);
        }

    }

    @Override
    public void listAll(Date skip, Function1<? super Record, Boolean> block) {
        dao.listAll(skip, block);
    }

    @Override
    public int countRecord(Record.Status status, Date date, Date date1, String s) {
        return this.dao.countRecord(status, date, date1, s);
    }

    @NotNull
    @Override
    public List<Record> listRecord(Record.Status status, Date date, Date date1, String s, int i, boolean b, String s1) {
        return this.dao.listRecord(status, date, date1, s, i, b, s1);
    }

    @Nullable
    @Override
    public Record selectRecordImages(String s) {
        return this.dao.selectRecordImages(s);
    }

    @NotNull
    @Override
    public List<TaskVerify> listVerify(TaskVerify.Status status, Date date, Date date1, int i, boolean b, String s) {
        return this.dao.listVerify(status, date, date1, i, b, s);
    }

    @Override
    public int countVerify(TaskVerify.Status status, Date date, Date date1) {
        return this.dao.countVerify(status, date, date1);
    }

    @Nullable
    @Override
    public TaskVerify selectVerifyImages(String s) {
        return this.dao.selectVerifyImages(s);
    }

    @NotNull
    @Override
    public List<TaskSearch> listSearch(TaskSearch.Status status, Date date, Date date1, int i, boolean b, String s) {
        return this.dao.listSearch(status, date, date1, i, b, s);
    }

    @Override
    public int countSearch(TaskSearch.Status status, Date date, Date date1) {
        return this.dao.countSearch(status, date, date1);
    }

    @Nullable
    @Override
    public TaskSearch selectSearchImages(String s) {
        return this.dao.selectSearchImages(s);
    }
}

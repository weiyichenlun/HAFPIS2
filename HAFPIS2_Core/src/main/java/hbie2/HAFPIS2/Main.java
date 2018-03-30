package hbie2.HAFPIS2;

import hbie2.HAFPIS2.Service.AbstractService;
import hbie2.HAFPIS2.Utils.ConfigUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/27
 * 最后修改时间:2018/3/27
 */
public class Main {
    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) throws Exception {
        Properties properties;
        PropertyConfigurator.configure("log4j.properties");
        if (args == null) {
            log.info("请输入一个配置文件名称(例如HSFP.properties):  ");
            System.exit(-1);
        } else {
            properties = ConfigUtils.getProp(args);
            startService(properties);
        }
    }

    private static void startService(Properties prop) {
        AbstractService service;
        try {
            service = (AbstractService) Class.forName(prop.getProperty("hafpis_service_class")).newInstance();
            service.init(prop);
            new Thread(service, service.getClass().getSimpleName()).start();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            log.error("Can't find hafpis_service_class: {}", prop.getProperty("hafpis_service_class"), e);
            System.exit(-1);
        }

    }
}
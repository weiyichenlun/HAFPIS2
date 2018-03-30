package hbie2.HAFPIS2.Utils;

import hbie2.HBIEClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/28
 * 最后修改时间:2018/3/28
 */
public class HbieUtils {
    private static Logger log = LoggerFactory.getLogger(HbieUtils.class);
    public HBIEClient hbie_FP;
    public HBIEClient hbie_PP;
    public HBIEClient hbie_LPP;
    public HBIEClient hbie_PLP;
    public HBIEClient hbie_FACE;
    public HBIEClient hbie_IRIS;
    private static String ipAddr;
    private static String[] ipAddrs;
    private static int fpPort;
    private static int ppPort;
    private static int lppPort;
    private static int plpPort;
    private static int facePort;
    private static int irisPort;

    private HbieUtils() {
        if (ipAddrs.length > 0) {
            int len = ipAddrs.length;
            String[] cpIpAddrs = new String[len];
            if (fpPort != -1) {
                for (int i = 0; i < len; i++) {
                    cpIpAddrs[i] = ipAddrs[i] + ":" + fpPort;
                }
                hbie_FP = new HBIEClient(cpIpAddrs[0]);
            } else {
                hbie_FP = null;
            }
            cpIpAddrs = new String[len];
            if (lppPort != -1) {
                for (int i = 0; i < len; i++) {
                    cpIpAddrs[i] = ipAddrs[i] + ":" + lppPort;
                }
                hbie_LPP = new HBIEClient(cpIpAddrs[0]);
            } else {
                hbie_LPP = null;
            }

            cpIpAddrs = new String[len];
            if (ppPort != -1) {
                for (int i = 0; i < len; i++) {
                    cpIpAddrs[i] = ipAddrs[i] + ":" + ppPort;
                }
                hbie_PP = new HBIEClient(cpIpAddrs[0]);
            } else {
                hbie_PP = null;
            }

            cpIpAddrs = new String[len];
            if (plpPort != -1) {
                for (int i = 0; i < len; i++) {
                    cpIpAddrs[i] = ipAddrs[i] + ":" + plpPort;
                }
                hbie_PLP = new HBIEClient(cpIpAddrs[0]);
            } else {
                hbie_PLP = null;
            }

            cpIpAddrs = new String[len];
            if (facePort != -1) {
                for (int i = 0; i < len; i++) {
                    cpIpAddrs[i] = ipAddrs[i] + ":" + facePort;
                }
                hbie_FACE = new HBIEClient(cpIpAddrs[0]);
            } else {
                hbie_FACE = null;
            }

            cpIpAddrs = new String[len];
            if (irisPort != -1) {
                for (int i = 0; i < len; i++) {
                    cpIpAddrs[i] = ipAddrs[i] + ":" + irisPort;
                }
                hbie_IRIS = new HBIEClient(cpIpAddrs[0]);
            } else {
                hbie_IRIS = null;
            }
        } else {
            log.info("host config error");
        }
        log.info("HBIEClient init end...");
    }


    public static final class HbieHolder{
        private static final HbieUtils HBIE_INSTANCE = new HbieUtils();
        public static final String[] ips = ipAddrs;
    }

    static {
        log.info("HBIEClient init begin...");
        ipAddr = ConfigUtils.getConfig("host");
        ipAddrs = ipAddr.split(",");
        log.info("host is " + ipAddr);
        try {
            String temp = ConfigUtils.getConfig("tenfp_port");
            if (temp == null) {
                fpPort = -1;
            } else {
                fpPort = Integer.parseInt(temp);
            }
        } catch (NumberFormatException e) {
            log.warn("tenfp_port in hbie.cfg.properties config error. {}\n Use default tenfp_port 1099", fpPort);
            fpPort = 1099;
        }
        try {
            String temp = ConfigUtils.getConfig("fourpalm_port");
            if (temp == null) {
                ppPort = -1;
            } else {
                ppPort = Integer.parseInt(temp);
            }
        } catch (NumberFormatException e) {
            log.warn("fourpalm_port in hbie.cfg.properties config error. {}\n Use default fourpalm_port 1100", ppPort);
            ppPort = 1100;
        }
        try {
            String temp = ConfigUtils.getConfig("latfp_port");
            if (temp == null) {
                lppPort = -1;
            } else {
                lppPort = Integer.parseInt(temp);
            }
        } catch (NumberFormatException e) {
            log.warn("latfp_port in hbie.cfg.properties config error. {}\n Use default latfp_port 1101", lppPort);
            lppPort = 1101;
        }
        try {
            String temp = ConfigUtils.getConfig("latpalm_port");
            if (temp == null) {
                plpPort = -1;
            } else {
                plpPort = Integer.parseInt(temp);
            }
        } catch (NumberFormatException e) {
            log.warn("latpalm_port in hbie.cfg.properties config error. {}\n Use default latpalm 1102", plpPort);
            plpPort = 1102;
        }
        try {
            String temp = ConfigUtils.getConfig("face_port");
            if (temp == null) {
                facePort = -1;
            } else {
                facePort = Integer.parseInt(temp);
            }
        } catch (NumberFormatException e) {
            log.warn("face_port in hbie.cfg.properties config error. {}\n Use default face_port 1103", facePort);
            facePort = 1103;
        }
        try {
            String temp = ConfigUtils.getConfig("iris_port");
            if (temp == null) {
                irisPort = -1;
            } else {
                irisPort = Integer.parseInt(temp);
            }
        } catch (NumberFormatException e) {
            log.warn("iris_port in hbie.cfg.properties config error. {}\n Use default iris_port 1104", irisPort);
            irisPort = 1104;
        }
    }

    public static HbieUtils getInstance() {
        return HbieHolder.HBIE_INSTANCE;
    }
}

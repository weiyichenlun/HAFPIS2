package hbie2.HAFPIS2.Utils;

import hbie2.HAFPIS2.Entity.HafpisSrchTask;
import hbie2.HAFPIS2.Entity.SrchDataBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/28
 * 最后修改时间:2018/3/28
 */
public class CommonUtils {
    private static Logger log = LoggerFactory.getLogger(CommonUtils.class);

    public static boolean check(HafpisSrchTask srchTask) {
        return srchTask.getSrchDataBeans() == null || srchTask.getSrchDataBeans().size() == 0;
    }

    public static String getDbsFilter(String srchDbMask) {
        StringBuilder filter = new StringBuilder();
        if (null == srchDbMask || srchDbMask.trim().isEmpty()) {
            return null;
        }
        for (int i = 0; i < srchDbMask.length(); i++) {
            if (srchDbMask.charAt(i) == '1') {
                filter.append("dbId=={").append(i+1).append("}").append("||");
            }
        }
        if (filter.length() >= 2) {
            filter.setLength(filter.length() - 2);
        }
        return filter.toString().trim().isEmpty() ? null : filter.toString();
    }

    public static String getDemoFilter(byte[] demofilter) {

        return null;
    }

    public static String getSolveOrDupFilter(int type, Integer solveordup) {
        int num = solveordup == null ? 0 : solveordup;
        String res = null;
        if (type == CONSTANTS.DBOP_TPP) {
            if (num == 0) {
                res = "TPCARDDUP=={" + num + "}";
            } else if (num == 1) {
                res = null;
            }
        } else if (type == CONSTANTS.DBOP_LPP || type == CONSTANTS.DBOP_PLP) {
            if (num == 0) {
                res = "SOLVEATTR=={" + num + "}";
            } else if (num == 1) {
                res = null;
            }
        }
        return res;
    }

    public static String mergeFilters(String... strings) {
        StringBuilder sb = new StringBuilder();
        // 0: enable 1: diasble
        boolean demoFilterEnable = ConfigUtils.getConfig("demo_filter_enable").equals("0");
        if (strings == null || strings.length == 0) {
            return null;
        }
        for (int i = 0; i < strings.length-1; i++) {
            if (strings[i] != null && strings[i].trim().length() > 0) {
                sb.append("(").append(strings[i]).append(")").append("&&");
            }
        }
        String demofilter = strings[strings.length - 1];
        if (demoFilterEnable && demofilter != null && demofilter.trim().length() > 0) {
            sb.append("(").append(demofilter).append(")").append("&&");
        }
        if (sb.length() == 0) {
            return null;
        } else {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    public static void sleep(int interval) {
        try {
            Thread.sleep(interval * 1000);
            log.debug("sleeping");
        } catch (InterruptedException e) {
            log.warn("Waiting Thread was interrupted: {}", e);
        }
    }

    public static void convert(HafpisSrchTask srchTask) {
        List<SrchDataBean> srchDataBeans = new ArrayList<>();
        byte[] srchdata = srchTask.getSrchdata();
        int len = srchdata.length;
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(srchdata));
        try {
            while (len > 0) {
                SrchDataBean temp = new SrchDataBean();
                temp.datatype = srchTask.getDatatype();
                // probeid
                for (int j = 0; j < temp.probeId.length; j++) {
                    temp.probeId[j] = dis.readByte();
                    len--;
                }
                // RPMNT RPImg
                for (int j = 0; j < temp.RpMntLen.length; j++) {
                    temp.RpMntLen[j] = dis.readInt();
                    len -= 4;
                }
                for (int j = 0; j < temp.RpImgLen.length; j++) {
                    temp.RpImgLen[j] = dis.readInt();
                    len -= 4;
                }
                // FPMNT flatImg
                for (int j = 0; j < temp.FpMntLen.length; j++) {
                    temp.FpMntLen[j] = dis.readInt();
                    len -= 4;
                }
                for (int j = 0; j < temp.FpImgLen.length; j++) {
                    temp.FpImgLen[j] = dis.readInt();
                    len -= 4;
                }
                // palmMnt palmImg
                for (int j = 0; j < temp.PalmMntLen.length; j++) {
                    temp.PalmMntLen[j] = dis.readInt();
                    len -= 4;
                }
                for (int j = 0; j < temp.PalmImgLen.length; j++) {
                    temp.PalmImgLen[j] = dis.readInt();
                    len -= 4;
                }
                // faceMnt faceImg
                for (int j = 0; j < temp.FaceMntLen.length; j++) {
                    temp.FaceMntLen[j] = dis.readInt();
                    len -= 4;
                }
                for (int j = 0; j < temp.FaceImgLen.length; j++) {
                    temp.FaceImgLen[j] = dis.readInt();
                    len -= 4;
                }
                // irisMnt irisImg
                for (int j = 0; j < temp.IrisMntLen.length; j++) {
                    temp.IrisMntLen[j] = dis.readInt();
                    len -= 4;
                }
                for (int j = 0; j < temp.IrisImgLen.length; j++) {
                    temp.IrisImgLen[j] = dis.readInt();
                    len -= 4;
                }
                // reservered
                for (int j = 0; j < temp.reserved.length; j++) {
                    temp.reserved[j] = dis.readInt();
                    len -= 4;
                }
                switch (srchTask.getDatatype()) {
                    case 1:
                        for (int i = 0; i < temp.RpMntLen.length; i++) {
                            if (temp.RpMntLen[i] == 0) {
                                temp.rpmnt[i] = null;
                            } else {
                                byte[] tempFea = new byte[temp.RpMntLen[i]];
                                dis.readFully(tempFea);
                                temp.rpmnt[i] = tempFea;
                                temp.rpmntnum++;
                                len -= temp.RpMntLen[i];
                            }
                        }
                        for (int i = 0; i < temp.FpMntLen.length; i++) {
                            if (temp.FpMntLen[i] == 0) {
                                temp.fpmnt[i] = null;
                            } else {
                                byte[] tempFea = new byte[temp.FpMntLen[i]];
                                dis.readFully(tempFea);
                                temp.fpmnt[i] = tempFea;
                                temp.fpmntnum++;
                                len -= temp.FpMntLen[i];
                            }
                        }
                        break;
                    case 4:
                        if (temp.RpMntLen[0] == 0) {
                            temp.latfpmnt = null;
                        } else {
                            int len1 = temp.RpMntLen[0];
                            len -= len1;
                            if (len1 == 6304) {
                                byte[] head = new byte[160];
                                dis.readFully(head);
                                byte[] tempFea1 = new byte[3072];
                                byte[] tempFea2 = new byte[3072];
                                dis.readFully(tempFea1);
                                dis.readFully(tempFea2);
                                temp.latfpmnt = tempFea1;
                                temp.latfpmnt_auto = tempFea2;
                            } else if (len1 == 3072) {
                                byte[] tempFea1 = new byte[3072];
                                dis.readFully(tempFea1);
                                temp.latfpmnt = tempFea1;
                                temp.latfpmnt_auto = null;
                            }
                        }
                        break;
                    case 2:
                        for (int i = 0; i < 4; i++) {
                            int len1 = temp.PalmMntLen[CONSTANTS.srchOrder[i]];
                            if (len1 == 0) {
                                temp.palmmnt[CONSTANTS.feaOrder[i]] = null;
                            } else {
                                len -= len1;
                                byte[] tempFea = new byte[len1];
                                dis.readFully(tempFea);
                                temp.palmmnt[CONSTANTS.feaOrder[i]] = tempFea;
                                temp.palmmntnum++;
                            }
                        }
                        break;
                    case 5:
                        if (temp.PalmMntLen[0] == 0) {
                            temp.latpalmmnt = null;
                        } else {
                            byte[] tempFea = new byte[temp.PalmMntLen[0]];
                            len -= temp.PalmMntLen[0];
                            dis.readFully(tempFea);
                            temp.latpalmmnt = tempFea;
                        }
                        break;
                    case 6:
                        for (int i = 0; i < 3; i++) {
                            int len1 = temp.FaceMntLen[i];
                            if (len1 == 0) {
                                temp.facemnt[i] = null;
                            } else {
                                len -= len1;
                                byte[] tempFea = new byte[len1];
                                dis.readFully(tempFea);
                                temp.facemnt[i] = tempFea;
                                temp.facemntnum++;
                            }
                        }
                        break;
                    case 7:
                        for (int i = 0; i < 2; i++) {
                            int len1 = temp.IrisMntLen[i];
                            if (len1 == 0) {
                                temp.irismnt[i] = null;
                            } else {
                                len -= len1;
                                byte[] tempFea = new byte[len1];
                                dis.readFully(tempFea);
                                temp.irismnt[i] = tempFea;
                                temp.irismntnum++;
                            }
                        }
                        break;
                }
                srchDataBeans.add(temp);
            }
            srchTask.setSrchDataBeans(srchDataBeans);
        } catch (IOException e) {
            log.error("Convert srchdata error. taskidd: {}", srchTask.getTaskidd(), e);
        }
    }


}

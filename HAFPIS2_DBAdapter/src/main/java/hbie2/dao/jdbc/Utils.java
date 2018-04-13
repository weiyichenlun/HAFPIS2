package hbie2.dao.jdbc;

import hbie2.ftp.FTPClientException;
import hbie2.ftp.FTPClientUtil;
import hbie2.nist.NistDecoder;
import hbie2.nist.nistType.NistImg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/15
 * 最后修改时间:2018/3/15
 */
public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);
    private static String date_format = "yyyy-MM-dd HH:mm:ss";
    public static String getFormatDate(long time_end) {
        Date adate = new Date(time_end);
        return getFormatDate(date_format, adate);
    }

    public static Date getDateFromStr(String dateStr) {
        return getDateFromStr(date_format, dateStr);
    }

    public static Date getDateFromStr(String dataFormat, String dateStr) {
        if (dataFormat == null || dateStr == null ) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(dataFormat);
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            log.error("parse date error. {}/{}", dataFormat, dateStr, e);
            return null;
        }
    }

    public static String getFormatDate(Date str){
        return getFormatDate(date_format,str);
    }

    public static String sqlDate2UtilDate(java.sql.Date sqlDate) {
        Date utilDate = new Date(sqlDate.getTime());
        return getFormatDate(date_format, utilDate);
    }

    public static String getFormatDate(String formatString, Date adate) {
        String currentDate="";
        SimpleDateFormat format = new SimpleDateFormat(formatString);
        currentDate = format.format(adate);
        return currentDate;
    }

    public static String getFormatDate(String formatString, String str){
        String currentDate = "";
        SimpleDateFormat format1 = new SimpleDateFormat(formatString);
        currentDate = format1.format(str);
        return currentDate;
    }

    public static byte[] readFile(String filename) throws IOException {
        File file = new File(filename);
        return readFile(file);
    }

    public static byte[] readFile(File file) throws IOException {
        byte[] data = new byte[(int) file.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        try {
            dis.readFully(data);
        } finally {
            dis.close();
        }
        return data;
    }

    /**
     * 删除指定pid的图像文件
     * @param path
     * @return
     */
    public static boolean deleteDir(String path) {
        File file = new File(path);
        return deleteDir(file);
    }

    public static boolean deleteDir(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null && children.length > 0) {
                for (int i = 0; i < children.length; i++) {
                    boolean succ = children[i].delete();
                    if (!succ) return false;
                }
            }
        }
        return file.delete();
    }


    public static byte[][] getFeatures(String fea_path) {
        byte[][] features = new byte[10][];
        int[] flag = new int[10];
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(new File(fea_path)));
            for (int i = 0; i < flag.length; i++) {
                flag[i] = dis.readInt();
                features[i] = new byte[flag[i]];
            }
            for (int i = 0; i < features.length; i++) {
                dis.readFully(features[i]);
            }
        } catch (FileNotFoundException e) {
            log.error("fea file not found: {}", fea_path, e);
        } catch (IOException e) {
            log.error("read fea file error: {}", fea_path, e);
        }
        return features;
    }

    public static Map<Integer, List<NistImg>> initFtpAndLoadNist(String ftp_host, int ftp_port, String ftp_usr,
                                                                 String ftp_pwd, String path, String name) {
        Map<Integer, List<NistImg>> result = new HashMap<>();
        FTPClientUtil ftpClient = new FTPClientUtil();
        ftpClient.setHost(ftp_host);
        ftpClient.setPort(ftp_port);
        ftpClient.setUsername(ftp_usr);
        ftpClient.setPassword(ftp_pwd);
        String remoteFilePath = path + ".\\" + name + ".nist";
        String localFilePath = ".\\" + name;
        try (OutputStream outputStream = new FileOutputStream(new File(localFilePath));){
            ftpClient.get(remoteFilePath, outputStream);
            result = NistDecoder.decode(localFilePath);
        } catch (IOException e) {
            log.error("Can't create local temp file for probeid:{}", name);
            return null;
        } catch (FTPClientException e) {
            log.error("Can't get nist file from ftp server for probeid: {}", name);
            return null;
        } finally {
            File file = new File(localFilePath);
            if (file.exists()) {
                try {
                    file.delete();
                } catch (Exception e) {
                    log.warn("Can't delete the temp file {}", localFilePath);
                }
            }
        }
        return result;
    }

}

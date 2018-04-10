package hbie2.nist.nativesdk;

import hbie2.nist.library.AscField;
import hbie2.nist.nistType.NistData;
import hbie2.nist.nistType.NistImg;
import hbie2.nist.nistType.NistRecType1;
import hbie2.nist.nistType.NistRecType10;
import hbie2.nist.nistType.NistRecType2;
import hbie2.nist.nistType.NistRecType9;
import hbie2.nist.nistType.NistTxt;
import hbie2.nist.nistType.RawNistData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static hbie2.nist.nativesdk.ImageFormat.*;

/**
 * Created by pms on 2017/3/14.
 */
public class SdkUtils {
    private static Logger log = LoggerFactory.getLogger(SdkUtils.class);

    public static AscField setAscField(NistData nistData, int idc, int type, int fldnum, byte[] val) {
        NistTxt nistTxtData = FindNistTxt(nistData, idc, type, fldnum);
        AscField ascField = new AscField();
        if (nistTxtData != null && nistTxtData.value != null && nistTxtData.valueLen > 0) {
            ascField.value = nistTxtData.value;
        } else {
            ascField.value = val;
        }
        ascField.fieldType = type;
        ascField.fieldNum = fldnum;
        if (ascField.value != null) {
            ascField.valueLen = ascField.value.length;
        } else {
            ascField.valueLen = 0;
        }
        if (ascField.value == null) {
            return null;
        }
        return ascField;
    }

    public static AscField setAscField(NistData nistData, int idc, int type, int fldnum, int val) {
        byte[] data = SdkUtils.IntToBytes(val);
        return setAscField(nistData, idc, type, fldnum, data);
    }

    public static AscField setSubField(AscField ascField, int type, int idc) {

        AscField.SubField subField = new AscField.SubField();
        subField.idc = idc;
        subField.type = type;
        ascField.numOfSubField++;
        ascField.subFields.add(subField);
        return ascField;

    }

    public static void setTypeIdc(NistRecType1 type1, int type, int idc) {
        NistRecType1.TypeIdc typeIdc = new NistRecType1.TypeIdc(type, idc);
        type1.typeIdcs.add(typeIdc);
        return;
    }

    public static byte[] intToBytesOf3(int value) {
        byte[] src = new byte[3];
        src[2] = String2Byte(String.valueOf((value % 10) & 0xff))[0];
        src[1] = String2Byte(String.valueOf(((value % 100 / 10) & 0xff)))[0];
        src[0] = String2Byte(String.valueOf((value / 100 & 0xff)))[0];

        return src;
    }

    public static byte[] IntToBytes(int n) {
        return String2Byte(String.valueOf(n));
    }

    public static int GetNumLen(int num) {
        int nLen = 0;

        if (num == 0) return 1;
        if (num < 0) {
            nLen = 1;
            num = -num;
        }
        while (num > 0) {
            num = num / 10;
            nLen++;
        }
        return nLen;
    }

    public static String DefaultImp(int type, int imp) {
        if (imp != 0) {
            return String.valueOf(imp);
        }
        switch (type) {
            case 10:
                return "FACE";
            case 11:
                return "0";
            case 12:
                return null;
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 19:
                return "0";
            case 18:
                return "3";
            case 20:
                return "S";
            case 21:
                return null;
            case 22:
                Calendar c = Calendar.getInstance();
                SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
                return f.format(c.getTime());
            case 98:
                return null;
            case 99:
                return null;
            default:
                return null;
        }

    }

    public static int DefaultSRT(int cmsCode) {
        switch (cmsCode) {
            case 0:
                return 0;
            default:
                return 1;
        }
    }


    public static String DefaultCsp(int type, int cmsCode) {
        switch (type) {
            case 13:
            case 14:
            case 15:
                return "GRAY";
            default:
                switch (cmsCode) {
                    case CMS_CODE_WSQ20:
                        return "GRAY";
                    default:
                        return "RGB";
                }
        }
    }

    public static NistTxt FindNistTxt(NistData nistData, int recIdc, int recType, int fieldNum) {
        int i;
        NistTxt nistTxtData;
        if (nistData == null) return null;
        for (i = 0; i < nistData.numOfTxt; i++) {
            nistTxtData = nistData.nistTxtList.get(i);
            if (nistTxtData == null) continue;
            if ((recIdc == -1 || nistTxtData.fieldIdc == recIdc) && nistTxtData.fieldType == recType && nistTxtData.fieldNum == fieldNum) {
                return nistTxtData;
            }
        }
        return null;
    }

    public static byte[] String2Byte(String s) {
        if (s == null) {
            return null;
        }
        byte[] d = new byte[s.length()];
        try {
            d = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return d;
    }

    public static byte[] getTodayDate() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateNowStr = sdf.format(d);
        return dateNowStr.getBytes();
    }

    public static byte[] getGMT() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateNowStr = sdf.format(d);
        dateNowStr += "Z";
        return dateNowStr.getBytes();
    }

    public static int getLenByType1(NistRecType1 type1) {
        int len = 0;
        for (AscField a : type1.ascFields) {
            len += (SdkUtils.GetNumLen(a.fieldType) + 1 + 3 + 1 + a.valueLen + 1);
        }
        return len;
    }

    public static int getLenByType2(NistRecType2 type2) {
        int len = 0;
        for (AscField a : type2.ascFields) {
            len += (SdkUtils.GetNumLen(a.fieldType) + 1 + 3 + 1 + a.valueLen + 1);
        }
        return len;
    }

    public static int getLenByType9(NistRecType9 type9) {
        int len = 0;
        for (AscField a : type9.ascFields) {
            len += (SdkUtils.GetNumLen(a.fieldType) + 1 + 3 + 1 + a.valueLen + 1);
        }
        return len;
    }

    public static synchronized int getLenByType10(NistRecType10 type10) {
        int len = 0;
        for (AscField a : type10.ascFields) {
            len += (SdkUtils.GetNumLen(a.fieldType) + 1 + 3 + 1 + a.valueLen + 1);
        }
        return len;
    }

    public static String byteArrayToStr(byte[] src) {
        String dest = null;
        try {
            dest = new String(src, "UTF-8").trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return dest;
    }

    public static int byteArrayToIntFromHigntoLow(byte[] src) {

        /*if(src.length>0) {
            int size=src.length-1;
            int value =((src[0] & 0xff) << (size*8));
            for (int i = 1; i < src.length; i++) {
                if ((size - i) * 8 > 0) {
                    value = value | ((src[i] & 0xff) << (size - i) * 8);
                }else {
                    value = value|(src[i] &0xff);
                }
            }
            return value;
        }*/
        if (src.length == 4) {
            int value = (int) (((src[0] & 0xFF) << 24)
                    | ((src[1] & 0xFF) << 16)
                    | ((src[2] & 0xFF) << 8)
                    | (src[3] & 0xFF));
            return value;
        }

        if (src.length == 2) {
            int value = (int) (((src[0] & 0xFF) << 8)
                    | (src[1] & 0xFF));
            return value;
        }
        return 0;
    }

    public static int byteArrayToIntFromLowtoHign(byte[] src) {
         /*if(src.length>0) {
            int size=src.length-1;
            int value =(src[0] & 0xff) << (size*8);
            for (int i = 1; i < src.length; i++) {
                if ((size - i) * 8 > 0) {
                    value = value | ((src[i] & 0xff) << (size - i) * 8);
                }else {
                    value = value|(src[i] &0xff);
                }
            }
            return value;
        }*/

        String dest = new String(src);
        return Integer.parseInt(dest);
    }

    public static byte[] IntTobyteArray(int value, int len) {
/*
        if (len == 4) {
            byte[] src = new byte[4];
            src[0] = (byte) ((value >> 24) & 0xFF);
            src[1] = (byte) ((value >> 16) & 0xFF);
            src[2] = (byte) ((value >> 8) & 0xFF);
            src[3] = (byte) (value & 0xFF);
            return src;
        }
        if (len == 2) {
            byte[] src = new byte[2];
            src[0] = (byte) ((value >> 8) & 0xFF);
            src[1] = (byte) (value & 0xFF);
            return src;
        }
*/
        byte[] src=new byte[len];
        for(int i=0;i<len;i++){
            src[i] =(byte)((value >> ((len-1-i)*8)) & 0xFF);
        }
        return src;
//        return null;
    }

    public static byte[] readImageBytesFromFile(String filePath) {
        File file = new File(filePath);
        byte[] res = new byte[(int) file.length()];
        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            dis.readFully(res);
            return res;
        } catch (IOException e) {
            log.error("Read file bytes error. file: {}", filePath, e);
            return null;
        }
    }

    public static byte[] readImageBytesFromFile(File file) {
        byte[] data = new byte[(int) file.length()];
        try {
            InputStream in = new FileInputStream(file);
            in.read(data);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }


    public static List<AscField> sortAscFields(List<AscField> ascFields) {
        Collections.sort(ascFields, new Comparator<AscField>() {
            @Override
            public int compare(AscField o1, AscField o2) {
                return o1.fieldNum - o2.fieldNum;
            }
        });
        return ascFields;
    }

    public static List<NistRecType1.TypeIdc> sortTypeIdcs(List<NistRecType1.TypeIdc> typeIdcs) {
        Collections.sort(typeIdcs, new Comparator<NistRecType1.TypeIdc>() {
            @Override
            public int compare(NistRecType1.TypeIdc o1, NistRecType1.TypeIdc o2) {
                return o1.fieldType - o2.fieldType;
            }
        });
        return typeIdcs;
    }


    public static void sortNistData(List<RawNistData> rawNistDatas) {
        Collections.sort(rawNistDatas, new Comparator<RawNistData>() {
            @Override
            public int compare(RawNistData o1, RawNistData o2) {
                return o1.idc - o2.idc;
            }
        });
    }


    public static void writeImageFiles(NistImg nistImgData) {
        String format = cmsCode2Str(nistImgData.cmsCode);
        if (nistImgData.cmsCode == CMS_CODE_WSQ20) {
            format = "WSQ";
        }
        File dir = new File("data");
        if (!dir.exists()) dir.mkdir();
        File file = new File(".\\data\\t-" + nistImgData.type + "-" + nistImgData.idc + "." + format);
        try {
            byte[] data = nistImgData.imgData;
            if (data == null) {
                return;
            }
            if (nistImgData.cmsCode == 0) {
                if (nistImgData.width > 0 && nistImgData.height > 0 && nistImgData.imgData != null) {
                    BufferedImage img = new BufferedImage(nistImgData.width, nistImgData.height, BufferedImage.TYPE_BYTE_GRAY);
                    System.arraycopy(nistImgData.imgData, 0, ((DataBufferByte) img.getRaster().getDataBuffer()).getData(), 0, nistImgData.imgDataLen);
                    Graphics g = img.getGraphics();
                    g.drawImage(img, 0, 0, null);
                    g.dispose();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ImageIO.write(img, format, bos);
                    data = bos.toByteArray();
                } else {
                    log.debug("write image error {},idc={}", nistImgData.type, nistImgData.idc);
                }
            }
            OutputStream out = new FileOutputStream(file);
            out.write(data);
            out.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void writeTxtFiles(List<NistTxt> nistTxtDataList, String fileName) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
//           int IdcOftype1= getIdcOftype1(nistTxtDataList);
            if (nistTxtDataList.size() < 1) {
                return;
            }
            bw.write(" recType" + nistTxtDataList.get(0).fieldType + "idc=" + nistTxtDataList.get(0).fieldIdc);
            bw.newLine();
            byte[] data = nistTxtDataList.get(0).value;
            bw.write("1." + nistTxtDataList.get(0).fieldNum + ":" + new String(data));
            bw.newLine();
            for (int i = 1; i < nistTxtDataList.size(); i++) {
                if (nistTxtDataList.get(i).fieldNum == 999) {
                    continue;
                }
                if (nistTxtDataList.get(i).fieldNum == 1) {
                    bw.write(" recType" + nistTxtDataList.get(i).fieldType + "idc=" + nistTxtDataList.get(i).fieldIdc);
                    bw.newLine();
                }
                byte[] data1 = nistTxtDataList.get(i).value;
                bw.write(nistTxtDataList.get(i).fieldType + "." + nistTxtDataList.get(i).fieldNum + ":" +
                        new String(data1));
                bw.newLine();
            }
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//
    }

    public static List<NistTxt> readTxtFilesByPath(String filepath) {
        List<NistTxt> nistData = new ArrayList<>();
        FileInputStream FIS = null;
        try {
            FIS = new FileInputStream(filepath);
            BufferedReader br = new BufferedReader(new InputStreamReader(FIS));
            int type;
            int idc = 0;
            String fldNum;
            int valLen;
            byte[] val;
            while (true) {
                String s = br.readLine();
                if (s == null || s.trim() == null) {
                    break;
                }
                if (s.split("idc=").length > 1) {
                    idc = Integer.parseInt(s.split("idc=")[1]);
                    continue;
                }
                String[] num = s.split("\\:");
                String[] t_f = num[0].split("\\.");
                type = Integer.parseInt(t_f[0]);
                fldNum = t_f[1];
                val = num[1].getBytes();
                valLen = val.length;
                NistTxt txt = new NistTxt(type, idc, Integer.parseInt(fldNum), val, valLen);
                nistData.add(txt);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nistData;
    }

    public static String cmsCode2Str(int cmscode) {
        switch (cmscode) {
            case CMS_CODE_JP2:
                return "JP2";
            case CMS_CODE_JP2L:
                return "JP2L";
            case CMS_CODE_JPEGB:
                return "JPG";
            case CMS_CODE_JPEGL:
                return "JPEGL";
            case CMS_CODE_PNG:
                return "PNG";
            case CMS_CODE_WSQ20:
                return "WSQ20";
            default:
                return "BMP";
        }
    }

    public static int str2CmsCode(String format) {
        switch (format) {
            case "JP2":
                return CMS_CODE_JP2;
            case "JP2L":
                return CMS_CODE_JP2L;
            case "JPG":
            case "JPEGB":
                return CMS_CODE_JPEGB;
            case "JPEGL":
                return CMS_CODE_JPEGL;
            case "PNG":
                return CMS_CODE_PNG;
            case "WSQ20":
            case "WSQ":
                return CMS_CODE_WSQ20;
            default:
                return CMS_CODE_NONE;
        }
    }

    public static int DefaultSAP(int SAP) {
        if (SAP != 0) {
            return SAP;
        } else {
            return 0;
        }
    }
}

package hbie2.nist.utils;

import hbie2.nist.library.NistMntData;
import hbie2.nist.nistType.NistImgData;
import hbie2.nist.nistType.NistTxtData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static hbie2.nist.nativesdk.ImageFormat.*;


/**
 * Created by pms on 2017/3/1.
 */
public class DecodeUtil {
    public static Logger log= LoggerFactory.getLogger(DecodeUtil.class);

    public static byte[] readFileBytes(String fileName) {
        File file = new File(fileName);
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

    public static String getImageFormat(int cmsCode) {
        switch (cmsCode) {
            case CMS_CODE_JP2:
                return "JP2";
            case CMS_CODE_JP2L:
                return "JP2L";
            case CMS_CODE_JPEGB:
                return "JPG";
            case CMS_CODE_JPEGL:
                return "JPRGL";
            case CMS_CODE_PNG:
                return "PNG";
            case CMS_CODE_WSQ20:
                return "WSQ20";
            default:
                return "BMP";
        }
    }

    public static void writeImageFiles(NistImgData nistImgData) {
        String format = getImageFormat(nistImgData.cmsCode);
        File file = new File(".\\data\\t_" + nistImgData.idc + "." + format);
        byte[] data = nistImgData.data.getByteArray(0, nistImgData.length);
        log.debug("image data size:{}", data.length);
        try {
            if (format.equals("BMP")) {
                BufferedImage img = new BufferedImage(nistImgData.width, nistImgData.height, BufferedImage.TYPE_BYTE_GRAY);
                System.arraycopy(nistImgData.data.getByteArray(0, nistImgData.length), 0, ((DataBufferByte) img.getRaster().getDataBuffer()).getData(), 0, nistImgData.length);
                Graphics g = img.getGraphics();
                g.drawImage(img, 0, 0, null);
                g.dispose();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write(img, format, bos);
                data = bos.toByteArray();
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


    public static void writeImageFiles(byte[] data, int widht, int height, int cmsCode, String FileName) {
        String format = getImageFormat(cmsCode);
        File file = new File(FileName + "." + format);
        if (format.equals("BMP")) {
            BufferedImage img = new BufferedImage(widht, height, BufferedImage.TYPE_BYTE_GRAY);
            System.arraycopy(data, 0, ((DataBufferByte) img.getRaster().getDataBuffer()).getData(), 0, data.length);
            Graphics g = img.getGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                ImageIO.write(img, "BMP", bos);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            data = bos.toByteArray();
        }
        try {
            OutputStream out = new FileOutputStream(file);
            out.write(data);
            out.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void writeMiscTxt(NistMntData nistMntData, String fileName) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write("idc=" + nistMntData.idc);
            bw.newLine();
            bw.write("imp=" + nistMntData.imp);
            bw.newLine();
            bw.write("dpi=" + nistMntData.dpi);
            bw.newLine();
            bw.write("core=" + nistMntData.coreList);
            bw.newLine();
            bw.write("delta=" + nistMntData.deltaList);
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeTxtFiles(List<NistTxtData> nistTxtDataList) {
        int fileNum = 0;
        try {
            for (NistTxtData txt : nistTxtDataList) {
                FileOutputStream fos = new FileOutputStream(".\\data\\" + fileNum + ".txt");
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                bw.write(String.valueOf(txt.fieldType));
                bw.newLine();
                bw.write(String.valueOf(txt.fieldIdc));
                bw.newLine();
                bw.write(String.valueOf(txt.fieldNum));
                bw.newLine();
                bw.write(String.valueOf(txt.valueLen));
                bw.newLine();
                byte[] data1 = txt.value.getByteArray(0, txt.valueLen);
                bw.write(new String(data1));
                bw.close();
                fos.close();
                fileNum++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void writeTxtFiles(List<NistTxtData> nistTxtDataList, String fileName) {
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
            byte[] data = nistTxtDataList.get(0).value.getByteArray(0, nistTxtDataList.get(0).valueLen);
            bw.write("1." + nistTxtDataList.get(0).fieldNum + ":" + new String(data));
            bw.newLine();
            for (int i = 1; i < nistTxtDataList.size(); i++) {
                if (nistTxtDataList.get(i).fieldType != nistTxtDataList.get(i - 1).fieldType) {
                    bw.write(" recType" + nistTxtDataList.get(i).fieldType + "idc=" + nistTxtDataList.get(i).fieldIdc);
                    bw.newLine();
                }
                byte[] data1 = nistTxtDataList.get(i).value.getByteArray(0, nistTxtDataList.get(i).valueLen);
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

    private static int getIdcOftype1(List<NistTxtData> nistTxtDataList) {
        Set<Integer> set = new HashSet<>();
        for (NistTxtData txt : nistTxtDataList) {
            set.add(txt.fieldIdc);
        }
        int IdcOftype1 = set.size();
        return IdcOftype1;

    }

   /* private static int setField3Oftype1(List<NistTxtData> txtDatas,List<NistImgData> imgDatas){
        Set< Integer> set=new HashSet<>();
        for(NistTxtData txt:txtDatas){
            set.add(txt.fieldIdc);
        }
        int IdcOftype1=set.size();
        Set< String> imgset=new HashSet<>();
        for (NistImgData img:imgDatas){
//            imgset.add(img.recType++)

        }
    }
*/

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}

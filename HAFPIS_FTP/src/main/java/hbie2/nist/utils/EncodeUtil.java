package hbie2.nist.utils;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import hbie2.nist.format.CxbioLibrary;
import hbie2.nist.format.DecOutParam;
import hbie2.nist.nativesdk.ImageFormat;
import hbie2.nist.nistType.NistImgData;
import hbie2.nist.nistType.NistTxtData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pms on 2017/3/7.
 */
public class EncodeUtil {



    public static int getImageFormatIndex(String format) {
        switch (format) {
            case "JP2":
                return ImageFormat.CMS_CODE_JP2;
            case "JP2L":
                return ImageFormat.CMS_CODE_JP2L;
            case "JPG":
                return ImageFormat.CMS_CODE_JPEGB;
            case "JPRGL":
                return ImageFormat.CMS_CODE_JPEGL;
            case "PNG":
                return ImageFormat.CMS_CODE_PNG;
            case "WSQ":
                return ImageFormat.CMS_CODE_WSQ20;
            default:
                return ImageFormat.CMS_CODE_NONE;
        }
    }

    public static NistImgData readImageFiles(String fileName, int imp, int type, int pos, int idc, int resx, int resy) {
        NistImgData image = new NistImgData();
        File file = new File(fileName);
        try {
            image.cmsCode = getImageFormatIndex(file.getName().split("\\.")[1]);
            byte[] data=null;
            BufferedImage bi=null;
            InputStream in = new FileInputStream(file);
            if(image.cmsCode!=1){
                bi = ImageIO.read(in);
                image.height = bi.getHeight();
                image.width = bi.getWidth();
            }else {
                int inLength= (int) file.length();
                byte[] ds= readImageBytesFromFile(file);
                ByteBuffer inBuf=ByteBuffer.wrap(ds);
                int inType=CxbioLibrary.CXBIO_FORMAT_WSQ;
                DecOutParam outResult=new DecOutParam();
                CxbioLibrary.INSTANCE.CxbioGetImageData(inBuf,inLength,inType,outResult);
                image.height=outResult.height;
                image.width=outResult.width;
            }
            image.pos = pos;
            image.type = type;
            image.idc = idc;
            image.imp = imp;
            image.resY=resy;
            image.resX=resx;
            if(image.cmsCode==0){
                data=((DataBufferByte)bi.getRaster().getDataBuffer()).getData();
            }else {
                data= readImageBytesFromFile(file);
            }
            image.length = data.length;
            image.data = new Pointer(Native.malloc(data.length));
            for(int j=0;j<data.length;j++){
                image.data.setByte(j, data[j]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
    public static  byte[] readImageBytesFromFile(File file){
        byte[] data=new byte[(int) file.length()];
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
    public static byte[] encodeImage(BufferedImage image)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
        param.setQuality(1, false);
        encoder.setJPEGEncodeParam(param);
        try {
            encoder.encode(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
    public static  List<ImageFile> parseImginfoFile(File file){
        try {
            String path=file.getParent();
            List<ImageFile> imageFileList=new ArrayList<>();
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            while (true) {
                String line = br.readLine();
                if(line==null){return imageFileList;}
                ImageFile img = new ImageFile();
                img.FilePath = path+"\\"+line.split(":")[0];
                line = br.readLine();
                if(line.endsWith(";")){
                    String[] parameter=line.split(";");
                    for(String p:parameter){
                        if (p.startsWith("imp")) {
                            img.imp = Integer.parseInt(p.split("=")[1]);
                        }
                        if(p.startsWith("idc")){
                            img.idc=Integer.parseInt(p.split("=")[1]);
                        }
                        if (p.startsWith("type")) {
                            img.type = Integer.parseInt(p.split("=")[1]);
                        }
                        if (p.startsWith("pos")) {
                            img.Fpos = Integer.parseInt(p.split("=")[1]);
                        }
                        if (p.startsWith("resX")) {
                            img.resX = Integer.parseInt(p.split("=")[1]);
                        }
                        if (p.startsWith("resY")) {
                            img.resY = Integer.parseInt(p.split("=")[1]);
                        }
                        if (p.startsWith("sap")) {
                            img.sap = Integer.parseInt(p.split("=")[1]);
                        }
                    }
                    imageFileList.add(img);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<NistTxtData> readTxtFiles(String filepath) {
        List<NistTxtData> nistData = new ArrayList<>();
        File file=new File(filepath);
        File[] files=file.listFiles();
        try {
            for (File f:files) {
                if(f.getName().toLowerCase().endsWith(".txt")){
                    NistTxtData txt = new NistTxtData();
                    FileInputStream fis = new FileInputStream(f);
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                    String s = br.readLine();
                    txt.fieldType = Integer.parseInt(s);
                    s = br.readLine();
                    txt.fieldIdc = Integer.parseInt(s);
                    s = br.readLine();
                    txt.fieldNum = Integer.parseInt(s);
                    s = br.readLine();
                    txt.valueLen = Integer.parseInt(s);
                    s = br.readLine();
                    txt.value = new Pointer(Native.malloc(txt.valueLen));
                    for(int j=0;j<txt.valueLen;j++){
                        txt.value.setByte(j, s.getBytes()[j]);
                    }
                    br.close();
                    fis.close();
                    nistData.add(txt);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nistData;
    }

    public static List<NistTxtData> readTxtFilesByPath(String filepath) {
        List<NistTxtData> nistData = new ArrayList<>();
        FileInputStream FIS = null;
        try {
            FIS = new FileInputStream(filepath);
            BufferedReader br = new BufferedReader(new InputStreamReader(FIS));
            int type;
            int idc = 0;String fldNum;
            int valLen;
            Pointer val;
            while(true){
                String s=br.readLine();
                if(s==null){
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
                byte[] vals = num[1].getBytes();
                valLen = vals.length;
                val = new Pointer(Native.malloc(valLen));
                for(int i=0;i<valLen;i++){
                    val.setByte(i, vals[i]);
                }
                String data=val.getString(0);
                NistTxtData txt = new NistTxtData(idc, type, Integer.parseInt(fldNum), val, valLen);
                nistData.add(txt);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nistData;
    }



}

package hbie2.nist.nistType;

import hbie2.nist.library.AscField;
import hbie2.nist.nativesdk.SdkUtils;

import java.util.ArrayList;
import java.util.List;

import static hbie2.nist.nativesdk.SdkUtils.DefaultImp;
import static hbie2.nist.nativesdk.SdkUtils.getTodayDate;

/**
 * Created by pms on 2017/3/13.
 */
public class NistRecType10 {
    public int recType;
    public int Idc;

    public int numOfField;
    public List<AscField> ascFields = new ArrayList<AscField>();


    public int imgDataLen;
    public byte[] imgData;
    public int imgWidth;
    public int imgHeigh;
    public int imgResX;
    public int imgResY;
    public int recLen;
    public int imgCmsCode;


    public NistRecType10() {
        super();
    }


    public NistRecType10(int recType, int idc, int nNumOfField, List<AscField> ascFields, int imgDataLen, byte[] imgData) {
        super();
        this.recType = recType;
        this.Idc = idc;
        this.numOfField = nNumOfField;
        this.ascFields = ascFields;
        this.imgDataLen = imgDataLen;
        this.imgData = imgData;
    }

    public static List<NistRecType10> encode(NistData nistData, int recType) {
        List<NistRecType10> nistRecType10s = new ArrayList<>();
        for (NistImg img : nistData.nistImgList) {
            if (img.type < 10) {
                continue;
            }
            if (img.type != recType) {
                continue;
            }
            NistRecType10 type = new NistRecType10(img.type, img.idc, 0, new ArrayList<AscField>(), img.imgDataLen, img.imgData);
            if (img.type == 10) {
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 1, 0));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 2, img.idc));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 3, DefaultImp(img.type, img.imp).getBytes())); //"FACE"
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 4, "SRC".getBytes()));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 5, getTodayDate()));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 6, img.width));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 7, img.height));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 8, "1".getBytes()));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 9, img.resX));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 10, img.resY));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 11, SdkUtils.cmsCode2Str(img.cmsCode).getBytes()));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 12, SdkUtils.DefaultCsp(img.type, img.cmsCode).getBytes()));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 13, SdkUtils.DefaultSAP(15))); //SAP
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 20, "F".getBytes()));
                for (int i = 14; i < 999; i++) {
                    if (i == 20) {
                        type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 20, "F".getBytes()));
                    }
                }
            } else if (img.type == 13 || img.type == 14 || img.type == 15) {
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 1, 0));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 2, img.idc));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 3, DefaultImp(img.type, img.imp).getBytes()));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 4, "SRC".getBytes()));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 5, getTodayDate()));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 6, img.width));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 7, img.height));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 8, "1".getBytes()));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 9, img.resX));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 10, img.resY));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 11, SdkUtils.cmsCode2Str(img.cmsCode).getBytes()));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 12, SdkUtils.DefaultCsp(img.type, img.cmsCode).getBytes()));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 13, img.pos));
                for (int i = 14; i < 999; i++) {
                    AscField ascField = SdkUtils.setAscField(nistData, img.idc, img.type, i, null);
                    if (ascField != null) {
                        type.ascFields.add(ascField);
                    }
                }
            } else {
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 1, 0));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 2, img.idc));
                String s = DefaultImp(img.type, img.imp);
                byte[] d = SdkUtils.String2Byte(s);
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 3, d));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 4, "SRC".getBytes()));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 5, getTodayDate()));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 6, img.width));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 7, img.height));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 8, 1));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 9, img.resX));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 10, img.resY));
                type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 12, 8));
                for (int i = 13; i < 999; i++) {
                    AscField ascField = SdkUtils.setAscField(nistData, img.idc, img.type, i, null);
                    if (ascField != null) {
                        type.ascFields.add(ascField);
                    }
                }
            }
            //填写图像数据
            type.imgData = img.imgData;
            type.imgDataLen = img.imgDataLen;
            type.imgHeigh = img.height;
            type.imgWidth = img.width;
            type.ascFields.add(SdkUtils.setAscField(nistData, img.idc, img.type, 999, type.imgData));

            type.numOfField = type.ascFields.size();
            /*updata  fldnum=1*/
            SysNum1OfNistType10(type);

            nistRecType10s.add(type);
        }

        return nistRecType10s;
    }

    private static synchronized NistRecType10 SysNum1OfNistType10(NistRecType10 nistRecType10) {

        int size = SdkUtils.getLenByType10(nistRecType10);
        //计算总的长度
        int len1 = SdkUtils.GetNumLen(size);
        size += SdkUtils.GetNumLen(size) - 1;
        //计算总长度位数
        int len2 = SdkUtils.GetNumLen(size);
        //如果位数增加了,则总长度加1
        if (len2 > len1) {
            size++;
        }
        /*更新 */
        for (int i = 0; i < nistRecType10.numOfField; i++) {
            AscField a = nistRecType10.ascFields.get(i);
            if (a.fieldType == nistRecType10.recType & a.fieldNum == 1 && a.valueLen == 1) {
                a.value = SdkUtils.IntToBytes(size);
                a.valueLen = a.value.length;
                nistRecType10.ascFields.set(i, a);
                break;
            }
        }
        return nistRecType10;
    }
}

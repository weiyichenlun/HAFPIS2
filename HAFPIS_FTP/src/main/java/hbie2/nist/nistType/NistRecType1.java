package hbie2.nist.nistType;

import hbie2.nist.library.AscField;
import hbie2.nist.nativesdk.SdkUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static hbie2.nist.nativesdk.ImageFormat.RS;
import static hbie2.nist.nativesdk.ImageFormat.US;

/**
 * Created by pms on 2017/3/13.
 */
public class NistRecType1{
    public int recLen;
    public int numOfField;
    public List<AscField> ascFields=new ArrayList<>();
    public int nNumOfTypeIdc;

    public List<TypeIdc> typeIdcs=new ArrayList<>();



    public NistRecType1() {
        super();
    }
    public NistRecType1(int nNumOfField, List<AscField> ascFields, int nNumOfTypeIdc, List<TypeIdc> typeIdcs) {
        super();
        this.numOfField = nNumOfField;
        this.ascFields = ascFields;
        this.nNumOfTypeIdc = nNumOfTypeIdc;
        this.typeIdcs = typeIdcs;
    }




    public static class TypeIdc  {
        public int fieldType;
        public int fieldIDC;

        public TypeIdc(int fieldType, int fieldIDC) {
            super();
            this.fieldType = fieldType;
            this.fieldIDC = fieldIDC;
        }
    }



    public static NistRecType1  encode(NistData nistData){
        NistRecType1 nistRecType1 = new NistRecType1();
        boolean bioSample = false;
        for (int i = 0; i < nistData.numOfImg; i++) {
            if (nistData.nistImgList.get(i).type == 13 || nistData.nistImgList.get(i).type == 7) {
                bioSample = true;
                break;
            }
        }
        nistRecType1.ascFields.add(SdkUtils.setAscField(nistData, -1, 1, 1, 0));
        nistRecType1.ascFields.add(SdkUtils.setAscField(nistData, -1, 1, 2, "0502".getBytes()));
        nistRecType1.ascFields.add(SdkUtils.setAscField(nistData, -1, 1, 3, 0));
        if (!bioSample) {
            nistRecType1.ascFields.add(SdkUtils.setAscField(nistData, -1, 1, 4, "CAR".getBytes()));//CAR AMN 16个字母中最大的
        } else {
            nistRecType1.ascFields.add(SdkUtils.setAscField(nistData, -1, 1, 4, "AMN".getBytes()));//交换类型
        }
        nistRecType1.ascFields.add(SdkUtils.setAscField(nistData, -1, 1, 5, SdkUtils.getTodayDate()));
        nistRecType1.ascFields.add(SdkUtils.setAscField(nistData, -1, 1, 6, 1));
        nistRecType1.ascFields.add(SdkUtils.setAscField(nistData, -1, 1, 7, "DAI000001".getBytes()));
        nistRecType1.ascFields.add(SdkUtils.setAscField(nistData, -1, 1, 8, "MDNISTIMG".getBytes()));
        if (nistData.transNo == null || nistData.transNo.length == 0) {
            nistRecType1.ascFields.add(SdkUtils.setAscField(nistData, -1, 1, 9, "jck slap".getBytes()));
        } else {
            nistRecType1.ascFields.add(SdkUtils.setAscField(nistData, -1, 1, 9, nistData.transNo));
        }
        nistRecType1.ascFields.add(SdkUtils.setAscField(nistData, -1, 1, 11, "19.69".getBytes()));
        nistRecType1.ascFields.add(SdkUtils.setAscField(nistData, -1, 1, 12, "19.69".getBytes()));
        nistRecType1.ascFields.add(SdkUtils.setAscField(nistData, -1, 1, 14, SdkUtils.getGMT()));//格林时间，格式：YYYYMMDDhhmmssZ
        if (bioSample) {
            String s = "NORAM ";
            int len = s.length();
            byte[] byte13 = s.getBytes();
            nistRecType1.ascFields.add(SdkUtils.setAscField(nistData, -1, 1, 13, byte13));
            s = "000 ASCII ";
            len = s.length();
            byte[] byte15 = s.getBytes();
            nistRecType1.ascFields.add(SdkUtils.setAscField(nistData, -1, 1, 15, byte15));
            s = "ORG APSNAME";
            len = s.length();
            byte[] byte16 = s.getBytes();
            nistRecType1.ascFields.add(SdkUtils.setAscField(nistData, -1, 1, 16, byte16));
        }
        nistRecType1.numOfField = nistRecType1.ascFields.size();


        /*updata fldnum=3 的值*/
        byte[] val3 = SetFieldNum3Val(nistData, nistRecType1);
        for (int i = 0; i < nistRecType1.numOfField; i++) {
            AscField a = nistRecType1.ascFields.get(i);
            if (a.fieldType == 1 & a.fieldNum == 3 && a.valueLen == 1) {
                a.value = val3;
                a.valueLen = a.value.length;
                nistRecType1.ascFields.set(i, a);
                break;
            }
        }

        /*计算type1长度*/
        int size = SdkUtils.getLenByType1(nistRecType1);
        //计算总长度位数
        size = SdkUtils.GetNumLen(size) + size - 1;
        int len2 = SdkUtils.GetNumLen(size);
        int len1 = SdkUtils.GetNumLen(size);
        //如果位数增加了,则总长度加1
        if (len2 > len1) {
            size++;
        }
      /*更新 */
        for (int i = 0; i < nistRecType1.numOfField; i++) {
            AscField a = nistRecType1.ascFields.get(i);
            if (a.fieldType == 1 & a.fieldNum == 1 && a.valueLen == 1) {
                a.value = SdkUtils.IntToBytes(size);
                a.valueLen = a.value.length;
                nistRecType1.ascFields.set(i, a);
                break;
            }
        }
        return nistRecType1;
    }


    public static byte[] SetFieldNum3Val(NistData nistData, NistRecType1 type1){
        int idc_n = 1;
        int idcOfType9_n = 1;
        List<NistImg> newNistImgs=new ArrayList<>();
        List<NistImg> nistImgs = sortNistImgListByType(nistData.nistImgList);
        for (NistImg img : nistImgs) {
            TypeIdc typeIdc = new TypeIdc(img.type, idc_n);
            img.idc = idc_n;
            idc_n++;
            type1.typeIdcs.add(typeIdc);
            newNistImgs.add(img);
        }
        Set<Integer> set=new HashSet();
        for(NistTxt txt:nistData.nistTxtList){
            if(txt.fieldType==9 && txt.fieldIdc!=0){
                set.add(txt.fieldIdc);
            }else if(txt.fieldType == 9 && txt.fieldIdc ==0){
                set.add(idcOfType9_n);
                idcOfType9_n++;
            }
        }
        for(int idc:set){
            TypeIdc typeIdc=new TypeIdc(9,idc);
            type1.typeIdcs.add(typeIdc);
        }

        /*添加type2的IDC=0*/
        TypeIdc typeIdc = new TypeIdc(2, 0);
        type1.typeIdcs.add(typeIdc);


        /*添加type1的idc=总量*/
        TypeIdc typeIdc1 = new TypeIdc(1, type1.typeIdcs.size());
        type1.typeIdcs.add(typeIdc1);
        SdkUtils.sortTypeIdcs(type1.typeIdcs);

        nistData.nistImgList=newNistImgs;

        AscField asc=new AscField();

        for(TypeIdc t:type1.typeIdcs){
           asc= SdkUtils.setSubField(asc,t.fieldType,t.fieldIDC);
        }
        asc.numOfSubField=asc.subFields.size();
        byte[] val=SysFldnum3OfType1(asc);

        return val;


    }
    public static List<NistImg> sortNistImgListByType(List<NistImg> nistImgList){
        Collections.sort(nistImgList, new Comparator<NistImg>() {
            @Override
            public int compare(NistImg o1, NistImg o2) {
                return o1.type-o2.type;

            }
        });
        return nistImgList;
    }
    public static byte[] SysFldnum3OfType1(AscField asc){
        if(asc.numOfSubField==0){
            System.out.println("数据错误");
            return null;
        }
        int len=0;
        for(AscField .SubField sub:asc.subFields){
            len+=SdkUtils.GetNumLen(sub.type)+1+SdkUtils.GetNumLen(sub.idc)+1;
        }
        byte[] data=new byte[len-1];
        int pos=0;
        for (int i=0;i<asc.numOfSubField;i++) {
            byte[] d = null;
            if (i == asc.numOfSubField - 1) {
                d = getSubFiledData(asc.subFields.get(i), true);
            } else {
                d = getSubFiledData(asc.subFields.get(i), false);
            }
            System.arraycopy(d, 0, data,pos,d.length);
            pos+=d.length;
        }


        return data;
    }

    private  static  byte[] getSubFiledData(AscField.SubField sub,boolean LastSub){
        if(sub==null){
            return null;
        }
        int len=0,pos=0;
        if(!LastSub) {
             len = SdkUtils.GetNumLen(sub.type) + 1 + SdkUtils.GetNumLen(sub.idc) + 1;
        }else {
             len = SdkUtils.GetNumLen(sub.type) + 1 + SdkUtils.GetNumLen(sub.idc);
        }
        byte[] data=new byte[len];
        byte[] t = SdkUtils.IntToBytes(sub.type);
        System.arraycopy(t, 0, data, pos, t.length);
        pos += t.length;
        data[pos] = US;
        pos++;
        t = SdkUtils.IntToBytes(sub.idc);
        System.arraycopy(t, 0, data, pos, t.length);

        if (!LastSub) {
            pos += t.length;
            data[pos] = RS;
        }
        return data;
    }

}

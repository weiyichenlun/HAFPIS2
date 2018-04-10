package hbie2.nist.nativesdk;

import hbie2.nist.format.CxbioLibrary;
import hbie2.nist.format.DecOutParam;
import hbie2.nist.library.AscField;
import hbie2.nist.nistType.NistData;
import hbie2.nist.nistType.NistImg;
import hbie2.nist.nistType.NistRecType1;
import hbie2.nist.nistType.NistRecType10;
import hbie2.nist.nistType.NistRecType2;
import hbie2.nist.nistType.NistRecType4;
import hbie2.nist.nistType.NistRecType7;
import hbie2.nist.nistType.NistRecType8;
import hbie2.nist.nistType.NistRecType9;
import hbie2.nist.nistType.NistTxt;
import hbie2.nist.nistType.RawNistData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static hbie2.nist.nativesdk.ImageFormat.FS;
import static hbie2.nist.nativesdk.ImageFormat.GS;

/**
 * Created by pms on 2017/3/13.
 */
public class RawImageEncodeImp implements RawImageEncode, SdkHandler {
    private Logger log = LoggerFactory.getLogger(getClass());


    @Override
    public NistData Nist_Init() {
        NistData nistData = new NistData();
        return nistData;
    }


    @Override
    public boolean AddTransNo(NistData nistData, String TransNo) {
        if (nistData == null) {
            error(SdkErrorCode.ENCODE_ADD_TRANSNO_ERROR, "invalid parameter[nistData=null]");
            return false;
        }
        nistData.transNo = SdkUtils.String2Byte(TransNo);
        return true;
    }

    @Override
    public NistImg ReadImageFiles(String fileName, int imp, int type, int pos, String cmsCode, int resx, int resy, int sap) {
        NistImg image = new NistImg();
        File file = new File(fileName);
//        try {
        image.cmsCode = SdkUtils.str2CmsCode(cmsCode);
//            byte[] data = null;
//            BufferedImage bi = null;
//            InputStream in = new FileInputStream(file);

        int inLength = (int) file.length();
        byte[] ds = SdkUtils.readImageBytesFromFile(file);
        ByteBuffer inBuf = ByteBuffer.wrap(ds);
        DecOutParam outResult = new DecOutParam();
        CxbioLibrary.INSTANCE.CxbioGetImageData(inBuf, inLength, image.cmsCode, outResult);

        image.height = outResult.height;
        image.width = outResult.width;

        image.pos = pos;
        image.type = type;
        image.imp = imp;
        image.resY = resy;
        image.resX = resx;

//            if (image.cmsCode == 0) {
//                data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
//            } else {
        byte[] data = SdkUtils.readImageBytesFromFile(file);
//            }
        image.imgDataLen = data.length;
        image.imgData = new byte[data.length];
        System.arraycopy(data, 0, image.imgData, 0, data.length);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return image;
    }

    @Override
    public boolean AddTxtData(NistData nistData, int Type, int idc, int fldnum, byte[] val, int valLen) {
        if (nistData == null) {
            error(SdkErrorCode.ENCODE_ADD_TXT_ERROR, "invalid parameter[nistData=null]");
//            log.info("无效的参数[nistData=null]");
            return false;
        }
        if (!(Type == 1 || Type == 2 || Type == 9 || Type >= 10 && Type <= 22 || Type == 98 || Type == 99)) {
            error(SdkErrorCode.ENCODE_ADD_TXT_ERROR, "add txt failed,invalid recType,must be 1,2,9,10-22,98,99");
            return false;
        }
        if (Type == 1 && idc != -1) {
            error(SdkErrorCode.ENCODE_ADD_TXT_ERROR, "add txt failed,while recType=1,recIdc must be -1");
            return false;
        }
        if (val == null || valLen <= 0) {
            error(SdkErrorCode.ENCODE_ADD_TXT_ERROR, "add txt failed,invalid parameter");
            return false;
        }
        for (int i = 0; i < nistData.numOfTxt; i++) {
            if (nistData.nistTxtList.get(i).fieldType == Type && nistData.nistTxtList.get(i).fieldIdc == idc &&
                    nistData.nistTxtList.get(i).fieldNum == fldnum) {
                error(SdkErrorCode.ENCODE_ADD_TXT_ERROR, "text repeated");
                return true;
            }
        }
        nistData.numOfTxt++;
        NistTxt nistTxt = new NistTxt(Type, idc, fldnum, val, valLen);
        nistData.nistTxtList.add(nistTxt);

        return true;
    }

    @Override
    public boolean AddImgData(NistData nistData, int Type, int idc, int imp, int pos, int resX, int resY, int cmsCode,
                              int w, int h, byte[] imgData, int imgLen, byte[] mirData, int mirLen) {
        if (nistData == null) {
            error(SdkErrorCode.ENCODE_ADD_IMAGE_ERROR, "  invalid parameter[nistData=null]");
            return false;
        }
        if (!((Type >= 3 && Type <= 8) || (Type >= 10 && Type <= 22) || (Type >= 98 && Type <= 99))) {
            error(SdkErrorCode.ENCODE_ADD_IMAGE_ERROR, " add image failed,invalid type");
            return false;
        }
        if ((Type != 7 && (imgData == null || imgLen <= 0)) || (Type == 7 && (imgData == null || imgLen <= 0) && (mirData == null || mirLen <= 0))) {
            error(SdkErrorCode.ENCODE_ADD_IMAGE_ERROR, " add image failed,invalid parameter");
            return false;
        }
//        for(int i=0;i<nistData.numOfImg;i++){
//            if(nistData.nistImgList.get(i).type==Type &&
//                    nistData.nistImgList.get(i).idc==idc){
//                log.info("图片添加重复");
//                return true;
//            }
//        }
        nistData.numOfImg++;
        NistImg nistImg = new NistImg(Type, idc, imp, pos, resX, resY, cmsCode, w, h, imgData, imgLen, mirData, mirLen);
        nistData.nistImgList.add(nistImg);

        return true;
    }

    @Override
    public int encodeType7(byte[] imgData, int imgSize, byte[] mirData, int mirSize, int binSize) {
        binSize = 28;
        if ((imgData != null && imgSize > 0) || (mirData != null && mirSize > 0)) {
            error(SdkErrorCode.ENCODE_TYPE7_DATA_ERROR, "data error");
            return -1;
        }
        if (imgData != null && imgSize > 0) {
            binSize += 32;
        }
        if (mirData != null && mirSize > 0) {
            binSize += 32;
        }
        byte[] binData = new byte[binSize + imgSize + mirSize];
        binData[0] = 2;
        binData[1] = 50;
        binData[12] = (byte) 255;
        binData[13] = (byte) 210;
        binData[14] = 1;
        binData[18] = (byte) 255;
        binData[23] = (byte) 255;
        binData[24] = (byte) 255;
        binData[26] = 1;
        binData[27] = 1;
        binSize += 28;
        if (imgData != null && imgSize > 0) {
            binData[3] = 2;
            binData[4] = (byte) (imgSize + 32);
//            LongSave(binData[4],imgSize+32);
            System.arraycopy(imgData, 0, binData, binData[32], imgSize);
        }
        if (mirData != null && mirSize > 0) {
            binData[3] = 1;
            binData[4] = (byte) (imgSize + 32);
            System.arraycopy(mirData, 0, binData, binData[32], mirSize);
        }

        return 0;
    }

    @Override
    public byte[] EncodeNistData(NistData nistData) {
        if (nistData == null) {
            error(SdkErrorCode.ENCODE_ERROR, " invalid parameter[nistData=null]");
        }
        List<RawNistData> rawNistDatas = new ArrayList<>();
        RawNistData data1 = NistRecType1EncodeFunc(nistData);
        if (data1 != null) {
            rawNistDatas.add(data1);
        }
        RawNistData data2 = NistRecType2EncodeFunc(nistData);
        if (data2 != null) {
            rawNistDatas.add(data2);

        }
        List<RawNistData> data3 = NistRecType3EncodeFunc(nistData, 3);
        if (data3 != null && data3.size() > 0) {
            rawNistDatas.addAll(data3);

        }
        List<RawNistData> data4 = NistRecType4EncodeFunc(nistData);
        if (data4 != null && data4.size() > 0) {
            rawNistDatas.addAll(data4);


        }
        List<RawNistData> data5 = NistRecType5EncodeFunc(nistData);
        if (data5 != null && data5.size() > 0) {
            rawNistDatas.addAll(data5);
        }
        List<RawNistData> data6 = NistRecType6EncodeFunc(nistData);
        if (data6 != null && data6.size() > 0) {
            rawNistDatas.addAll(data6);
        }
        List<RawNistData> data7 = NistRecType7EncodeFunc(nistData);
        if (data7 != null && data7.size() > 0) {
            rawNistDatas.addAll(data7);
        }
        List<RawNistData> data8 = NistRecType8EncodeFunc(nistData);
        if (data8 != null && data8.size() > 0) {
            rawNistDatas.addAll(data8);
        }
        List<RawNistData> data9 = NistRecType9EncodeFunc(nistData);
        if (data9 != null && data9.size() > 0) {
            rawNistDatas.addAll(data9);
        }
        List<RawNistData> data10 = NistRecType10EncodeFunc(nistData, 10);
        if (data10 != null && data10.size() > 0) {
            rawNistDatas.addAll(data10);

        }
        List<RawNistData> data11 = NistRecType11EncodeFunc(nistData);
        if (data11 != null && data11.size() > 0) {
            rawNistDatas.addAll(data11);
        }
        List<RawNistData> data12 = NistRecType12EncodeFunc(nistData);
        if (data12 != null && data12.size() > 0) {
            rawNistDatas.addAll(data12);
        }
        List<RawNistData> data13 = NistRecType13EncodeFunc(nistData);
        if (data13 != null && data13.size() > 0) {
            rawNistDatas.addAll(data13);
        }
        List<RawNistData> data14 = NistRecType14EncodeFunc(nistData);
        if (data14 != null && data14.size() > 0) {
            rawNistDatas.addAll(data14);
        }
        List<RawNistData> data15 = NistRecType15EncodeFunc(nistData);
        if (data15 != null && data15.size() > 0) {
            rawNistDatas.addAll(data15);
        }
        List<RawNistData> data16 = NistRecType16EncodeFunc(nistData);
        if (data16 != null && data16.size() > 0) {
            rawNistDatas.addAll(data16);
        }
        List<RawNistData> data17 = NistRecType17EncodeFunc(nistData);
        if (data17 != null && data17.size() > 0) {
            rawNistDatas.addAll(data17);
        }
        List<RawNistData> data18 = NistRecType18EncodeFunc(nistData);
        if (data18 != null && data18.size() > 0) {
            rawNistDatas.addAll(data18);
        }
        List<RawNistData> data19 = NistRecType19EncodeFunc(nistData);
        if (data19 != null && data19.size() > 0) {
            rawNistDatas.addAll(data19);
        }
        List<RawNistData> data20 = NistRecType20EncodeFunc(nistData);
        if (data20 != null && data20.size() > 0) {
            rawNistDatas.addAll(data20);
        }
        List<RawNistData> data21 = NistRecType21EncodeFunc(nistData);
        if (data21 != null && data21.size() > 0) {
            rawNistDatas.addAll(data21);
        }
        List<RawNistData> data22 = NistRecType22EncodeFunc(nistData);
        if (data22 != null && data22.size() > 0) {
            rawNistDatas.addAll(data22);
        }
        List<RawNistData> data98 = NistRecType98EncodeFunc(nistData);
        if (data98 != null && data98.size() > 0) {
            rawNistDatas.addAll(data98);
        }
        List<RawNistData> data99 = NistRecType99EncodeFunc(nistData);
        if (data99 != null && data99.size() > 0) {
            rawNistDatas.addAll(data99);
        }

        for (RawNistData raw : rawNistDatas) {
            log.debug("type" + raw.type + "   lenght:{}", raw.dataSize);
        }

        int len = 0;
        for (RawNistData raw : rawNistDatas) {
            len += raw.dataSize;
        }
        /*与type1中的idcs同步*/
        SdkUtils.sortNistData(rawNistDatas);
        int pos = 0;
        byte[] data = new byte[len];
        for (RawNistData raw : rawNistDatas) {
            System.arraycopy(raw.data, 0, data, pos, raw.dataSize);
            pos += raw.dataSize;
        }
        return data;
    }


    private byte[] EncodeAscField(AscField ascField, Boolean LastAsc) {

        if (ascField.value == null || ascField.valueLen <= 0) return null;
        int len = SdkUtils.GetNumLen(ascField.fieldType) + 1 + 3 + 1 + ascField.valueLen + 1;
        byte[] pData = new byte[len];
        int nPos = 0;
        byte[] ts = SdkUtils.IntToBytes(ascField.fieldType);
        System.arraycopy(ts, 0, pData, nPos, ts.length);
        nPos += ts.length;
        pData[nPos] = 46;//"."
        nPos++;
        System.arraycopy(SdkUtils.intToBytesOf3(ascField.fieldNum), 0, pData, nPos, 3);
        nPos += 3;
        pData[nPos] = 58;//":"
        nPos++;
        System.arraycopy(ascField.value, 0, pData, nPos, ascField.valueLen);
        nPos += ascField.valueLen;
        if (!LastAsc) {
            pData[nPos] = GS;
        } else {
            pData[nPos] = FS;
        }
        return pData;
    }

    public RawNistData NistRecType1EncodeFunc(NistData nistData) {
        NistRecType1 type1 = NistRecType1.encode(nistData);
        byte[] data = new byte[SdkUtils.getLenByType1(type1)];
        int pos = 0;
        if (type1 == null) {
            error(SdkErrorCode.ENCODE_TYPE1_DATA_ERROR, "encode type1 error");
            return null;
        }
        if (type1.numOfField != type1.ascFields.size()) {
            error(SdkErrorCode.DECODE_TYPE1_DATA_ERROR, "type1 ascField is invalid");
            return null;
        }
        int size = Integer.parseInt(SdkUtils.byteArrayToStr(type1.ascFields.get(0).value));
        int len = SdkUtils.getLenByType1(type1);
        if (size != len) {
            error(SdkErrorCode.ENCODE_TYPE1_DATA_ERROR, "type1 length is invalid");
            return null;
        }
        type1.ascFields = SdkUtils.sortAscFields(type1.ascFields);
        for (int i = 0; i < type1.numOfField; i++) {
            AscField a = type1.ascFields.get(i);
            byte[] d = null;
            if (i == type1.numOfField - 1) {
                d = EncodeAscField(a, true);
            } else {
                d = EncodeAscField(a, false);
            }
            if (d == null) {
                error(SdkErrorCode.ENCODE_TYPE1_DATA_ERROR, "encode type1 ascfield==null");
                continue;
            }
            System.arraycopy(d, 0, data, pos, d.length);
            pos += d.length;
        }
        return new RawNistData(data, pos, 1, 0);
    }

    public RawNistData NistRecType2EncodeFunc(NistData nistData) {
        NistRecType2 type2 = NistRecType2.encode(nistData);
        if(type2 == null){
            error(SdkErrorCode.ENCODE_TYPE2_DATA_ERROR,"encode type2 error");
            return null;
        }
        if(type2.ascFields.size()!=type2.numOfField){
            error(SdkErrorCode.ENCODE_TYPE2_DATA_ERROR,"type2 ascFields size is invalid");
            return null;
        }
        int size= Integer.parseInt(SdkUtils.byteArrayToStr(type2.ascFields.get(0).value));
        int len=SdkUtils.getLenByType2(type2);
        if(size !=len){
            error(SdkErrorCode.ENCODE_TYPE2_DATA_ERROR,"type2 length is invalid");
            return null;
        }
        byte[] data = new byte[SdkUtils.getLenByType2(type2)];
        int pos = 0;
        type2.ascFields = SdkUtils.sortAscFields(type2.ascFields);
        for (int i = 0; i < type2.numOfField; i++) {
            AscField a = type2.ascFields.get(i);
            byte[] d = null;
            if (i == type2.numOfField - 1) {
                d = EncodeAscField(a, true);
            } else {
                d = EncodeAscField(a, false);
            }
            System.arraycopy(d, 0, data, pos, d.length);
            pos += d.length;
        }
        return new RawNistData(data, pos, 2, 0);
    }

    private byte[] getDataByRecType4(NistRecType4 type4) {
        if (type4.recLen == 0) {
            error(SdkErrorCode.ENCODE_TYPE4_DATA_ERROR, "type data 4 length is invalid");
            return null;
        }
        byte[] data = new byte[type4.recLen];
        int pos = 0;
        System.arraycopy(SdkUtils.IntTobyteArray(type4.recLen, 4), 0, data, pos, 4);
        pos += 4;
        data[pos] = (byte) type4.imgIdc;
        pos++;
        data[pos] = (byte) type4.imgImp;
        pos++;
        System.arraycopy(type4.imgPos, 0, data, pos, 6);
        pos += 6;
        data[pos] = (byte) type4.imgRes;
        pos++;
        System.arraycopy(SdkUtils.IntTobyteArray(type4.width, 2), 0, data, pos, 2);
        pos += 2;
        System.arraycopy(SdkUtils.IntTobyteArray(type4.height, 2), 0, data, pos, 2);
        pos += 2;
        data[pos] = (byte) type4.cmsCode;
        pos++;
        System.arraycopy(type4.imgData, 0, data, pos, type4.imgDataLen);
        pos += type4.imgDataLen;
        return data;
    }

    public List<RawNistData> NistRecType3EncodeFunc(NistData nistData, int type) {

        List<NistRecType4> type4List = NistRecType4.encode(nistData, type);
        List<RawNistData> rawNistDataList = new ArrayList<>();
        if (type4List == null) {
            return null;
        }
        for (NistRecType4 type4 : type4List) {
            byte[] d = getDataByRecType4(type4);
            RawNistData raw = new RawNistData(d, d.length, type4.recType, type4.imgIdc);
            rawNistDataList.add(raw);
        }
        return rawNistDataList;
    }

    public List<RawNistData> NistRecType4EncodeFunc(NistData nistData) {
        return NistRecType3EncodeFunc(nistData, 4);
    }

    public List<RawNistData> NistRecType5EncodeFunc(NistData nistData) {
        return NistRecType3EncodeFunc(nistData, 5);
    }

    public List<RawNistData> NistRecType6EncodeFunc(NistData nistData) {
        return NistRecType3EncodeFunc(nistData, 6);
    }

    private byte[] getDataByRecType7(NistRecType7 type7) {
        if (type7.recLen == 0) {
            error(SdkErrorCode.ENCODE_TYPE7_DATA_ERROR, "type data 7 lenght is invalid");
            return null;
        }
        byte[] data = new byte[type7.recLen];
        int pos = 0;
        System.arraycopy(SdkUtils.IntTobyteArray(type7.recLen, 4), 0, data, pos, 4);
        pos += 4;
        data[pos] = (byte) type7.imgIdc;
        pos++;
        System.arraycopy(type7.imgData, 0, data, pos, type7.imgDataLen);
        pos += type7.imgDataLen;

        return data;
    }

    public List<RawNistData> NistRecType7EncodeFunc(NistData nistData) {
        List<NistRecType7> type7List = NistRecType7.encode(nistData);
        List<RawNistData> rawNistDataList = new ArrayList<>();
        if (type7List == null) {
            return null;
        }
        for (NistRecType7 type7 : type7List) {
            byte[] d = getDataByRecType7(type7);
            RawNistData raw = new RawNistData(d, d.length, type7.imgIdc, type7.imgIdc);
            rawNistDataList.add(raw);
        }
        return rawNistDataList;
    }


    private byte[] getDataByRecType8(NistRecType8 type8) {
        if (type8.recLen == 0) {
            error(SdkErrorCode.ENCODE_TYPE8_DATA_ERROR, "type data 8 lenght is invalid");
            return null;
        }
        byte[] data = new byte[type8.recLen];
        int pos = 0;
        System.arraycopy(SdkUtils.IntTobyteArray(type8.recLen, 4), 0, data, pos, 4);
        pos += 4;
        data[pos] = (byte) type8.imgIdc;
        data[++pos] = (byte) type8.imgSig;
        data[++pos] = (byte) type8.cmsCode;
        data[++pos] = (byte) type8.imgRes;
        pos++;
        System.arraycopy(SdkUtils.IntTobyteArray(type8.height, 2), 0, data, pos, 2);
        pos += 2;
        System.arraycopy(SdkUtils.IntTobyteArray(type8.height, 2), 0, data, pos, 2);
        pos += 2;
        System.arraycopy(type8.imgData, 0, data, pos, type8.imgDataLen);

        pos += type8.imgDataLen;

        return data;
    }

    public List<RawNistData> NistRecType8EncodeFunc(NistData nistData) {
        List<NistRecType8> type8List = NistRecType8.encode(nistData);
        List<RawNistData> rawNistDataList = new ArrayList<>();
        if (type8List == null) {
            return null;
        }
        for (NistRecType8 type8 : type8List) {
            byte[] d = getDataByRecType8(type8);
            RawNistData raw = new RawNistData(d, d.length, type8.imgIdc, type8.imgIdc);
            rawNistDataList.add(raw);
        }
        return rawNistDataList;
    }


    public List<RawNistData> NistRecType9EncodeFunc(NistData nistData) {
        List<NistRecType9> type9List = NistRecType9.encode(nistData);
        List<RawNistData> rawNistDataList = new ArrayList<>();
        if (type9List == null) {
            return null;
        }
        for (NistRecType9 type9 : type9List) {
            byte[] d = getDataByRecType9(type9);
            RawNistData raw = new RawNistData(d, d.length, type9.recType, type9.idc);
            rawNistDataList.add(raw);
        }
        return rawNistDataList;
    }

    private byte[] getDataByRecType9(NistRecType9 type9) {
        type9.ascFields = SdkUtils.sortAscFields(type9.ascFields);
        int size = SdkUtils.getLenByType9(type9);
        String s = SdkUtils.byteArrayToStr(type9.ascFields.get(0).value);
        if (size != Integer.parseInt(s)) {
            error(SdkErrorCode.ENCODE_TYPE9_DATA_ERROR, "type9 lenght is invalid");
            return null;
        }
        if(type9.ascFields.size()!=type9.numOfField){
            error(SdkErrorCode.ENCODE_TYPE9_DATA_ERROR,"type9 ascFields is invalid");
            return null;
        }
        if (type9.numOfField == 0) {
            return null;
        }
        byte[] data = new byte[size];
        int pos = 0;
        for (int i = 0; i < type9.numOfField; i++) {
            AscField a = type9.ascFields.get(i);
            byte[] d = null;
            if (i == type9.numOfField - 1) {
                d = EncodeAscField(a, true);
            } else {
                d = EncodeAscField(a, false);
            }
            if (d != null) {
                System.arraycopy(d, 0, data, pos, d.length);
                pos += d.length;
            }
        }
        return data;
    }

    public List<RawNistData> NistRecType10EncodeFunc(NistData nistData, int type) {
        List<NistRecType10> type10List = NistRecType10.encode(nistData, type);
        List<RawNistData> rawNistDataList = new ArrayList<>();
        if (type10List == null) {
            return null;
        }
        for (NistRecType10 type10 : type10List) {
            byte[] d = getDataByRecType10(type10);
            RawNistData raw = new RawNistData(d, d.length, type10.recType, type10.Idc);
            rawNistDataList.add(raw);
        }
        return rawNistDataList;
    }

    private byte[] getDataByRecType10(NistRecType10 type10) {
        int pos = 0;
        int len = SdkUtils.getLenByType10(type10);
        String s = SdkUtils.byteArrayToStr(type10.ascFields.get(0).value);
        if (len != Integer.parseInt(s)) {
            error(SdkErrorCode.ENCODE_TYPE10_DATA_ERROR, "type" + type10.recType + " lenght is invalid");
            return null;
        }
        if (type10.ascFields.size() != type10.numOfField) {
            error(SdkErrorCode.ENCODE_TYPE9_DATA_ERROR, "type"+type10.recType+" ascFields is invalid");
            return null;
        }
        if (type10.numOfField == 0) {
            return null;
        }
        byte[] data = new byte[len];
        for (int i = 0; i < type10.numOfField; i++) {
            AscField a = type10.ascFields.get(i);
            byte[] d = null;
            if (i == type10.numOfField - 1) {
                d = EncodeAscField(a, true);
            } else {
                d = EncodeAscField(a, false);
            }
            if (d == null) {
                error(SdkErrorCode.ENCODE_TYPE10_DATA_ERROR, "TYPE 10 d=null");
                continue;
            }
            System.arraycopy(d, 0, data, pos, d.length);
            pos += d.length;
        }
        return data;
    }

    public List<RawNistData> NistRecType11EncodeFunc(NistData nistData) {
        return NistRecType10EncodeFunc(nistData, 11);
    }

    public List<RawNistData> NistRecType12EncodeFunc(NistData nistData) {
        return NistRecType10EncodeFunc(nistData, 12);
    }

    public List<RawNistData> NistRecType13EncodeFunc(NistData nistData) {
        return NistRecType10EncodeFunc(nistData, 13);
    }

    public List<RawNistData> NistRecType14EncodeFunc(NistData nistData) {
        return NistRecType10EncodeFunc(nistData, 14);
    }

    public List<RawNistData> NistRecType15EncodeFunc(NistData nistData) {
        return NistRecType10EncodeFunc(nistData, 15);
    }

    public List<RawNistData> NistRecType16EncodeFunc(NistData nistData) {
        return NistRecType10EncodeFunc(nistData, 16);
    }

    public List<RawNistData> NistRecType17EncodeFunc(NistData nistData) {
        return NistRecType10EncodeFunc(nistData, 17);
    }

    public List<RawNistData> NistRecType18EncodeFunc(NistData nistData) {
        return NistRecType10EncodeFunc(nistData, 18);
    }

    public List<RawNistData> NistRecType19EncodeFunc(NistData nistData) {
        return NistRecType10EncodeFunc(nistData, 19);
    }

    public List<RawNistData> NistRecType20EncodeFunc(NistData nistData) {
        return NistRecType10EncodeFunc(nistData, 20);
    }

    public List<RawNistData> NistRecType21EncodeFunc(NistData nistData) {
        return NistRecType10EncodeFunc(nistData, 21);
    }

    public List<RawNistData> NistRecType22EncodeFunc(NistData nistData) {
        return NistRecType10EncodeFunc(nistData, 22);
    }

    public List<RawNistData> NistRecType98EncodeFunc(NistData nistData) {
        return NistRecType10EncodeFunc(nistData, 98);
    }

    public List<RawNistData> NistRecType99EncodeFunc(NistData nistData) {
        return NistRecType10EncodeFunc(nistData, 99);
    }

    @Override
    public void error(SdkErrorCode code, String msg) {
        log.error(msg);
    }
}

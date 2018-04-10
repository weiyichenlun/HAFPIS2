package hbie2.nist.nativesdk;

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

import java.util.ArrayList;
import java.util.List;

import static hbie2.nist.nativesdk.ImageFormat.*;


/**
 * Created by pms on 2017/3/13.
 */
public class NistPackDecodeImp implements NistPackDecode, SdkHandler {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public RawNistData DecodeInit(String filePath) {
//        File f = new File(filePath);
//        byte[] data = SdkUtils.readImageBytesFromFile(f);
        byte[] data = SdkUtils.readImageBytesFromFile(filePath);
        if (data == null || data.length == 0) {
            return null;
        } else {
            RawNistData rawNistData = new RawNistData();
            rawNistData.dataSize = data.length;
            rawNistData.data = data;
            return rawNistData;
        }
    }

    @Override
    public NistData DecodeNistData(RawNistData rawNistData) {
        NistData nistData = new NistData();
        if (rawNistData.data == null || rawNistData.dataSize <= 0) {
            error(SdkErrorCode.DECODE_ERROR, "NIST数据包错误");
            return null;
        }
        int nNextPos = 0;
        NistRecType1 type1 = NistRecType1DecodeFunc(rawNistData, nNextPos);
        nNextPos = type1.recLen;
        for (AscField asc : type1.ascFields) {
            NistTxt txt = new NistTxt(asc.fieldType, -1, asc.fieldNum, asc.value, asc.valueLen);
            nistData.nistTxtList.add(txt);
            nistData.numOfTxt++;
        }
        for (NistRecType1.TypeIdc typeIdc : type1.typeIdcs) {
            int type = typeIdc.fieldType;
            int idc = typeIdc.fieldIDC;
            switch (type) {
                case 2:
                    NistRecType2 type2 = NistRecType2DecodeFunc(rawNistData, nNextPos);
                    nNextPos = type2.recLen;
                    if (type2 == null) {
                        error(SdkErrorCode.DECODE_TYPE2_DATA_ERROR, "decode error【NistRecType2==null】");
                        break;
                    }
                    type2.recType = type;
                    for (AscField asc : type2.ascFields) {
                        if (asc == null) {
                            error(SdkErrorCode.DECODE_TYPE2_DATA_ERROR, "decode error【type2.ascField==null】");
                            continue;
                        }
                        NistTxt txt = new NistTxt(type2.recType, idc, asc.fieldNum, asc.value, asc.valueLen);
                        nistData.nistTxtList.add(txt);
                        nistData.numOfTxt++;
                    }
                    break;
                case 3:
                case 4:
                case 5:
                case 6:
                    NistRecType4 type3 = NistRecType4DecodeFunc(rawNistData, nNextPos);
                    nNextPos = type3.recLen;
                    if (type3 == null) {
                        error(SdkErrorCode.DECODE_TYPE4_DATA_ERROR, "decode error【NistRecType4==null】");
                        break;
                    }
                    type3.recType = type;
                    NistImg img = new NistImg(type3.recType, idc, type3.imgImp, SdkUtils.byteArrayToIntFromHigntoLow(type3.imgPos),
                            type3.imgRes, type3.imgRes, type3.cmsCode, type3.width, type3.height, type3.imgData, type3.imgDataLen, null, 0);
                    nistData.nistImgList.add(img);
                    log.debug("image size {}", img.imgDataLen);
                    nistData.numOfImg++;
                    break;
                case 7:
                    NistRecType7 type7 = NistRecType7DecodeFunc(rawNistData, nNextPos);
                    nNextPos = type7.recLen;
                    if (type7 == null) {
                        error(SdkErrorCode.DECODE_TYPE7_DATA_ERROR, "decode error【NistRecType7==null】");
                        break;
                    }
                    img = new NistImg(type, idc, 0, 0, 0, 0, 0, 0, 0, type7.imgData, type7.imgDataLen, null, 0);
                    nistData.nistImgList.add(img);
                    nistData.numOfImg++;
                    break;
                case 8:
                    NistRecType8 type8 = NistRecType8DecodeFunc(rawNistData, nNextPos);
                    nNextPos = type8.recLen;
                    if (type8 == null) {
                        error(SdkErrorCode.DECODE_TYPE8_DATA_ERROR, "decode error【NistRecType8==null】");
                        break;
                    }
                    img = new NistImg(type, idc, 0, 0,
                            type8.imgRes, type8.imgRes, type8.cmsCode, type8.width, type8.height, type8.imgData, type8.imgDataLen, null, 0);
                    nistData.nistImgList.add(img);
                    nistData.numOfImg++;
                    break;
                case 9:
                    NistRecType9 type9 = NistRecType9DecodeFunc(rawNistData, nNextPos);
                    nNextPos = type9.recLen;
                    if (type9 == null) {
                        error(SdkErrorCode.DECODE_TYPE9_DATA_ERROR, "decode error【NistRecType9==null】");
                        break;
                    }
                    type9.recType = type;
                    for (AscField asc : type9.ascFields) {
                        if (asc == null) {
                            error(SdkErrorCode.DECODE_TYPE9_DATA_ERROR, "decode error【type9.ascField=null】");
                            continue;
                        }
                        NistTxt txt = new NistTxt(type9.recType, idc, asc.fieldNum, asc.value, asc.valueLen);
                        nistData.nistTxtList.add(txt);
                        nistData.numOfTxt++;
                    }
                    break;
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 98:
                case 99:
                    NistRecType10 type10 = NistRecType10DecodeFunc(rawNistData, nNextPos, type);
                    nNextPos = type10.recLen;
                    for (AscField asc : type10.ascFields) {
                        if (asc == null) {
                            error(SdkErrorCode.DECODE_TYPE10_DATA_ERROR, "type10.ascField=null");
                            continue;
                        }
                        NistTxt txt = new NistTxt(type10.recType, idc, asc.fieldNum, asc.value, asc.valueLen);
                        nistData.nistTxtList.add(txt);
                        nistData.numOfTxt++;
                    }
                    log.debug("image length: {}，w={}，h={}", type10.imgDataLen, type10.imgWidth, type10.imgHeigh);
                    img = new NistImg(type, idc, 0, 0,
                            type10.imgResX, type10.imgResY, type10.imgCmsCode, type10.imgWidth, type10.imgHeigh, type10.imgData, type10.imgDataLen, null, 0);
                    nistData.nistImgList.add(img);
                    nistData.numOfImg++;
                    break;
            }

        }
        return nistData;


    }

    private NistRecType1 NistRecType1DecodeFunc(RawNistData rawNistData, int nNextPos) {
        NistRecType1 type1 = new NistRecType1();
        int nCurPos = nNextPos;
        int BegPos = 0, EndPos = 0;
        byte[] data = rawNistData.data;

        for (nCurPos = nNextPos; nCurPos < rawNistData.dataSize; nCurPos++) {
            BegPos = EndPos;

            if (data[nCurPos] == FS || data[nCurPos] == GS) {
                EndPos = nCurPos + 1;
                byte[] ascValue = new byte[nCurPos - BegPos];
                System.arraycopy(data, BegPos, ascValue, 0, ascValue.length);
                AscField ascField = DecodeAscField(ascValue);
                if (ascField.numOfSubField != 0) {
                    for (AscField.SubField sub : ascField.subFields) {
                        SdkUtils.setTypeIdc(type1, sub.type, sub.idc);
                    }
                }
                type1.ascFields.add(ascField);
                if (data[nCurPos] == FS) {
//                    log.debug("type end pos:{}",nCurPos);
                    break;
                }

            }
        }


        nNextPos = nCurPos + 1;
        type1.recLen = nNextPos;

        return type1;

    }

    private NistRecType2 NistRecType2DecodeFunc(RawNistData rawNistData, int nNextPos) {

        int nCurPos = nNextPos;
        int BegPos = nCurPos;
        int EndPos = nNextPos;
        byte[] data = rawNistData.data;
        NistRecType2 type2 = new NistRecType2();
        for (nCurPos = nNextPos; nCurPos < rawNistData.dataSize; nCurPos++) {
            BegPos = EndPos;
            if (data[nCurPos] == FS || data[nCurPos] == GS) {
                EndPos = nCurPos + 1;
                byte[] ascValue = new byte[nCurPos - BegPos];
                System.arraycopy(data, BegPos, ascValue, 0, ascValue.length);
                AscField ascField = DecodeAscField(ascValue);
                type2.ascFields.add(ascField);
            }
            if (data[nCurPos] == FS) {
//                log.debug("type end pos:{}",nCurPos);
                break;
            }
        }

        nNextPos = nCurPos + 1;
        type2.recLen = nNextPos;
        return type2;
    }

    private NistRecType4 NistRecType4DecodeFunc(RawNistData rawNistData, int nNextPos) {
        int nCurPos = nNextPos;
        int EndPos = nCurPos;
        byte[] data = rawNistData.data;

        NistRecType4 type4 = new NistRecType4();
        byte[] typeLen = new byte[4];
        System.arraycopy(data, nCurPos, typeLen, 0, 4);
        type4.recLen = SdkUtils.byteArrayToIntFromHigntoLow(typeLen);
        type4.imgDataLen = type4.recLen - 18;

        nCurPos += 4;
        type4.imgIdc = data[nCurPos];
        nCurPos++;
        type4.imgImp = data[nCurPos];
        nCurPos++;
        System.arraycopy(data, nCurPos, type4.imgPos, 0, 6);
        nCurPos += 6;
        type4.imgRes = data[nCurPos];
        nCurPos++;
        byte[] width = new byte[2];
        System.arraycopy(data, nCurPos, width, 0, 2);
        type4.width = SdkUtils.byteArrayToIntFromHigntoLow(width);
        nCurPos += 2;
        byte[] height = new byte[2];
        System.arraycopy(data, nCurPos, height, 0, 2);
        type4.height = SdkUtils.byteArrayToIntFromHigntoLow(height);
        nCurPos += 2;
        type4.cmsCode = data[nCurPos];
        nCurPos++;


        type4.imgData = new byte[type4.imgDataLen];
        System.arraycopy(data, nCurPos, type4.imgData, 0, type4.imgDataLen);
        nCurPos += type4.imgDataLen;
        EndPos = nCurPos;

        nNextPos = EndPos;
        log.debug("type{} end pos :{}", 4, nCurPos);
        type4.recLen = nNextPos;
        return type4;
    }


    private NistRecType7 NistRecType7DecodeFunc(RawNistData rawNistData, int nNextPos) {
        int nCurPos = nNextPos;
        int BegPos = nCurPos;
        int EndPos = nCurPos;

        byte[] data = rawNistData.data;
        NistRecType7 type7 = new NistRecType7();

        byte[] typeLen = new byte[4];
        System.arraycopy(data, nCurPos, typeLen, 0, 4);
        type7.recLen = SdkUtils.byteArrayToIntFromHigntoLow(typeLen);
        nCurPos += 4;
        type7.imgIdc = data[nCurPos];
        nCurPos++;

        BegPos = nCurPos;
        type7.imgDataLen = type7.recLen - 5;
        type7.imgData = new byte[type7.imgDataLen];
        System.arraycopy(data, nCurPos, type7.imgData, 0, type7.imgDataLen);
        EndPos += type7.imgDataLen;

        nNextPos = EndPos + 1;
        type7.recLen = nNextPos;
        return type7;
    }

    private NistRecType8 NistRecType8DecodeFunc(RawNistData rawNistData, int nNextPos) {
        int nCurPos = nNextPos;
        int BegPos = nCurPos;
        int EndPos = nCurPos;

        byte[] data = rawNistData.data;
        NistRecType8 type8 = new NistRecType8();

        byte[] typeLen = new byte[4];
        System.arraycopy(data, nCurPos, typeLen, 0, 4);
        type8.recLen = SdkUtils.byteArrayToIntFromHigntoLow(typeLen);
        nCurPos += 4;
        type8.imgIdc = data[nCurPos];
        nCurPos++;
        type8.imgSig = data[nCurPos];
        nCurPos++;
        type8.cmsCode = data[nCurPos];
        nCurPos++;
        type8.imgRes = data[nCurPos];
        nCurPos++;
        byte[] height = new byte[2];
        System.arraycopy(data, nCurPos, height, 0, 2);
        type8.height = SdkUtils.byteArrayToIntFromHigntoLow(height);
        nCurPos += 2;
        byte[] width = new byte[2];
        System.arraycopy(data, nCurPos, width, 0, 2);
        type8.width = SdkUtils.byteArrayToIntFromHigntoLow(width);
        nCurPos += 2;
        BegPos = nCurPos;
        type8.imgDataLen = type8.recLen - 12;
        type8.imgData = new byte[type8.imgDataLen];
        System.arraycopy(data, BegPos, type8.imgData, 0, type8.imgDataLen);
        type8.imgDataLen = type8.imgData.length;
        nCurPos += type8.imgDataLen;

        nNextPos = nCurPos + 1;

        type8.recLen = nNextPos;
        return type8;
    }

    private NistRecType9 NistRecType9DecodeFunc(RawNistData rawNistData, int nNextPos) {
        int nCurPos = nNextPos;
        int BegPos = nCurPos;
        int EndPos = nCurPos;
        NistRecType9 type9 = new NistRecType9();
        byte[] data = rawNistData.data;

        for (nCurPos = nNextPos; nCurPos < data.length; nCurPos++) {
            BegPos = EndPos;
            if (data[nCurPos] == FS || data[nCurPos] == GS) {
                EndPos = nCurPos + 1;
                byte[] ascValue = new byte[nCurPos - BegPos];
                System.arraycopy(data, BegPos, ascValue, 0, ascValue.length);
                AscField ascField = DecodeAscField(ascValue);
                type9.ascFields.add(ascField);
                switch (ascField.fieldNum) {

                }
            }
            if (data[nCurPos] == FS) {
                break;
            }
        }

        nNextPos = nCurPos + 1;
        type9.recLen = nNextPos;
        return type9;
    }

    private NistRecType10 NistRecType10DecodeFunc(RawNistData rawNistData, int nNextPos, int type) {
        int nCurPos = nNextPos;
        int BegPos = nCurPos;
        int EndPos = nCurPos;
        NistRecType10 type10 = new NistRecType10();
        type10.recType = type;
        byte[] data = rawNistData.data;
        int reclen = 0, txtLen = 0;
        for (nCurPos = nNextPos; nCurPos < data.length; nCurPos++) {
            BegPos = EndPos;
            if (data[nCurPos] == FS || data[nCurPos] == GS) {


                EndPos = nCurPos + 1;
                byte[] ascValue = new byte[nCurPos - BegPos];
                System.arraycopy(data, BegPos, ascValue, 0, ascValue.length);
                AscField asc = DecodeAscField(ascValue);
                switch (asc.fieldNum) {
                    case 1:
                        reclen = SdkUtils.byteArrayToIntFromLowtoHign(asc.value);
                        break;
                    case 6:
                        type10.imgWidth = SdkUtils.byteArrayToIntFromLowtoHign(asc.value);
                        break;
                    case 7:
                        type10.imgHeigh = SdkUtils.byteArrayToIntFromLowtoHign(asc.value);
                        break;
                    case 9:
                        type10.imgResX = SdkUtils.byteArrayToIntFromLowtoHign(asc.value);
                        break;
                    case 10:
                        type10.imgResY = SdkUtils.byteArrayToIntFromLowtoHign(asc.value);
                        break;
                    case 11:
                        type10.imgCmsCode = SdkUtils.str2CmsCode(new String(asc.value));
                        break;
                    case 999:
                        txtLen = BegPos - nNextPos; //文本长度
                        type10.imgDataLen = (reclen - SdkUtils.GetNumLen(type10.recType) - 1 - SdkUtils.GetNumLen(999) - 1 - 1) - txtLen; //图像长度 格式10.999:data.
                        BegPos += SdkUtils.GetNumLen(asc.fieldType) + 1 + SdkUtils.GetNumLen(asc.fieldNum) + 1;
                        if (type10.imgDataLen > 0) {
                            type10.imgData = new byte[type10.imgDataLen];
                            System.arraycopy(data, BegPos, type10.imgData, 0, type10.imgDataLen);
                        }
                        asc.value = type10.imgData;
                        asc.valueLen = type10.imgDataLen;

                        nCurPos = BegPos + asc.valueLen;

                        break;
                }

                type10.ascFields.add(asc);

                if (data[nCurPos] == FS && asc.fieldNum == 999) {
                    if ((nCurPos - nNextPos) != (reclen - 1)) {
                        error(SdkErrorCode.DECODE_TYPE10_DATA_ERROR, "imageData length error");
                    }
                    break;
                }


            }
        }

        nNextPos = nCurPos + 1;
        type10.recLen = nNextPos;

        return type10;
    }


    private AscField DecodeAscField(byte[] Acsdata) {
        AscField ascField = new AscField();
        int nCurPos = 0;
        int BegPos = 0, EndPos = 0;
        for (; nCurPos < Acsdata.length; nCurPos++) {
            EndPos = nCurPos;
            if (Acsdata[nCurPos] == 46) {
                /*type*/
                byte[] type = new byte[nCurPos - BegPos];
                System.arraycopy(Acsdata, BegPos, type, 0, nCurPos);
                BegPos = EndPos + 1;
                ascField.fieldType = Integer.parseInt(SdkUtils.byteArrayToStr(type));

            } else if (Acsdata[nCurPos] == 58) {
                /*fldnuber*/
                byte[] fldnumber = new byte[nCurPos - BegPos];
                System.arraycopy(Acsdata, BegPos, fldnumber, 0, fldnumber.length);
                ascField.fieldNum = Integer.parseInt(SdkUtils.byteArrayToStr(fldnumber));
                BegPos = nCurPos + 1;
                EndPos = nCurPos = Acsdata.length;

                if (ascField.fieldNum == 999) {
                    return ascField;
                }
                /* value*/
                ascField.valueLen = nCurPos - BegPos;
                ascField.value = new byte[ascField.valueLen];
                System.arraycopy(Acsdata, BegPos, ascField.value, 0, ascField.valueLen);
            }
        }
        if (ascField.fieldNum == 3) {
//            System.out.println("num3 len :"+ascField.valueLen);
            ascField.subFields.addAll(DecodeSubFiledList(ascField.value));
            ascField.numOfSubField = ascField.subFields.size();
        }
        return ascField;
    }

    private List<AscField.SubField> DecodeSubFiledList(byte[] AscValue) {
        List<AscField.SubField> subFields = new ArrayList<>();
        int nCurPos = 0;
        int BegPos = 0, EndPos = 0;
        for (; nCurPos < AscValue.length; nCurPos++) {
            EndPos = nCurPos;
            if (AscValue[nCurPos] == RS || AscValue[nCurPos] == GS) {
                byte[] subData = new byte[nCurPos - BegPos];
                System.arraycopy(AscValue, BegPos, subData, 0, subData.length);
                AscField.SubField subField = DecodeSubFiled(subData);
                subFields.add(subField);
                BegPos = EndPos + 1;
            }
        }
        /*最后一个TypeIdc*/
        nCurPos = AscValue.length;
        byte[] subData = new byte[nCurPos - BegPos];
        System.arraycopy(AscValue, BegPos, subData, 0, subData.length);
        AscField.SubField subField = DecodeSubFiled(subData);
        subFields.add(subField);

        return subFields;
    }

    private AscField.SubField DecodeSubFiled(byte[] subData) {
        AscField.SubField sub = new AscField.SubField();
        int nCurPos;
        int BegPos = 0, EndPos = 0;
        for (nCurPos = 0; nCurPos < subData.length; nCurPos++) {
            EndPos = nCurPos;
            if (subData[nCurPos] == US) {
                byte[] type = new byte[nCurPos - BegPos];
                System.arraycopy(subData, BegPos, type, 0, type.length);

                nCurPos = subData.length;
                BegPos = EndPos + 1;
                byte[] idc = new byte[nCurPos - BegPos];
                System.arraycopy(subData, BegPos, idc, 0, idc.length);

                sub.type = Integer.parseInt(SdkUtils.byteArrayToStr(type));
                sub.idc = Integer.parseInt(SdkUtils.byteArrayToStr(idc));
                break;
            }
        }
        return sub;
    }

    @Override
    public String GetTransNo(NistData nistData) {
        if (nistData == null) {
            error(SdkErrorCode.DECODE_ERROR, "parse transNo error");
            return null;
        }
        for (NistTxt txt : nistData.nistTxtList) {
            if (txt.fieldType == 1 && txt.fieldNum == 9) {
                nistData.transNo = txt.value;
            }
        }
        return SdkUtils.byteArrayToStr(nistData.transNo);
    }

    @Override
    public void error(SdkErrorCode code, String msg) {
        log.error(msg);
    }
}

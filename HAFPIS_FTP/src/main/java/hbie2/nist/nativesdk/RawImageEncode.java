package hbie2.nist.nativesdk;

import hbie2.nist.nistType.NistData;
import hbie2.nist.nistType.NistImg;

/**
 * Created by pms on 2017/3/13.
 */
public interface RawImageEncode {
    public NistData Nist_Init();


    public int encodeType7(byte[] imgData, int imgSize, byte[] mirData, int mirSize, int binSize);


    public boolean AddTransNo(NistData nistData, String TransNo);

    public NistImg ReadImageFiles(String fileName, int imp, int type, int pos, String cmsCode, int resx, int resy, int sap);

    public boolean AddTxtData(NistData nistData, int Type, int idc, int fldnum, byte[] val, int valLen);

    public boolean AddImgData(NistData nistData, int Type, int idc, int imp, int pos, int resX, int resY, int cmsCode, int w, int h, byte[] imgData, int imgLen, byte[] mirData, int mirLen);

    public byte[] EncodeNistData(NistData nistData);
}

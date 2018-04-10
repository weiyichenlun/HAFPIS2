package hbie2.nist.nistType;

import hbie2.nist.nativesdk.SdkUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pms on 2017/3/13.
 */
public class NistRecType8 {

    public int recLen;
    public int imgIdc;
    public int imgSig;
    public int cmsCode;
    public int imgRes;
    public int width;
    public int height;
    public int imgDataLen;
    public byte[] imgData;


    public NistRecType8() {
        super();
    }

    public NistRecType8(int recLen, int imgIdc, int imgSig, int cmsCode, int imgRes, int width, int height, int imgDataLen, byte[] imgData) {
        super();
        this.recLen = recLen;
        this.imgIdc = imgIdc;
        this.imgSig = imgSig;
        this.cmsCode = SdkUtils.DefaultSRT(cmsCode);
        this.imgRes = imgRes;
        this.width = width;
        this.height = height;
        this.imgDataLen = imgDataLen;
        this.imgData = imgData;
    }

    public static List<NistRecType8> encode(NistData nistData) {
        List<NistRecType8> nistRecType8s = new ArrayList<>();
        for (NistImg img : nistData.nistImgList) {
            if (img.type == 8) {
                int recLen = img.imgDataLen + 12;
                NistRecType8 type8 = new NistRecType8(recLen, img.idc, 0, img.cmsCode, img.resX, img.width, img.height, img.imgDataLen, img.imgData);
                nistRecType8s.add(type8);
            }
        }
        return nistRecType8s;
    }
}

package hbie2.nist.nistType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pms on 2017/3/13.
 */
public class NistRecType4 {
    public int recLen;
    public int recType;
    public int imgIdc;
    public int imgImp;
    public byte[] imgPos = new byte[6];
    public int imgRes;//ISR
    public int width;
    public int height;
    public int cmsCode;
    public int imgDataLen;
    public byte[] imgData;


    public NistRecType4() {
        super();
    }

    public NistRecType4(int recLen, int recType, int imgIdc, int cmsCode, int imgImp, int imgRes, int width, int height,
                        byte[] imgData, int imgDataLen, byte[] imgPos) {
        super();
        this.recLen = recLen;
        this.imgIdc = imgIdc;
        this.cmsCode = cmsCode;
        this.recType = recType;
        this.imgImp = imgImp;
        this.imgRes = imgRes;
        this.imgPos = imgPos;
        this.width = width;
        this.height = height;
        this.imgDataLen = imgDataLen;
        this.imgData = imgData;
    }

    public static List<NistRecType4> encode(NistData nistData, int type) {
        List<NistRecType4> nistRecType4s = new ArrayList<>();
        for (NistImg img : nistData.nistImgList) {
            if (img.type == type) {
                int recLen = (18 + img.imgDataLen);
                byte[] pos = new byte[6];
                pos[5] = (byte) ((img.pos >> 40) & 0xFF);
                pos[4] = (byte) ((img.pos >> 32) & 0xFF);
                pos[3] = (byte) ((img.pos >> 24) & 0xFF);
                pos[2] = (byte) ((img.pos >> 16) & 0xFF);
                pos[1] = (byte) ((img.pos >> 8) & 0xFF);
                pos[0] = (byte) (img.pos & 0xFF);
                NistRecType4 type4 = new NistRecType4(recLen, img.type, img.idc, img.cmsCode, img.imp, img.resX, img.width, img.height, img.imgData, img.imgDataLen, pos);
                nistRecType4s.add(type4);
            }

        }

        return nistRecType4s;
    }
}

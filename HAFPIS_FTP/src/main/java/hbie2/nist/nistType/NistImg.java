package hbie2.nist.nistType;

/**
 * Created by pms on 2017/3/13.
 */
public class NistImg {
    public int type;
    public int idc;
    public int imp;
    public int pos;
    public int resX;
    public int resY;
    public int cmsCode;
    public int width;
    public int height;
    public int imgDataLen;
    public byte[] imgData;
    public byte[] mirData;
    public int mirLen;

//    public int SAP;


    public NistImg() {
        super();
    }

    public NistImg(int type, int idc, int imp, int pos, int resX, int resY, int cmsCode, int w, int h, byte[] imgData, int imgLen, byte[] mirData, int mirLen) {
        super();
        this.type = type;
        this.idc = idc;
        this.cmsCode = cmsCode;
        this.pos = pos;
        this.imp = imp;
        this.height = h;
        this.width = w;
        this.resX = resX;
        this.resY = resY;
        this.imgData = imgData;
        this.imgDataLen = imgLen;
        this.mirData = mirData;
        this.mirLen = mirLen;
    }


}

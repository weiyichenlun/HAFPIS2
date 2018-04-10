package hbie2.nist.nistType;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by pms on 2017/3/13.
 */
public class NistRecType7 {

    public int recLen;
    public int imgIdc;
    public int imgDataLen;
    public byte[] imgData;


    public NistRecType7() {
        super();
    }

    public NistRecType7(int recLen, int imgIdc, int imgDataLen, byte[] imgData) {
        super();
        this.recLen = recLen;
        this.imgIdc = imgIdc;
        this.imgDataLen = imgDataLen;
        this.imgData = imgData;
    }

    public static List<NistRecType7> encode(NistData nistData) {
        List<NistRecType7> nistRecType7s = new ArrayList<>();
        for (NistImg img : nistData.nistImgList) {
            if (img.type == 7) {
                int recLen = (5 + img.imgDataLen);
                NistRecType7 type7 = new NistRecType7(recLen, img.idc, img.imgDataLen, img.imgData);
                nistRecType7s.add(type7);
            }
        }
        return nistRecType7s;
    }
}

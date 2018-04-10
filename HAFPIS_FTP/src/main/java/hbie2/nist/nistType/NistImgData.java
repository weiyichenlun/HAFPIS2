package hbie2.nist.nistType;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Created by pms on 2017/3/7.
 */
public class NistImgData extends Structure {
    /**
     * \u56fe\u50cf\u7c7b\u578b
     */
    public int type;
    /**
     * \u56fe\u50cf\u6807\u793a\u7b26
     */
    public int idc;
    /**
     * \u56fe\u50cf\u7c7b\u578b
     */
    public int imp;
    /**
     * \u6307\u4f4d\u3001\u638c\u4f4d\u7b49
     */
    public int pos;
    /**
     * X\u56fe\u50cf\u5206\u8fa8\u7387
     */
    public int resX;
    /**
     * Y\u65b9\u5411\u56fe\u50cf\u5206\u8fa8\u7387
     */
    public int resY;
    /**
     * \u538b\u7f29\u4ee3\u7801
     */
    public int cmsCode;
    /**
     * \u56fe\u50cf\u5bbd\u5ea6
     */
    public int width;
    /**
     * \u56fe\u50cf\u9ad8\u5ea6
     */
    public int height;
    /**
     * \u6570\u636e\u5927\u5c0f
     */
    public int length;
    /**
     * \u6570\u636e<br>
     * C recType : unsigned char*
     */
    public Pointer data;

    public NistImgData() {
        super();
    }

    public NistImgData(Pointer peer) {
        super(peer);
    }

    protected List<String> getFieldOrder() {
        return Arrays.asList("type", "idc", "imp", "pos", "resX", "resY", "cmsCode", "width", "height", "length", "data");
    }

    public static class ByReference extends NistTxtData implements Structure.ByReference {

    }

    ;

    public static class ByValue extends NistTxtData implements Structure.ByValue {

    }

    ;

}

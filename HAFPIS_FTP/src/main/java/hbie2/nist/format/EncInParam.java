package hbie2.nist.format;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Created by pms on 2017/3/13.
 */
public class EncInParam  extends Structure {
    public int inWidth;
    public int inHeight;
    public int buf_size;
    /** only WSQ */
    public float rate;
    public int resolution;
    public int encType;
    public Pointer inBuf;
    public EncInParam() {
        super();
    }
    protected List<String> getFieldOrder() {
        return Arrays.asList("inWidth", "inHeight", "buf_size", "rate", "resolution", "encType", "inBuf");
    }
    /**
     * @param rate only WSQ<br>
     * @param inBuf C recType : unsigned char*
     */
    public EncInParam(int inWidth, int inHeight, int buf_size, float rate, int resolution, int encType, Pointer inBuf) {
        super();
        this.inWidth = inWidth;
        this.inHeight = inHeight;
        this.buf_size = buf_size;
        this.rate = rate;
        this.resolution = resolution;
        this.encType = encType;
        this.inBuf = inBuf;
    }
    public EncInParam(Pointer peer) {
        super(peer);
    }
    public static class ByReference extends EncInParam implements Structure.ByReference {

    };
    public static class ByValue extends EncInParam implements Structure.ByValue {

    };
}

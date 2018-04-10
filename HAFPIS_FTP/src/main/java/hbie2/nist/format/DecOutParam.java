package hbie2.nist.format;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Created by pms on 2017/3/13.
 */
public class DecOutParam extends Structure {
    public int width;
    public int height;
    public int buf_size;
    public int resolution;
    /** C recType : unsigned char* */
    public Pointer buf;
    public DecOutParam() {
        super();
    }
    protected List<String > getFieldOrder() {
        return Arrays.asList("width", "height", "buf_size", "resolution", "buf");
    }
    /** @param buf C recType : unsigned char* */
    public DecOutParam(int width, int height, int buf_size, int resolution, Pointer buf) {
        super();
        this.width = width;
        this.height = height;
        this.buf_size = buf_size;
        this.resolution = resolution;
        this.buf = buf;
    }
    public DecOutParam(Pointer peer) {
        super(peer);
    }
    public static class ByReference extends DecOutParam implements Structure.ByReference {

    };
    public static class ByValue extends DecOutParam implements Structure.ByValue {

    };
}

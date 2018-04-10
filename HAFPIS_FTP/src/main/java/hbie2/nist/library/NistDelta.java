package hbie2.nist.library;

import java.util.Arrays;
import java.util.List;

/**
 * Created by pms on 2017/3/1.
 */
public class NistDelta {
    public int cx;
    public int cy;
    public NistDelta() {
        super();
    }
    protected List<? > getFieldOrder() {
        return Arrays.asList("cx", "cy");
    }
    public NistDelta(int cx, int cy) {
        super();
        this.cx = cx;
        this.cy = cy;
    }
}

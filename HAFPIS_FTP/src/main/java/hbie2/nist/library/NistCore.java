package hbie2.nist.library;

import java.util.Arrays;
import java.util.List;

/**
 * Created by pms on 2017/3/1.
 */
public class NistCore {
    public int cx;
    public int cy;
    public int cd;
    public int radiu;
    public NistCore() {
        super();
    }
    protected List<? > getFieldOrder() {
        return Arrays.asList("cx", "cy", "cd", "radiu");
    }
    public NistCore(int cx, int cy, int cd, int radiu) {
        super();
        this.cx = cx;
        this.cy = cy;
        this.cd = cd;
        this.radiu = radiu;
    }
}

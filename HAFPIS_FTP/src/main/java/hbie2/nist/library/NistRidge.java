package hbie2.nist.library;

import java.util.Arrays;
import java.util.List;

/**
 * Created by pms on 2017/3/1.
 */
public class NistRidge {
    public int mntIndex;
    public int RidgeCnt;
    public NistRidge() {
        super();
    }
    protected List<? > getFieldOrder() {
        return Arrays.asList("mntIndex", "RidgeCnt");
    }
    public NistRidge(int mntIndex, int RidgeCnt) {
        super();
        this.mntIndex = mntIndex;
        this.RidgeCnt = RidgeCnt;
    }
}

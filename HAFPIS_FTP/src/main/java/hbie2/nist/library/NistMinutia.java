package hbie2.nist.library;

import java.util.Arrays;
import java.util.List;

/**
 * Created by pms on 2017/3/1.
 */
public class NistMinutia{
    public int index;
    public int x;
    public int y;
    public int d;
    public int q;
    public byte type;
    public int nNumOfRidge;
    public NistRidge ridgeList;

    public NistMinutia() {
        super();
    }
    protected List<?> getFieldOrder() {
        return Arrays.asList("index", "x", "y", "d", "q", "recType", "nNumOfRidge", "ridgeList");
    }

    public NistMinutia(int index, int x, int y, int d, int q, byte type, int nNumOfRidge, NistRidge ridgeList) {
        super();
        this.index = index;
        this.x = x;
        this.y = y;
        this.d = d;
        this.q = q;
        this.type = type;
        this.nNumOfRidge = nNumOfRidge;
        this.ridgeList = ridgeList;
    }

}

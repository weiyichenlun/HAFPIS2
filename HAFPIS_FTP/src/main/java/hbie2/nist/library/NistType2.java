package hbie2.nist.library;

import java.util.Arrays;
import java.util.List;

/**
 * Created by pms on 2017/3/1.
 */
public class NistType2 {
    public int fieldNum;
    public byte[] value;
    public int valueLen;
    public NistType2() {
        super();
    }
    protected List<? > getFieldOrder() {
        return Arrays.asList("fieldNum", "value", "valueLen");
    }
    /** @param value C recType : unsigned char* */
    public NistType2(int fieldNum, byte[] value, int valueLen) {
        super();
        this.fieldNum = fieldNum;
        this.value = value;
        this.valueLen = valueLen;
    }
}

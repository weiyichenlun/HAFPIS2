package hbie2.nist.nistType;

/**
 * Created by pms on 2017/3/13.
 */
public class NistTxt {
    public int fieldIdc;
    public int fieldType;
    public int fieldNum;
    public byte[] value;
    public int valueLen;

    public NistTxt() {
        super();
    }

    public NistTxt(int fieldType, int fieldIdc, int fieldNum, byte[] value, int valueLen) {
        super();
        this.fieldType = fieldType;
        this.fieldIdc = fieldIdc;
        this.fieldNum = fieldNum;
        this.value = value;
        this.valueLen = valueLen;
    }

}

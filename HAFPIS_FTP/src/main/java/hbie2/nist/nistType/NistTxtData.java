package hbie2.nist.nistType;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Created by pms on 2017/3/7.
 */
public class NistTxtData extends Structure {
    public int fieldIdc;
    /**
     * type2 or type9
     */
    public int fieldType;
    public int fieldNum;
    /**
     * C recType : unsigned char*
     */
    public Pointer value;
    public int valueLen;

    public NistTxtData() {
        super();
    }

    protected List<String> getFieldOrder() {
        return Arrays.asList("fieldIdc", "fieldType", "fieldNum", "value", "valueLen");
    }

    /**
     * @param fieldType type2 or type9<br>
     * @param value     C recType : unsigned char*
     */
    public NistTxtData(int fieldIdc, int fieldType, int fieldNum, Pointer value, int valueLen) {
        super();
        this.fieldIdc = fieldIdc;
        this.fieldType = fieldType;
        this.fieldNum = fieldNum;
        this.value = value;
        this.valueLen = valueLen;
    }

    public NistTxtData(Pointer peer) {
        super(peer);
    }

    public static class ByReference extends NistTxtData implements Structure.ByReference {

    }

    ;

    public static class ByValue extends NistTxtData implements Structure.ByValue {

    }

    ;
}

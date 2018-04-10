package hbie2.nist.library;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pms on 2017/3/7.
 */
public class AscField{

    public int fieldType;
    public int fieldNum;
    public int valueLen;
    public byte[] value;
    public int numOfSubField;
    public List<SubField> subFields=new ArrayList<>();

    public AscField() {
        super();
    }

    public AscField(int fieldType, int fieldNum, int valueLen, byte[] value, int numOfSubField, List<SubField> subFields) {
        super();
        this.fieldType = fieldType;
        this.fieldNum = fieldNum;
        this.valueLen = valueLen;
        this.value = value;
        this.numOfSubField = numOfSubField;
        this.subFields = subFields;
    }

    public static class SubField {
        public int type;
        public int idc;

    }
}

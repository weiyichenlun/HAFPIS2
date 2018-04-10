package hbie2.nist.nistType;

import hbie2.nist.library.AscField;
import hbie2.nist.nativesdk.SdkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pms on 2017/3/13.
 */
public class NistRecType2{
    public int recLen;
    public int recType;
    public int numOfField;
    public List<AscField> ascFields=new ArrayList<>();


    public static Logger log= LoggerFactory.getLogger(NistRecType2.class);
    public NistRecType2() {
        super();
    }

    public NistRecType2(int recType, int numOfField, List<AscField> ascFields) {
        super();
        this.recType = recType;
        this.numOfField = numOfField;
        this.ascFields = ascFields;
    }


    public static NistRecType2 encode(NistData nistData){

        NistRecType2 nistRecType2 = new NistRecType2();
        nistRecType2.recType=2;
        nistRecType2.ascFields.add(SdkUtils.setAscField(nistData, 0, 2, 1, 0));
        nistRecType2.ascFields.add(SdkUtils.setAscField(nistData, 0, 2, 2, SdkUtils.String2Byte("0")));
        nistRecType2.ascFields.add(SdkUtils.setAscField(nistData, 0, 2, 3, SdkUtils.String2Byte("domain defined text place holder")));

        nistRecType2.numOfField=nistRecType2.ascFields.size();

        nistRecType2 = SysNistType2(nistRecType2,nistData);

        return nistRecType2;
    }


    /* 计算type2的长度 */
    public static synchronized NistRecType2 SysNistType2(NistRecType2 nistRecType2,NistData nistData){
        int size=SdkUtils.getLenByType2(nistRecType2);
        //计算总的长度
        int len1=SdkUtils.GetNumLen(size);

        size+=SdkUtils.GetNumLen(size)-1;//减一表示之前值为0的长度，
        //计算总长度位数
       int len2=SdkUtils.GetNumLen(size);
        //如果位数增加了,则总长度加1
        if(len2>len1)
        {
            size++;
        }
             /*更新 */
        for (int i = 0; i < nistRecType2.numOfField; i++) {
            AscField a = nistRecType2.ascFields.get(i);
            if (a.fieldType == 2 & a.fieldNum == 1 && a.valueLen == 1) {
                a.value = SdkUtils.IntToBytes(size);
                a.valueLen = a.value.length;
                nistRecType2.ascFields.set(i, a);
                break;
            }
        }
        return nistRecType2;
    }
}

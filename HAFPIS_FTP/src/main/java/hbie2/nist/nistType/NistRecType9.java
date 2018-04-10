package hbie2.nist.nistType;

import hbie2.nist.library.AscField;
import hbie2.nist.nativesdk.SdkUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by pms on 2017/3/28.
 */
public class NistRecType9 {
    public int recLen;

    public int recType;
    public int idc;
    public int numOfField;
    public List<AscField> ascFields=new ArrayList<>();


    public NistRecType9(){
        super();
    }
    public NistRecType9(int type,int idc){
        this.idc=idc;
        this.recType =type;
    }



    public static List<NistRecType9> encode(NistData nistData) {

        List<NistRecType9> nistType9List=new ArrayList<>();
        Set<Integer> set=new HashSet();
        for(NistTxt txt:nistData.nistTxtList){
            if(txt.fieldType==9 && txt.fieldIdc!=0){
                set.add(txt.fieldIdc);
            }
        }
        for(int nIdc:set) {
            NistRecType9 nistRecType9=new NistRecType9();
            nistRecType9.idc=nIdc;
            nistRecType9.recType=9;
            for (NistTxt txt : nistData.nistTxtList) {
                if (txt.fieldType == 9 && txt.fieldIdc==nIdc  ) {
                    if (nistRecType9.numOfField == 0) {
                        nistRecType9.ascFields.add(SdkUtils.setAscField(nistData, nIdc, 9, 1, 0));
                        nistRecType9.ascFields.add(SdkUtils.setAscField(nistData, nIdc, 9, 2, txt.fieldIdc));
                        nistRecType9.numOfField+=2;
                    }
                    nistRecType9.ascFields.add(SdkUtils.setAscField(nistData, nIdc, 9, txt.fieldNum,txt.value));
                    nistRecType9.numOfField++;
                }
            }

            /* updata fldnum=1, 计算长度*/
            nistRecType9 = SysNum1OfNistType9(nistRecType9);

            nistType9List.add(nistRecType9);
        }


        return nistType9List;




    }


    /* 计算type9的长度 */
    private static synchronized NistRecType9 SysNum1OfNistType9(NistRecType9 nistRecType9){
        AscField ascField=new AscField();
        int size=SdkUtils.getLenByType9(nistRecType9);
        //计算总的长度
        int len1 = SdkUtils.GetNumLen(size);
        size += SdkUtils.GetNumLen(size) - 1;
        //计算总长度位数
        int len2=SdkUtils.GetNumLen(size);
        //如果位数增加了,则总长度加1
        if(len2>len1)
        {
            size++;
        }
        /*更新 */
        for (int i = 0; i < nistRecType9.numOfField; i++) {
            AscField a = nistRecType9.ascFields.get(i);
            if (a.fieldType == 9 && a.fieldNum == 1 && a.valueLen == 1) {
                a.value = SdkUtils.IntToBytes(size);
                a.valueLen = a.value.length;
                nistRecType9.ascFields.set(i, a);
                break;
            }
        }
        return nistRecType9;
    }

}

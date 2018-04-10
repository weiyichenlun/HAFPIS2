package hbie2.nist.nistType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pms on 2017/3/14.
 */
public class NistData {
    public byte[] transNo;
    public List<NistTxt> nistTxtList = new ArrayList<>();
    public int numOfTxt;
    public List<NistImg> nistImgList = new ArrayList<>();
    public int numOfImg;


    public NistData() {

    }

    public synchronized List<NistImg> GetImgList() {
        if (nistImgList == null) {
            return new ArrayList<>();
        } else {
            return nistImgList;
        }
    }

    public synchronized List<NistTxt> GetTxtList() {
        if (nistTxtList == null) {
            return new ArrayList<>();
        } else {
            return nistTxtList;
        }
    }


}
